package com.test.stroll;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class Tab_gallery extends Fragment {

    private static final String TAG ="Tab_gallery";

    Calendar initialTime = Calendar.getInstance();
    /*Datepicker 와 Timepicker 에서 default값*/
    // today
    int t_year;
    int t_month;
    int t_day;
    private Date tdate ;
    private Date onedate;
    private Date twodate;


    //json 파싱
    String myJSON;
    private static final String TAG_RESULT = "result";
    private static final String TAG_SEQ = "bid";
    private static final String TAG_TITLE = "title";
    private static final String TAG_LIMIT_P = "limit_p";
    private static final String TAG_DATE = "date";
    private static final String TAG_COURSE_NAME ="course_name";
    private static final String TAG_DETAIL_NAME ="detail_name";
//    private static final String TAG_TIME = "time";
//    private static final String TAG_YOIL = "yoil";
//    private static final String TAG_DAY = "day";

    JSONArray board = null;

    Context mContext;
    RecyclerView rv_board;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<BoardItem> boardItemList;

    private ChatClient mChatClient;
    private  static String num;

    private int lastPosition =-1;

    //생성자
    public Tab_gallery() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tab_gallery, container, false);

        t_year = initialTime.get(Calendar.YEAR);
        t_month = initialTime.get(Calendar.MONTH);
        t_day = initialTime.get(Calendar.DAY_OF_MONTH);

        //Add board FloatingActionButton
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), AddBoard.class);
                startActivityForResult(intent,99);

            }
        });

        mContext = getActivity();
        rv_board = (RecyclerView) view.findViewById(R.id.rv_board);
        rv_board.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this.getActivity());
        rv_board.setLayoutManager(layoutManager);
        rv_board.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        rv_board.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), rv_board ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
