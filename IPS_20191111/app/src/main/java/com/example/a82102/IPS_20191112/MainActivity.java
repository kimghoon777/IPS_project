package com.example.a82102.IPS_20191112;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Float.parseFloat;


public class MainActivity extends AppCompatActivity {

    static List<APinf> list = new ArrayList<>(); // 블루투스 정보 저장
    String apssid;
    String apmac;
    String aprssi;

    public static String[] beacon_rssi = new String[13]; // rssi 저장

    public static int[] stop_sending_coupon = new int[13]; //coupon 한번 보내면 더 이상 안보내게 하기 위한 flag 설정

    BluetoothAdapter mBluetoothAdapter; // 데이터와 리스트 뷰 사이의 통신을 위한 다리 역할

    final static int BLUETOOTH_REQUEST_CODE = 100; // 블루투스 요청 액티비티 코드

    Button btnLocation;

    SimpleAdapter adapterDevice;
    Handler handler;

    //list - Device 목록 저장
    List<Map<String, String>> dataDevice;
    List<BluetoothDevice> bluetoothDevices;
    int selectDevice;

    // 푸시


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = new MyView( this);

        addContentView(view, new LinearLayout.LayoutParams(5000, 10000)); // canvas 크기

        for (int i = 0; i < 13; i++)
            beacon_rssi[i] = "-100";  // rssi값 초기화

        for (int i = 0; i < 13; i++)
            stop_sending_coupon[i] = 1; //쿠폰 더 이상 안보내게 하기 위한 flag 설정

        btnLocation = findViewById(R.id.btnLocation);

        //Adapter2
        dataDevice = new ArrayList<>();
        adapterDevice = new SimpleAdapter(this, dataDevice, android.R.layout.simple_list_item_2, new String[]{"name", "address"}, new int[]{android.R.id.text1, android.R.id.text2});

        //검색된 블루투스 디바이스 데이터
        bluetoothDevices = new ArrayList<>();
        //선택한 디바이스 없음
        selectDevice = -1;

        //블루투스 지원 유무 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //블루투스를 지원하지 않으면 null을 리턴한다 // 아니요 누르면 꺼짐
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않는 단말기 입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //블루투스 브로드캐스트 리시버 등록
        //리시버1
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        //리시버2
        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        searchFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBluetoothSearchReceiver, searchFilter);
        //리시버3
        IntentFilter scanmodeFilter = new IntentFilter();
        scanmodeFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBluetoothScanmodeReceiver, scanmodeFilter);

        //2. 블루투스가 꺼져있으면 사용자에게 활성화 요청하기
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        } else {

        }

        handler = new Handler();

