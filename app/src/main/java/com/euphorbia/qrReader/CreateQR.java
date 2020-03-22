package com.euphorbia.qrReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.UUID;

public class CreateQR extends AppCompatActivity {

    private EditText editText;
    private Button CreateBtn;
    private Button SaveImage;
    private Button ShareImage;
    private ImageView ImageView;

    String text = null;

    Bitmap bitmap;

    UUID uuid = UUID.randomUUID();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qr);

        editText = findViewById(R.id.editText);
        CreateBtn = findViewById(R.id.CreateBtn);
        SaveImage = findViewById(R.id.SaveImage);
        ShareImage = findViewById(R.id.ShareImage);
        ImageView = findViewById(R.id.ImageView);

        SaveImage.setVisibility(View.GONE);


        CreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    text = editText.getText().toString();

                    Hashtable hints = new Hashtable();
                    hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

                    BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 700, 700, hints);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    ImageView.setImageBitmap(bitmap);
                    SaveImage.setVisibility(View.VISIBLE);
                    ShareImage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                }

            }
        });

        SaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QrCode";
                File folder = new File(strFolderPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                OutputStream out = null;
                String strFilePath = strFolderPath + "/";

                try {
                    File fileCacheItem = new File(strFilePath + uuid.toString() + ".jpg");
                    fileCacheItem.createNewFile();
                    out = new FileOutputStream(fileCacheItem);
                    // 비트맵을 png로 변환
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(fileCacheItem));
                    sendBroadcast(intent);
                    Toast.makeText(getApplicationContext(), "저장이 완료되었습니다", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        ShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QrCode";
                File folder = new File(strFolderPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                OutputStream out = null;
                String strFilePath = strFolderPath + "/";

                try {
                    File fileCacheItem = new File(strFilePath + uuid.toString() + ".jpg");
                    fileCacheItem.createNewFile();
                    out = new FileOutputStream(fileCacheItem);
                    // 비트맵을 png로 변환
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(fileCacheItem));
                    sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");

                String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QrCode/" + uuid.toString() + ".jpg";

                File imageFileToShare = new File(imagePath);
                Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", imageFileToShare);

                share.putExtra(Intent.EXTRA_STREAM, uri);

                startActivity(Intent.createChooser(share, "Share image to..."));
            }
        });
    }
}
