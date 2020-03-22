package com.euphorbia.qrReader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private final String dbName = "qrDB.db";
    private final String tableName = "qrList";

    private String contents;

    private ImageButton cameraBtn; // 카메라 버튼
    private ImageButton imageBtn; // 갤러리 버튼
    private ImageButton history; // 이전기록 버튼
    private ImageButton createQR; // QR코드생성 버튼
    private ImageButton help; // 도움말 버튼

    private TextView textViewName, textViewAddress;

    private IntentIntegrator qrScan;

    //애드몹
    private InterstitialAd mInterstitialAd;

    SQLiteDatabase qrDB;

    ArrayList<Data> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkSelfPermission();

        MobileAds.initialize(this, "ca-app-pub-3984644439850454~5713860830");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3984644439850454/1527203818");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        qrDB = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);

        list = new ArrayList<>();

        cameraBtn = findViewById(R.id.cameraBtn);
        imageBtn = findViewById(R.id.imageBtn);
        history = findViewById(R.id.history);
        createQR = findViewById(R.id.createQR);
        help = findViewById(R.id.help);

        textViewName = findViewById(R.id.textViewName);
        textViewAddress = findViewById(R.id.Copyright);

        textViewAddress.setSelected(true);

        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        qrScan.setBeepEnabled(false);
        qrScan.setCaptureActivity(AnyOrientationCaptureActivity.class);

        try {
            qrDB.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (idx INTEGER PRIMARY KEY AUTOINCREMENT, date VARCHAR(50), title VARCHAR(50), content VARCHAR(50) );");

        } catch (SQLiteException se) {

            Toast.makeText(getApplicationContext(), se.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("", se.getMessage());

        }

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.setPrompt("QR코드를 화면에 맞춰주세요");
                qrScan.initiateScan();
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 101);

            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        createQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), CreateQR.class);
                startActivity(intent);

            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            try {
                InputStream is = getContentResolver().openInputStream(data.getData());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap bMap = BitmapFactory.decodeStream(is);

                int width = 1000; // 축소시킬 너비
                int height = 1000; // 축소시킬 높이
                float bmpWidth = bMap.getWidth();
                float bmpHeight = bMap.getHeight();

                if (bmpWidth > width) {
                    // 원하는 너비보다 클 경우의 설정
                    float mWidth = bmpWidth / 100;
                    float scale = width / mWidth;
                    bmpWidth *= (scale / 100);
                    bmpHeight *= (scale / 100);
                } else if (bmpHeight > height) {
                    // 원하는 높이보다 클 경우의 설정
                    float mHeight = bmpHeight / 100;
                    float scale = height / mHeight;
                    bmpWidth *= (scale / 100);
                    bmpHeight *= (scale / 100);
                }

                Bitmap resizedBmp = Bitmap.createScaledBitmap(bMap, (int) bmpWidth, (int) bmpHeight, true);

                is.close();

                String date = null;
                String title = "이름없음";
                contents = null;

                int[] intArray = new int[resizedBmp.getWidth() * resizedBmp.getHeight()];
                resizedBmp.getPixels(intArray, 0, resizedBmp.getWidth(), 0, 0, resizedBmp.getWidth(), resizedBmp.getHeight());

                LuminanceSource source = new RGBLuminanceSource(resizedBmp.getWidth(), resizedBmp.getHeight(), intArray);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                Reader reader = new MultiFormatReader();
                Result result1 = reader.decode(bitmap);
                contents = result1.getText();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

                Calendar c1 = Calendar.getInstance();

                date = sdf.format(c1.getTime());

                // 만들어진 qr 정보를 DB에 입력
                qrDB.execSQL("INSERT INTO " + tableName + " (date, title, content) VALUES ('" + date + "', '" + title + "', '" + contents + "');");

                textViewName.setText("QR코드(바코드) 인식 결과 : " + contents);

                Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
                    @Override
                    public String transformUrl(Matcher match, String url) {
                        return "https://search.naver.com/search.naver?sm=top_hty&fbm=1&ie=utf8&query=" + contents;
                    }
                };

                Pattern pattern1 = Pattern.compile(contents);

                if (contents.substring(0, 3).equals("978") || contents.substring(0, 3).equals("979")) {

                    Linkify.addLinks(textViewName, pattern1, "", null, mTransform);

                } else if (contents.substring(0, 3).equals("010")) {

                    Linkify.addLinks(textViewName, Linkify.PHONE_NUMBERS);

                } else {

                    Linkify.addLinks(textViewName, Linkify.WEB_URLS);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == 101 && resultCode == RESULT_CANCELED) {
        }

        if (result != null) {
            //qrcode 가 없으면
            if (result.getContents() == null) {
            } else {
                try {

                    String date = null;
                    String title = "이름없음";
                    contents = result.getContents();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

                    Calendar c1 = Calendar.getInstance();

                    date = sdf.format(c1.getTime());

                    // 만들어진 qr 정보를 DB에 입력
                    qrDB.execSQL("INSERT INTO " + tableName + " (date, title, content)  Values ('" + date + "', '" + title + "', '" + contents + "');");

                    textViewName.setText("QR코드(바코드) 인식 결과 : " + result.getContents());

                    Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
                        @Override
                        public String transformUrl(Matcher match, String url) {
                            return "https://search.naver.com/search.naver?sm=top_hty&fbm=1&ie=utf8&query=" + contents;
                        }
                    };

                    Pattern pattern1 = Pattern.compile(contents);

                    if (contents.substring(0, 3).equals("978") || contents.substring(0, 3).equals("979")) {

                        Linkify.addLinks(textViewName, pattern1, "", null, mTransform);

                    } else if (contents.substring(0, 3).equals("010")) {

                        Linkify.addLinks(textViewName, Linkify.PHONE_NUMBERS);

                    } else {

                        Linkify.addLinks(textViewName, Linkify.WEB_URLS);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {

            super.onActivityResult(requestCode, resultCode, data);

        }

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            int length = permissions.length;
            for (int i = 0; i < length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }


    public void checkSelfPermission() {
        String temp = "";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.CAMERA + " ";
        }

        if (TextUtils.isEmpty(temp) == false) {
            ActivityCompat.requestPermissions(this, temp.trim().split(" "), 1);
        } else {
        }
    }
}
