package com.euphorbia.qrReader;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryDataActivity extends AppCompatActivity {


    private final String dbName = "qrDB.db";
    private final String tableName = "qrList";

    SQLiteDatabase ReadDB;

    private EditText editText;
    private TextView tv;
    private Button webSearch;
    private ImageButton ib;
    private Button confirm, cancel;

    Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_data);

        ReadDB = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);

        editText = findViewById(R.id.editText);
        tv = findViewById(R.id.tv1);
        webSearch = findViewById(R.id.webSearch);
        ib = findViewById(R.id.DeleteBtn);
        confirm = findViewById(R.id.confirm1);
        cancel = findViewById(R.id.cancel1);

        data = (Data) getIntent().getSerializableExtra("data");

        editText.setText(data.getTitle());

        tv.setText(data.getContent());

        Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher match, String url) {
                return "http://mobile.kyobobook.co.kr/showcase/book/KOR/" + data.getContent();
            }
        };

        if (data.getContent().substring(0, 3).equals("978") || data.getContent().substring(0, 3).equals("979")) {

            Pattern pattern1 = Pattern.compile(data.getContent());
            Linkify.addLinks(tv, pattern1, "", null, mTransform);

        } else if (data.getContent().substring(0, 3).equals("010")) {

            Linkify.addLinks(tv, Linkify.PHONE_NUMBERS);

        } else {

            Linkify.addLinks(tv, Linkify.WEB_URLS);

        }


        webSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, data.getContent());
                startActivity(intent);

            }
        });


        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReadDB.execSQL("DELETE FROM " + tableName + " WHERE idx = '" + data.getIdx() + "';");

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = editText.getText().toString();

                ReadDB.execSQL("UPDATE " + tableName + " SET title = '" + title + "' WHERE idx = '" + data.getIdx() + "';");

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

    }
}
