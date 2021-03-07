package com.xforce.meetinghorn;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.InputStream;
import java.net.URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.io.InputStreamReader;
import android.app.ProgressDialog;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import android.widget.ListView;
import android.os.AsyncTask;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;


public class MainActivity extends AppCompatActivity {
    private Spinner spinner_r,spinner_f,spinner_s;
    private Button btn;
    private String json_url = "https://www.yitama.com/api/common/v3/wow/classic/meetinghorn/list?realm=希尔盖&faction=Horde&activityName=&keyword=&activityMode=&p=1&pageSize=30";
    private String realm,faction,faction_text;
    private String[] fruit = { "Apple", "Banana", "Orange", "Watermelon",
            "Pear", "Grape", "Pineapple", "Strawberry", "Cherry", "Mango" };
    ArrayList<HashMap<String,String>> dataList;
    String activity,character,comment;
    private ListView listView;
    ArrayList<String> items = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner_s = (Spinner)findViewById(R.id.spinner_subzone);
        spinner_r = (Spinner)findViewById(R.id.spinner_realm);
        spinner_f = (Spinner)findViewById(R.id.spinner_faction);
        btn = (Button) this.findViewById(R.id.button_ok);
        listView = (ListView) findViewById(R.id.listview_content);

        String[] subzone = getResources().getStringArray(R.array.subzone);

        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this,
                R.array.subzone, android.R.layout.simple_spinner_item);

        // 设置下拉菜单的样式
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 将数据绑定到spinner上
        spinner_s.setAdapter(aa);

        // 添加监听事件

        spinner_s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // 得到事件中选中的值
                Spinner s = (Spinner) arg0;
                String pro = (String) s.getItemAtPosition(arg2);
                ArrayAdapter<CharSequence> realmAdapter = null;
                if (pro.equals("一区")) {

                    realmAdapter = ArrayAdapter.createFromResource(
                            MainActivity.this, R.array.realm_1,
                            android.R.layout.simple_spinner_item);
                } else if (pro.equals("五区")) {

                    realmAdapter = ArrayAdapter.createFromResource(
                            MainActivity.this, R.array.realm_5,
                            android.R.layout.simple_spinner_item);
                }else if (pro.equals("八区")) {

                    realmAdapter = ArrayAdapter.createFromResource(
                            MainActivity.this, R.array.realm_8,
                            android.R.layout.simple_spinner_item);
                }
                spinner_r.setAdapter(realmAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                realm = spinner_r.getSelectedItem().toString();
                faction_text = spinner_f.getSelectedItem().toString();
                if(faction_text.equals("联盟")){
                    faction = "Alliance";
                }else {
                    faction = "Horde";
                }
                json_url = "https://www.yitama.com/api/common/v3/wow/classic/meetinghorn/list?realm="+ realm + "&faction="+ faction +"&activityName=&keyword=&activityMode=&p=1&pageSize=30";
                new JsonTask().execute();
            }
    });
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... strings) {

            String current = "";
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(json_url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(stream);
                int data = stream.read();
                while (data!=-1){
                    current +=(char) data;
                    data = isr.read();
                }
                return  current;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return current;
        }

        @Override
        protected void onPostExecute(String result) {
            dataList = new ArrayList<>();
            try{
                JSONObject jsonObject = JSONObject.fromObject(result);
                String sdata = jsonObject.getString("data");
                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");

                for(int i = 0; i< jsonArray.size(); i++){

                    JSONObject jsonObject_ = jsonArray.getJSONObject(i);
                    activity = jsonObject_.getString("activity");
                    character = jsonObject_.getString("character") +"   公会:"+jsonObject_.getString("guildName") +"   人数：" +jsonObject_.getString("currentMembers") +"/" +jsonObject_.getString("totalMembers") + "   评分："+jsonObject_.getString("scoreAvgPercent");
                    comment = activity + "\n" +jsonObject_.getString("comment");
                    items.add(comment);

                    HashMap<String,String> datas = new HashMap<>();
                    datas.put("comment",comment);
                    datas.put("character",character);
                    datas.put("activity",activity);
                    dataList.add(datas);

                }
            }catch (JSONException e){
                e.printStackTrace();
            }

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this,
                    dataList,
                    R.layout.row_layout,
                    new String[]{"comment","character"},
                    new int[]{R.id.textView1, R.id.textView2} );
            listView.setAdapter(adapter);

            Collections.sort(dataList, new Comparator<HashMap<String, String>>()
            {
                @Override
                public int compare(HashMap<String, String> a, HashMap<String, String> b)
                {
                    return a.get("activity").compareTo(b.get("activity"));
                }
            });
        }
    }
}