// 시간 UI

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!Thread.interrupted())
                            try {
                                Thread.sleep(2000);
                                runOnUiThread(new Runnable() // 실시간으로 UI를 바꾸기 위해 - 사용자 위치 표시
                                {
                                    @Override
                                    public void run() {
                                        if (mBluetoothAdapter.isDiscovering()) {
                                            mBluetoothAdapter.cancelDiscovery();
                                        }
                                        mBluetoothAdapter.startDiscovery();
                                        RequestThread thread = new RequestThread();
                                        thread.start();
                                        Log.v("check", "UI run");
                                    }
                                });
                            } catch (InterruptedException e) {
                                Log.v("check","UI 오류");
                            }
                    }
                })).start();

            }
        });


    } // onCreate

    private long pressTime=0;

    @Override
    public void onBackPressed() { // 뒤로가기 2번
        if(System.currentTimeMillis() - pressTime <2000){
            finishAffinity();
            return;
        }
        Toast.makeText(this,"한 번더 누르시면 앱이 종료됩니다",Toast.LENGTH_LONG).show();
        pressTime = System.currentTimeMillis();
    }


    class RequestThread extends Thread { // rssi값 전송 thread

        public void run() {
            Log.v("zz", "bbbbbb");
            request();
        }


    }

    int AP_Count = 0;
    int Data_Count = 0;

    private void request() { // rssi값 전송용
        ImageView point; // 사용자 위치 point
        point = findViewById(R.id.point);
        point.setX(0);
        point.setY(0);
        try {
            String temp = ""; // 수신 rssi값 저장변수
            // rssi값 데이터 처리 (공백추가) // 간단한 서버 데이터 처리를 위해
            for (int i = 0; i < 13; i++) {
                if (i==0)
                    temp += beacon_rssi[i];
                else
                    temp += " " + beacon_rssi[i];
            }

            Socket socket = new Socket("121.155.161.100", 5800); // 라즈베리파이 ip / port 번호
            DataOutputStream outstream = new DataOutputStream(socket.getOutputStream()); // 데이터 전송스트림
            Log.v("check", temp); // 전송 데이터 확인
            outstream.writeUTF(temp); // 데이터 전송
            outstream.flush();

            // 수신 데이터 저장
            DataInputStream input_1 = new DataInputStream(socket.getInputStream());
            InputStreamReader input_2 = new InputStreamReader(input_1);
            BufferedReader br = new BufferedReader(input_2);

            try {
                String[] pos = (br.readLine()).split(","); // 수신한 데이터 ","로 분리 // 좌표값 ex 650, 900

            Log.v("check","pos1 +: "+pos[0]+"pos2 +: "+pos[1]); // 좌표 확인
            float posX = parseFloat(pos[0]); // x좌표 값 저장
            float posY = parseFloat(pos[1]); // y좌표 값 저장

                // point 이동
                point.setX(posX);
                point.setY(posY);

            }catch (Exception e){
                Log.v("recv","error");
            }
            socket.close(); // 소켓조요
        } catch (IOException e) {
            e.printStackTrace(); // 오류 확인
        }

    }


    // 블루투스 search
    private final BroadcastReceiver mBluetoothSearchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("qwe", "onReceive   :  " + action);
            switch (action) {
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    dataDevice.clear();
                    bluetoothDevices.clear();
                    AP_Count = 0;
                    list.clear();

                    Log.v("김밥", "블루투스 검색 시작");
                    Toast.makeText(MainActivity.this, "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
                    break;


                //블루투스 디바이스 찾음
                case BluetoothDevice.ACTION_FOUND:

                    while (AP_Count != 100) {
                        //검색한 블루투스 디바이스의 객체를 구한다
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        AP_Count++; // 찾은 비콘 갯수
                        adapterDevice.notifyDataSetChanged();

                        //블루투스 디바이스 저장
                        bluetoothDevices.add(device);

                        apssid = "Name:  " + device.getName(); //device.getName() : 블루투스 디바이스의 이름
                        apmac = "MAC:  " + device.getAddress(); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                        aprssi = "RSSI:  " + String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

                        switch (device.getAddress()) {
                            case "C2:01:1E:00:02:F9":  // beacon 01
                                beacon_rssi[0] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon1");
                                break;
                            case "C2:01:1E:00:02:FA":  // beacon 02
                                beacon_rssi[1] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon2");
                                break;
                            case "C2:01:1E:00:02:FB":  // beacon 03
                                beacon_rssi[2] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon3");
                                break;
                            case "C2:01:1E:00:02:FE":  // beacon 04
                                beacon_rssi[3] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon4");
                                break;
                            case "C2:01:1E:00:02:FF":  // beacon 05
                                beacon_rssi[4] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "5번째: " + beacon_rssi[4]);
                                break;
                            case "C2:01:1E:00:03:00":  // beacon 06
                                beacon_rssi[5] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "6번째: " + beacon_rssi[5]);
                                break;
                            case "C2:01:1E:00:03:01":  // beacon 07
                                beacon_rssi[6] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "7번째: " + beacon_rssi[6]);
                                break;
                            case "C2:01:1E:00:03:02":  // beacon 08
                                beacon_rssi[7] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon8");
                                break;
                            case "C2:01:1E:00:03:04":  // beacon 09
                                beacon_rssi[8] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon9");
                                break;
                            case "C2:01:1E:00:03:07":  // beacon 10
                                beacon_rssi[9] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon10");
                                break;
                            case "C2:01:1E:00:03:C0":  // beacon 11
                                beacon_rssi[10] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon11");
                                break;
                            case "C2:01:1E:00:03:B1":  // beacon 12
                                beacon_rssi[11] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon12");
                                break;
                            case "C2:02:0B:00:05:1C":  // beacon 13
                                beacon_rssi[12] = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                                Log.v("rssi", "beacon13");
                                break;

                        }

                        Sending_Coupon();  // 쿠폰 발급

                        list.add(new APinf(apssid, apmac, aprssi));
                        Data_Count++;

                        break;
                    }

                    //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(MainActivity.this, "블루투스 검색 종료", Toast.LENGTH_SHORT).show();
                    btnLocation.setEnabled(true);
                    Log.v("김밥", "종료");
                    //AP_Count=0;
                    break;

            }
        }
    };

    //블루투스 검색응답 모드 BroadcastReceiver
    BroadcastReceiver mBluetoothScanmodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
            switch (state) {
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                    Toast.makeText(MainActivity.this, "다른 블루투스 기기에서 내 휴대폰을 찾을 수 있습니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    // 블루투스 검색 버튼 클릭 // 지금은 안씀
    public void mOnBluetoothSearch(View v) {
        //검색버튼 비활성화
        Log.v("김밥", "ddddddzzzz");
        btnLocation.setEnabled(false);
        //mBluetoothAdapter.isDiscovering() : 블루투스 검색중인지 여부 확인
        //mBluetoothAdapter.cancelDiscovery() : 블루투스 검색 취소
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //mBluetoothAdapter.startDiscovery() : 블루투스 검색 시작
        //mBluetoothAdapter.startDiscovery();
    }

    // 블루투스 활성화
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BLUETOOTH_REQUEST_CODE:
                //블루투스 활성화 승인
                if (resultCode == Activity.RESULT_OK) {

                }
                //블루투스 활성화 거절
                else {
                    Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
        }
    }

    // 블루투스 종료
    @Override
    protected void onDestroy() {
        unregisterReceiver(mBluetoothSearchReceiver);
        unregisterReceiver(mBluetoothScanmodeReceiver);
        super.onDestroy();
    }

    // 예전 앱
    class APinf {
        String SSID_inf = "";
        String MAC_inf = "";
        String RSSI_inf = "";

        public APinf(String SSID_inf, String MAC_inf, String RSSI_inf) {
            this.SSID_inf = SSID_inf;
            this.MAC_inf = MAC_inf;
            this.RSSI_inf = RSSI_inf;
        }
    }

    // 쿠폰 발송
    private void Sending_Coupon() {

        if (favorite_item.check_box[4]==1 && Integer.parseInt(beacon_rssi[4]) >= -60) { // rssi값이 -60 이상인 경우 && 체크박스 체크한 경우 발송

            if (stop_sending_coupon[4] == 1) { // 쿠폰 발송 1회만

                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // SDK 버전확인
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti1";
                    String Noti_Channel_Group_ID = "Noti_Group1";

                     // 푸시 채널 선언
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);


                    // 채널이 있는지 체크해서 없을경우 만들고 있으면 채널을 재사용합니다.
                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    notificationManager.createNotificationChannel(notificationChannel);

                    // 쿠폰 커스텀을 위한 부분
                    NotificationCompat.Style style = new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.snack_coupon11)); // 큰 사진 띄우기
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02)) // 작은 사진 띄우기
                            .setSmallIcon(R.drawable.ic_eating) // 더 작은 사진
                            .setWhen(System.currentTimeMillis()) // 띄어주는 시간
                            .setShowWhen(true)
                            .setAutoCancel(true) // 옆으로 치우면 사라짐
                            .setStyle(style)//noti 큰 그림 띄우기용
                            .setContentTitle("Snack 몰")
                            .setContentText("과자 특가 할인!!");

                    notificationManager.notify(0, builder.build()); // 여러개의 알림을 위한 id값
                    stop_sending_coupon[4] = 0;//비콘2 쿠폰 발급 더이상 안함..
                }
            }
            else
                return;
        }//5번 비콘 과자 쿠폰 발송 마지막 줄..

        else if (favorite_item.check_box[2]==1 && Integer.parseInt(beacon_rssi[2]) >= -60) {
        if (stop_sending_coupon[2] == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                int importance = NotificationManager.IMPORTANCE_HIGH;
                String Noti_Channel_ID = "Noti2";
                String Noti_Channel_Group_ID = "Noti_Group2";

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

                if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                    Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                notificationManager.createNotificationChannel(notificationChannel);

                NotificationCompat.Style style = new NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.mungu_coupon1));// noti 큰 사진 띄우기

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02))
                        .setSmallIcon(R.drawable.ic_mungu)
                        .setWhen(System.currentTimeMillis())
                        .setShowWhen(true)
                        .setAutoCancel(true)
                        .setStyle(style)//noti 큰 그림 띄우기 용
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentTitle("MH 문구점")
                        .setContentText("전품목 50% 세일!!");
