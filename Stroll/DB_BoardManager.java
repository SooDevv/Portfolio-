package com.test.stroll;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Administrator on 2017-05-24.
 */

public class DB_BoardManager {

    private String urlPath;

    private final String add_board_UrlPath =
            "http://115.71.233.85/addBoard.php";


    //db root 에 접속하여 게시판(board) 에 관한 db 저장할 데이터
    private String title;
    private String limit;

    private ArrayList<String> results;

    public ArrayList<String> addBoard(String title, String limit){

        urlPath = add_board_UrlPath;
        this.title = title;
        this.limit = limit;

        try{
            results = new AddBoardInfo().execute().get();
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e) {
            e.printStackTrace();
        }

        return results;
    }

    class AddBoardInfo extends AsyncTask<Void, Void, ArrayList<String>>{


        @Override
        protected ArrayList<String> doInBackground(Void... voids) {

            try{
                URL url = new URL(urlPath);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");

                String param = "title="+title+"&limit="+limit;
                Log.d("param",param);

                OutputStream outputStream = con.getOutputStream();
                outputStream.write(param.getBytes());
                outputStream.flush();
                outputStream.close();

                BufferedReader rd = null;
                rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
                String line = null;
                while((line =rd.readLine())!= null){
                    Log.d("BufferReader",line);
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
} // end of DB_BoardManager~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~