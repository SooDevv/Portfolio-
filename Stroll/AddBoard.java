package com.test.stroll;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddBoard extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback{

    private static final String TAG = "AddBoard";
    private DB_BoardManager db_boardManager;
    private ArrayList<String> results;

    private EditText et_title ;
    private EditText et_limit ;

    private String writer;

    private Time mtime;

    Button btn_add_meet ;
    Button btn_date;
    Button btn_time;
    TextView tv_yoil;
    TextView tv_day;

    final int DIALOG_DATE = 1;
    final int DIALOG_TIME = 2;

    Spinner spinner01;
    Spinner spinner02;
    ArrayList<Course> courseList;
    ArrayList<DetailCourse> detailCourseList;
    ArrayList<MeetPlace> meetPlaces;
    String course_name;
    String detail_name;

    String time ;
    String yoil ;
    String day ;





    private String URL_COURSE = "http://115.71.233.85/getCourse.php";

    Calendar initialTime = Calendar.getInstance();
    /*Datepicker 와 Timepicker 에서 default값*/
    // today
    int t_hour;
    int t_minute;
    int t_year;
    int t_month;
    int t_day;
    private Date mdate ;

    /*Datepicker 와 Timepicker 에서 선택한 값*/
    //choose
    int c_year;
    int c_month;
    int c_day;
    int c_hour;
    int c_minute;

    GoogleMap googleMap;
    double lat;
    double lng;
    MapFragment mapFragment;

    //생성자
    public AddBoard(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_board);


        init();
    }

    public void init(){

        //스피너 초기화
        spinner01 = (Spinner) findViewById(R.id.spinner01);
        courseList = new ArrayList<Course>();
        new GetCourse().execute();
        spinner01.setOnItemSelectedListener(courseListener);


        spinner02 = (Spinner) findViewById(R.id.spinner02);
        detailCourseList = new ArrayList<DetailCourse>();
        spinner02.setOnItemSelectedListener(detailListener);

//        courseSpinners();


        //picker dialog에서 현재날짜와 현재시간 맞추기 위함.
        t_hour = initialTime.get(Calendar.HOUR_OF_DAY);
        t_minute = initialTime.get(Calendar.MINUTE);
        t_year = initialTime.get(Calendar.YEAR);
        t_month = initialTime.get(Calendar.MONTH);
        t_day = initialTime.get(Calendar.DAY_OF_MONTH);

        et_title = (EditText) findViewById(R.id.add_bd_title);
        et_limit = (EditText) findViewById(R.id.add_bd_limit);

        btn_add_meet= (Button) findViewById(R.id.add_bd_btn_meet); // 참석버튼
        btn_date = (Button) findViewById(R.id.btn_date); // Date picker
        btn_time = (Button) findViewById(R.id.btn_time); // Time picker
        btn_date.setHint(t_year+"년"+(t_month+1)+"월"+t_day+"일");
        btn_time.setHint(t_hour+"시"+t_minute+"분");

        //달력 cardview
        tv_yoil = (TextView) findViewById(R.id.tv_yoil);
        tv_day = (TextView) findViewById(R.id.tv_day);

        //cardView tv_yoil & tv_day
        mdate = new Date(t_year,t_month,t_day);
        DateFormat dateFormat = new SimpleDateFormat("E");
        String yoil = dateFormat.format(mdate);
        tv_yoil.setHint(yoil);

        String cal_day = String.valueOf(t_day);
        tv_day.setHint(cal_day);


        //로그인 후에 로그인 유지
        writer = SaveSharedPreference.getUserId(AddBoard.this);
        Log.d("AddBoardUserId",writer);


        //지도 설정
        FragmentManager fragmentManager= getFragmentManager();
         mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.addmap);
         mapFragment.getMapAsync(this);


        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_DATE);
            }
        });
        btn_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_TIME);
            }
        });

        db_boardManager = new DB_BoardManager();
        results = new ArrayList<String>();
    }

    //모임만들기 버튼
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            //모임만들기 버튼
            case R.id.add_bd_btn_meet:

                String title = et_title.getText().toString();
                String bd_people = et_limit.getText().toString();
                String date = getDateTime();
                String writer = SaveSharedPreference.getUserId(AddBoard.this);

                Log.d("add_bd_btn_meet", "글제목" + title + "인원수" + bd_people + "날짜" + date + "작성자" + writer);



                while (true) {
                    //예외처리
                    if (et_title.length() < 1) {
                        Toast.makeText(getApplicationContext(), "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (bd_people.getBytes().length <= 0) {
                        Toast.makeText(getApplicationContext(), "정원을 입력해주세요", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (btn_date.getText().length() <=0) {
                        Toast.makeText(getApplicationContext(), "모임날짜를 설정해 주세요", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (btn_time.getText().length()<=0) {
                        Toast.makeText(getApplicationContext(), "모임시간을 설정해 주세요", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (course_name.equals("nothing")) {
                        Toast.makeText(getApplicationContext(), "코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (detail_name.length() < 1) {
                        Toast.makeText(getApplicationContext(), "상세 코스를 선택해 주세요", Toast.LENGTH_SHORT).show();
                        break;
                    } else {

                        //db 에 넣어주기
                        insertToDatabase(title, bd_people, date, writer, course_name, detail_name);
                    }

                    break;
                }
        }
    }

    //datepicker 랑 timepicker 를 date로
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(c_year-1900,c_month-1,c_day,c_hour,c_minute);
        Log.d("getDateTime",date.toString());
        return dateFormat.format(date);
    }

    /**
     * SPINNER 01 DATA
     */
    private class GetCourse extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... arg0) {

                ServiceHandler jsonParser = new ServiceHandler();
                String json = jsonParser.makeServiceCall(URL_COURSE, ServiceHandler.GET);

                Log.d("Result: ", ">"+ json);

                if(json != null) {
                    try{
                        JSONObject jsonObject = new JSONObject(json);
                        if(jsonObject != null){
                            JSONArray course = jsonObject.getJSONArray("result");

                            for(int i=0 ; i<course.length(); i++){
                                JSONObject courseObj = (JSONObject) course.get(i);
                                Course cour = new Course(courseObj.getInt("no"), courseObj.getString("name"));
                                courseList.add(cour);
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }else {
                    Log.e("JSON Data", "Didn't receive any data from server!");
                }
                return null;
            }

          @Override
          protected void onPostExecute(Void result) {
              super.onPostExecute(result);

              populateSpinner();
          }
        }
   /*SPINNER 01 LIST*/
    private void populateSpinner() {
        List<String> labels = new ArrayList<String>();
        for (int i = 0; i < courseList.size();i++){
            labels.add(courseList.get(i).getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.notifyDataSetChanged();
        spinner01.setAdapter(spinnerAdapter);
    }
    /*SPINNER 01 SELECTED LISTENER*/
    private AdapterView.OnItemSelectedListener courseListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            Toast.makeText(getApplicationContext(),"선택한 position은 ."+position,Toast.LENGTH_SHORT).show();

            detailCourseList.clear();
            getDetailCouurse(position);

            if(position==1){
                course_name = "nothing";
            }



//            getDetailCouurse(position);

        /*    switch (position){
                case(0):
                    Toast.makeText(getApplicationContext(),"코스를 선택해주세요.",Toast.LENGTH_SHORT).show();
                    break;
                case(po):
                    getDetailCouurse(1);
                    break;
            }*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * SPINNER 02 DATA
     * @param position
     */
    public void getDetailCouurse (final int position){ // int position = spinner01 의 position값
       class GetDetailCourseJSON extends AsyncTask<Void,Void,Void>{

           int num = position;

           @Override
           protected Void doInBackground(Void... params) {

               try{
                   URL url = new URL("http://115.71.233.85/getDetailCourse.php");
                   HttpURLConnection con = (HttpURLConnection) url.openConnection();
                   con.setDoInput(true); // 쓰기가능
                   con.setDoOutput(true); //읽기가능
                   con.setUseCaches(false); //no cash
                   con.setRequestMethod("POST");

                   String param = "num="+num;
                   Log.d("detail_param",param);

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

                   /*json 받아온 값 Arraylist 에 담음. => spinner 02 에 뿌려주기 위함. */
                   if (json != null){
                       try {
                           JSONObject jsonObject = new JSONObject(json);
                           if(jsonObject != null){
                               JSONArray detailcourse = jsonObject.getJSONArray("result");
                               Log.d("JSONArray detailcourse", detailcourse.toString());

                               for(int i=0; i<detailcourse.length(); i++){
                                   JSONObject detailObj = (JSONObject) detailcourse.get(i);
                                   DetailCourse detail = new DetailCourse(detailObj.getInt("num"),
                                           detailObj.getString("course_name"), detailObj.getString("detail_name"), detailObj.getDouble("lat"),detailObj.getDouble("lng"));
                                   detailCourseList.add(detail);
                                   Log.d("course_name",detailObj.getString("course_name"));
                                   Log.d("getDetailCourse-json-detailCourseList",String.valueOf(detailCourseList.size()));
                               }
                           }
                       }catch (JSONException e){
                           e.printStackTrace();
                       }
                   }else{
                       Log.e("JSON Data-detailCourse", "Didn't receive any data from server!");
                   }


               }catch (UnsupportedEncodingException e){
                   e.printStackTrace();
               }catch (IOException e){
                   e.printStackTrace();
               }
               return null;
           }

           @Override
           protected void onPostExecute(Void result) {
               super.onPostExecute(result);

                populateSpinner02();
               Log.d("populateSpinner02-onPostExecute","populateSpinner02-onPostExecute");
           }
       }

        GetDetailCourseJSON getDetailCourseJSON = new GetDetailCourseJSON();
        getDetailCourseJSON.execute();
    }

    /*SPINNER 02 LIST*/
    private void populateSpinner02() {
        List<String> labels = new ArrayList<String>();
        for (int i = 0; i < detailCourseList.size();i++){
            labels.add(detailCourseList.get(i).getDetail_name());

            Log.d("detailCourseList.size()", String.valueOf(detailCourseList.size()));
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.notifyDataSetChanged();
        spinner02.setAdapter(spinnerAdapter);
    }

    /*SPINNER 02 SELECTED LISTENER*/
    private AdapterView.OnItemSelectedListener detailListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            lat = detailCourseList.get(position).getLat();
            lng = detailCourseList.get(position).getLng();
            Log.d("spinner02_lat", String.valueOf(lat));
            Log.d("spinner02_lng", String.valueOf(lng));

            setGpsCurrent(lat,lng);

            course_name = detailCourseList.get(position).getCourse_name();
            detail_name = detailCourseList.get(position).getDetail_name();

            Log.d("board_table",course_name+"!!"+detail_name);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * 모임개설글
     * insert into board
     * @param title
     * @param limit
     */
    private void insertToDatabase(final String title, final String limit, final String date, final String writer,
                                  final String course_name, final  String detail_name){

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                loading = ProgressDialog.show(AddBoard.this,
                        "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                loading.dismiss();
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();


                DateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                try {
                    Date d = f.parse(date);
                    DateFormat timeformat = new SimpleDateFormat("hh:mm");
                    time = timeformat.format(d);

                    DateFormat yoilformat = new SimpleDateFormat("E");
                    yoil = yoilformat.format(d);

                    DateFormat dayformat = new SimpleDateFormat("dd");
                    day = dayformat.format(d);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
//                Intent mainIntent = new Intent(getApplicationContext(), Tab_gallery.class);
//                startActivity(mainIntent);
                Intent intent = new Intent();
                intent.putExtra("b_id", "방금 올린 글");
                intent.putExtra("b_title",title);
                intent.putExtra("b_limit",limit);
                intent.putExtra("b_date",date);
                intent.putExtra("b_time",time);
                intent.putExtra("b_course_name",course_name);
                intent.putExtra("b_detail_name",detail_name);
                intent.putExtra("b_yoil",yoil);
                intent.putExtra("b_day",day);
                Log.d("Add_board.putExtra",title+limit+date+time+detail_name+yoil+day);


                setResult(RESULT_OK,intent);

                Log.d("setresult_yoil",yoil);
                Log.d("setresult_day",day);

                finish();
            }

            @Override
            protected String doInBackground(String... params) {

                try{
                    String title = (String)params[0];
                    String limit = (String)params[1];
                    String date =  (String)params[2];
                    String writer = (String)params[3];
                    String course_name = (String)params[4];
                    String detail_name = (String)params[5];

                    Log.d("insertIntoBoard",title+"//"+limit+"//"+date+"//"+writer+"//"+course_name+"//"+detail_name);


                    String link="http://115.71.233.85/addBoard.php";
                    String data  = URLEncoder.encode("title", "UTF-8") + "="
                            + URLEncoder.encode(title, "UTF-8");
                    data += "&" + URLEncoder.encode("limit", "UTF-8") + "="
                            + URLEncoder.encode(limit, "UTF-8");
                    data += "&" + URLEncoder.encode("date", "UTF-8") + "="
                            + URLEncoder.encode(date, "UTF-8");
                    data += "&" + URLEncoder.encode("writer", "UTF-8") + "="
                            + URLEncoder.encode(writer, "UTF-8");
                    data += "&" + URLEncoder.encode("course_name", "UTF-8") + "="
                            + URLEncoder.encode(course_name, "UTF-8");
                    data += "&" + URLEncoder.encode("detail_name", "UTF-8") + "="
                            + URLEncoder.encode(detail_name, "UTF-8");


                    Log.d("data!!!",data.toString());


                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr =
                            new OutputStreamWriter(conn.getOutputStream());

                    wr.write( data );
                    wr.flush();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                        Log.d(TAG, "readLine"+line);
                        break;
                    }
                    return sb.toString();
                }
                catch(Exception e){

                    return new String("Exception: " + e.getMessage());
                }

            }
        }

        InsertData task = new InsertData();
        task.execute(title,limit,date,writer,course_name,detail_name);
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DATE :
                DatePickerDialog dpd = new DatePickerDialog(
                        AddBoard.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                                Toast.makeText(getApplicationContext(),
//                                        year+"년 "+(monthOfYear+1)+"월 "+dayOfMonth+"일 을 선택했습니다",
//                                        Toast.LENGTH_SHORT).show();
                                btn_date.setText(year+"년 "+(monthOfYear+1)+"월 "+dayOfMonth+"일");


                                Date d = new Date(year, monthOfYear,dayOfMonth-1);
                                DateFormat dateFormat = new SimpleDateFormat("E");
                                String cal_yoil = dateFormat.format(d);

                                tv_yoil.setText(cal_yoil);
                                Log.d("cal_yoil",cal_yoil);

                                // cardView 요일
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm");
//                                Date d = new Date(year, monthOfYear,dayOfMonth-1);

//                                mdate = d;
//
//                                Log.d("date", d.toString());
//                                String dayOfTheWeek = sdf.format(d);
//                                tv_yoil.setText(dayOfTheWeek);
//
                                //cardView 날짜
                                String cal_day = String.valueOf(dayOfMonth);
                                tv_day.setText(cal_day);

//                                mdate = String.format("%d / %d/ %d", year, (monthOfYear+1), dayOfMonth);
                                Toast.makeText(getApplicationContext(),year+"년 "+(monthOfYear+1)+"월 "+dayOfMonth+"일",Toast.LENGTH_SHORT).show();
                                c_year = year;
                                c_month = monthOfYear+1;
                                c_day = dayOfMonth;

                            }
                        },
                  t_year, t_month, t_day);

                return dpd;

            case DIALOG_TIME :
                TimePickerDialog tpd = new TimePickerDialog(
                        AddBoard.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                                Toast.makeText(getApplicationContext(),
//                                        hourOfDay +"시 " + minute+"분 을 선택했습니다",
//                                        Toast.LENGTH_SHORT).show();
                                btn_time.setText(hourOfDay +"시 " + minute+"분");

//                                mtime = String.format("%d 시 %d분", hourOfDay,minute);
                                Toast.makeText(getApplicationContext(),hourOfDay +"시 " + minute+"분",Toast.LENGTH_SHORT).show();
                                c_hour = hourOfDay;
                                c_minute = minute;
                            }
                        },
                       t_hour,t_minute,false);

                return tpd;
        }


        return super.onCreateDialog(id);
    }

    @Override
    public void onMapReady( final GoogleMap map) {

        this.googleMap = map;

/*        if(Double.isNaN(lat) && Double.isNaN(lng)){

            LatLng SEOUL = new LatLng(37.689157, 127.046733);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(SEOUL);
            markerOptions.title("서울");
            markerOptions.snippet("한국의 수도");
            map.addMarker(markerOptions);

            map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
            map.animateCamera(CameraUpdateFactory.zoomTo(15));
        }else{*/
            LatLng SEOUL = new LatLng(37.689157, 127.046733);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(SEOUL);
            markerOptions.title("서울");
            markerOptions.snippet("한국의 수도");
            map.addMarker(markerOptions);

            map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
            map.animateCamera(CameraUpdateFactory.zoomTo(15));
//        }


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getApplicationContext(), "hit", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("TabHost","Tab_seconds".toString());
        startActivity(intent);

    }

    private void setGpsCurrent(double lat , double lng){

        LatLng SEOUL = new LatLng(lat, lng);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

}