//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build()); //notification 겹치기 id만 바꾸면 된다!!
                stop_sending_coupon[2] = 0; // 비콘3 더 이상 쿠폰 안 보내도록 설정
            }
            else
                return;
        }
        else
            return;
    }//3번 비콘 문구 쿠폰 발송 마지막 줄...

        else if (favorite_item.check_box[3]==1 && Integer.parseInt(beacon_rssi[3]) >= -60) {
            if (stop_sending_coupon[3] == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti2";
                    String Noti_Channel_Group_ID = "Noti_Group2";

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);

                    NotificationCompat.Style style = new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.shoes_coupon));// noti 큰 사진 띄우기

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02))
                            .setSmallIcon(R.drawable.ic_shoes)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setStyle(style)//noti 큰 그림 띄우기 용
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("IPS Shoes Store")
                            .setContentText("지금 사용가능한 쿠폰이 있습니다.");

//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(2, builder.build()); //notification 겹치기 id만 바꾸면 된다!!
                    stop_sending_coupon[3] = 0; // 비콘3 더 이상 쿠폰 안 보내도록 설정
                }
                else
                    return;
            }
            else
                return;
        }//비콘4 신발 쿠폰 발송 마지막 줄...

        else if (favorite_item.check_box[5]==1 && Integer.parseInt(beacon_rssi[5]) >= -60) {
            if (stop_sending_coupon[5] == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti2";
                    String Noti_Channel_Group_ID = "Noti_Group2";

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);

                    String bigText = "초가을 먹거리 대전"
                            + "11월 14일 ~ 11월 18일, 단 4일간"
                            + "샤인 머스켓 청포도(500g): 11,500원 "
                            + "무화과(무농약, 1kg): 11,900원 "
                            + "거봉(2kg): 19,800원 ";

                    NotificationCompat.Style style1 = new NotificationCompat.BigTextStyle()
                    .bigText(bigText);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02))
                            .setSmallIcon(R.drawable.ic_eating)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setStyle(style1)//noti 글자 확대용
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("상엽이네 싱싱청과물")
                            .setContentText("특가 세일 중!");

