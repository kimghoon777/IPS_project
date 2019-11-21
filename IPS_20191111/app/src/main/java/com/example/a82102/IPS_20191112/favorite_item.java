package com.example.a82102.IPS_20191112;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class favorite_item extends AppCompatActivity { // 쿠폰 발송

    public static int[] check_box= new int[13]; //체크 박스 체크 확인용 배열

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_item);

        Toast.makeText(getApplicationContext(),"3개이하로 체크하세요.",Toast.LENGTH_LONG).show();


        for(int i=0; i<13;i++){
            check_box[i]=0; //체크 박스 체크 유무 확인용 작업
        }

        // 체크박스
        final CheckBox cb1 = (CheckBox)findViewById(R.id.checkBox1);
        final CheckBox cb2 = (CheckBox)findViewById(R.id.checkBox2);
        final CheckBox cb3 = (CheckBox)findViewById(R.id.checkBox3);
        final CheckBox cb4 = (CheckBox)findViewById(R.id.checkBox4);
        final CheckBox cb5 = (CheckBox)findViewById(R.id.checkBox5);
        final CheckBox cb6 = (CheckBox)findViewById(R.id.checkBox6);
        final CheckBox cb7 = (CheckBox)findViewById(R.id.checkBox7);
        final CheckBox cb8 = (CheckBox)findViewById(R.id.checkBox8);

        Button b = (Button)findViewById(R.id.button1);
        final TextView tv = (TextView)findViewById(R.id.textView2);

        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String result = "";  // 결과를 출력할 문자열  ,  항상 스트링은 빈문자열로 초기화 하는 습관을 가지자
                if(cb1.isChecked() == true) {
                    result += cb1.getText().toString();
                    check_box[2]=1;
                }
                if(cb2.isChecked() == true) {
                    result += cb2.getText().toString();
                    check_box[3]=1;
                }
                if(cb3.isChecked() == true) {
                    result += cb3.getText().toString();
                    check_box[4]=1;
                }
                if(cb4.isChecked() == true) {
                    result += cb4.getText().toString();
                    check_box[5]=1;
                }
                if(cb5.isChecked() == true) {
                    result += cb5.getText().toString();
                    check_box[7]=1;
                }
                if(cb6.isChecked() == true) {
                    result += cb6.getText().toString();
                    check_box[9]=1;
                }
                if(cb7.isChecked() == true) {
                    result += cb7.getText().toString();
                    check_box[10]=1;
                }
                if(cb8.isChecked() == true) {
                    result += cb8.getText().toString();
                    check_box[12]=1;
                }

                tv.setText("선택결과:" + result);

            } // end onClick
        }); // end setOnClickListener


    }

    public void onbuttonclicked(View v){ //다음 페이지 이동
        Intent intent = new Intent(this, MainActivity.class );
        startActivity(intent);
    }
}
