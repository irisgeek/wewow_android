package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iris on 17/3/19.
 */
public class Institute implements Parcelable {

    private String read_count;
    private String title;
    private String image;
    private String id;
    private String liked_count;
    private String order;


    public static final Creator<Institute> CREATOR = new Creator<Institute>() {
        @Override
        public Institute createFromParcel(Parcel in) {
            Institute institute=new Institute();


            return institute;
        }

        @Override
        public Institute[] newArray(int size) {
            return new Institute[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public String getRead_count() {
        return read_count;
    }

    public void setRead_count(String read_count) {
        this.read_count = read_count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLiked_count() {
        return liked_count;
    }

    public void setLiked_count(String liked_count) {
        this.liked_count = liked_count;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