//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(3, builder.build()); //notification 겹치기 id만 바꾸면 된다!!
                    stop_sending_coupon[5] = 0; // 비콘3 더 이상 쿠폰 안 보내도록 설정
                }
                else
                    return;
            }
            else
                return;
        }//비콘6 과일 메시지 발송 마지막 줄...

        else if (favorite_item.check_box[7]==1 && Integer.parseInt(beacon_rssi[7]) >= -60) {
            if (stop_sending_coupon[7] == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti2";
                    String Noti_Channel_Group_ID = "Noti_Group2";

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);

                    String bigText = "전 품목 10% 할인"
                            + "양배추: 2000원"
                            + "오  이(5개): 2000원"
                            + "상  추(10장): 3400원"
                            + "버  섯(300g): 4000원"
                            + "콩나물(300g): 2500원";

                    NotificationCompat.Style style1 = new NotificationCompat.BigTextStyle()
                    .bigText(bigText);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02))
                            .setSmallIcon(R.drawable.ic_eating)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setStyle(style1)//noti 글자 확대용
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("지훈이네 야채가게")
                            .setContentText("신선가득 슈퍼세일");

//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(4, builder.build()); //notification 겹치기 id만 바꾸면 된다!!
                    stop_sending_coupon[7] = 0; // 비콘8 더 이상 쿠폰 안 보내도록 설정
                }
                else
                    return;
            }
            else
                return;
        }//8번 비콘 야채 메시지 발송 마지막 줄...


        else if (favorite_item.check_box[9]==1 && Integer.parseInt(beacon_rssi[9]) >= -60) {
            if (stop_sending_coupon[9] == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti2";
                    String Noti_Channel_Group_ID = "Noti_Group2";

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);

                    NotificationCompat.Style style = new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.cosmetic_coupon));// noti 큰 사진 띄우기

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02))
                            .setSmallIcon(R.drawable.ic_cosmetic)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setStyle(style)//noti 큰 그림 띄우기 용
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("더페이스샵")
                            .setContentText("더페이스샵 특가 50% 할인!");

