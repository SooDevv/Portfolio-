package com.test.stroll;

/**
 * Created by Administrator on 2017-05-31.
 */



public class BoardItem {

    String bid;
    String title ;
    String date;
    String time;
    String yoil;
    String day;
    String limit;
    String course_name;
    String detail_name;



    public BoardItem(String bid, String title, String date, String time, String limit,String course_name,String detail_name, String yoil, String day){
        this.bid = bid;
        this.title = title;
        this.date = date;
        this.time = time;
        this.yoil = yoil;
        this.day = day;
        this.limit = limit;
        this.course_name = course_name;
        this.detail_name = detail_name;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getYoil() {
        return yoil;
    }

    public void setYoil(String yoil) {
        this.yoil = yoil;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getDetail_name() {
        return detail_name;
    }

    public void setDetail_name(String detail_name) {
        this.detail_name = detail_name;
    }
}
