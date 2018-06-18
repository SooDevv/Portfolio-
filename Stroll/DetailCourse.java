package com.test.stroll;

/**
 * Created by Administrator on 2017-06-09.
 */

public class DetailCourse {

    private int num;
    private String course_name;
    private String detail_name;
    private double lat;
    private double lng;

    public DetailCourse(){}

    public DetailCourse(int num, String course_name, String detail_name, double lat, double lng){

        this.num = num;
        this.course_name = course_name;
        this.detail_name = detail_name;
        this.lat = lat;
        this.lng = lng;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString(){
        return detail_name;
    }
}