//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(5, builder.build()); //notification 겹치기 id만 바꾸면 된다!!
                    stop_sending_coupon[9] = 0; // 비콘10 더 이상 쿠폰 안 보내도록 설정
                }
                else
                    return;
            }
            else
                return;
        }//비콘10번 화장품 쿠폰 발송 마지막 줄...

        else if (favorite_item.check_box[10]==1 && Integer.parseInt(beacon_rssi[10]) >= -60) {
            if (stop_sending_coupon[10] == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti2";
                    String Noti_Channel_Group_ID = "Noti_Group2";

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);

                    NotificationCompat.Style style = new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.meat_coupon2));// noti 큰 사진 띄우기

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02))
                            .setSmallIcon(R.drawable.ic_eating)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setStyle(style)//noti 큰 그림 띄우기 용
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("Alex 정육점")
                            .setContentText("신선한 고기 on SALE!!");

//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(6, builder.build()); //notification 겹치기 id만 바꾸면 된다!!
                    stop_sending_coupon[10] = 0; // 비콘11 더 이상 쿠폰 안 보내도록 설정
                }
                else
                    return;
            }
            else
                return;
        }//11번 비콘 육류 메시지 발송 마지막 줄...

        else if (favorite_item.check_box[12]==1 && Integer.parseInt(beacon_rssi[12]) >= -60) {
            if (stop_sending_coupon[12] == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Toast.makeText(getApplicationContext(), "오레오이상", Toast.LENGTH_SHORT).show();

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti2";
                    String Noti_Channel_Group_ID = "Noti_Group2";

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);

                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                        Toast.makeText(getApplicationContext(), "채널이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "채널이 없어서 만듭니다.", Toast.LENGTH_SHORT).show();
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    notificationManager.createNotificationChannel(notificationChannel);

                    NotificationCompat.Style style = new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.drink_coupon11));// noti 큰 사진 띄우기

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.katok02))
                            .setSmallIcon(R.drawable.ic_drink)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setStyle(style)//noti 큰 그림 띄우기 용
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("Kool Store")
                            .setContentText("오늘만 이 가격! 특가 세일!!");

//                            .setContentIntent(pendingIntent);

