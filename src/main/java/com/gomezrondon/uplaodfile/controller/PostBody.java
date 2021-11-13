package com.gomezrondon.uplaodfile.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostBody {
    private String bucket;
    private String lang;

    public PostBody() {
    }

    public PostBody(String bucket, String lang) {
        this.bucket = bucket;
        this.lang = lang;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
