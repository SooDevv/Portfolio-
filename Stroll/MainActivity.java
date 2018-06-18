package com.test.stroll;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    ViewPager vp;

    TabHost mTab;
    pagerAdapter mpageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mpageAdapter = new pagerAdapter(getSupportFragmentManager());
        vp = (ViewPager) findViewById(R.id.vp);

        vp = (ViewPager)findViewById(R.id.vp);
        Button btn_1 = (Button)findViewById(R.id.btn_1);
        Button btn_2 = (Button)findViewById(R.id.btn_2);
        Button btn_3 = (Button)findViewById(R.id.btn_3);
        Button btn_4 = (Button)findViewById(R.id.btn_4);
        Button btn_5 = (Button) findViewById(R.id.btn_5);



        vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        vp.setCurrentItem(0);

        btn_1.setOnClickListener(movePageListener);
        btn_1.setTag(0);
        btn_2.setOnClickListener(movePageListener);
        btn_2.setTag(1);
        btn_3.setOnClickListener(movePageListener);
        btn_3.setTag(2);
        btn_4.setOnClickListener(movePageListener);
        btn_4.setTag(3);
        btn_5.setOnClickListener(movePageListener);
        btn_5.setTag(4);

    }

    View.OnClickListener movePageListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int tag = (int) v.getTag();
            vp.setCurrentItem(tag);
        }
    };


    public class pagerAdapter extends FragmentStatePagerAdapter
    {
        public pagerAdapter(android.support.v4.app.FragmentManager fm)
        {
            super(fm);
        }
        @Override
        public android.support.v4.app.Fragment getItem(int position)
        {
            switch(position)
            {
                case 0:
                    return new Tab_allLoadInfo();
                case 1:
                    return new Tab_first();
                case 2:
                    return new Tab_gallery();
                case 3:
                    return new Tab_third();
                case 4:
                    return new Tab_chat();
                default:
                    return null;
            }
        }
        @Override
        public int getCount()
        {
            return 5;
        }
/*
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }*/
    }

/*    @Override
    protected void onResume() {
        super.onResume();
        mpageAdapter.notifyDataSetChanged();
    }*/
}

       /* mTab = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        //Tab_allLoadInfo
        intent = new Intent(this, Tab_allLoadInfo.class);
        spec = mTab.newTabSpec("Tab_allLoadInfo")
                .setIndicator("", getResources().getDrawable(R.drawable.search_load))
                .setContent(intent);
        mTab.addTab(spec);

        //첫번째 탭 (1)
        intent = new Intent(this, Tab_first.class);
        spec = mTab.newTabSpec("Tab_first")
                .setIndicator("", getResources().getDrawable(R.drawable.placeholder))
                .setContent(intent);
        mTab.addTab(spec);

        //두번쨰 탭 (2)
        intent = new Intent(this, Tab_second.class);
        spec = mTab.newTabSpec("Tab_seconds")
                .setIndicator("",getResources().getDrawable(R.drawable.walktogether))
                .setContent(intent);
        mTab.addTab(spec);

        //세번째 탭(3)
        intent = new Intent(this, Tab_third.class);
        spec = mTab.newTabSpec("Tab_third")
                .setIndicator("",getResources().getDrawable(R.drawable.person))
                .setContent(intent);
        mTab.addTab(spec);*/



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }

