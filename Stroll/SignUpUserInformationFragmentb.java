package com.test.stroll;

/**
 * Created by Administrator on 2017-06-01.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SignUpUserInformationFragmentb extends Fragment {

    private DB_Manager db_manager;
    private ArrayList<String> results;

    private EditText et_name;
    private EditText et_id;
    private EditText et_pwd;
    private EditText et_pwdAgain;
    private EditText et_phone;
    private EditText et_email;
    private Button btn_idchk;
    private Button btn_signup;

    private String gender;
    private String id;

    // 회원가입시 id 중복체크
    private String urlPath;
    private final String Id_check_UrlPath =
            "http://115.71.233.85/idcheck.php";
    private String line = null;

    public SignUpUserInformationFragmentb(){ //생성자

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_signup_user_informationb, container, false);

        et_name = (EditText) rootView.findViewById(R.id.et_name);
        et_id = (EditText) rootView.findViewById(R.id.et_id);
        et_pwd = (EditText) rootView.findViewById(R.id.et_pwd);
        et_pwdAgain = (EditText) rootView.findViewById(R.id.et_pwdAgain);
        et_phone = (EditText) rootView.findViewById(R.id.et_phone);
        et_email = (EditText) rootView.findViewById(R.id.et_email);
        btn_idchk = (Button) rootView.findViewById(R.id.btn_idchk);
        btn_signup = (Button) rootView.findViewById(R.id.btn_signup);


        // 성별 입력
        final RadioGroup rg = (RadioGroup) rootView.findViewById(R.id.radiogroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(group.getId() == R.id.radiogroup){
                    switch (checkedId){
                        case R.id.f:
                            gender = "f";
                            break;
                        case R.id.m:
                            gender ="m";
                            break;
                    }
                }
            }
        });


        //아이디 중복 체크
        btn_idchk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id =et_id.getText().toString();
                Log.d("중복체크id",id);
                Id_check(id);
            }
        });

        //회원가입 완료 버튼
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = et_name.getText().toString();
                id = et_id.getText().toString();
                String pwd = et_pwd.getText().toString();
                String phone = et_phone.getText().toString();
                String email = et_email.getText().toString();

            db_manager.signup_user_informationb(name,gender,id,pwd,phone,email);
                Toast.makeText(getActivity(), "회원가입이 완료되었습니다. 로그인해주세요", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(getActivity(), Login.class);
                startActivity(mainIntent);
            }
        });


        db_manager = new DB_Manager();
        results = new ArrayList<String>();

        return rootView;
    }

    /**
     * 입력한 id 값 받아와서 Idcheck() 로 서버통신
     * @param id
     * @return
     */
    public ArrayList<String> Id_check(String id) {

        urlPath = Id_check_UrlPath;
        this.id = id;

        try{
            results = new Idcheck().execute().get();
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        return results;
    }

    class Idcheck extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {

            try{

                URL url = new URL(urlPath);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true); // 쓰기가능
                con.setDoOutput(true); //읽기가능
                con.setUseCaches(false); //no cash
                con.setRequestMethod("POST");


                String check =
                        "id="+id;
                Log.d("중복체크할 id",check);

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

                    if(line.equals("사용 가능한 아이디 입니다.")){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "사용가능한 아이디입니다. ", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }else{
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "중복된 아이디 입니다. 다른 아이디를 설정해주세요 ", Toast.LENGTH_LONG).show();
                                et_id.getText().clear();
                            }
                        });
                    }


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

    }


}
