package com.zyw.horrarndoo.gooview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.zyw.horrarndoo.gooview.view.GooViewAapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GooViewAapter adapter;
    private List<String> list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        for (int i = 0; i < 120; i++){
            list.add("content - " + i);
        }
        adapter = new GooViewAapter(MainActivity.this, list);
        ListView listView= (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
    }
}