//                        Toast.makeText(getActivity(), (position+1)+"번쨰 클릭!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity(), boardItemList.get(position).getBid(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();

                        intent.setClass(getActivity(), DetailsActivity.class);

                        intent.putExtra("getId",boardItemList.get(position).getBid());
                        Log.d(TAG, "getId:"+boardItemList.get(position).getBid());
                        intent.putExtra("getTitle", boardItemList.get(position).getTitle());
                        intent.putExtra("getDate",boardItemList.get(position).getDate());
                        intent.putExtra("getYoil",boardItemList.get(position).getYoil());
                        intent.putExtra("getTime",boardItemList.get(position).getTime());
                        intent.putExtra("getLimit",boardItemList.get(position).getLimit());
                        intent.putExtra("getCourse_name",boardItemList.get(position).getCourse_name());
                        intent.putExtra("getDetail_name", boardItemList.get(position).getDetail_name());

                        Log.d(TAG,"intent_putExtra_getDetail_name"+boardItemList.get(position).getDetail_name());

                        startActivity(intent);

                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );


        boardItemList = new ArrayList<BoardItem>();
        getData("http://115.71.233.85/getBoardData.php");

        return view;
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~end of oncreateVIew


    /**
     * Add board 에서 넘어온 값 리스트로 가져옴.
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("CallOnActivityResult","onActivityResult()"); // onActivityResult 가 불리는지
        if(resultCode == RESULT_OK)
        {
            if(requestCode==99)
            {
                Log.d("onActivityResult데이타", data.getStringExtra("b_detail_name"));
                boardItemList.add(new BoardItem(
                        data.getStringExtra("b_id"),
                        data.getStringExtra("b_title"),
                        data.getStringExtra("b_date"),
                        data.getStringExtra("b_time"),
                        data.getStringExtra("b_limit"),
                        data.getStringExtra("b_course_name"),
                        data.getStringExtra("b_detail_name"),
                        data.getStringExtra("b_yoil"),
                        data.getStringExtra("b_day")
                ));

            }
            adapter.notifyItemInserted(0);
            getIndex("http://115.71.233.85/getIndex.php");
        }

    }


    /**
     * AsyncTask 비동기 방식
     * 입력한 URL (서버)에서 마지막 추가한 index값 가져옴
     * @param url
     */
    public void getIndex(String url) {
        class GetIndexJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                        Log.d("마지막index : ", json);
                        num = json;
                    }

                    Log.d("sb.toString().trim()",sb.toString().trim());
                    return sb.toString().trim();


                } catch (Exception e) {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        mChatClient = new ChatClient();
                        mChatClient.setBid(num);
                        mChatClient.roomConnect();
           /*     for(int i =0; i< chatMemberArr.size() ; i++){
                    String nickName = chatMemberArr.get(i).toString();
                    mChatClient.setNickName(nickName);
                    Log.d(TAG, "init()_chatMemberArrSize : "+chatMemberArr.size());
                    Log.d(TAG,"init()_nickName : "+nickName);
                    mChatClient.connect();
                }*/
                    }
                }).start();
            }
        }
        GetIndexJSON index = new GetIndexJSON();
        index.execute(url);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~end of getdata

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
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                        Log.d("json", json);
                    }

                    Log.d("sb.toString().trim()",sb.toString().trim());
                    return sb.toString().trim();


                } catch (Exception e) {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String result) {

                //result = sb.toString().trim()
                myJSON = result;
                showList();

                adapter = new BoardAdapter(boardItemList, mContext);
                rv_board.setAdapter(adapter);

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~end of getdata

    /**
     * getData 로 불러온 데이터를 json 파싱을 통해 String 값으로 변환 후 arraylist에 넣어줌 .
     */
    protected void showList() {
        try {

            //myJSON = sb.toString().trim()
            JSONObject jsonObject = new JSONObject(myJSON);

            //JsonArray board;
            board = jsonObject.getJSONArray(TAG_RESULT);

            for (int i = 0; i < board.length(); i++) {
                JSONObject c = board.getJSONObject(i);
                String seq = c.getString(TAG_SEQ);
                String title = c.getString(TAG_TITLE);
                String limit = c.getString(TAG_LIMIT_P);
                String dates = c.getString(TAG_DATE);
                String course_name = c.getString(TAG_COURSE_NAME);
                String detail_name = c.getString(TAG_DETAIL_NAME);
                Log.d("dates",dates);
//                String time = c.getString(TAG_TIME);
//                String yoil = c.getString(TAG_YOIL);
//                String day = c.getString(TAG_DAY);


                //DateTime 값 받아와서 date 로 짜름
                DateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date d = f.parse(dates);
                Log.d("Date_d", d.toString());
                DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateformat.format(d);
                Log.d("date로 짜른거",date);

                //DateTime 값 받아와서 time으로 짜름
                DateFormat timeformat = new SimpleDateFormat("aa h시mm분");
                String time = timeformat.format(d);
                Log.d("time으로 짜른거",time);

                DateFormat yoilformat = new SimpleDateFormat("E");
                String yoil = yoilformat.format(d);
                Log.d("yoil로 짜른거", yoil);

                DateFormat dayformat = new SimpleDateFormat("dd");
                String day ;
//                String day = dayformat.format(d);
//                Log.d("day로 짜른거", day);

                tdate = new Date(t_year-1900,t_month,t_day);
                onedate = new Date(t_year-1900,t_month,t_day+1);
                twodate = new Date(t_year-1900,t_month,t_day+2);

                //오늘
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date1 = simpleDateFormat.format(tdate);
                Date curdate = simpleDateFormat.parse(date1);
                Log.d("curdate",curdate.toString());

                //어제
                String date3 = simpleDateFormat.format(onedate);
                Date yesterday = simpleDateFormat.parse(date3);
                Log.d("yesterday",yesterday.toString());

                //모레
                String date4 = simpleDateFormat.format(twodate);
                Date twdDaysAfter = simpleDateFormat.parse(date4);
                Log.d("twdDaysAfter",twdDaysAfter.toString());

                //달력의 date . pick 한 date
                String date2 = simpleDateFormat.format(d);
                Date meetdate = simpleDateFormat.parse(date2);
                Log.d("meetdate",meetdate.toString());

                Log.d("tdate",tdate.toString());
                if(curdate.equals(meetdate)){
                    day = "오늘";
                }else if(yesterday.equals(meetdate)){
                    day ="내일";
                }
//                else  if(twdDaysAfter.equals(meetdate)){
//                    day="모레";
//                }
                else{
                     day = dayformat.format(d);
                }


                boardItemList.add(new BoardItem(seq,title,date,time,limit,course_name,detail_name,yoil,day) );
                Log.d("seq",seq);
                Log.d("title",title);
                Log.d("limit",limit);
                Log.d("date",dates);
                Log.d("detail_name",detail_name);
                Log.d("time",time);
                Log.d("yoil",yoil);
                Log.d("day",day);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (IndexOutOfBoundsException e){
            Log.e("Error", "IndexOutOfBoundsException in RecyclerView happens");
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~end of showList

//    private String getDateTime() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat(
//                "yyyy-MM-dd HH:mm", Locale.getDefault());
//        Date date = new Date(c_year-1900,c_month,c_day,c_hour,c_minute);
//        Log.d("getDateTime",date.toString());
//        return dateFormat.format(date);
//    }

    /**
     *  BoardAdapter
     */
    class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {

        private Context context;
        private ArrayList<BoardItem> boardItemlist;

        public BoardAdapter(ArrayList<BoardItem> items, Context mContext) {
            boardItemlist = items;
            context = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_item, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            Log.d("boardItemList", boardItemlist.get(0).title);

            String b = String.valueOf(boardItemlist.size());
            Log.d("boardItemListSize",b);

//            Log.d("boardItemList",boardItemlist.get(position).get(0).toString());

            String day = boardItemlist.get(position).day;
            holder.tv_title.setText(boardItemlist.get(position).title);
            holder.tv_date.setText(boardItemlist.get(position).date);
            holder.tv_time.setText(boardItemlist.get(position).time);
            holder.tv_yoil.setText(boardItemlist.get(position).yoil);
            holder.tv_day.setText(boardItemlist.get(position).day);
             if(day.equals("오늘")){
                holder.tv_day.setTextColor(Color.RED);
            }else{
                holder.tv_day.setTextColor(Color.BLACK);
            }
            holder.tv_limit.setText(boardItemlist.get(position).limit);
            holder.tv_location.setText(boardItemlist.get(position).detail_name);
        }


        @Override
        public int getItemCount() {
            {
                return boardItemlist.size();
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView tv_title, tv_date,tv_time,tv_yoil,tv_day,tv_limit ,tv_location;
            ImageView img_clock, img_marker;

            public ViewHolder (View itemView) {

                super(itemView);
                tv_title = (TextView) itemView.findViewById(R.id.tv_title);
                tv_date = (TextView) itemView.findViewById(R.id.tv_date);
                tv_time = (TextView) itemView.findViewById(R.id.tv_time);
                tv_yoil = (TextView) itemView.findViewById(R.id.tv_yoil);
                tv_day = (TextView) itemView.findViewById(R.id.tv_day);
                tv_limit = (TextView) itemView.findViewById(R.id.tv_limit);
                tv_location = (TextView) itemView.findViewById(R.id.tv_location);
                img_clock = (ImageView) itemView.findViewById(R.id.img_clock);
                img_marker = (ImageView) itemView.findViewById(R.id.img_marker);

                itemView.setOnClickListener(this);

            }

            //itemview 클릭시 발생하는 clickEvent
            @Override
            public void onClick(View v) { //custom dialog 띄워주기 구현

//                Toast.makeText(getActivity(), tv_item.getText().toString(), Toast.LENGTH_SHORT).show();

            }

        } // end of ViewHolder
    }
}
