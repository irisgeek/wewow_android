package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iris on 17/3/22.
 */
public class Article implements Parcelable {

    private String wewow_category;
    private String image_284_160;
    private String image_320_160;
    private String id;
    private String title;


    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            Article article = new Article();


            return article;
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public String getWewow_category() {
        return wewow_category;
    }

    public void setWewow_category(String wewow_category) {
        this.wewow_category = wewow_category;
    }

    public String getImage_284_160() {
        return image_284_160;
    }

    public void setImage_284_160(String image_284_160) {
        this.image_284_160 = image_284_160;
    }

    public String getImage_320_160() {
        return image_320_160;
    }

    public void setImage_320_160(String image_320_160) {
        this.image_320_160 = image_320_160;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
