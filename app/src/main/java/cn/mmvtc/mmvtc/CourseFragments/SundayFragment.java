package cn.mmvtc.mmvtc.CourseFragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.mmvtc.mmvtc.MainActivity;
import cn.mmvtc.mmvtc.R;
import cn.mmvtc.mmvtc.utils.HttpUtils;
import okhttp3.Call;


/**
 * A simple {@link Fragment} subclass.
 */
public class SundayFragment extends Fragment {
    private ListView listView;
    private SimpleAdapter adapter;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private String courseUrl = "";
    private int column = 7;  //表格当前列数
    private SharedPreferences sp;
    private String viewstate = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_week, container, false);
        sp = getContext().getSharedPreferences("data", getContext().MODE_PRIVATE);
        listView = (ListView) v.findViewById(R.id.list_monday);
        courseUrl = MainActivity.getCourseUrl();
        adapter = new SimpleAdapter(getActivity(), list, R.layout.week_item, new String[]{"course"}, new int[]{R.id.tv_course});
        listView.setAdapter(adapter);
        getTableData();
        return v;
    }


    public void getTableData() {

        //判断本地是否有存储过学期学年
        String xueqi = sp.getString("xueqi", null);
        String xuenian = sp.getString("xuenian", null);
        String defaultNF = sp.getString("defaultNF", null);
        String defaultXQ = sp.getString("defaultXQ", null);

        if ((TextUtils.isEmpty(xueqi) && TextUtils.isEmpty(xuenian)) || (xueqi.equals(defaultXQ) && xuenian.equals(defaultNF))) {
            OkHttpUtils
                    .get()
                    .url(courseUrl)
                    .addHeader("Referer", courseUrl)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Document html = Jsoup.parse(response);
                            Elements e = html.select("input[name=__VIEWSTATE]"); //这里的到密钥
                            viewstate = e.get(0).attr("value");
                            GetData(response);
                        }
                    });
        } else {
            Map<String, String> my_header = new HashMap<String, String>();
            my_header.put("Referer", courseUrl);
            my_header.put("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:47.0) Gecko/20100101 Firefox/47.0");
            OkHttpUtils
                    .post()
                    .url(courseUrl)
                    .headers(my_header)
                    .addParams("__EVENTTARGET", "xnd")
                    .addParams("__EVENTARGUMENT", "")
                    .addParams("__VIEWSTATE", viewstate)
                    .addParams("xnd", xuenian)
                    .addParams("xqd", xueqi)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            GetData(response);
                        }
                    });
        }

    }

    private void GetData(String response) {//
        if (!list.isEmpty()) {
            list.clear();
        }

        Document html = Jsoup.parse(response);
        Elements courseTable = html.select("#Table1");

        String[] courses = new String[13];
        Elements tr = courseTable.select("tr");
        HttpUtils.loge("tr元素", tr.size() + "");
        //得到第三列数据
        String allData = "";
        for (int i = 2; i < tr.size(); i++) {
            Elements td = tr.get(i).select("td[align=Center]");
            for (int j = 0; j < td.size(); j++) {
                // 数据值而且值不能为空
                if ((column - 1 == j) && (!td.get(column - 1).text().isEmpty()))
                    allData += td.get(column - 1).text().trim() + "=";
            }
        }
        //将字符串转成数组
        courses = allData.split("=");
        // HttpUtils.loge("allData", courses.length+"");
        HttpUtils.loge("courses111", courses.length + "");
        if (courses.length <= 1) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("course", "无");
            list.add(map);
        } else {
            for (int i = 0; i < courses.length; i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("course", courses[i]);
                list.add(map);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
