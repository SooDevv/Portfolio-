package com.test.stroll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.content.Context.MODE_PRIVATE;

public class Tab_chat extends Fragment {

    public Tab_chat() {
    }
    private ChatClient mChatClient;
    private static final String TAG = "Tab_chat";
    int userSeq;
    private Users user;
    SharedPreferences sharedUser;
    Integer bidJSON;
    Date date;
    ArrayList roomList = new ArrayList();
    ArrayList bidList = new ArrayList();
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView rv_chatRoom;
    Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_tab_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //User 정보
        sharedUser = getActivity().getSharedPreferences("sharedUser", MODE_PRIVATE);
        user = new Users();
        Integer seq = sharedUser.getInt("seq", 0);
        Log.d(TAG, "user정보 :" + seq.toString());
        user.setSeq(seq);
        userSeq = user.getSeq();
        rv_chatRoom = (RecyclerView) view.findViewById(R.id.rv_chatRoom);
        rv_chatRoom.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this.getActivity());
        rv_chatRoom.setLayoutManager(layoutManager);


        rv_chatRoom.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), rv_chatRoom ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
//                        Toast.makeText(getActivity(), (position+1)+"번쨰 클릭!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity(), bidList.get(position).toString(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), Tab_chatting.class);
                        intent.putExtra("bid",bidList.get(position).toString());
                        getActivity().startActivity(intent);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );

        roomList = new ArrayList();
        getUserBoard();

    }

    /**
     * 해당 게시판 유저 이미지 경로 붙여와서 Arraylist저장
     */
    public void getUserBoard() {
        class GetUserBoardJSON extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    URL url = new URL("http://115.71.233.85/attendBoard.php");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoInput(true); // 쓰기가능
                    con.setDoOutput(true); //읽기가능
                    con.setUseCaches(false); //no cash
                    con.setRequestMethod("POST");

                    String param = "seq=" + userSeq;
                    Log.d("userSEQ", param);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(param.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader rd = null;
                    rd = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    String json;
                    while ((json = rd.readLine()) != null) {
                        sb.append(json + "\n");
                        Log.d("getUserBoardJSON", json);
                        break;
                    }
//                   Log.d("getDetailJson.trim()",sb.toString().trim());

                   /*json 받아온 값 Arraylist 에 담음. */
                    if (json != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if (jsonObject != null) {
                                JSONArray userBoard = jsonObject.getJSONArray("result");
                                Log.d("JSONArray userInfo", userBoard.toString());

                                for (int i = 0; i < userBoard.length(); i++) {
                                    JSONObject userBoardObj = (JSONObject) userBoard.get(i);
                                    bidJSON = userBoardObj.getInt("bid");
                                    Log.d(TAG, "bidJSON" + bidJSON.toString());
                                    String dates = userBoardObj.getString("date");

                                    //String to Date 형변환
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    try {
                                        date = format.parse(dates);
                                        Log.d(TAG, "boardInfo_DATE : " + date.toString());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    //DateTime 값 받아와서 date 로 짜름
                                    DateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                    Date d = f.parse(dates);
                                    Log.d("Date_d", d.toString());
                                    DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
                                    String datecrop = dateformat.format(d);
                                    Log.d("date로 짜른거", datecrop);

                                    roomList.add(datecrop);
                                    bidList.add(bidJSON);
                                    Log.d(TAG, "bidList_size" + bidList.size());
                                    Log.d(TAG, "roomList_size :" + String.valueOf(roomList.size()));
                                    Log.d(TAG,"roomList_get(0)"+roomList.get(0));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("JSON_imgPath", "Didn't receive any data from server!");
                    }


                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < roomList.size(); i++) {
                    System.out.println("one index " + i + " : value " + roomList.get(i));
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                adapter = new roomAdapter(roomList,mContext);
                rv_chatRoom.setAdapter(adapter);
                Log.d(TAG,"onPost :"+roomList.size());
            }
        }

        GetUserBoardJSON getUserBoardJSON = new GetUserBoardJSON();
        getUserBoardJSON.execute();
    }

    class roomAdapter extends RecyclerView.Adapter<roomAdapter.ViewHolder> {

        Context context;
        private ArrayList roomList;

        public roomAdapter(ArrayList list, Context mContext) {
            roomList = list;
            context = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatroom, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,final int position) {

            Log.d(TAG, "onBindViewHolder_RoomList"+roomList.get(0));
            Log.d(TAG, "onBindViewHolder_RoomListSize"+roomList.size());


            holder.tv_roomName.setText(roomList.get(position).toString()+" 모임방 >>");
        }

        @Override
        public int getItemCount() {
            {
                Log.d(TAG,"getItemCount"+roomList.size());
                return roomList.size();
            }
    }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView tv_roomName;

            public ViewHolder (View itemView){
                super(itemView);

                tv_roomName = (TextView) itemView.findViewById(R.id.tv_roomname);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {


            }
        }

    }
}
