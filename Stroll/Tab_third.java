package com.test.stroll;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class Tab_third extends Fragment {

    public Tab_third() {
    }

    private Button logout;
    private RelativeLayout mMainRelativeLayout = null;
    private ImageView img_profile;
    private TextView tv_id;

    private static final String TAG = "Tab_third";
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    Context context;
    Uri fileuri;
    String imgPath;
    String userid;
    String imgName;
    String glideLoad;

    ScrollView scrollView;

    SharedPreferences sharedUser;
    private Users user;
    int userSeq;
    ArrayList boardList = new ArrayList<String>();
    ArrayList bidList = new ArrayList();
    LinearLayout attendList;
    Integer bidJSON;

    Date date;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.thirdtab, container, false);

        img_profile = (ImageView) view.findViewById(R.id.img_profile);
        img_profile.setOnClickListener(profileListener);
        personalImage();

//        String url = "http://115.71.233.85/"+glideLoad;
//        Glide.with(getContext()).load(url).into(img_profile);


        //로그인 id 유지
        userid = SaveSharedPreference.getUserId(this.getActivity());
        Log.d(TAG, "userid: " + userid);
        tv_id = (TextView) view.findViewById(R.id.tv_id);
        tv_id.setText(userid + " 님");

        //User 정보
        sharedUser = getActivity().getSharedPreferences("sharedUser", MODE_PRIVATE);
        user = new Users();
        Integer seq = sharedUser.getInt("seq",0);
        Log.d(TAG,"user정보 :"+seq.toString());
        user.setSeq(seq);
        userSeq = user.getSeq();

        scrollView = (ScrollView) view.findViewById(R.id.scroll);
        attendList = (LinearLayout) view.findViewById(R.id.attendList);

        getUserBoard();

//        ArrayList<String> list = new ArrayList<String>();
//        list.add("6월 24일 모임 -  산책해요~~!!");
//        list.add("6월 25일 모임 -  8코스 풀코스로 걸으실 분 모집");
//        list.add("6월 27일 모임 -  산책하고 간단하게 맥주");
//        list.add("6월 29일 모임 -  불타는 토요일엔 산책해여!");
//        list.add("7월 13일 모임 -  살 독하게 빼실분?? ");
//        list.add("7월 14일 모임 -  살빼서 워터파크가자!!");
//        list.add("7월 15일 모임 -  노원 정기모임");
//        list.add("7월 18일 모임 -  북한산 정복합니다");
//        list.add("7월 20일 모임 -  모해요? 나와요!");
/*
        for (int i = 0; i < list.size(); i++) {
            Log.d(TAG, "listSize" + list.size());

            final Button categoryButton = new Button(getActivity().getApplicationContext());
            attendList.addView(categoryButton);
            categoryButton.setText(list.get(i) + "");
            categoryButton.setTag(i);
            categoryButton.setTextColor(Color.BLACK);
            categoryButton.setBackgroundColor(Color.WHITE);
            categoryButton.setTextSize(17);
            categoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), categoryButton.getTag().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }*/
/*
        for (int i = 0; i <list.size(); i++){

            final TextView tv_attend = new TextView(getActivity().getApplicationContext());
            tv_attend.setTextSize(17);
            tv_attend.setTextColor(Color.BLACK);
            attendList.addView(tv_attend);
            tv_attend.setText(i+list.get(i)+"");

        }*/

        logout = (Button) view.findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);

                getActivity().finish();
            }
        });

  /*      new DownloadImageTask((ImageView) view.findViewById(R.id.img_profile))
                .execute("http://115.71.233.85/uploads/20170223_094014.jpg");*/

        return view;
    }

    private View.OnClickListener profileListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakePhotoAction();
                }
            };
            DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakeAlbumAction();
                }
            };

            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };

            new AlertDialog.Builder(getActivity())
                    .setTitle("업로드할 이미지 선택")
                    .setPositiveButton("사진촬영", cameraListener)
                    .setNeutralButton("앨범선택", albumListener)
                    .setNegativeButton("취소", cancelListener)
                    .show();
        }
    };

    public void doTakePhotoAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, PICK_FROM_CAMERA);
       /* intent.putExtra(MediaStore.EXTRA_OUTPUT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());

        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0 );
        intent.putExtra("aspectY", 0 );
        intent.putExtra("outputX", 200 );
        intent.putExtra("outputY", 150 );

        try{
            intent.putExtra("data", true);
            startActivityForResult(intent,PICK_FROM_CAMERA);
        } catch (ActivityNotFoundException e){

        }*/
    }

    public void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
