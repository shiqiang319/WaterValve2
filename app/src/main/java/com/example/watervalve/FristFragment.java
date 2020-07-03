package com.example.watervalve;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.watervalve.Login.ScanMessage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static com.example.watervalve.Utility.BtnShow;
import static com.example.watervalve.Utility.SetCommandJson;

public class FristFragment extends Fragment {
    @Override
    public void onStart(){
        super.onStart();
        Log.e("开始","onStart");
        handler.postDelayed(runnable,1000);
    }
    @Override
    public void onPause(){
        super.onPause();
        Log.e("停止","onPause");
        handler.removeCallbacks(runnable);
    }
    private SwipeRefreshLayout swipeRefresh;
    private SharedPreferences prefs;
    private Data newdata;
    private Spinner spinner;
    private TextView fahoukaidu;
    private TextView shebeiwendu;
    private Button  jia;
    private Button  jian;
    private Button  zidong;
    private EditText fakouxiaxian;
    private EditText fakoushangxian;
    private EditText kaiguanxianshi;
    private static final int UPDATE_TEXT=1;
    private  Boolean fag;
    private static Context mContext =null;

    private List<CharSequence> eduList = null;
    private ArrayAdapter<CharSequence> eduAdapter = null;
    private int num;
    private String topic;

    public static void main(String[] args) {

    }

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frist, null);
        mContext=getActivity();
        swipeRefresh=view.findViewById(R.id.swipe_refresh);
        spinner=view.findViewById(R.id.Spi);
        fahoukaidu=view.findViewById(R.id.fakoukaidu);
        shebeiwendu=view.findViewById(R.id.shebeiwendu);
        jia=view.findViewById(R.id.kdjia);
        jian=view.findViewById(R.id.kdjian);
        zidong=view.findViewById(R.id.zidong);
        fakouxiaxian=view.findViewById(R.id.fakouxiaxian);
        fakoushangxian=view.findViewById(R.id.fakoushangxian);
        kaiguanxianshi=view.findViewById(R.id.kaiguanxianshi);

        ScanMessage lastmessage= LitePal.findLast(ScanMessage.class);
        topic=lastmessage.getTopic().trim();
        //设置spinner
        eduList = new ArrayList<CharSequence>();
        num=Integer.valueOf(lastmessage.getNum().trim());
        for (int i=0;i<=num;i++){
            eduList.add(String.valueOf(i));
        }
        eduAdapter = new ArrayAdapter<CharSequence>(this.getActivity(),android.R.layout.simple_spinner_item,eduList);
        eduAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(eduAdapter);

        //下拉刷新
        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
              // Utility.requestData();
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        //"/a1yPGkxyv1q/SimuApp/user/update",
                        topic,
                        "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[1]},\"version\":\"1.0.0\"}",
                        0,
                        false);
               Log.e("下拉刷新","已发送查询指令");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //从SharedPreferences读取数据
                        prefs=getActivity().getSharedPreferences("datastore",0);
                        String dataString=prefs.getString("data","");
                        if (dataString !=""){
                            Log.e("SharedPreferences数据读取",dataString);
                            newdata=Utility.handleDataResponse(dataString);
                            showDataInfo(newdata);
                            prefs.edit().clear().commit();//清除SharedPreferences数据
                        }else {
                            Toast.makeText(getActivity(), "获取数据失败，请检查设备是否上线！", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                },500);
            }
        });

        SpiListener(spinner);
        BtnListener(jia,1);
        BtnListener(jian,2);
        ZidongBtnListener(zidong);
        return view;
    }
    //展示Data实体类中的数据
    private void showDataInfo(Data newdata){
        spinner.setSelection(newdata.Id);
        //刷新TextView
        shebeiwendu.setText(newdata.Para.get(7).toString());
        fahoukaidu.setText(newdata.Para.get(5).toString());
        //刷新Button
        if ((newdata.Para.get(4) & 1)==1){
            BtnShow(jia,"停止+","#FF0000");


        }else {
            BtnShow(jia,"开度+","#00AA44");
        }

        if ((newdata.Para.get(4) & 2)==2){
            BtnShow(jian,"停止-","#FF0000");


        }else {
            BtnShow(jian,"开度—","#00AA44");
        }
        if ((newdata.Para.get(0) & 256)==256){
            BtnShow(zidong,"停止","#FF0000");


        }else {
            BtnShow(zidong,"自动","#00AA44");
        }

    }
    //Spinner点击事件
    private void SpiListener(final  Spinner spi){
        spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String cardNumber = getActivity().getResources().getStringArray(R.array.ctype)[position];
                if (cardNumber.equals("0")) {
                    return;
                }
                //Toast.makeText(getActivity(), "你正在操作阀门：" + cardNumber, Toast.LENGTH_SHORT).show();
                String inputx= spinner.getSelectedItem().toString();
                Log.e("设置阀门：",inputx);
                Integer x=Integer.parseInt(inputx);
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        //"/a1yPGkxyv1q/SimuApp/user/update",
                        topic,
                        Utility.CommandJson(x,112,1),
                        0,
                        false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //从SharedPreferences读取数据
                        prefs=getActivity().getSharedPreferences("datastore",0);
                        String dataString=prefs.getString("data","");
                        if (dataString!=""){
                            Log.e("（Spinner）返回数据读取",dataString);
                            newdata=Utility.handleDataResponse(dataString);
                            showDataInfo(newdata);//刷新界面
                            prefs.edit().clear().commit();//清除SharedPreferences数据
                        }else {
                            Toast.makeText(getActivity(), "获取数据失败，请检查设备是否上线！", Toast.LENGTH_SHORT).show();
                        }
                    }
                },500);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    //按钮点击事件
    private void BtnListener(final Button btn,final int para){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // timer.schedule(task,0,5000);
                String inputx= spinner.getSelectedItem().toString();
                Integer x=Integer.parseInt(inputx);
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        //"/a1yPGkxyv1q/SimuApp/user/update",
                        topic,
                        Utility.CommandJson(x,67,para),
                        0,
                        false);
                Log.e("Btn","开度已发送指令"+ Utility.CommandJson(x,67,para));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //从SharedPreferences读取数据
                        prefs=getActivity().getSharedPreferences("datastore",0);
                        String dataString=prefs.getString("data","");
                        if (dataString!=""){
                            Log.e("开度按钮数据读取",dataString);
                            newdata=Utility.handleDataResponse(dataString);
                            showDataInfo(newdata);//刷新界面
                            prefs.edit().clear().commit();//清除SharedPreferences数据
                        }else {
                          //  Toast.makeText(getActivity(), "获取数据失败，请先刷新界面或检查设备是否上线！", Toast.LENGTH_SHORT).show();
                        }
                    }
                },500);
            }
        });
    }
    //按钮点击事件
    private void ZidongBtnListener(final Button btn){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputx= spinner.getSelectedItem().toString();
                Integer x=Integer.parseInt(inputx);
                int P1;
                JSONArray jsonArray=new JSONArray();
                if (TextUtils.isEmpty(fakouxiaxian.getText())||TextUtils.isEmpty(fakoushangxian.getText())||TextUtils.isEmpty(kaiguanxianshi.getText())){
                    Toast.makeText(getActivity(), "有参数未设置，参数配置失败！" , Toast.LENGTH_SHORT).show();
                    return;
                }
                Integer P2=Integer.parseInt(fakouxiaxian.getText().toString().trim());
                Integer P3=Integer.parseInt(fakoushangxian.getText().toString().trim());
                Integer P4=Integer.parseInt(kaiguanxianshi.getText().toString().trim());
                if (zidong.getText().toString().trim().equals("自动")){
                    P1=17;
                }else P1=16;
                jsonArray.put(P1);
                jsonArray.put(P2);
                jsonArray.put(P3);
                jsonArray.put(P4);
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        //"/a1yPGkxyv1q/SimuApp/user/update",
                        topic,
                        SetCommandJson(x,97,jsonArray),
                        0,
                        false);
                Log.e("自动Button","发送数据："+SetCommandJson(x,97,jsonArray));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //从SharedPreferences读取数据
                        prefs=getActivity().getSharedPreferences("datastore",0);
                        String dataString=prefs.getString("data","");
                        if (dataString!=""){
                            Log.e("自动按钮数据读取",dataString);
                            newdata=Utility.handleDataResponse(dataString);
                            showDataInfo(newdata);//刷新界面
                            prefs.edit().clear().commit();//清除SharedPreferences数据
                        }else {
                            Toast.makeText(getActivity(), "获取数据失败，请检查设备是否上线！", Toast.LENGTH_SHORT).show();
                        }

                    }
                },500);
            }
        });
    }

    public static Handler handler0=new Handler(){
        public  void handleMessage(Message msg){
            switch(msg.what){
                case UPDATE_TEXT:
                    Toast.makeText(mContext,"已连接到服务器！",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private Handler handler1=new Handler(){
        public  void handleMessage(Message msg){
            switch(msg.what){
                case UPDATE_TEXT:
                    showDataInfo(newdata);//刷新界面
                    break;
                    default:
                        break;
            }
        }
    };
       final Handler handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
              //  Utility.requestData();
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        //"/a1yPGkxyv1q/SimuApp/user/update",
                        topic,
                        "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[1]},\"version\":\"1.0.0\"}",
                        0,
                        false);
                Log.e("刷新","已发送查询指令");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //从SharedPreferences读取数据
                        prefs=getActivity().getSharedPreferences("datastore",0);
                        String dataString=prefs.getString("data","");
                        if (dataString!=""){
                            Log.e("刷新数据读取",dataString);
                            newdata=Utility.handleDataResponse(dataString);
                            Message message=new Message();
                            message.what=UPDATE_TEXT;
                            handler1.sendMessage(message);
                            prefs.edit().clear().commit();//清除SharedPreferences数据
                        }else {
                           // Toast.makeText(getActivity(), "获取数据失败，请检查设备是否上线！", Toast.LENGTH_SHORT).show();
                        }
                    }
                },500);
                handler.postDelayed(this,1000);
            }
        };

}