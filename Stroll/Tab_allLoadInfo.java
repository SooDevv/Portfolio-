package com.test.stroll;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * 각 둘레길 코스별 정보 CLASS
 * 정보  1)custom dialog 를 이용한 전반적인 둘레길 정보 (ex. 소요시간, 난이도 등)
 *       2)지도보기 = Gmap 위에 tmap 길찾기 적용하여 polyline 그려준것.
 */

public class Tab_allLoadInfo extends Fragment {

    public Tab_allLoadInfo()
    {
    }

    Context mContext;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private SharedPreferences shared_position;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.all_load_info);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.all_load_info, container, false);

        mContext = getActivity();
        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        shared_position = this.getActivity().getSharedPreferences("shared_position", MODE_PRIVATE);


        /**
         * 데이터 초기화 후에
         * ItemList 에 아이템 객체 넣기
         */

        ArrayList<LoadItem> loadItemList = new ArrayList<LoadItem>();

        loadItemList.add(new LoadItem("수락·불암산코스", R.drawable.num1));
        loadItemList.add(new LoadItem("용마·아차산코스", R.drawable.num2));
        loadItemList.add(new LoadItem("고덕·일차산코스", R.drawable.num3));
        loadItemList.add(new LoadItem("대모·우면산코스",R.drawable.num4));
        loadItemList.add(new LoadItem("관악산코스",R.drawable.num5));
        loadItemList.add(new LoadItem("안양천코스",R.drawable.num6));
        loadItemList.add(new LoadItem("봉선·앵봉산코스",R.drawable.num7));
        loadItemList.add(new LoadItem("북한산코스",R.drawable.num8));

        layoutManager = new LinearLayoutManager(this.getActivity());
        adapter = new MyAdapter(loadItemList,mContext);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        return layout;

    }



    //예제1 방식
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

        private Context context;
        private ArrayList<LoadItem> itemList;

        private int lastPosition =-1;


        /**
         * 생성자
         * @param items
         * @param mContext
         */
        public MyAdapter(ArrayList<LoadItem> items, Context mContext){
            itemList = items;
            context = mContext;
        }

        /**
         * item 레이아웃 view를 holder에 붙임.
         * @param viewGroup
         * @param viewType
         * @return
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        /**
         * listview 의 getview 와 비슷 = 넘겨 받은 데이터를 화면에 출력하는 역할
         * item 과 viewholder 를 bind
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.tv_item.setText(itemList.get(position).title);
            holder.img_item.setImageResource(itemList.get(position).number);
            setAnimation(holder.itemView, position);

            holder.btn_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // 지도보여주기 구현

                    Toast.makeText(getActivity(), (position+1)+"번쨰 클릭!", Toast.LENGTH_SHORT).show();
//                    CourseMapFragment map = CourseMapFragment.newInstance();
//                    PublicDefine.courseMapFragment = map;
//                    PublicDefine.courseMapFragment.setCourse(position);
//                    Intent intent = new Intent(this, this.be);
//                    map.startActivity();

                    SharedPreferences.Editor editor = shared_position.edit();
                    editor.putInt("position_course",position+1);
                    editor.commit();

                    Intent intent = new Intent(getActivity(), NaverMap.class);
                    startActivity(intent);

//                    String url = "https://m.map.naver.com";
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        /**
         * view 의 재활용을 위한 viewHolder
         */
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView tv_item;
            Button btn_item;
            ImageView img_item;

            public ViewHolder (View itemView) {

                super(itemView);
                tv_item = (TextView) itemView.findViewById(R.id.tv_item);
                btn_item = (Button) itemView.findViewById(R.id.btn_item);
                img_item = (ImageView) itemView.findViewById(R.id.img_item);
                itemView.setOnClickListener(this);
            }

            //itemview 클릭시 발생하는 clickEvent
            @Override
            public void onClick(View v) { //custom dialog 띄워주기 구현

                Toast.makeText(getActivity(), tv_item.getText().toString(), Toast.LENGTH_SHORT).show();

            }

        } // end of ViewHolder

        private void setAnimation(View viewToAnimate, int position)
        {
            // 새로 보여지는 뷰라면 애니메이션을 해줍니다
            if (position > lastPosition)
            {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }



        //예제1방식

//        initLayout();
//        initData();
    } // OnCreate~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 //end of MyAdapter~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


}
