package com.example.watervalve.Login;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.dommy.qrcode.util.Constant;
import com.example.watervalve.MainActivity;
import com.example.watervalve.MyMqttClient;
import com.example.watervalve.R;
import com.google.zxing.activity.CaptureActivity;

import org.litepal.LitePal;

public class Login extends Activity implements View.OnClickListener{                 //登录界面活动

    public int pwdresetFlag=0;
    private EditText mAccount;                        //用户名编辑
    private EditText mPwd;                            //密码编辑
    private Button mRegisterButton;                   //注册按钮
    private Button mLoginButton;                      //登录按钮
    private Button mCancleButton;                     //注销按钮
    private CheckBox mRememberCheck;
    private Button btnQrCode;                         // 扫码

    private SharedPreferences login_sp;
    private String userNameValue,passwordValue;

    private View loginView;                           //登录
    private View loginSuccessView;
    private TextView loginSuccessShow;
    private TextView mChangepwdText;
    private UserDataManager mUserDataManager;         //用户数据管理类


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //通过id找到相应的控件
        mAccount = (EditText) findViewById(R.id.login_edit_account);
        mPwd = (EditText) findViewById(R.id.login_edit_pwd);
        mRegisterButton = (Button) findViewById(R.id.login_btn_register);
        mLoginButton = (Button) findViewById(R.id.login_btn_login);
        mCancleButton = (Button) findViewById(R.id.login_btn_cancle);
        loginView=findViewById(R.id.login_view);
        loginSuccessView=findViewById(R.id.login_success_view);
        loginSuccessShow=(TextView) findViewById(R.id.login_success_show);

        mChangepwdText = (TextView) findViewById(R.id.login_text_change_pwd);

        mRememberCheck = (CheckBox) findViewById(R.id.Login_Remember);

        login_sp = getSharedPreferences("userInfo", 0);
        String name=login_sp.getString("USER_NAME", "");
        String pwd =login_sp.getString("PASSWORD", "");
        boolean choseRemember =login_sp.getBoolean("mRememberCheck", false);
        boolean choseAutoLogin =login_sp.getBoolean("mAutologinCheck", false);
        //如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
        if(choseRemember){
            mAccount.setText(name);
            mPwd.setText(pwd);
            mRememberCheck.setChecked(true);
        }

        mRegisterButton.setOnClickListener(mListener);                      //采用OnClickListener方法设置不同按钮按下之后的监听事件
        mLoginButton.setOnClickListener(mListener);
        mCancleButton.setOnClickListener(mListener);
        mChangepwdText.setOnClickListener(mListener);

