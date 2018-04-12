package com.keemsa.news;

/**
 * Created by sebastian on 08/07/16.
 */
public class News {
    private String type;
    private String webUrl;
    private String headline;
    private String thumbnailUrl;

    public News(String type, String webUrl, String headline) {
        this.type = type;
        this.webUrl = webUrl;
        this.headline = headline;
    }


    public News(String type, String webUrl, String headline, String thumbnailUrl) {
        this.type = type;
        this.webUrl = webUrl;
        this.headline = headline;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
