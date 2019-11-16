package cn.mmvtc.mmvtc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.ant.liao.GifView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import cn.mmvtc.mmvtc.utils.HttpUtils;
import okhttp3.Call;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_user, et_password, et_vertify;
    private CheckBox isSave;
    private ImageView iv_vertify;
    private Button btn_login;
    private Button tv_clear;
    private SharedPreferences sharedPreferences;
    private LoginInfo loginInfo = new LoginInfo();
    private String switchVertifyUrl = "http://jwc.mmvtc.cn/CheckCode.aspx";
    private String LoginUrl = "http://jwc.mmvtc.cn/default2.aspx";
    private String name = "";
    private String password = "";
    private String vertify = "";
    private GifView bg;
    private String studentName = "";
    private String infoUrl = "";
    private String scoreUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();//初始化控件
        last_read();//上次是不是保存密码
        getVertify();
    }

    private void loginFail(String tip) {
        Toast.makeText(LoginActivity.this, tip, Toast.LENGTH_LONG).show();
        et_vertify.setText("");
        refreshVertify();
    }


    //初始化控件
    private void initView() {
        /*bg= (GifView) findViewById(R.id.bg);
        bg.setGifImage(R.drawable.bg1);

        bg.setGifImageType(GifView.GifImageType.SYNC_DECODER);*/
        et_user = (EditText) findViewById(R.id.et_user);
        et_password = (EditText) findViewById(R.id.et_password);
        et_vertify = (EditText) findViewById(R.id.et_vertify);
        isSave = (CheckBox) findViewById(R.id.cb_isSave);
        iv_vertify = (ImageView) findViewById(R.id.iv_vertify);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_clear = (Button) findViewById(R.id.clear_log);
        tv_clear.setOnClickListener(this);
        iv_vertify.setOnClickListener(this);
        btn_login.setOnClickListener(this);
    }

    /**
     * 上次登录是否有记住密码
     */
    private void last_read() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String stuUser = sp.getString("user", null);
        String stuPassword = sp.getString("password", null);
        if (!(TextUtils.isEmpty(stuUser) && TextUtils.isEmpty(stuPassword))) {
            et_user.setText(stuUser);
            et_password.setText(stuPassword);
            isSave.setChecked(true);
        }
    }

    //保存密码
    private void saveUser(String user, String password) {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("user", user);
        editor.putString("password", password);
        editor.commit();
    }

    //清除记录
    private void clear_log() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
        et_user.setText("");
        et_password.setText("");
        Toast.makeText(this, "清除成功", Toast.LENGTH_SHORT).show();
    }

    //按钮点击操作
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_vertify:
                refreshVertify();
                break;
            case R.id.btn_login:
                exLogin();
                break;
            case R.id.clear_log:
                this.clear_log();
                break;
        }
    }


    /**
     * 登录
     */
    private void exLogin() {
        name = et_user.getText().toString().trim();
        password = et_password.getText().toString().trim();
        vertify = et_vertify.getText().toString().trim();
        if (name.equals("")) {
            Toast.makeText(this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.equals("")) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (vertify.length() != 4) {
            Toast.makeText(this, "请输入4位验证码", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "登陆中,请稍后片刻...", Toast.LENGTH_SHORT).show();
        login();
    }

    //登录请求
    private void login() {
        OkHttpUtils
                .post()
                .url(LoginUrl)
                .addParams("__VIEWSTATE", "dDw3OTkxMjIwNTU7Oz5qFv56B08dbR82AMSOW+P8WDKexA==")
                .addParams("TextBox1", name)
                .addParams("TextBox2", password)
                .addParams("TextBox3", vertify)
                .addParams("RadioButtonList1", "学生")
                .addParams("Button1", "")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.i("TAG", "onError: " + e);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        checkLogin(response);// 判断登陆结果
                    }
                });
    }

    // 登陆结果
    private void checkLogin(String content) {
        if (content.indexOf("验证码不正确") != -1) {
            refreshVertify();
        } else if (content.indexOf("密码错误") != -1) {
            refreshVertify();
        } else if (content.indexOf("用户名不存在或未按照要求参加教学活动") != -1) {
            refreshVertify();
        } else if (content.indexOf("欢迎你") == -1) {
            Toast.makeText(this, "登陆成功", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("isLogin", true);
                    intent.putExtra("name", name);
                    intent.putExtra("password", password);
                    intent.putExtra("studentName", studentName);
                    intent.putExtra("infoUrl", infoUrl);
                    intent.putExtra("scoreUrl", scoreUrl);
                    startActivity(intent);
                    finish();
                }
            }, 1100);
            Document html = Jsoup.parse(content);
            Elements top = html.select("#headDiv .nav > li.top");
            studentName = html.select("span#xhxm").text();
            studentName = studentName.substring(0, studentName.length() - 2);
            String info = top.get(2).select(".sub > li:first-child a").attr("href");
            String course = top.get(3).select(".sub > li:first-child a").attr("href");
            String xgPsw = top.get(2).select(".sub > li:nth-child(2) a").attr("href");
            String cj = top.get(3).select(".sub > li:nth-child(4) a").attr("href");
            String encodeName = null;
            try {
                encodeName = URLEncoder.encode(studentName, "gb2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            infoUrl = info.replaceAll(studentName, encodeName);
            scoreUrl = cj.replaceAll(studentName, encodeName);
            if (isSave.isChecked()) {// 是否保存账号密码
                saveUser(name, password);
            }
        } else {
            Log.i("why:", content);
        }
    }

    /**
     * 得到验证码
     */
    private void getVertify() {
        OkHttpUtils
                .get()
                .url(switchVertifyUrl)
                .build()
                .execute(new BitmapCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(LoginActivity.this, "获取验证码失败！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Bitmap bitmap, int id) {
                        iv_vertify.setImageBitmap(bitmap);
                    }
                });
    }

    // 刷新验证码
    private void refreshVertify() {
        getVertify();
    }
}
