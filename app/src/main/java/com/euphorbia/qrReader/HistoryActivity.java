package com.euphorbia.qrReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {

    private final String dbName = "qrDB.db";
    private final String tableName = "qrList";

    SQLiteDatabase ReadDB;

    ArrayList<Data> list;

    RecyclerAdapter adapter;

    SearchView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        list = new ArrayList<>();

        sv = findViewById(R.id.sv);

        ReadDB = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);

        Cursor c = ReadDB.rawQuery("SELECT * FROM " + tableName, null);

        list.clear();
        if (c != null) {
            if (c.moveToFirst()) {
                do {

                    int idx = c.getInt(c.getColumnIndex("idx"));
                    String date = c.getString(c.getColumnIndex("date"));
                    String title = c.getString(c.getColumnIndex("title"));
                    String content = c.getString(c.getColumnIndex("content"));

                    Data data = new Data(idx, date, title, content);

                    list.add(data);

                } while (c.moveToNext());
            }
        }

        if (list.size() == 0) {

            list.clear();
            list.add(new Data(1, "이전 기록이 없습니다.", "이전 기록이 없습니다.", "이전 기록이 없습니다."));

        }

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Collections.reverse(list);
        adapter = new RecyclerAdapter(list);

        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Data data = list.get(position);
                Intent intent = new Intent(getApplicationContext(), HistoryDataActivity.class);
                intent.putExtra("data", data);
                intent.putExtra("position", position);
                startActivityForResult(intent, 101);
            }
        });

        recyclerView.setAdapter(adapter);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data1) {
        super.onActivityResult(requestCode, resultCode, data1);

        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {

                Cursor c = ReadDB.rawQuery("SELECT * FROM " + tableName, null);

                list.clear();

                if (c != null) {
                    if (c.moveToFirst()) {
                        do {

                            int idx = c.getInt(c.getColumnIndex("idx"));
                            String date = c.getString(c.getColumnIndex("date"));
                            String title1 = c.getString(c.getColumnIndex("title"));
                            String content = c.getString(c.getColumnIndex("content"));

                            Data data = new Data(idx, date, title1, content);

                            list.add(data);

                        } while (c.moveToNext());
                    }
                }

                if (list.size() == 0) {

                    list.clear();
                    list.add(new Data(1, "이전 기록이 없습니다.", "이전 기록이 없습니다.", "이전 기록이 없습니다."));

                }

                RecyclerView recyclerView = findViewById(R.id.rv);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                Collections.reverse(list);
                adapter = new RecyclerAdapter(list);
                adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Data data = list.get(position);
                        Intent intent = new Intent(getApplicationContext(), HistoryDataActivity.class);
                        intent.putExtra("data", data);
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 101);
                    }
                });
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            } else {
            }
        }
    }
}
