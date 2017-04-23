package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iris on 17/3/22.
 */
public class Post implements Parcelable {


    private String image_642_320;
    private String id;
    private String title;


    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            Post article = new Post();


            return article;
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }



    public String getImage_642_320() {
        return image_642_320;
    }

    public void setImage_642_320(String image_642_320) {
        this.image_642_320 = image_642_320;
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
