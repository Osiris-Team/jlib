package com.osiris.autoplug.core.search;

public class SearchResult {
    public boolean isUpdateAvailable;
    public Exception exception;
    public String latestVersion;
    public String downloadUrl;
    public String assetFileName;
    /**
     * Hash of the asset that is usually used to verify the download. <br>
     * May be null, depends on the content provider: <br>
     * - GitHub provides only sha256. <br>
     * - Maven provides only sha1 and md5. <br>
     */
    public String sha256;
    /**
     * Hash of the asset that is usually used to verify the download. <br>
     * May be null, depends on the content provider: <br>
     * - GitHub provides only sha256. <br>
     * - Maven provides only sha1 and md5. <br>
     */
    public String sha1;
    /**
     * Hash of the asset that is usually used to verify the download. <br>
     * May be null, depends on the content provider: <br>
     * - GitHub provides only sha256. <br>
     * - Maven provides only sha1 and md5. <br>
     */
    public String md5;

    public SearchResult(boolean isUpdateAvailable, Exception exception, String latestVersion, String downloadUrl,
                        String assetFileName, String sha256, String sha1, String md5) {
        this.isUpdateAvailable = isUpdateAvailable;
        this.exception = exception;
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
        this.assetFileName = assetFileName;
        this.sha256 = sha256;
        this.sha1 = sha1;
        this.md5 = md5;
    }

    public String toPrintString() {
        return "SearchResult{" +
                "isUpdateAvailable=" + isUpdateAvailable +
                ", exception=" + exception +
                ", latestVersion='" + latestVersion + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", assetFileName='" + assetFileName + '\'' +
                ", sha256='" + sha256 + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
