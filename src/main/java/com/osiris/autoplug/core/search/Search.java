package com.osiris.autoplug.core.search;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.osiris.autoplug.core.json.Json;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class containing static methods
 * to retrieve update information for assets/files
 * of specific software/repositories/releases hosted
 * on GitHub and Maven.
 */
public class Search {

    /**
     * @param groupId            the projects groupId.
     * @param artifactId         the projects artifactId.
     * @param currentVersion     current version of the installed software.
     * @param assetNamePredicate predicate that contains the asset name and is used to determine/find the asset to download.
     */
    public static SearchResult maven(String groupId, String artifactId, String currentVersion, Predicate<String> assetNamePredicate) {
        Exception exception = null;
        boolean updateAvailable = false;
        String downloadUrl = null;
        String latestVersion = null;
        String downloadFile = null;
        String sha1 = null;
        String md5 = null;
        try {
            String url = "https://repo1.maven.org/maven2/"+
                    groupId.replaceAll("\\.", "/") + "/" + artifactId;
            Document document = Jsoup.connect(url).get();

            // Go thorough all versions and get the latest version
            // or null if current version is <= latest version.
            Element latestAsset = null;
            for (Element child : document.getElementsByAttributeValue("id", "contents").get(0).children()) {
                String name = child.attr("title");
                if(latestAsset==null){
                    if(Version.isLatestBigger(currentVersion, name))
                        latestAsset = child;
                } else{
                    if(Version.isLatestBigger(latestAsset.attr("title"), name))
                        latestAsset = child;
                }
            }

            if(latestAsset!=null){
                updateAvailable = true;
                latestVersion = latestAsset.attr("title");
                url = url + "/" +latestVersion;
                Elements assets = Jsoup.connect(url).get().getElementsByAttributeValue("id", "contents").get(0).children();
                for (Element asset : assets) {
                    String name = asset.attr("title");
                    if (assetNamePredicate.test(name)) {
                        downloadFile = name;
                        downloadUrl = url + name;
                        break;
                    }
                }
                String expectedSha1AssetName = downloadFile + ".sha1";
                String expectedMd5AssetName = downloadFile + ".md5";
                for (Element asset : assets) {
                    String name = asset.attr("title");
                    if(name.equals(expectedSha1AssetName)){
                        sha1 = IOUtils.toString(new URL(url + name), StandardCharsets.UTF_8);
                    } else if (name.equals(expectedMd5AssetName)){
                        md5 = IOUtils.toString(new URL(url + name), StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            exception = e;
        }

        return new SearchResult(updateAvailable, exception, latestVersion, downloadUrl, downloadFile, null, sha1, md5);
    }

    /**
     * @param repoName           GitHub repository name.
     * @param currentVersion     current version of the installed software.
     * @param assetNamePredicate predicate that contains the asset name and is used to determine/find the asset to download.
     */
    public static SearchResult github(String repoName, String currentVersion, Predicate<String> assetNamePredicate) {
        Exception exception = null;
        boolean updateAvailable = false;
        String downloadUrl = null;
        String latestVersion = null;
        String downloadFile = null;
        String sha256 = null;
        try {
            JsonObject latestRelease = Json.fromUrlAsObject("https://api.github.com/repos/" + repoName + "/releases/latest");
            latestVersion = latestRelease.get("tag_name").getAsString();
            if (latestVersion != null)
                latestVersion = latestVersion.replaceAll("[^0-9.]", ""); // Before passing over remove everything except numbers and dots
            if (Version.isLatestBigger(currentVersion, latestVersion)) {
                updateAvailable = true;
                // Contains JsonObjects sorted by their asset-names lengths, from smallest to longest.
                // The following does that sorting.
                List<JsonObject> sortedArtifactObjects = new ArrayList<>();
                for (JsonElement e :
                        latestRelease.getAsJsonArray("assets")) {
                    JsonObject obj = e.getAsJsonObject();
                    String name = obj.get("name").getAsString();
                    if (sortedArtifactObjects.size() == 0) sortedArtifactObjects.add(obj);
                    else {
                        int finalIndex = 0;
                        boolean isSmaller = false;
                        for (int i = 0; i < sortedArtifactObjects.size(); i++) {
                            String n = sortedArtifactObjects.get(i).get("name").getAsString();
                            if (name.length() < n.length()) {
                                isSmaller = true;
                                finalIndex = i;
                                break;
                            }
                        }
                        if (!isSmaller) sortedArtifactObjects.add(obj);
                        else sortedArtifactObjects.add(finalIndex, obj);
                    }
                }

                // Find asset-name containing our provided asset-name
                for (JsonObject obj : sortedArtifactObjects) {
                    String name = obj.get("name").getAsString();
                    if (assetNamePredicate.test(name)) {
                        downloadFile = name;
                        downloadUrl = obj.get("browser_download_url").getAsString();
                        break;
                    }
                }

                if (downloadUrl == null) {
                    List<String> names = new ArrayList<>();
                    for (JsonObject obj :
                            sortedArtifactObjects) {
                        String n = obj.get("name").getAsString();
                        names.add(n);
                    }
                    throw new Exception("Failed to find an asset-name matching the assetNamePredicate inside of " + Arrays.toString(names.toArray()));
                }

                // Determine sha256
                String expectedShaAssetName = downloadFile + ".sha256";
                for (JsonObject obj : sortedArtifactObjects) {
                    String name = obj.get("name").getAsString();
                    if (name.equals(expectedShaAssetName)) {
                        sha256 = IOUtils.toString(new URL(obj.get("browser_download_url").getAsString()), StandardCharsets.UTF_8);
                        break;
                    }
                }

            }
        } catch (Exception e) {
            exception = e;
        }

        return new SearchResult(updateAvailable, exception, latestVersion, downloadUrl, downloadFile, sha256, null, null);
    }
}
