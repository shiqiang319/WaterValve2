package com.example.watervalve;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.watervalve.Login.ScanMessage;

import org.json.JSONArray;
import org.litepal.LitePal;

import static com.example.watervalve.Utility.SetCommandJson;

public class SecondFragment extends Fragment {
    private SharedPreferences prefs;
    private EditText wenduxiaxian;
    private EditText wendushangxian;
    private Button   shezhi;
    private String   topic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_second, null);
        wenduxiaxian=view.findViewById(R.id.wenduxiaxian);
        wendushangxian=view.findViewById(R.id.wendushangxian);
        shezhi=view.findViewById(R.id.shezhi);
        ShezhiBtnListener(shezhi);

        ScanMessage lastmessage= LitePal.findLast(ScanMessage.class);
        topic=lastmessage.getTopic().trim();
        return view;
    }
    //按钮点击事件
    private void ShezhiBtnListener(final Button btn){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray jsonArray=new JSONArray();
                if (TextUtils.isEmpty(wenduxiaxian.getText())||TextUtils.isEmpty(wendushangxian.getText())){
                    Toast.makeText(getActivity(), "有参数未设置，参数配置失败！" , Toast.LENGTH_SHORT).show();
                    return;
                }
                Integer P1=Integer.parseInt(wenduxiaxian.getText().toString().trim());
                Integer P2=Integer.parseInt(wendushangxian.getText().toString().trim());
                jsonArray.put(P1);
                jsonArray.put(P2);
                MyMqttClient.sharedCenter().setSendData(
                        //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                        //"/a1yPGkxyv1q/SimuApp/user/update",
                        topic,
                        SetCommandJson(1,111,jsonArray),
                        0,
                        false);
                Log.e("自动Button","发送数据："+SetCommandJson(1,111,jsonArray));
            }
        });
    }
}