        ImageView image = (ImageView) findViewById(R.id.logo);             //使用ImageView显示logo
        image.setImageResource(R.drawable.logo);

        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();                              //建立本地数据库
        }
        initView();
    }
    //扫码
    private void initView() {
        btnQrCode = (Button) findViewById(R.id.btn_qrcode);
        btnQrCode.setOnClickListener(this);

        // tvResult = (TextView) findViewById(R.id.txt_result);
    }
    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(Login.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_qrcode:
                startQrCode();
                break;
        }
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setMessage("注册此设备信息？");   //设置对话框的内容
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //扫描结果回调
                if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String[] scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN).split("\\*");
                    String userName = scanResult[0].trim();
                    String userPwd = scanResult[1].trim();
                    Log.e("账户",scanResult[0]);
                    Log.e("账户密码",scanResult[1]);
                    //检查用户是否存在
                    int count=mUserDataManager.findUserByName(userName);
                    //用户已经存在时返回，给出提示文字
                    if(count>0){
                        Toast.makeText(Login.this, getString(R.string.name_already_exist, userName),Toast.LENGTH_SHORT).show();
                        return ;
                    } else {
                        UserData mUser = new UserData(userName, userPwd);
                        mUserDataManager.openDataBase();
                        long flag = mUserDataManager.insertUserData(mUser); //新建用户信息
                        //数据库存入扫码信息
                        ScanMessage scanMessage=new ScanMessage();
                        scanMessage.setClientid(scanResult[2]);
                        scanMessage.setMqqtuser(scanResult[3]);
                        scanMessage.setMqqtpwd(scanResult[4]);
                        scanMessage.setMqqttip(scanResult[5]);
                        scanMessage.setTopic(scanResult[6]);
                        scanMessage.setNum(scanResult[7]);
                        scanMessage.save();
                        Log.e("数据库存入","ClientId"+scanResult[2]);
                        Log.e("数据库存入","user"+scanResult[3]);
                        Log.e("数据库存入","Pwd"+scanResult[4]);
                        Log.e("数据库存入","Ip"+scanResult[5]);
                        Log.e("数据库存入","Topic"+scanResult[6]);
                        Log.e("spinner","num"+scanResult[7]);
                        if (flag == -1) {
                            Toast.makeText(Login.this, getString(R.string.register_fail),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(Login.this, getString(R.string.register_success),Toast.LENGTH_SHORT).show();
//                            Intent intent_Register_to_Login = new Intent(Register.this,Login.class) ;    //切换User Activity至Login Activity
//                            startActivity(intent_Register_to_Login);
//                            finish();
                            mAccount.setText(userName);
                            mPwd.setText(userPwd);
                            //取出数据库信息
                            ScanMessage lastmessage=LitePal.findLast(ScanMessage.class);
                            MyMqttClient.sharedCenter().setClientId(lastmessage.getClientid());
                            MyMqttClient.sharedCenter().setMqttUserString(lastmessage.getMqqtuser());
                            MyMqttClient.sharedCenter().setMqttPwdString(lastmessage.getMqqtpwd());
                            MyMqttClient.sharedCenter().setMqttIPString(lastmessage.getMqqttip());
                            // MainActivity.setTopic(lasttmessage.getTopic());

                            MyMqttClient.sharedCenter().setConnect();
                        }
                    }
                }

            }

        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {//设置取消按钮
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(Login.this, "您已取消添加！", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog b = builder.create();
        b.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(Login.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // 文件读写权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(Login.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    OnClickListener mListener = new OnClickListener() {                  //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_btn_register:                            //登录界面的注册按钮
                    Intent intent_Login_to_Register = new Intent(Login.this,Register.class) ;    //切换Login Activity至User Activity
                    startActivity(intent_Login_to_Register);
                    finish();
                    break;
                case R.id.login_btn_login:                              //登录界面的登录按钮
                    login();
                    break;
                case R.id.login_btn_cancle:                             //登录界面的注销按钮
                    cancel();
                    break;
                case R.id.login_text_change_pwd:                             //登录界面的注销按钮
                    Intent intent_Login_to_reset = new Intent(Login.this,Resetpwd.class) ;    //切换Login Activity至User Activity
                    startActivity(intent_Login_to_reset);
                    finish();
                    break;
            }
        }
    };

    public void login() {                                              //登录按钮监听事件
        if (isUserNameAndPwdValid()) {
            String userName = mAccount.getText().toString().trim();    //获取当前输入的用户名和密码信息
            String userPwd = mPwd.getText().toString().trim();
            SharedPreferences.Editor editor =login_sp.edit();
            int result=mUserDataManager.findUserByNameAndPwd(userName, userPwd);
            if(result==1){                                             //返回1说明用户名和密码均正确
                //保存用户名和密码
                editor.putString("USER_NAME", userName);
                editor.putString("PASSWORD", userPwd);

                //是否记住密码
                if(mRememberCheck.isChecked()){
                    editor.putBoolean("mRememberCheck", true);
                }else{
                    editor.putBoolean("mRememberCheck", false);
                }
                editor.commit();
                //取出数据库信息
//                ScanMessage lastmessage=LitePal.findLast(ScanMessage.class);
//                MyMqttClient.sharedCenter().setClientId(lastmessage.getClientid());
//                MyMqttClient.sharedCenter().setMqttUserString(lastmessage.getMqqtuser());
//                MyMqttClient.sharedCenter().setMqttPwdString(lastmessage.getMqqtpwd());
//                MyMqttClient.sharedCenter().setMqttIPString(lastmessage.getMqqttip());
//                // MainActivity.setTopic(lasttmessage.getTopic());
//
//                MyMqttClient.sharedCenter().setConnect();

                Intent intent = new Intent(Login.this, MainActivity.class) ;    //切换Login Activity至User Activity
                startActivity(intent);
                finish();
               // Toast.makeText(this, getString(R.string.login_success),Toast.LENGTH_SHORT).show();//登录成功提示
            }else if(result==0){
                Toast.makeText(this, getString(R.string.login_fail),Toast.LENGTH_SHORT).show();  //登录失败提示
            }
        }
    }
    public void cancel() {           //注销
        if (isUserNameAndPwdValid()) {
            //删除数据库所有信息
            LitePal.deleteAll(ScanMessage.class);
            String userName = mAccount.getText().toString().trim();    //获取当前输入的用户名和密码信息
            String userPwd = mPwd.getText().toString().trim();
            int result=mUserDataManager.findUserByNameAndPwd(userName, userPwd);
            if(result==1){                                             //返回1说明用户名和密码均正确
//                Intent intent = new Intent(Login.this,User.class) ;    //切换Login Activity至User Activity
//                startActivity(intent);
                Toast.makeText(this, getString(R.string.cancel_success),Toast.LENGTH_SHORT).show();//登录成功提示
                mPwd.setText("");
                mAccount.setText("");
                mUserDataManager.deleteUserDatabyname(userName);
            }else if(result==0){
                Toast.makeText(this, getString(R.string.cancel_fail),Toast.LENGTH_SHORT).show();  //登录失败提示
            }
        }

    }

    public boolean isUserNameAndPwdValid() {
        if (mAccount.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.account_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (mPwd.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.pwd_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mUserDataManager != null) {
            mUserDataManager.closeDataBase();
            mUserDataManager = null;
        }
        super.onPause();
    }

}