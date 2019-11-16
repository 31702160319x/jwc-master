package cn.mmvtc.mmvtc;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mmvtc.mmvtc.utils.HttpUtils;
import okhttp3.Call;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScoreFragment extends Fragment {
    private ListView listView;
    private SimpleAdapter adapter;
    private List<Map<String, String>> list = new ArrayList<Map<String,String>>();
    private String scoreUrl = "";
    private String viewstate = "";

    public ScoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_score, container, false);
        scoreUrl=MainActivity.getScoreUrl();
        listView = (ListView) v.findViewById(R.id.listView);
        adapter = new SimpleAdapter(getActivity(), list, R.layout.score_item, new String[]{"nianfen","xueqi","className","score"}, new int[]{R.id.nianfen,R.id.xueqi,R.id.className,R.id.score});
        listView.setAdapter(adapter);
        getViewstate();
        return v;
    }

    private void getViewstate() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Referer", scoreUrl);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:47.0) Gecko/20100101 Firefox/47.0");
        OkHttpUtils
                .post()
                .headers(headers)
                .url(scoreUrl)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Document html = Jsoup.parse(response);
                        Elements e = html.select("input[name=__VIEWSTATE]");//这里的到密钥
                        viewstate = e.get(0).attr("value");
                        getScore();
                    }
                });
    }

    private void getScore() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Referer", scoreUrl);
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:47.0) Gecko/20100101 Firefox/47.0");
        OkHttpUtils
                .post()
                .url(scoreUrl)
                .headers(headers)
                .addParams("__EVENTTARGET","")
                .addParams("__VIEWSTATE",viewstate)
                .addParams("ddlXN", "")
                .addParams("ddlXQ","")
                .addParams("ddl_kcxz", "")
                .addParams("btn_zcj", "历年成绩")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        getScoreItem(response);
                    }
                });
    }

    private void getScoreItem(String html) {//解析html获取数据
        Document content = Jsoup.parse(html);
        Element ScoreList = content.getElementById("Datagrid1");
        Elements tr = ScoreList.getElementsByTag("tr");
        for (int i = 1; i < tr.size(); i++) {
            Elements td = tr.get(i).getElementsByTag("td");
            String nianfen=td.get(0).text();
            String xueqi = td.get(1).text();
            String className = td.get(3).text();
            String score = td.get(8).text();
            Map<String, String> map = new HashMap<String, String>();
            map.put("nianfen",nianfen);
            map.put("xueqi", xueqi);
            map.put("className", className);
            map.put("score", score);
            list.add(map);
        }
        adapter.notifyDataSetChanged();
        HttpUtils.loge("DATA",list.toString());
    }
}
