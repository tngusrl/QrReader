package com.euphorbia.qrReader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    TextView HelpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        HelpTextView = findViewById(R.id.HelpTextView);

        HelpTextView.setText(Html.fromHtml("바코드나 QR코드를 감지할 때 가장 중요한 것은 카메라가 인식할 수 있을 정도의 <U>밝기와 기울어지지 않아야</U> 합니다. <br><br> 또한 빛반사나 그림자가 있어서도 안됩니다. <br><br> 바코드 인식 카메라 <U>세로 화면에서 붉은색 선이 바코드의 정중앙</U>을 지날 수 있도록 맞춰주세요. <br><br> 코드를 스캔 영역에 가득 채울경우 초점이 흐릿해져 오히려 정확도가 떨어질수 있으므로 사진과 같이 적당한 거리에서 코드를 인식해주세요.", Html.FROM_HTML_MODE_LEGACY));
    }
}
