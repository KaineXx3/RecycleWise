package com.example.fyp1.Model;

public class NewsModel {
    private String newsId;
    private String title;
    private String information;
    private String date;
    private String imgUrl;

    public NewsModel(){

    }

    public NewsModel(String newsId, String title, String information, String imgUrl, String date) {
        this.newsId = newsId;
        this.title = title;
        this.information = information;
        this.imgUrl = imgUrl;
        this.date=date;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getImageUrl() {
        return imgUrl;
    }

    public void setImageUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }
}
