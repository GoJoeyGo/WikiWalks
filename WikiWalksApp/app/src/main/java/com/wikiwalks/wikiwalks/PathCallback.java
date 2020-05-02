package com.wikiwalks.wikiwalks;

public interface PathCallback {
    void onSuccess(String result);
    void onFailure(String result);
}
