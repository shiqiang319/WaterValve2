package com.example.watervalve;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.Button;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Utility {
    public static Data handleDataResponse(String response){
        try{
            JSONObject jsonObject=new JSONObject(response);
            return new Gson().fromJson(String.valueOf(jsonObject),Data.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static  void requestData(){
        MyMqttClient.sharedCenter().setSendData(
                //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                "/a1yPGkxyv1q/SimuApp/user/update",
                "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[1]},\"version\":\"1.0.0\"}",
                0,
                false);
    }
    public static  void szrequestData(){
        MyMqttClient.sharedCenter().setSendData(
                //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                "/a1yPGkxyv1q/SimuApp/user/update",
                "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":43,\"Para\":[1]},\"version\":\"1.0.0\"}",
                0,
                false);
    }
    public static  void xtrequestData(){
        MyMqttClient.sharedCenter().setSendData(
                //"/sys/a1S917F388O/wenxin/thing/event/property/post",
                "/a1yPGkxyv1q/SimuApp/user/update",
                "{\"method\":\"thing.event.property.post\",\"id\":\"1111\",\"params\":{\"Id\":1,\"Cmd\":112,\"Para\":[1]},\"version\":\"1.0.0\"}",
                0,
                false);
    }
    public static void BtnShow(final Button btn, final String BtnMsg, final String color){
        btn.setText(BtnMsg);
        btn.setTextColor(Color.parseColor(color));
    }
    //改变shape背景色
    public static void BtnBgShowColor(final Button btn, final String color){
        GradientDrawable gd = (GradientDrawable) btn.getBackground();
        gd.setColor(Color.parseColor(color));
    }
    //构建指令报文
    public static String CommandJson(Integer id,int cmd,int para){
        //创建JSON
        JSONObject jsonObject = new JSONObject();
        JSONObject object_1 = new JSONObject();
        JSONArray jsonArray=new JSONArray();
        try {
            jsonArray.put(para);
            object_1.put("Para",jsonArray);
            object_1.put("Id",id);
            object_1.put("Cmd",cmd);
            jsonObject.put("method", "thing.event.property.post");
            jsonObject.put("params",  object_1);
            jsonObject.put("version", "1.0.0");
        } catch (JSONException e){
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
    //设置/系统界面构建指令报文
    public static String SetCommandJson(Integer id,Integer cmd,JSONArray jsonArray){
        //创建JSON
        JSONObject jsonObject = new JSONObject();
        JSONObject object_1 = new JSONObject();
        try {
            object_1.put("Para",jsonArray);
            object_1.put("Id",id);
            object_1.put("Cmd",cmd);
            jsonObject.put("method", "thing.event.property.post");
            jsonObject.put("params",  object_1);
            jsonObject.put("version", "1.0.0");
        } catch (JSONException e){
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
}
