package com.test.stroll;


public class MsgInfo {
    private String nickName;
    private String message;
    private long time;

    public MsgInfo(String nickName, String message, long time){
        this.nickName = nickName;
        this.message = message;
        this.time = time;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickNmae) {
        this.nickName = nickNmae;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MsgInfo [nickName=" +nickName  + ", message=" + message + ", time=" + time + "]";
    }
}
