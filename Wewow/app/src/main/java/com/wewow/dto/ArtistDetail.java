package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by iris on 17/3/22.
 */
public class ArtistDetail implements Parcelable {

    private Artist artist;
    private List<Article> articles;


    public static final Creator<ArtistDetail> CREATOR = new Creator<ArtistDetail>() {
        @Override
        public ArtistDetail createFromParcel(Parcel in) {
            ArtistDetail artist = new ArtistDetail();


            return artist;
        }

        @Override
        public ArtistDetail[] newArray(int size) {
            return new ArtistDetail[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }


    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}
