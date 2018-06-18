package com.test.stroll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class Login extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG ="Login";

    static public boolean login_state=false;

    SessionCallback callback;
    Button btn_login, btn_kakao_login, btn_signup;
    private EditText login_id, login_pwd;
    String lid, lpwd;
    private DB_Manager db_manager;
    private ArrayList<String> results;
    private CheckBox login_check;
    private SharedPreferences pref;
    private boolean saveLoginData;

    private String urlPath;
    private final String login_check_UrlPath =
            "http://115.71.233.85/logincheck.php";

    private String id;
    private String pwd;
    private String line = null;
    Handler mhandler = new Handler();
    static Context mContext;
    StringBuilder jsonHtml = new StringBuilder();

    String myJSON;
    JSONArray userInfo = null;
    private static final String TAG_RESULT = "result";
    private static final String TAG_SEQ = "seq";
    private static final String TAG_NAME = "name";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_ID = "id";
    private static final String TAG_IMGPATH ="imgPath";





    public Login() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        pref = getSharedPreferences("pref", MODE_PRIVATE);

        login_check = (CheckBox) findViewById(R.id.login_check);

        btn_login = (Button) findViewById(R.id.btn_login);
        login_id = (EditText) findViewById(R.id.login_id);
        login_pwd = (EditText) findViewById(R.id.login_pwd);

        login_id.setOnClickListener(this);
        login_pwd.setOnClickListener(this);
        btn_login.setOnClickListener(this);

        db_manager = new DB_Manager();
        results = new ArrayList<String>();


        load();

        if(saveLoginData){
            login_id.setText(id);
            login_pwd.setText(pwd);
            login_check.setChecked(saveLoginData);
            Log.d("oncreate shared!!",id);
        }


        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //로그아웃 성공 후 하고싶은 내용 코딩 ~
                Toast.makeText(Login.this, "카카오톡 로그인 성공 ", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Login.this, MainActivity.class);
//                startActivity(intent);
            }
        });

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);



    } //end of oncreate

    public void onStop() {
        super.onStop();
        save();
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            //간편로그인시 호출 ,없으면 간편로그인시 로그인 성공화면으로 넘어가지 않음
            if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
                return;
            }

            super.onActivityResult(requestCode, resultCode, data);
        }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Logger.d(message);
                    Toast.makeText(Login.this, "카카오톡 로그인 실패 ", Toast.LENGTH_SHORT).show();

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        finish();
                    } else {
                        //redirectMainActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                @Override
                public void onNotSignedUp() {
                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.
                    Log.e("UserProfile", userProfile.toString());
                    Toast.makeText(Login.this, "카카오톡 로그인 성공 ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {

        }
    }

        @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.login_id:
                login_id.setText(lid);
                break;

            case R.id.login_pwd:
                login_id.setText(lpwd);
                break;

            case R.id.btn_login:

                String id = login_id.getText().toString();
                String pwd = login_pwd.getText().toString();
                login_check(id, pwd);
                save();
                break;


            case R.id.btn_signup:
                Intent sign_intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(sign_intent);
                break;
        }
    }
    private void save(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        login_id = (EditText) findViewById(R.id.login_id);
        login_pwd = (EditText) findViewById(R.id.login_pwd);
        login_check = (CheckBox) findViewById(R.id.login_check);

        editor.putBoolean("SAVE_LOGIN_DATA", login_check.isChecked());
        editor.putString("editid", login_id.getText().toString());
        editor.putString("editpwd", login_pwd.getText().toString());

        SaveSharedPreference.setUserId(Login.this, login_id.getText().toString());


        editor.commit();

   /*     SharedPreferences.Editor editor = appData.edit();

        editor.putBoolean("SABE_LOGIN_DATA", login_check.isChecked());
        editor.putString("ID", login_id.getText().toString().trim());
        editor.putString("PWD", login_pwd.getText().toString().trim());
        Log.d("shaeredId ",login_id.getText().toString().trim());

        editor.apply();*/
    }
    private void load(){

        saveLoginData = pref.getBoolean("SAVE_LOGIN_DATA", false);
        id = pref.getString("editid","");
        pwd = pref.getString("editpwd","");
//
//        login_id.setText(id);
//        login_pwd.setText(pwd);

//        saveLoginData = appData.getBoolean("SAVE_LOGIN_DATA", false);
//        id = appData.getString("ID", "");
//        pwd = appData.getString("PWD", "");
    }


    /*로그인 확인 부분*/
    /*--유저에게 id.pwd를 입력받는다*/
    public ArrayList<String> login_check(String id, String pwd) {

        urlPath = login_check_UrlPath;
        this.id = id;
        this.pwd = pwd;

        try{
            results = new Login.LoginCheck().execute().get();
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        return results;
    }// end of login_check(로그인 입력값 받기)



    class LoginCheck extends AsyncTask<Void, Void, ArrayList<String>>{


        @Override
        protected ArrayList<String> doInBackground(Void... voids) {

            try{

                URL url = new URL(urlPath);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true); // 쓰기가능
                con.setDoOutput(true); //읽기가능
                con.setUseCaches(false); //no cash
                con.setRequestMethod("POST");


                DB_Manager db = new DB_Manager();
                String shapwd = db.SHA256(pwd);

                String check =
                        "id="+id+"&pwd="+shapwd;
                Log.d("login_check",check);

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(check.getBytes());
                outputStream.flush();
                outputStream.close();

                BufferedReader rd = null;
                rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
//                String result = "";

                while ((line = rd.readLine()) != null) {
                    Log.d("BufferReader", line);
//                    result += line;
//                    jsonHtml.append(line +"\n");

                    if(line.equals("user found")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Login.this, "로그인성공!!", Toast.LENGTH_SHORT).show();
                                getData("http://sjsoft.vps.phps.kr/getUserData.php");
                            }
                        });
                        startActivity(new Intent(Login.this, MainActivity.class));
                        finish();

                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Login.this, "아이디 또는 비밀번호가 일치하지 않습니다. ", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }

