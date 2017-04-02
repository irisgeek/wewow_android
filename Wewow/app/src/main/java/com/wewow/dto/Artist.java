package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iris on 17/3/22.
 */
public class Artist implements Parcelable {

    private String image;
    private String nickname;
    private String id;
    private String desc;
    private String article_count;
    private String follower_count;
    private String followed;


    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            Artist artist = new Artist();


            return artist;
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getImage());
        dest.writeString(getNickname());
        dest.writeString(getId());
        dest.writeString(getDesc());
        dest.writeString(getArticle_count());
        dest.writeString(getFollower_count());
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getArticle_count() {
        return article_count;
    }

    public void setArticle_count(String article_count) {
        this.article_count = article_count;
    }

    public String getFollower_count() {
        return follower_count;
    }

    public void setFollower_count(String follower_count) {
        this.follower_count = follower_count;
    }

    public String getFollowed() {
        return followed;
    }

    public void setFollowed(String followed) {
        this.followed = followed;
    }
}
