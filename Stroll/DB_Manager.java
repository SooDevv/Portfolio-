package com.test.stroll;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

//회원가입 db 관리
//로그인 db관리

public class DB_Manager {

    private String urlPath;
    //회원 가입의 정보를 MySQL 에 저장할 php 를 포함한 도메인 주소를 입력한다.
    private final String signup_user_information_UrlPath=
            "http://115.71.233.85/signup_user_information.php";
    private final String signup_user_information_UrlPath2=
            "http://115.71.233.85/signup.php";
    //로그인정보를 확인할 php 도메인주소
    private final String login_check_UrlPath =
            "http://115.71.233.85/logincheck.php";


    //DB root에 접속하여 회원 가입에 관한 db 저장할 데이터
    private String id;
    private String gender;
    private String name;
    private String pwd;
    private String shapwd;
    private String phone;
    private String email;
    private ArrayList<String> results;
    private Context context;


    public String SHA256(String pwd) {

        String SHA = "";

        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(pwd.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }

 // SignupUserInformationFragmentb
    public ArrayList<String> signup_user_informationb(String name, String gender, String id, String pwd, String phone, String email){

        urlPath = signup_user_information_UrlPath2;

        this.name = name;
        this.gender = gender;
        this.id = id;
        this.pwd = pwd;
        this.phone = phone;
        this.email = email;

        try{
            results = new SignupUserInformationb().execute().get();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        return results;
    } //end of signup_user_information (회원가입 입력값 받기)

    /*문자열로 이루어진 데이터를 서버에 post 방식으로 전송*/
    /*HttpURLConnection 방식을 이용해 연결을 하고, OutputStream, BufferedReader 를 통해 보낸다*/
    class SignupUserInformationb extends AsyncTask<Void, Void, ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {

            try { //xml 파싱을 위한 과정
                URL url = new URL(urlPath); //set url
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true); // 쓰기가능
                con.setDoOutput(true); //읽기가능
                con.setUseCaches(false); //no cash
                con.setRequestMethod("POST");


                DB_Manager db = new DB_Manager();
                String shapwd = db.SHA256(pwd);

                String param =
                        "name="+name+"&gender="+gender+"&id="+id+"&pwd="+shapwd+"&phone="+phone+"&email="+email;
                Log.d("insertusers",param);

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(param.getBytes());
                outputStream.flush();
                outputStream.close();

                BufferedReader rd = null;
                rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
                String line = null;
                while ((line = rd.readLine()) != null){
                    Log.d("BufferReader", line);
                }
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExcute(ArrayList<String> qResults) {
            super.onPostExecute(qResults);
        }
    }// end of class 'SignupUserInformation';


} //end of class 'DB_Manager'
