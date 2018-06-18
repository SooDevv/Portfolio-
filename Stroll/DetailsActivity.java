package com.test.stroll;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;


public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG ="DetailsActivity";
    TextView tv_detailTitle,tv_detailDate,tv_detailTime,tv_detailCoursename, tv_detailDetailCourse, tv_detailLimit, tv_detailViewPeople;
    ImageView img_detailClock, img_detailMarker, img_detailLimit;
    Button btn_attend;
    Boolean attend = true;
    GoogleMap googleMap;
    MapFragment mapFragment;
    Context mContext;

    String detailId;
    HorizontalScrollView scrollView;
    LinearLayout innerLinearLayout;
    Users users;
    Board board;
    HashSet<Board> hashBoard;
    HashSet<Users> hashUser;


    String userid;
    SharedPreferences sharedUser;
    private Users user;
    private String myJSON;
    JSONArray boardInfo = null;
    Date date;
    private static final String TAG_RESULT = "result";
    private static final String TAG_BID = "bid";
    private static final String TAG_TITLE = "title";
    private static final String TAG_LIMIT = "limit_p";
    private static final String TAG_DATE = "date";
    private static final String TAG_WRITER = "writer";
    private static final String TAG_COURSENAME ="course_name";
    private static final String TAG_DETAILNAME ="detail_name";

    //user 정보
    int userSeq;
    String userName, userGender, userId, userImgPath;

    //게시판 정보
    int boardId;
    Date boardDate;
    String boardTitle, boardLimit, boardWriter, boardDetailCourse;
    String limit;

    ArrayList imgPathList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details_fragment);
        init();
     }

    public void init(){

        //TextView
        tv_detailTitle = (TextView) findViewById(R.id.tv_detailTitle);
        tv_detailDate = (TextView) findViewById(R.id.tv_detailDate);
        tv_detailTime = (TextView) findViewById(R.id.tv_detailTime);
        tv_detailCoursename = (TextView) findViewById(R.id.tv_detailCoursename);
        tv_detailDetailCourse = (TextView) findViewById(R.id.tv_detailDetailCourse);
        tv_detailLimit = (TextView) findViewById(R.id.tv_detailLimit);
//        tv_detailViewPeople = (TextView) findViewById(R.id.tv_detailViewPeople);

        //ImageView
        img_detailClock = (ImageView) findViewById(R.id.img_detailClock);
        img_detailMarker = (ImageView) findViewById(R.id.img_detailMarker);
        img_detailLimit = (ImageView) findViewById(R.id.img_limit);


        //Button
        btn_attend = (Button) findViewById(R.id.btn_attend);
        btn_attend.setOnClickListener(attendClickListener);


        //scroll view
        scrollView = (HorizontalScrollView) findViewById(R.id.horizonView);
        innerLinearLayout = (LinearLayout)findViewById(R.id.innerLinearLayout);

        users = new Users();
        board = new Board();
        hashBoard = new HashSet<Board>();
        hashUser = new HashSet<Users>();

//        userid =  SaveSharedPreference.getUserId(this);
//        users.setId(userid);
//        Log.d(TAG,"users의id :"+users.getId());

//        ArrayList<Drawable> people = new ArrayList<Drawable>();
//        people.add()



        //map fragment
        FragmentManager fragmentManager= getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_detail);
        mapFragment.getMapAsync(this);


        Intent intent = getIntent();
        detailId= intent.getExtras().getString("getId");
     /*   String detailTitle = intent.getExtras().getString("getTitle");
        String detailDate = intent.getExtras().getString("getDate");
        String getTime = intent.getExtras().getString("getTime");
        String getYoil = intent.getExtras().getString("getYoil");
        String getLimit = intent.getExtras().getString("getLimit");
        String getCourse_name = intent.getExtras().getString("getCourse_name");
        String getDetail_name = intent.getExtras().getString("getDetail_name");

        tv_detailTitle.setText(detailTitle);
        tv_detailDate.setText(detailDate+" "+getYoil+"요일");
        tv_detailTime.setText(getTime);
        tv_detailLimit.setText(getLimit);
        tv_detailCoursename.setText(getCourse_name);
        tv_detailDetailCourse.setText(getDetail_name);*/

        Log.d(TAG, "getId:"+detailId);
        Log.d(TAG, "getId!!:"+intent.getExtras().getInt("getId"));
        Log.d(TAG,"getIntent():Detail_name"+intent.getExtras().getString("getDetail_name"));


        sharedUser = getSharedPreferences("sharedUser", MODE_PRIVATE);
        user = new Users();
        userSetting();
        getData("http://sjsoft.vps.phps.kr/getBoardInfo.php");

        attendState("http://sjsoft.vps.phps.kr/user_board.php","SELECT*FROM user_board WHERE seq="+user.getSeq()+" AND bid="+detailId);
        getImgPath();

    }

    //로그인한 현재 유저 정보
    public void userSetting(){

        Integer seq = sharedUser.getInt("seq",0);
        String name = sharedUser.getString("name",null);
        String gender = sharedUser.getString("gender", null);
        String id = sharedUser.getString("id", null);
        String imgPath = sharedUser.getString("imgPath",null);

        user.setSeq(seq);
        user.setName(name);
        user.setGender(gender);
        user.setId(id);
        user.setImgPath(imgPath);

        userSeq = user.getSeq();
        userName = user.getName();
        userGender = user.getGender();
        userId = user.getId();
        userImgPath = user.getImgPath();

        Log.d(TAG, "userSetting :"+userSeq+"!!-"+userName+userGender+userId+userImgPath);
    }

    //현재 들어와있는 게시판 정보.
    public void boardSetting(){

        boardId = board.getBid();
        boardDate = board.getDate();
        boardTitle = board.getTitle();
        boardLimit = board.getLimit_p();
        boardWriter = board.getWriter();
        boardDetailCourse = board.getDetailCourseName();

        Log.d(TAG, "boardSetting :"+boardId+"!!-"+boardTitle+boardLimit+boardWriter+boardDetailCourse);
    }

    //Detail Course 의 위치정보.
    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        LatLng SEOUL = new LatLng(37.689157, 127.046733);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private View.OnClickListener attendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(!attend){ // false - 참가 안 한 상태 default
                btn_attend.setSelected(true);
                btn_attend.setText("참 석");
                btn_attend.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.attendbtn));
                attend = true;
                Toast.makeText(getApplicationContext(),"취소하셨습니다",Toast.LENGTH_SHORT).show();

                user.cancelBoard(board);
                attendState("http://sjsoft.vps.phps.kr/user_board.php","DELETE FROM user_board WHERE seq="+user.getSeq()+" AND bid="+board.getBid());
                Log.d(TAG, "getUsersSize-cancel :"+ hashBoard.size());



            }else{ //참가한 상태 attend = true;
//                attend = false;
                btn_attend.setSelected(false);
                btn_attend.setText("취 소");
                btn_attend.setBackgroundColor(Color.GRAY);



                Log.d(TAG, "getUsers :"+user.getBoards());
                Log.d(TAG, "getUsersSize :"+ hashBoard.size());

                if(imgPathList.size() == Integer.parseInt(limit)){
                    Toast.makeText(getApplicationContext(),"참여인원이 꽉찼습니다.",Toast.LENGTH_SHORT).show();
                    btn_attend.setText("참 석");
                    btn_attend.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.attendbtn));
                }else{
                    attend = false;
                    Toast.makeText(getApplicationContext(),"참석!!",Toast.LENGTH_SHORT).show();

                    user.setBoards(hashBoard);
                    user.addBoard(board);
                    user.getBoards();
                    attendState("http://sjsoft.vps.phps.kr/user_board.php","INSERT INTO user_board(seq, bid) VALUES("+user.getSeq()+","+board.getBid()+")");
                }

            }

        }
    };

    /**
     * 모임 참석/취소에 관한 DB 업데이트
     * @param url
     * @param query
     */
    public void attendState(String url,final String query) {
        class AttendStateData extends AsyncTask<String, Void, String> {

            ProgressDialog loading ;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(DetailsActivity.this,
                        "Please Wait", null, true, true);
            }

            @Override
            protected String doInBackground(String... params) {

                loading.dismiss();

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
                            "query="+query;
                    Log.d(TAG, "query값 : "+query);
                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(check.getBytes());
                    outputStream.flush();
                    outputStream.close();


                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        Log.d("user_board//attendState", line);

                        if(line.equals("query 수정 완료attendance")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailsActivity.this, "이미 참석하였습니다", Toast.LENGTH_SHORT).show();

                                    btn_attend.setSelected(false);
                                    btn_attend.setText("취 소");
                                    btn_attend.setBackgroundColor(Color.GRAY);

                                    attend = false;

                                    user.setBoards(hashBoard);
                                    user.addBoard(board);
                                    user.getBoards();
                                }
                            });


                        }else if(line.equals("query 수정 완료absent")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailsActivity.this, "오고싶으면 참석 눌러랏", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        AttendStateData g = new AttendStateData();
        g.execute(url);
    }

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

                    Integer bid = Integer.parseInt(detailId);
                    String check =
                            "bid="+bid;
                    Log.d(TAG, "bid값: ");
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
                boardInfo();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    /**
     * getData 로 불러온 데이터를 json 파싱을 통해 String 값으로 변환 후 arraylist에 넣어줌 .
     */
    protected void boardInfo() {
        try {

            //myJSON = sb.toString().trim()
            JSONObject jsonObject = new JSONObject(myJSON);

            //JsonArray board;
            boardInfo = jsonObject.getJSONArray(TAG_RESULT);

            JSONObject c = boardInfo.getJSONObject(0);
            Integer bid = c.getInt(TAG_BID);
            String title = c.getString(TAG_TITLE);
            limit = c.getString(TAG_LIMIT);
            String dates = c.getString(TAG_DATE);
            String writer = c.getString(TAG_WRITER);
            String course_name = c.getString(TAG_COURSENAME);
            String detail_name = c.getString(TAG_DETAILNAME);


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

            //DateTime 값 받아와서 time으로 짜름
            DateFormat timeformat = new SimpleDateFormat("aa h시mm분");
            String timecrop = timeformat.format(d);
            Log.d("time으로 짜른거",timecrop);

            board.setBid(bid);
            board.setTitle(title);
            board.setLimit_p(limit);
            board.setDate(date);
            board.setWriter(writer);
            board.setDetailCourseName(detail_name);

            Log.d(TAG,"boardInfo:"+bid+"/"+title+"/"+limit+"/"+date+"/"+writer+"/"+detail_name+"!!!");

            boardSetting();
            board.getUsers();
            Log.d(TAG,"hashUser :"+board.getUsers().toString());

            //세팅
            tv_detailTitle.setText(title);
            tv_detailDate.setText(datecrop);
            tv_detailTime.setText(timecrop);

//            tv_detailLimit.setText(limit);
            tv_detailCoursename.setText(course_name);
            tv_detailDetailCourse.setText(detail_name);

            Log.d(TAG, "boardSetting :"+boardId+"!!-"+boardTitle+boardLimit+boardWriter+boardDetailCourse);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    /**
     * 해당 게시판 유저 이미지 경로 붙여와서 Arraylist저장
     */
    public void getImgPath (){
        class GetImgPathJSON extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... params) {

                try{
                    URL url = new URL("http://115.71.233.85/detailsBoard.php");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoInput(true); // 쓰기가능
                    con.setDoOutput(true); //읽기가능
                    con.setUseCaches(false); //no cash
                    con.setRequestMethod("POST");

                    String param = "bid="+detailId;
                    Log.d("userImgPath_bid",param);

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
                        Log.d("getDetailJSON",json);
                        break;
                    }
//                   Log.d("getDetailJson.trim()",sb.toString().trim());

                   /*json 받아온 값 Arraylist 에 담음. => imgPath 추출 */
                    if (json != null){
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject != null){
                                JSONArray userInfo = jsonObject.getJSONArray("result");
                                Log.d("JSONArray userInfo", userInfo.toString());

                                for(int i=0; i<userInfo.length(); i++){
                                    JSONObject imgPathObj = (JSONObject) userInfo.get(i);
                                    imgPathList.add("http://115.71.233.85/"+imgPathObj.getString("imgPath"));
                                    Log.d(TAG, "imgPath!!!"+imgPathObj.getString("imgPath"));
                                    Log.d(TAG,"imgPathList_size :"+String.valueOf(imgPathList.size()));
                                }
                            }
                        }catch (JSONException e){
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

                for(int i = 0; i < imgPathList.size(); i++) {
                    System.out.println("one index " + i + " : value " + imgPathList.get(i));
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                tv_detailLimit.setText(imgPathList.size()+" / "+limit);

                for (int i = 0; i <imgPathList.size(); i++){

                    Log.d(TAG, "LimitImgView_size :"+imgPathList.size());

                    final CircleImageView personImg = new CircleImageView(getApplication());

                    final TextView name = new TextView(getApplication());

                    innerLinearLayout.addView(personImg);
                    innerLinearLayout.addView(name);


                    try{
                        Glide.with(getApplicationContext()).load(imgPathList.get(i)).into(personImg);
                    }catch (IllegalArgumentException ex){
                        Log.wtf("Glide-tag", String.valueOf(personImg.getTag()));
                    }

//            personImg.setImageResource(R.drawable.person3);
                    name.setText("");


                    personImg.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Log.e("Tag",""+personImg.getTag());
                        }
                    });
                }

            }
        }

        GetImgPathJSON getImgPathJSON = new GetImgPathJSON();
        getImgPathJSON.execute();
    }
}
