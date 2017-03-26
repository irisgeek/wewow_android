package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iris on 17/3/18.
 */
public class collectionCategory implements Parcelable {


    private String id;
    private String title;



    public static final Creator<collectionCategory> CREATOR = new Creator<collectionCategory>() {
        @Override
        public collectionCategory createFromParcel(Parcel in) {

            collectionCategory p = new collectionCategory();

            p.id=in.readString();
            p.title=in.readString();
            return p;
        }

        @Override
        public collectionCategory[] newArray(int size) {
            return new collectionCategory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
