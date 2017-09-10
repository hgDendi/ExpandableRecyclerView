package com.hgdendi.expandablerecyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<SampleGroupBean> list = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            final List<SampleChildBean> childList = new ArrayList<>(i);
            for (int j = 0; j < i; j++) {
                childList.add(new SampleChildBean("child " + i));
            }
            list.add(new SampleGroupBean(childList, "group " + i));
        }

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SampleAdapter(list));
    }
}