//                    show(jsonHtml.toString()); // 읽어온 데이터를 처리하기 위해 show 라는 함수로 보냄
//                    Log.d("jsonHtml.toStirng값!!!!", jsonHtml.toString());
                }





                           /* if(line.equals("연결성공!로그인 성공")){
                                Toast.makeText(Login.this, "로그인성공", Toast.LENGTH_SHORT).show();
                                Intent mainintent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(mainintent);
                                finish();
                            }else{
                                Toast.makeText(Login.this, "일치하는 아이디 또는 비밀번호가 없습니다", Toast.LENGTH_SHORT).show();

                            }*/




            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;

        }

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

        protected void onPostExcute(ArrayList<String> qResults) {
            super.onPostExecute(qResults);
        }

    }// end of class ''Login_check;

    /**
     * AsyncTask 비동기 방식
     * 입력한 URL (서버)에서 데이터 불러옴
     * @param url
     */
    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {

                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoInput(true); // 쓰기가능
                    con.setDoOutput(true); //읽기가능
                    con.setUseCaches(false); //no cash
                    con.setRequestMethod("POST");

                    String check =
                            "id="+id;
                    Log.d(TAG, "id값: ");
                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(check.getBytes());
                    outputStream.flush();
                    outputStream.close();



                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                        Log.d("json", json);
                    }

                    Log.d("sb.toString().trim()", sb.toString().trim());
                    return sb.toString().trim();


                } catch (Exception e) {
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String result) {

                //result = sb.toString().trim()
                myJSON = result;
                userInfo();

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
        }


    /**
     * getData 로 불러온 데이터를 json 파싱을 통해 String 값으로 변환 후 arraylist에 넣어줌 .
     */
    protected void userInfo() {
        try {

            //myJSON = sb.toString().trim()
            JSONObject jsonObject = new JSONObject(myJSON);

            //JsonArray board;
            userInfo = jsonObject.getJSONArray(TAG_RESULT);

                JSONObject c = userInfo.getJSONObject(0);
                Integer seq = c.getInt(TAG_SEQ);
                String name = c.getString(TAG_NAME);
                String gender = c.getString(TAG_GENDER);
                String id = c.getString(TAG_ID);
                String imgPath = c.getString(TAG_IMGPATH);
            Log.d(TAG,"userInfo:"+seq+"/"+name+"/"+gender+"/"+id+"!!!");

            SharedPreferences sharedUser = getSharedPreferences("sharedUser", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedUser.edit();

            editor.putInt("seq", seq);
            editor.putString("name", name);
            editor.putString("gender", gender);
            editor.putString("id", id);
            editor.putString("imgPath", imgPath);

            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}//end of activity 'login'
