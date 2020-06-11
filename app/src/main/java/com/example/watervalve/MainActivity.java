package com.example.watervalve;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    //未选中的Tab图片
    private int[] unSelectTabRes = new int[]{R.drawable.zhuangtai
            ,  R.drawable.shezhi};
    //选中的Tab图片
    private int[] selectTabRes = new int[]{R.drawable.zhuangtai_press,  R.drawable.shezhi_press_press
    };
    //Tab标题
    private String[] title = new String[]{"状态参数", "温度设置"};
    private ViewPager viewPager;
    private TabLayout tabLayout;
    //定时器用于轮训订阅主题
    private Timer timerSubscribeTopic = null;
    private TimerTask TimerTaskSubscribeTopic = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
        MyMqttClient.sharedCenter().setConnect();
        MyMqttClient.sharedCenter().setOnServerReadStringCallback(new MyMqttClient.OnServerReadStringCallback() {
            @Override//Topic:主题  Msg.toString():接收的消息  MsgByte:16进制消息
            public void callback(String Topic, final MqttMessage Msg, byte[] MsgByte) {
                Log.e("主活动收到新数据："," Msg"+Msg.toString() );
                //将数据存储到SharedPreferences中
                prefs=getSharedPreferences("datastore",0);
                SharedPreferences.Editor edit=prefs.edit();
                edit.putString("data",Msg.toString());
                edit.commit();
            }
        });

        /**
         * 订阅主题成功回调
         */
        MyMqttClient.sharedCenter().setOnServerSubscribeCallback(new MyMqttClient.OnServerSubscribeSuccessCallback() {
            @Override
            public void callback(String Topic, int qos) {
                //if (Topic.equals("/sys/a1S917F388O/shebei/thing/service/property/set")){//订阅1111成功
                if (Topic.equals("/a1yPGkxyv1q/Fjg1/user/get")){//订阅1111成功
                    stopTimerSubscribeTopic();//订阅到主题,停止订阅
                }
            }
        });
        startTimerSubscribeTopic();//定时订阅主题
        //MyMqttClient.sharedCenter().setUnSubscribe("/sys/a1S917F388O/shebei/thing/service/property/set");//取消订阅消息
        MyMqttClient.sharedCenter().setUnSubscribe("/a1yPGkxyv1q/Fjg1/user/get");//取消订阅消息
    }
    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewpager_content_view);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_view);

        //使用适配器将ViewPager与Fragment绑定在一起
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        //将TabLayout与ViewPager绑定
        tabLayout.setupWithViewPager(viewPager);

      /*  //设置方式一：
        //获取底部的单个Tab
        tabAtOne = tabLayout.getTabAt(0);
        tabAttwo = tabLayout.getTabAt(1);
        tabAtthree = tabLayout.getTabAt(2);
        tabAtfour = tabLayout.getTabAt(3);

        //设置Tab图片
        tabAtOne.setIcon(R.drawable.i8live_menu_home_press);
        tabAttwo.setIcon(R.drawable.i8live_menu_information_normal);
        tabAtthree.setIcon(R.drawable.i8live_menu_game_normal);
        tabAtfour.setIcon(R.drawable.i8live_menu_personl_normal);*/

        //设置方式二：
        for (int i = 0; i < title.length; i++) {
            if (i == 0) {
                tabLayout.getTabAt(0).setIcon(selectTabRes[0]);
            } else {
                tabLayout.getTabAt(i).setIcon(unSelectTabRes[i]);
            }
        }

    }

    private void initData() {

    }

    private void initListener() {
        //TabLayout切换时导航栏图片处理
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {//选中图片操作

                for (int i = 0; i < title.length; i++) {
                    if (tab == tabLayout.getTabAt(i)) {
                        tabLayout.getTabAt(i).setIcon(selectTabRes[i]);
                        viewPager.setCurrentItem(i);
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {//未选中图片操作

                for (int i = 0; i < title.length; i++) {
                    if (tab == tabLayout.getTabAt(i)) {
                        tabLayout.getTabAt(i).setIcon(unSelectTabRes[i]);
                    }
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    //自定义适配器
    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return new SecondFragment();//设置
            } else {
                return new FristFragment();//首页
            }
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
    /**
     * 定时器每隔1S尝试订阅主题
     */
    private void startTimerSubscribeTopic(){
        if (timerSubscribeTopic == null) {
            timerSubscribeTopic = new Timer();
        }
        if (TimerTaskSubscribeTopic == null) {
            TimerTaskSubscribeTopic = new TimerTask() {
                @Override
                public void run() {
                    MyMqttClient.sharedCenter().setSubscribe("/a1yPGkxyv1q/Fjg1/user/get",0);//订阅主题1111,消息等级0
                    //MyMqttClient.sharedCenter().setSubscribe("/sys/a1S917F388O/shebei/thing/service/property/set",0);//订阅主题1111,消息等级0
                }
            };
        }
        if(timerSubscribeTopic != null && TimerTaskSubscribeTopic != null )
            timerSubscribeTopic.schedule(TimerTaskSubscribeTopic, 0, 1000);
    }

    private void stopTimerSubscribeTopic(){
        if (timerSubscribeTopic != null) {
            timerSubscribeTopic.cancel();
            timerSubscribeTopic = null;
        }
        if (TimerTaskSubscribeTopic != null) {
            TimerTaskSubscribeTopic.cancel();
            TimerTaskSubscribeTopic = null;
        }
    }

    //当活动不再可见时调用
    @Override
    protected void onStop()
    {
        super.onStop();
        stopTimerSubscribeTopic();//停止定时器订阅
    }

    /**
     * 当处于停止状态的活动需要再次展现给用户的时候，触发该方法
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        startTimerSubscribeTopic();//定时订阅主题
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimerSubscribeTopic();
    }
}
