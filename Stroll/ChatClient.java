package com.test.stroll;

import android.util.Log;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class ChatClient {

    static int chatStat =0;

    private final String IP = "115.71.233.85";
    private final int PORT = 8888;
    private  static String nickName ;
    private  static String num ;
    private final String TAG ="ChatClient";

    private DataInputStream in;
    private DataOutputStream out;

    private Socket socket;
    private String msg;
    private String name;
    private String info;
    private String bid;

    public void setBid(String bid) {
        this.bid = bid;
    }
    public void setNickName(String nickname){
        this.nickName =nickname;
    }
    public void setNum(String num){
        this.num =num;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    //클라이언트 접속시 소캣생성 및 연결

    public void connect(){
        try {
            socket = new Socket(IP,PORT);
            System.out.println("서버-클라이언트 연결됨");
            out = new DataOutputStream(this.socket.getOutputStream());

//            ClientWrite clientWrite = new ClientWrite(info);
            name = nickName;
            Log.d(TAG,"connect()- "+nickName+num);
            out.writeUTF("req_enterRoom|"+nickName+"|"+num);
            out.flush();
            ClientRead clientRead = new ClientRead(socket);
            clientRead.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void roomConnect(){
        try {
            socket = new Socket(IP,PORT);
            System.out.println("방 추가 연결");
            out = new DataOutputStream(this.socket.getOutputStream());
            out.writeUTF("createRoom|"+bid);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void bye(){
        try {
            socket = new Socket(IP,PORT);
            out = new DataOutputStream(this.socket.getOutputStream());
            out.writeUTF("Tab_chat|"+nickName+"|"+bid);
            out.flush();
            Log.d(TAG,"bye_bid"+bid.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


//서버로 메세지 보내기

    public void sendMsg(String msg){
        long time = System.currentTimeMillis();
        name = nickName;
        Log.d(TAG, "SendMsg_NickName = "+name);
        final MsgInfo msgInfo = new MsgInfo(name,msg,time);

        final Gson gson = new Gson();
        new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        out.writeUTF("req_say|"+gson.toJson(msgInfo));
                        out.flush();
                        Log.d(TAG,"gson:::"+gson.toJson(msgInfo).toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        }).start();

    }

    public void close(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientWrite extends Thread{

        public ClientWrite(String nickName) {
            name = nickName;
            try {
                out = new DataOutputStream(socket.getOutputStream());
//                out.writeUTF(num);
                out.writeUTF(nickName);
                out.flush();
                System.out.println("id : "+nickName+"접속 완!료!");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("wiriteUTF IOExeption");
            }
        }

        @Override
        public void run() {
            Scanner in = new Scanner(System.in);

            while (true) {
                System.out.print("메세지 입력 : ");
                // Json구성
                String msg = in.nextLine();
                long time = System.currentTimeMillis();
                MsgInfo msgInfo = new MsgInfo(name, msg, time);

                Gson gson = new Gson();
//					String json = "{\"nickName\":\"" + nickName + "\",\"msg\":\"" + msg + "\",\"time\":\"" + time + "\"}";
                try {
                    out.writeUTF(gson.toJson(msgInfo));
//                    System.out.println(gson.toJson(msgInfo));
                    out.flush();
                    Log.d(TAG, "ClientWrite : "+gson.toJson(msgInfo));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

/*    private class ClientRead extends Thread{

        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //JSON 파싱
            try {
                //계속 듣기만
                while (in != null) {

                    String json = in.readUTF();
                    Log.d(TAG, "ClientRead_JSON :"+json);
                    try {
                        MsgInfo msgInfo = new Gson().fromJson(json, MsgInfo.class);
                        Log.d(TAG,"ClientRead"+msgInfo.toString());

                        EventBus.getDefault().post(msgInfo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                socket = null;
            }

        }
    }*/

    class ClientRead extends Thread{
        Socket socket;
        DataInputStream in;

        public ClientRead(Socket socket){
            this.socket = socket;

            try {
                in = new DataInputStream(this.socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // 생성자~~~~~~~~~~~~~~~~~~

        /**메시지 파서*/
        public String[] getMsgParse(String msg){
            System.out.println("msgParse() =>msg?"+ msg);
            String[] tmpArr = msg.split("[|]");
            return tmpArr;
        }

        @Override
        public void run() {

            while (in!=null){

                try {
                    String msg = in.readUTF();

                    String[] msgArr = getMsgParse(msg.substring(msg.indexOf("|")+1));

                    if(msg.startsWith("enterRoom#yes")){
                        System.out.println("[##] 채팅방 ("+msgArr[0]+")에 입장하였습니다.");
                        ChatClient.chatStat=2;
                    }else if(msg.startsWith("say")){
                        //say|대화내용
                        System.out.println("["+msgArr[0]+"] ");
                        try{
                            MsgInfo msgInfo= new Gson().fromJson(msgArr[0], MsgInfo.class);
                            Log.d(TAG,"ClientRead = "+msgInfo.toString());

                            EventBus.getDefault().post(msgInfo);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

/*    class Sender extends Thread{
        Socket socket;
        DataOutputStream out;
        String nick;

        public Sender(Socket socket){
            this.socket = socket;

            try {
                out = new DataOutputStream(this.socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }// 생성자~~~~~~~~~~~~~~~~~~~

        @Override
        public void run() {

            whil
        }
    }*/

}
