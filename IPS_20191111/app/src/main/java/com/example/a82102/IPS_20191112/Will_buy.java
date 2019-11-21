package com.example.a82102.IPS_20191112;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Will_buy extends AppCompatActivity { // 구매리스트

    public static int[] check_box1= new int[13]; // 체크 박스 체크 확인용 배열
    public static String[] purchase_list = new String[13]; // 상품 String 값 저장
    public static String result_2 = new String(); // 데이터 처리용 => , 추가
    public static int[] draw_line = new int[19]; // 경로 저장용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_will_buy);

        Toast.makeText(getApplicationContext(),"2개만 체크하세요.",Toast.LENGTH_LONG).show();

        // 초기화
        for (int i = 0; i < 19; i++)
            draw_line[i] = 0;

        for(int i=0;i<13;i++)
            check_box1[i]=0; //체크 박스 체크 유무 확인용 작업
        for(int i=0;i<2;i++)
            purchase_list[i]=""; //구매 리스트 문자열 배열 초기화

        // 체크박스
        final CheckBox cb1 = findViewById(R.id.checkBox1);
        final CheckBox cb2 = findViewById(R.id.checkBox2);
        final CheckBox cb3 = findViewById(R.id.checkBox3);
        final CheckBox cb4 = findViewById(R.id.checkBox4);
        final CheckBox cb5 = findViewById(R.id.checkBox5);
        final CheckBox cb6 = findViewById(R.id.checkBox6);
        final CheckBox cb7 = findViewById(R.id.checkBox7);
        final CheckBox cb8 = findViewById(R.id.checkBox8);

        // 확인 버튼
        Button b = findViewById(R.id.button1);
        final TextView tv = findViewById(R.id.textView2);

        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String result = "";  // 결과를 출력할 문자열  ,  항상 스트링은 빈문자열로 초기화 하는 습관을 가지자

                RouteThread routeThread = new RouteThread(); // RouteThread 선언
                routeThread.start(); // routeThread 실행

                if(cb1.isChecked() == true) {
                    result += ","+cb1.getText().toString();
                    purchase_list[2] = "문구";// 구매리스트 클릭 시 해당 문자열 배열에 저장...
                    check_box1[2]=1; //비콘 3번
                }
                if(cb2.isChecked() == true) {
                    result += ","+cb2.getText().toString();
                    purchase_list[3] = "신발";
                    check_box1[3]=1;//비콘 4번
                }
                if(cb3.isChecked() == true) {
                    result += ","+cb3.getText().toString();
                    purchase_list[4] = "과자";
                    check_box1[4]=1;//비콘 5번
                }
                if(cb4.isChecked() == true) {
                    result += ","+cb4.getText().toString();
                    purchase_list[5] = "과일";
                    check_box1[5]=1;
                }
                if(cb5.isChecked() == true) {
                    result += ","+cb5.getText().toString();
                    purchase_list[7] = "야채";
                    check_box1[7]=1;
                }
                if(cb6.isChecked() == true) {
                    result += ","+cb6.getText().toString();
                    purchase_list[9] = "화장품";
                    check_box1[9]=1;
                }
                if(cb7.isChecked() == true) {
                    result += ","+cb7.getText().toString();
                    purchase_list[10] = "육류";
                    check_box1[10]=1;
                }
                if(cb8.isChecked() == true) {
                    result += ","+cb8.getText().toString();
                    purchase_list[12] = "음료";
                    check_box1[12]=1;//비콘 13번
                }
                result_2 = result.substring(1);

                tv.setText("선택결과:" + result_2);

            } // end onClick
        }); // end setOnClickListener
    }

    public void onbuttonclicked(View v){ //다음 페이지 이동
        Intent intent = new Intent(this, favorite_item.class );
        startActivity(intent);
    }

    class RouteThread extends Thread {
        public void run() {
            String host = "121.155.161.100";
            //String host = "192.168.0.140";
            int port = 5800;
            int ct=0;
            int i=0;

            try { // 소켓 통신할때 예외처리 필수
                Socket socket = new Socket(host, port); // 소켓 선언
                DataOutputStream outStream = new DataOutputStream((socket.getOutputStream())); // 데이터 전송스트림 선언
                outStream.writeUTF(result_2); // 앱 -> 서버 보낼 문자열
                outStream.flush(); // 보냄

                // 수신한 데이터
                DataInputStream input_1 = new DataInputStream(socket.getInputStream());
                InputStreamReader input_2 = new InputStreamReader(input_1);
                BufferedReader br = new BufferedReader(input_2);//create a BufferReader object for input

                String recv = br.readLine().substring(1); // recv: 받은 데이터, substring(1) : 파이썬->앱 인코딩문제로 쓰레기값 제외한 데이터 저장
                Log.v("qwe", "수신한 데이터 : " + recv +" // "+ct+"번째"); // 데이터 확인용 Log


                String recv2[] = recv.split(","); // 받은 데이터 ","로 분리

                Log.v("asd","가공한 데이터 : " + recv2);


                for(i=0;i<recv2.length;i++) { // 수신한 배열의 길이만큼 반복 ex) 경로: 과자-A-B-음료 이면 길이가 4 -> 4번 반복

                    if (recv2[i].equals("line_a_b")) {
                       draw_line[0]=1;
                        Log.v("asd", "실행1");

                    }
                    if (recv2[i].equals("line_13_b")) {
                        draw_line[1]=1;
                        Log.v("asd", "실행2");
                        //}
                    }
                    if (recv2[i].equals("line_11_10")) {
                        draw_line[2]=1;
                        Log.v("asd", "실행3");
                        //}
                    }
                    if (recv2[i].equals("line_11_b")) {
                        draw_line[3]=1;
                        Log.v("asd", "실행4");
                        //}
                    }
                    if (recv2[i].equals("line_13_3")) {
                        draw_line[4]=1;
                        Log.v("asd", "실행5");
                        //}
                    }
                    if (recv2[i].equals("line_b_10")) {
                        draw_line[5]=1;
                        Log.v("asd", "실행6");
                        //}
                    }
                    if (recv2[i].equals("line_3_b")) {
                        draw_line[6]=1;
                        Log.v("asd", "실행7");
                        //}
                    }
                    if (recv2[i].equals("line_4_6")) {
                        draw_line[7]=1;
                        Log.v("asd", "실행8");
                        //}
                    }
                    if (recv2[i].equals("line_4_a")) {
                        draw_line[8]=1;
                        Log.v("asd", "실행9");
                        //}
                    }
                    if (recv2[i].equals("line_5_4")) {
                        draw_line[9]=1;
                        Log.v("asd", "실행10");
                        //}
                    }
                    if (recv2[i].equals("line_5_6")) {
                        draw_line[10]=1;
                        Log.v("asd", "실행11");
                        //}
                    }
                    if (recv2[i].equals("line_5_a")) {
                        draw_line[11]=1;
                        Log.v("asd", "실행12");
                        //}
                    }
                    if (recv2[i].equals("line_8_10")) {
                        draw_line[12]=1;
                        Log.v("asd", "실행13");
                        //}
                    }
                    if (recv2[i].equals("line_8_11")) {
                        draw_line[13]=1;
                        Log.v("asd", "실행14");
                        //}
                    }
                    if (recv2[i].equals("line_8_b")) {
                        draw_line[14]=1;
                        Log.v("asd", "실행15");
                        //}
                    }
                    if (recv2[i].equals("line_a_10")) {
                        draw_line[15]=1;
                        Log.v("asd", "실행16");
                        //}
                    }if (recv2[i].equals("line_a_11")) {
                        draw_line[16]=1;
                        Log.v("asd", "실행17");
                        //}
                    }if (recv2[i].equals("line_a_6")) {
                        draw_line[17]=1;
                        Log.v("asd", "실행18");
                        //}
                    }
                    if (recv2[i].equals("line_a_8")) {
                        draw_line[18]=1;
                        Log.v("asd", "실행19");
                        //}
                    }

                }


                socket.close(); // 소켓 종료
            } catch (Exception e) { // 예외처리
                e.printStackTrace();
                Log.v("qwe", "ㅜㅜ");
            }
            Log.v("qwe", "qq");
        }
    }

}
