package com.test.stroll;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017-06-16.
 */

public class Board {

    private int bid;
    private Date date;
    private String title;
    private String limit_p;
    private String writer;
    private String detailCourseName;


    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLimit_p() {
        return limit_p;
    }

    public void setLimit_p(String limit_p) {
        this.limit_p = limit_p;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getDetailCourseName() {
        return detailCourseName;
    }

    public void setDetailCourseName(String detailCourseName) {
        this.detailCourseName = detailCourseName;
    }

    private Set<Users> users = new HashSet<Users>();

    //누가 이 모임에 참석 했는지 알기위해 getter 만 public
    public Set<Users> getUsers() {
        return users;
    }

    @SuppressWarnings("unused")
    private void setUsers(Set<Users> users) {
        this.users = users;
    }
}
