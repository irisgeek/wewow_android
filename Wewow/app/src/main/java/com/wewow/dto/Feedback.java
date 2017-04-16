package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iris on 17/4/14.
 */
public class Feedback implements Parcelable{

    private String content;
    private String image_height;
    private String from;
    private String content_type;
    private String time;
    private String reply_to;
    private String id;
    private String image_width;


    public static final Creator<Feedback> CREATOR = new Creator<Feedback>() {
        @Override
        public Feedback createFromParcel(Parcel in) {
            return new Feedback();
        }

        @Override
        public Feedback[] newArray(int size) {
            return new Feedback[size];
        }
    };

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage_height() {
        return image_height;
    }

    public void setImage_height(String image_height) {
        this.image_height = image_height;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReply_to() {
        return reply_to;
    }

    public void setReply_to(String reply_to) {
        this.reply_to = reply_to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage_width() {
        return image_width;
    }

    public void setImage_width(String image_width) {
        this.image_width = image_width;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeString(image_height);
        dest.writeString(from);
        dest.writeString(content_type);
        dest.writeString(time);
        dest.writeString(reply_to);
        dest.writeString(id);
        dest.writeString(image_width);
    }
}