//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(7, builder.build()); //notification 겹치기 id만 바꾸면 된다!!
                    stop_sending_coupon[12] = 0; // 비콘3 더 이상 쿠폰 안 보내도록 설정
                }
                else
                    return;
            }
            else
                return;
        }//13번 비콘 음료 쿠폰 발송 마지막 줄...


    }//coupon 발급 함수 끝....


    public void Shoes_Button(View view){ // 신발 클릭 시 링크 이동
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rhadufakfl12.github.io/startbootstrap-shop-homepage-gh-pages/startbootstrap-shop-homepage-gh-pages/index.html"));
        startActivity(myIntent);
    }
    public void Mungu_Button(View view){ // 문구 클릭 시 링크 이동
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dreamdepot.co.kr/shop/?n_media=27758&n_query=%EB%AC%B8%EA%B5%AC%EC%82%AC%EC%9D%B4%ED%8A%B8&n_rank=3&n_ad_group=grp-m001-01-000001108182579&n_ad=nad-a001-01-000000069607894&n_keyword_id=nkw-m001-01-000001131776353&n_keyword=%EB%AC%B8%EA%B5%AC%EC%82%AC%EC%9D%B4%ED%8A%B8&n_campaign_type=1&NaPm=ct%3Dk2ps2828%7Cci%3D0yS0001M3WjrVa3PHuZ9%7Ctr%3Dsa%7Chk%3D90d7744fad1c32991864a5c92ef176f6ee297a4b"));
        startActivity(myIntent);
    }
    public void Cosmetic_Button(View view){ // 화장품 클릭 시 링크 이동
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.thefaceshop.com/mall/index.jsp"));
        startActivity(myIntent);
    }// 문구, 신발, 화장품 가게 링크 연결 버튼

    // 경로 표시부분
    protected class MyView extends View {
        public MyView(Context context)
        {
            super(context);
        }
        public void onDraw(Canvas canvas)
        {
            canvas.drawColor(Color.TRANSPARENT);//투명도

            Paint MyPaint = new Paint(); // Paint 선언
            MyPaint.setStrokeWidth(15f); // 굵기
            MyPaint.setStyle(Paint.Style.FILL); // 펜 스타일
            MyPaint.setColor(Color.RED); // 펜 색깔

            // 선 시작 좌표, 종료 좌표 지정
                if (Will_buy.draw_line[0] == 1) {
                    canvas.drawLine(1060, 660, 1040, 2145, MyPaint);
                    Will_buy.draw_line[0] = 0;
                    Log.v("draw", "실행1");

                }
                if (Will_buy.draw_line[1] == 1) {
                    canvas.drawLine(580, 2310, 1040, 2145, MyPaint);
                    Will_buy.draw_line[1] = 0;
                    Log.v("draw", "실행2");
                    //}
                }
                if (Will_buy.draw_line[2] == 1) {
                    canvas.drawLine(1120, 955, 1120, 1650, MyPaint);
                    Will_buy.draw_line[2] = 0;
                    Log.v("draw", "실행3");
                    //}
                }
                if (Will_buy.draw_line[3] == 1) {
                    canvas.drawLine(1120, 955, 1040,2145 , MyPaint);
                    Will_buy.draw_line[3] = 0;
                    Log.v("draw", "실행4");
                    //}
                }
                if (Will_buy.draw_line[4] == 1) {
                    canvas.drawLine(580, 2210, 800, 2000, MyPaint);
                    Will_buy.draw_line[4] = 0;
                    Log.v("draw", "실행5");
                    //}
                }
                if (Will_buy.draw_line[5] == 1) {
                    canvas.drawLine(1040, 2095, 1120, 1650, MyPaint);
                    Will_buy.draw_line[5] = 0;
                    Log.v("draw", "실행6");
                    //}
                }
                if (Will_buy.draw_line[6] == 1) {
                    canvas.drawLine(800, 2100, 1040, 2145, MyPaint);
                    Will_buy.draw_line[6] = 0;
                    Log.v("draw", "실행7");
                    //}
                }
                if (Will_buy.draw_line[7] == 1) {
                    canvas.drawLine(918, 298, 1100, 615, MyPaint);
                    Will_buy.draw_line[7] = 0;
                    Log.v("draw", "실행8");
                    //}
                }
                if (Will_buy.draw_line[8] == 1) {
                    canvas.drawLine(918, 298, 1060, 660, MyPaint);
                    Will_buy.draw_line[8] = 0;
                    Log.v("draw", "실행9");
                    //}
                }
                if (Will_buy.draw_line[9] == 1) {
                    canvas.drawLine(420, 700, 918, 298, MyPaint);
                    Will_buy.draw_line[9] = 0;
                    Log.v("draw", "실행10");
                    //}
                }
                if (Will_buy.draw_line[10] == 1) {
                    canvas.drawLine(420, 700, 1100, 615, MyPaint);
                    Will_buy.draw_line[10] = 0;
                    Log.v("draw", "실행11");
                    //}
                }
                if (Will_buy.draw_line[11] == 1) {
                    canvas.drawLine(420, 700, 1060, 660, MyPaint);
                    Will_buy.draw_line[11] = 0;
                    Log.v("draw", "실행12");
                    //}
                }
                if (Will_buy.draw_line[12] == 1) {
                    canvas.drawLine(1000, 1340, 1120, 1650, MyPaint);
                    Will_buy.draw_line[12] = 0;
                    Log.v("draw", "실행13");
                    //}
                }
                if (Will_buy.draw_line[13] == 1) {
                    canvas.drawLine(1000, 1340, 1120, 955, MyPaint);
                    Will_buy.draw_line[13] = 0;
                    Log.v("draw", "실행14");
                    //}
                }
                if (Will_buy.draw_line[14] == 1) {
                    canvas.drawLine(1000, 1340, 1040, 2145, MyPaint);
                    Will_buy.draw_line[14] = 0;
                    Log.v("draw", "실행15");
                    //}
                }
                if (Will_buy.draw_line[15] == 1) {
                    canvas.drawLine(1060, 660, 1120, 1650, MyPaint);
                    Will_buy.draw_line[15] = 0;
                    Log.v("draw", "실행16");
                    //}
                }
                if (Will_buy.draw_line[16] == 1) {
                    canvas.drawLine(1060, 660, 1120, 955, MyPaint);
                    Will_buy.draw_line[16] = 0;
                    Log.v("draw", "실행17");
                    //}
                }
                if (Will_buy.draw_line[17] == 1) {
                    canvas.drawLine(1060, 660, 1100, 615, MyPaint);
                    Will_buy.draw_line[17] = 0;
                    Log.v("draw", "실행18");
                    //}
                }
                if (Will_buy.draw_line[18] == 1) {
                    canvas.drawLine(1060, 660, 1000, 1340, MyPaint);
                    Will_buy.draw_line[18] = 0;
                    Log.v("draw", "실행19");
                    //}
                }
            }


        }// 경로표시 선 그려주는 클래스 선언
    }






