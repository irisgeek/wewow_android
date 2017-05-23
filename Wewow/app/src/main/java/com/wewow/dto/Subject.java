package com.wewow.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by iris on 17/3/22.
 */
public class Subject implements Parcelable {

    private String title;
    private String date;
    private String content;
    private String image;
    private String share_link;


    private List<List<Institute>> institutes;
    private List<String> instituteDescs;


    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        @Override
        public Subject createFromParcel(Parcel in) {
            Subject subject = new Subject();


            return subject;
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<List<Institute>> getInstitutes() {
        return institutes;
    }

    public void setInstitutes(List<List<Institute>> institutes) {
        this.institutes = institutes;
    }

    public List<String> getInstituteDescs() {
        return instituteDescs;
    }

    public void setInstituteDescs(List<String> instituteDescs) {
        this.instituteDescs = instituteDescs;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getShare_link() {
        return share_link;
    }

    public void setShare_link(String share_link) {
        this.share_link = share_link;
    }
}
