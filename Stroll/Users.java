package com.test.stroll;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017-06-16.
 */

public class Users {

    private int seq;
    private String name;
    private String gender;
    private String id;
    private String imgPath;


    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    //user n:m board
    private Set<Board> boards = new HashSet<Board>();

    public Set<Board> getBoards() {
        return boards;
    }

    public void setBoards(Set<Board> boards) {
        this.boards = boards;
    }

    //모임 참석
    public void addBoard(Board g){
        this.boards.add(g);
    }

    //모임 취소
    public void cancelBoard(Board g){
        this.boards.remove(g);
    }
}
