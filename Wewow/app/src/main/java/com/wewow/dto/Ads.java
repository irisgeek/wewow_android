package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iris on 17/3/22.
 */
public class Ads implements Parcelable {

    private String content;
    private String id;
    private String image;
    private String target;
    private String title;
    private String type;


    public static final Creator<Ads> CREATOR = new Creator<Ads>() {
        @Override
        public Ads createFromParcel(Parcel in) {
            Ads ads = new Ads();


            return ads;
        }

        @Override
        public Ads[] newArray(int size) {
            return new Ads[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getImage());
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


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

