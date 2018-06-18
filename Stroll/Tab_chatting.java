package com.test.stroll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class Tab_chatting extends AppCompatActivity implements View.OnClickListener{

    public Tab_chatting() {
    }

    private final String TAG = "Tab_chatting";
    private EditText mMessageEdit;
    private ChatClient mChatClient;
    private ListView mListView;
    private List<MsgInfo> mChatData;
    private MyAdapter myAdapter;

    private  static String userid;
    private String bid;
    private ArrayList chatMemberArr;

    SharedPreferences sharedUser;
    private Users user;
    private static int userSeq;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_chatting);

        //User 정보
        sharedUser = this.getSharedPreferences("sharedUser",MODE_PRIVATE);
        user = new Users();
        Integer seq = sharedUser.getInt("seq",0);
        Log.d(TAG, "User_Seq : "+seq.toString());
        user.setSeq(seq);
        userSeq = user.getSeq();

        Intent intent = getIntent();
        bid = intent.getStringExtra("bid");
        Log.d(TAG,"bid : "+bid);
        chatMemberArr = new ArrayList();
//        getChatMember();

        init();
    }


    public void init() {

        userid = SaveSharedPreference.getUserId(this);
        Log.d(TAG, "userId: " + userid);

        mMessageEdit = (EditText) findViewById(R.id.edit_message);
        mListView = (ListView) findViewById(R.id.list_chat);

        mChatData = new ArrayList<>();
        mChatClient = new ChatClient();

        myAdapter = new MyAdapter(this, mChatData);
        mListView.setAdapter(myAdapter);



        // 서버에 접속
        new Thread(new Runnable() {
            @Override
            public void run() {

                mChatClient = new ChatClient();

                Log.d(TAG, "socket_infoMsg : "+ bid+":"+userid);
                mChatClient.setNickName(userid);
                mChatClient.setNum(bid);
                mChatClient.connect();
           /*     for(int i =0; i< chatMemberArr.size() ; i++){
                    String nickName = chatMemberArr.get(i).toString();
                    mChatClient.setNickName(nickName);
                    Log.d(TAG, "init()_chatMemberArrSize : "+chatMemberArr.size());
                    Log.d(TAG,"init()_nickName : "+nickName);
                    mChatClient.connect();
                }*/
            }
        }).start();
    }// end of init();

    /**
     * 채팅방 번호(bid) 에 따른 사용자 번호(seq) db로부터 불러온 뒤 ArrayList에 저장.
     *
     */
    public void getChatMember(){
        class GetChatMemberJSON extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    URL url = new URL("http://115.71.233.85/chatMember.php");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true); // 쓰기가능
                    con.setDoOutput(true); // 읽기가능
                    con.setUseCaches(false); // No cash
                    con.setRequestMethod("POST");

                    String param = "bid="+bid;
                    Log.d(TAG, "param_"+param);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(param.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader bufferedReader = null;
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        stringBuilder.append(json +"\n");
                        Log.d("getChatMember",json);
                        break;
                    }

                    /**JSON 받아온 값을 ArrayList에 담음*/
                if(json != null){
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        if(jsonObject != null){
                            JSONArray chatMember = jsonObject.getJSONArray("result");
                            Log.d(TAG,"JSONArray ChatMember"+chatMember.toString());

                            for(int i=0; i< chatMember.length(); i++){
                                JSONObject chatMemberObj = (JSONObject) chatMember.get(i);
                                String seq = chatMemberObj.getString("seq");

                                //chatMemberArr 에 추가
                                chatMemberArr.add(seq);
                                Log.d(TAG, "chatMemberArr_size : "+chatMemberArr.size()); // 채팅방에 있는 회원의 수.
                                Log.d(TAG, "chatMemberArr_get(0) : "+chatMemberArr.get(i)); // 회원의 seq 넘버.
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }//end of doInBackground

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                // 서버에 접속
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        mChatClient = new ChatClient();
//                mChatClient.setNickName(userid);
                        for(int i =0; i< chatMemberArr.size() ; i++){
                            String nickName = chatMemberArr.get(i).toString();
                            mChatClient.setNickName(nickName);
                            Log.d(TAG, "init()_chatMemberArrSize : "+chatMemberArr.size());
                            Log.d(TAG,"init()_nickName : "+nickName);
                            mChatClient.connect();
                        }
                    }
                }).start();
            }
        }



        GetChatMemberJSON getChatMemberJSON = new GetChatMemberJSON();
        getChatMemberJSON.execute();
    }

        @Override
    public void onClick(View v) {
//        String sendMsg= mMessageEdit.getText().toString();
//        Intent intent = new Intent(this.getApplicationContext(), ChatService.class);
//        intent.putExtra("msg",sendMsg);
//            intent.putExtra("nick",userid);
//        this.startService(intent);
        mChatClient.sendMsg(mMessageEdit.getText().toString());
        Log.d(TAG,"onClick_sendMsg :"+mMessageEdit.getText().toString());
        mMessageEdit.setText("");

            for(int i=0; i <1000; i++){
                HashMap<String, String > group = new HashMap<String , String>();

            }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mChatClient = new ChatClient();
                mChatClient.setBid(bid);
                mChatClient.bye();
            }
        }).start();
        EventBus.getDefault().unregister(this);
    }

    Handler mHandler = new Handler();

    @Subscribe
    @WorkerThread
    public void onMessage(final MsgInfo msgInfo) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatData.add(msgInfo);
                myAdapter.notifyDataSetChanged();
            }
        });
    }

    private static class MyAdapter extends BaseAdapter {
        private SimpleDateFormat mmSimpleDateFormat = new SimpleDateFormat("a hh:mm");

        private final LayoutInflater mmLayoutInflater;
        private final List<MsgInfo> mmData;

        public MyAdapter(Context context, List<MsgInfo> data) {
            mmLayoutInflater = LayoutInflater.from(context);
            mmData = data;
        }

        @Override
        public int getCount() {
            return mmData.size();
        }

        @Override
        public Object getItem(int position) {
            return mmData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            String nick = String.valueOf(userSeq);
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mmLayoutInflater.inflate(R.layout.item_chat, parent, false);
                holder.image = (ImageView) convertView.findViewById(R.id.img_you);
                holder.message_me = (TextView) convertView.findViewById(R.id.msg_me);
                holder.message_you = (TextView) convertView.findViewById(R.id.msg_you);
                holder.time_me = (TextView) convertView.findViewById(R.id.time_me);
                holder.time_you = (TextView) convertView.findViewById(R.id.time_you);
                holder.nickname = (TextView) convertView.findViewById(R.id.nickname);
                holder.layout_me = (LinearLayout) convertView.findViewById(R.id.layout_me);
                holder.layout_you = (LinearLayout) convertView.findViewById(R.id.layout_you);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MsgInfo msgInfo = (MsgInfo) getItem(position);
            if (msgInfo.getNickName().equals(userid)) { // nick
                holder.message_me.setText(msgInfo.getMessage());
                holder.time_me.setText(mmSimpleDateFormat.format(new Date()));

                holder.layout_me.setVisibility(View.VISIBLE);
                holder.layout_you.setVisibility(View.GONE);
            } else {
                holder.message_you.setText(msgInfo.getMessage());
                holder.time_you.setText(mmSimpleDateFormat.format(new Date()));
                holder.nickname.setText(msgInfo.getNickName());
//                holder.image.setImageResource(R.drawable.girl);

                holder.layout_me.setVisibility(View.GONE);
                holder.layout_you.setVisibility(View.VISIBLE);

                if (position > 0 && msgInfo.getNickName().equals(((MsgInfo)getItem(position - 1)).getNickName())) {
                    holder.image.setVisibility(View.INVISIBLE);
                    holder.nickname.setVisibility(View.GONE);
                } else {
                    holder.image.setVisibility(View.VISIBLE);
                    holder.nickname.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }

        private static class ViewHolder {
            LinearLayout layout_you;
            LinearLayout layout_me;
            TextView time_me;
            TextView time_you;
            TextView message_me;
            TextView message_you;
            TextView nickname;
            ImageView image;
        }
    }

}
