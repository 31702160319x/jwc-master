package cn.mmvtc.mmvtc.News;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.mmvtc.mmvtc.MainActivity;
import cn.mmvtc.mmvtc.R;

public class NewsActivity extends AppCompatActivity {

    private List<News> newsList;
    private NewsAdapter adapter;
    private Handler handler;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        newsList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.news_lv);
        getNews();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    adapter = new NewsAdapter(NewsActivity.this, newsList);
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            News news = newsList.get(position);
                            Intent intent = new Intent(NewsActivity.this, NewsDisplayActvivity.class);
                            intent.putExtra("news_url", news.getNewsUrl());
                            startActivity(intent);
                        }
                    });
                }
            }
        };

    }


    private void getNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i <= 10; i++) {
                        Document doc = Jsoup.connect("https://www.mmvtc.cn/templet/jsjgcx/ShowClass.jsp?id=1212&pn=" + Integer.toString(i)).get();
                        //https://www.mmvtc.cn/templet/jsjgcx/ShowClass.jsp?id=1221/1221&.pn=" + Integer.toString(i)).get();
                        Elements div_cbox = doc.getElementsByClass("cbox");
                        Elements li = div_cbox.select("li");
                        Log.i("li69", "run: " + li);
                        for (int j = 0; j < li.size(); j++) {
                            String title = li.get(j).select("a[target=_blank]").text();
                            Log.i("title", "run: " + title);
                            String time = li.get(j).getElementsByClass("columns large-2 medium-3 small-4 text-right").text();
                            String url = li.get(j).select("a[target=_blank]").attr("href");
                            if (!url.contains("https://")) {
                                url = "https://www.mmvtc.cn/" + url;
                            }
                            Log.i("title_div", "run: " + url);
                            Log.i("time", "run: " + time);
                            News news = new News(title, url, null, time);
                            newsList.add(news);
                        }
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}




