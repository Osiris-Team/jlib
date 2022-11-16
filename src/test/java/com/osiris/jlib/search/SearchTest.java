package com.osiris.jlib.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchTest {

    @Test
    void maven() throws Exception {
        SearchResult result = Search.maven("org.kill-bill.billing.installer", "kpm", "0.0", assetName ->
                !assetName.endsWith(".asc") &&
                !assetName.equals(".sha1") &&
                !assetName.endsWith(".md5") &&
                assetName.contains("linux"));
        if(result.exception!=null) throw result.exception;
        assertTrue(result.isUpdateAvailable);
        assertNotNull(result.downloadUrl);
        assertNotNull(result.assetFileName);
        assertNotNull(result.md5);
        assertNotNull(result.sha1);
    }

    @Test
    void github() throws Exception {
        SearchResult result = Search.github("noseglid/atom-build", "0.0", assetName -> true);
        if(result.exception!=null && !result.exception.getMessage().contains("asset-name")) throw result.exception;
        assertTrue(result.isUpdateAvailable);
    }
}