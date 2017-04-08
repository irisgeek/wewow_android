package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by suncjs on 2017/4/8.
 * 生活实验室
 */

public class LabCollection implements Parcelable {

    public long id;
    public String image;
    public String title;
    public String date;
    public String liked_count;
    public int order;
    public String read_count;
    public String image_688_316;
    public String image_642_320;

    public LabCollection() {

    }

    protected LabCollection(Parcel in) {
        id = in.readLong();
        image = in.readString();
        title = in.readString();
        date = in.readString();
        liked_count = in.readString();
        order = in.readInt();
        read_count = in.readString();
        image_688_316 = in.readString();
        image_642_320 = in.readString();
    }

    public static final Creator<LabCollection> CREATOR = new Creator<LabCollection>() {
        @Override
        public LabCollection createFromParcel(Parcel in) {
            return new LabCollection(in);
        }

        @Override
        public LabCollection[] newArray(int size) {
            return new LabCollection[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(image);
        parcel.writeString(title);
        parcel.writeString(date);
        parcel.writeString(liked_count);
        parcel.writeInt(order);
        parcel.writeString(read_count);
        parcel.writeString(image_688_316);
        parcel.writeString(image_642_320);
    }
}
