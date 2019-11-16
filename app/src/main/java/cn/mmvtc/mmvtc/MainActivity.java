package cn.mmvtc.mmvtc;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URLEncoder;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.mmvtc.mmvtc.Adapter.MyFragmentPagerAdapter;
import cn.mmvtc.mmvtc.News.NewsActivity;
import cn.mmvtc.mmvtc.utils.HttpUtils;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {

    private Button seach_news;
    private ViewPager pager;
    private RadioGroup radioGroup;
    private RadioButton rb_me, rb_score, rb_course;
    private TextView tv_studentName;
    private MyFragmentPagerAdapter adapter;
    private ImageView img;
    private LoginInfo loginInfo;
    private SharedPreferences sharedPreferences;
    private static String referer_url = "";//referer链接
    private static String infoUrl = "";//个人信息链接
    private static String scoreUrl = "";//成绩信息
    private static String courseUrl = "";//课表查询
    private static String cookie = "";  //cookie
    private static String name = "";
    private static String studentName = "";
    private String imgUrl = "http://jwc.mmvtc.cn/";
    private boolean flag = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        studentName = intent.getStringExtra("studentName");
        tv_studentName.setText(studentName);
        referer_url = "http://jwc.mmvtc.cn/xs_main.aspx?xh=" + name;
        infoUrl = "http://jwc.mmvtc.cn/xsgrxx.aspx?xh=" + name + "&xm=" + URLEncoder.encode(studentName) + "&gnmkdm=N121501";
        scoreUrl = "http://jwc.mmvtc.cn/xscjcx.aspx?xh=" + name + "&xm=" + URLEncoder.encode(studentName) + "&gnmkdm=N121065";
        //http://jwc.mmvtc.cn/xskbcx.aspx?xh=学号&xm=%CD%F5%BD%F0%B3%C7&gnmkdm=N121603
        courseUrl = "http://jwc.mmvtc.cn/xskbcx.aspx?xh=" + name + "&xm=" + URLEncoder.encode(studentName) + "&gnmkdm=N121603";
        //得到登录者的头像
        getImgMes();

    }


    private void init() {
        radioGroup = (RadioGroup) findViewById(R.id.rg_tab_bar);
        rb_me = (RadioButton) findViewById(R.id.rb_me);
        rb_score = (RadioButton) findViewById(R.id.rb_score);
        rb_course = (RadioButton) findViewById(R.id.rb_course);
        tv_studentName = (TextView) findViewById(R.id.tv_studentName);
        img = (ImageView) findViewById(R.id.img);
        rb_course.setChecked(true);
        radioGroup.setOnCheckedChangeListener(this);
        List<Fragment> fragments = new ArrayList<Fragment>();//设置fragment
        fragments.add(new CourseFragment());
        fragments.add(new ScoreFragment());
        fragments.add(new InfoFragment());
        adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragments);//初始化adapter
        pager = (ViewPager) findViewById(R.id.viewpager);//设置ViewPager
        pager.setOffscreenPageLimit(3);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);
        pager.setOnPageChangeListener(this);
        seach_news = (Button) findViewById(R.id.seach_news);
        seach_news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                startActivity(intent);
            }
        });


    }

    //得到头像图片URL连接
    private void getImgMes() {
        HttpUtils.loge("InfoUrl", infoUrl);
        OkHttpUtils
                .get()
                .url(infoUrl)
                .addHeader("Referer", infoUrl)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.i("MyselfFragmentException", "onError: " + e);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Document dom = Jsoup.parse(response);
                        String img = dom.select("#xszp").attr("src");//得到
                        imgUrl += img;
                        HttpUtils.loge("imgUrl114", imgUrl);
                        getImgByte();
                    }
                });
    }

    //获得imgurl后下载
    private void getImgByte() {
        OkHttpUtils
                .get()
                .url(imgUrl)
                .build()
                .execute(new BitmapCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(Bitmap bitmap, int id) {
                        img.setImageBitmap(bitmap);
                    }
                });

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 2) {
            switch (pager.getCurrentItem()) {
                case 0:
                    rb_course.setChecked(true);
                    break;
                case 1:
                    rb_score.setChecked(true);
                    break;
                case 2:
                    rb_me.setChecked(true);
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int arg1) {
        switch (arg1) {
            case R.id.rb_course:
                pager.setCurrentItem(0);
                break;
            case R.id.rb_score:
                pager.setCurrentItem(1);
                break;
            case R.id.rb_me:
                pager.setCurrentItem(2);
                break;
        }
    }


    public static String getInfoUrl() {
        return infoUrl;
    }

    public static String getScoreUrl() {
        return scoreUrl;
    }

    public static String getCourseUrl() {
        return courseUrl;
    }


    //双击退出软件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (flag == false) {
                flag = true;
                Toast.makeText(getApplicationContext(), "再按一次退出软件", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flag = false;
                    }
                }, 2000);
            } else {
                //清除数据学期年份
                SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.commit();
                finish();
                System.exit(0);
            }
        }
        return false;
    }







    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }
}