//        intent.setType("image/*");
//        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult정상적으로 호출");
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA) {
                Uri path = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                img_profile.setImageBitmap(bitmap);
                Uri uri = data.getData();
                getImageNameToUri(uri);

                String fileName = null;
                File[] listFiles = (new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/").listFiles());

                if (listFiles[0].getName().endsWith(".jpg") || listFiles[0].getName().endsWith(".bmp"))
                    fileName = listFiles[0].getName();

                File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + fileName);

//                uploadImage(file.toString());
//                imgName = fileName;
                Log.d(TAG, "Camera_file_path" + file.toString());
            }
            if (requestCode == PICK_FROM_ALBUM) {
                Uri uri = data.getData();
                getImageNameToUri(uri);


                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                    img_profile.setImageBitmap(bitmap);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public String getImageNameToUri(Uri data) {
        String[] proj = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.ORIENTATION
        };
        Cursor cursor = this.getActivity().getContentResolver().query(data, proj, null, null, null);
        cursor.moveToFirst();

        int column_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        int column_title = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
        int column_orientation = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION);

        imgPath = cursor.getString(column_data);
        Log.d(TAG, "Album_File_Path" + imgPath);
        imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
        Log.d(TAG, "imgName:" + imgName);
        String imgTitle = cursor.getString(column_title);
        String imgOrient = cursor.getString(column_orientation);


        uploadImage(imgPath);
        Log.d(TAG, "requestImgpath" + imgPath);

        return imgName;
    }


    public void uploadImage(final String filePath) {
        class update extends AsyncTask<String, Void, String> {


            // 서버에 업로드한 이미지를 db(users) 에 넣어줌
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                insertIntoDate();
            }

            @Override
            protected String doInBackground(String... params) {

                Log.d(TAG, "Class Update");

                try {
                    HttpClient client = new DefaultHttpClient();
                    String serverUrl = "http://115.71.233.85/uploadImage.php";
                    HttpPost post = new HttpPost(serverUrl);

                    File glee = new File(filePath);
                    FileBody bin = new FileBody(glee);

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                    builder.addPart("image", bin);
                    post.setEntity(builder.build());
                    client.execute(post);

                    HttpResponse response = client.execute(post);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent(), "UTF-8"));
                    String sResponse;
                    StringBuilder s = new StringBuilder();

                    while ((sResponse = reader.readLine()) != null) {
                        s = s.append(sResponse);
                        break;
                    }
                    System.out.println("Response: " + s);
                    return s.toString();

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }

        update task = new update();
        task.execute();


    } // end of uploadImage

    protected void insertIntoDate() {
        class insertUsers extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    String link = "http://115.71.233.85/addUserImg.php";
                    String imgPath = "uploads/" + imgName;
                    String id = userid;
                    Log.d(TAG, "onPostExecuteData: " + imgPath + id);

                    String data = URLEncoder.encode("id", "UTF-8") + "="
                            + URLEncoder.encode(id, "UTF-8");
                    data += "&" + URLEncoder.encode("imgPath", "UTF-8") + "="
                            + URLEncoder.encode(imgPath, "UTF-8");

                    Log.d(TAG, "Data!!" + data.toString());

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr =
                            new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        Log.d(TAG, "line :" + line);
                        break;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        insertUsers insert = new insertUsers();
        insert.execute();
    }

    protected void personalImage() {
        class personal extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                String url = "http://115.71.233.85/" + glideLoad;
                Log.d(TAG, "personalImageURL" + url);
                if (glideLoad != null) {
                    Glide.with(getActivity()).load(url).into(img_profile);
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                Log.d(TAG, "personalImage실행?");

                try {
                    String link = "http://115.71.233.85/getImagePath.php";
                    String id = userid;
                    Log.d(TAG, "personal ID: " + id);

                    String data = URLEncoder.encode("id", "UTF-8") + "="
                            + URLEncoder.encode(id, "UTF-8");


                    Log.d(TAG, "personalImageData!!!" + data.toString());

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr =
                            new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        Log.d(TAG, "line~personal :" + line);
                        glideLoad = line;
                        break;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        personal image = new personal();
        image.execute();
    }

    /**
     * 해당 게시판 유저 이미지 경로 붙여와서 Arraylist저장
     */
    public void getUserBoard(){
        class GetUserBoardJSON extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... params) {

                try{
                    URL url = new URL("http://115.71.233.85/attendBoard.php");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoInput(true); // 쓰기가능
                    con.setDoOutput(true); //읽기가능
                    con.setUseCaches(false); //no cash
                    con.setRequestMethod("POST");

                    String param = "seq="+userSeq;
                    Log.d("userSEQ",param);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(param.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader rd = null;
                    rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
                    String json ;
                    while((json = rd.readLine()) != null){
                        sb.append(json+"\n");
                        Log.d("getUserBoardJSON",json);
                        break;
                    }
//                   Log.d("getDetailJson.trim()",sb.toString().trim());

                   /*json 받아온 값 Arraylist 에 담음. */
                    if (json != null){
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject != null){
                                JSONArray userBoard = jsonObject.getJSONArray("result");
                                Log.d("JSONArray userInfo", userBoard.toString());

                                for(int i=0; i<userBoard.length(); i++){
                                    JSONObject userBoardObj = (JSONObject) userBoard.get(i);
                                    bidJSON  = userBoardObj.getInt("bid");
                                    Log.d(TAG,"bidJSON"+bidJSON.toString());
                                    String title =  userBoardObj.getString("title");
                                    String dates = userBoardObj.getString("date");

                                    //String to Date 형변환
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    try{
                                        date = format.parse(dates);
                                        Log.d(TAG, "boardInfo_DATE : "+date.toString());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    //DateTime 값 받아와서 date 로 짜름
                                    DateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                    Date d = f.parse(dates);
                                    Log.d("Date_d", d.toString());
                                    DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                                    String datecrop = dateformat.format(d);
                                    Log.d("date로 짜른거",datecrop);

                                    boardList.add("<"+datecrop+">"+title);
                                    bidList.add(bidJSON);
                                    Log.d(TAG, "bidList_size"+bidList.size());
                                    Log.d(TAG, "userBoardTitle!!!"+userBoardObj.getString("title"));
                                    Log.d(TAG,"boardList_size :"+String.valueOf(boardList.size()));
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Log.e("JSON_imgPath", "Didn't receive any data from server!");
                    }


                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }

                for(int i = 0; i < boardList.size(); i++) {
                    System.out.println("one index " + i + " : value " + boardList.get(i));
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);


                for (int i = 0; i < boardList.size(); i++) {
                    Log.d(TAG, "boardList_Size" + boardList.size());

                    final Button categoryButton = new Button(getActivity().getApplicationContext());
                    attendList.addView(categoryButton);
                    categoryButton.setText(boardList.get(i) + "");
                    categoryButton.setTag(bidList.get(i));
                    categoryButton.setTextColor(Color.BLACK);
                    categoryButton.setBackgroundColor(Color.WHITE);
                    categoryButton.setTextSize(17);
                    categoryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getContext(), categoryButton.getTag().toString(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                            intent.putExtra("getId",boardItemList.get(position).getBid());

                            intent.putExtra("getId",categoryButton.getTag().toString());
                            startActivity(intent);

                        }
                    });
                }

            }
        }

        GetUserBoardJSON getUserBoardJSON = new GetUserBoardJSON();
        getUserBoardJSON.execute();
    }
}
