package wifibs.dexian.secretdevltd.com.wifibikesecurity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class Locker extends AppCompatActivity {

    private ImageView iv_status;
    private TextView tv_statusText, tv_connected;
    private String status;
    private String password;;
    private Button btn_setting, btn_Connect, btn_Alarm;
    private String keyWord;

    //Server variable
    private static Socket s;
    private static SocketAddress socketAddress;
    private static PrintWriter printWriter;
    private static String IP;
    private static int port;
    private boolean serverStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker);

        //TESTING
        //startActivity(new Intent(getApplicationContext(),Login.class));
        //TESTING

        IP = getIP();
        port = getPort();
        password = getPassword();
        status = getStatus();
        keyWord = getKeyWord();

        iv_status = findViewById(R.id.iv_status);
        tv_statusText = findViewById(R.id.tv_statusText);
        tv_connected = findViewById(R.id.tv_connected);
        btn_setting = findViewById(R.id.btn_setting);
        //btn_Connect = findViewById(R.id.btn_Connect);
        btn_Alarm = findViewById(R.id.btn_Alarm);

        /*Thread ReceiveThread = new Thread(new MyServerThread());
        ReceiveThread.start();*/

        if(status.equals("Bike01_ON")){
            iv_status.setImageResource(R.drawable.locked);
            tv_statusText.setText("Touch to unlock your bike");
        }else if(status.equals("Bike01_OFF")){
            iv_status.setImageResource(R.drawable.unlocked);
            tv_statusText.setText("Touch to lock your bike");
        }

        //checkServer();

        iv_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.i("XIAN","Line touch isConnected() = "+s.isConnected());
                checkServer();
                //changeUI();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i("XIAN","Server connection status : "+serverStatus);

                if(IP.equals("NULL") || port == -1){
                    Toast.makeText(getApplicationContext(),"IP or PORT is not set!!",Toast.LENGTH_SHORT).show();
                }else{

                    if(serverStatus){

                        if(status.equals("Bike01_ON")){

                            AlertDialog.Builder myBuilder = new AlertDialog.Builder(Locker.this);
                            View myView = getLayoutInflater().inflate(R.layout.custom_password_dialog, null);

                            Button btn_passDone = myView.findViewById(R.id.btn_passDone);
                            final EditText et_password = myView.findViewById(R.id.et_password);


                            myBuilder.setView(myView);
                            final AlertDialog Dialog = myBuilder.create();
                            Dialog.show();

                            btn_passDone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String userPass = et_password.getText().toString();

                                    if(userPass.equals(password)){

                                        status = "Bike01_OFF";

                                        ServerTask st = new ServerTask();
                                        st.execute();

                                        storeStatus(status);

                                        iv_status.setImageResource(R.drawable.unlocked);
                                        tv_statusText.setText("Touch to lock your bike");

                                        Dialog.cancel();

                                    }else{
                                        Toast.makeText(getApplicationContext(),"Password don't match!",Toast.LENGTH_SHORT).show();
                                        et_password.setText("");
                                    }
                                }
                            });

                        }else{

                            AlertDialog.Builder myBuilder = new AlertDialog.Builder(Locker.this);
                            View myView = getLayoutInflater().inflate(R.layout.custom_password_dialog, null);

                            Button btn_passDone = myView.findViewById(R.id.btn_passDone);
                            final EditText et_password = myView.findViewById(R.id.et_password);


                            myBuilder.setView(myView);
                            final AlertDialog Dialog = myBuilder.create();
                            Dialog.show();

                            btn_passDone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String userPass = et_password.getText().toString();

                                    if(userPass.equals(password)){

                                        status = "Bike01_ON";

                                        ServerTask st = new ServerTask();
                                        st.execute();

                                        storeStatus(status);

                                        iv_status.setImageResource(R.drawable.locked);
                                        tv_statusText.setText("Touch to unlock your bike");

                                        Dialog.cancel();

                                    }else{
                                        Toast.makeText(getApplicationContext(),"Password don't match!",Toast.LENGTH_SHORT).show();
                                        et_password.setText("");
                                    }
                                }
                            });

                        }

                    }else{
                        Toast.makeText(getApplicationContext(),"Server is not Connected",Toast.LENGTH_SHORT).show();
                        Log.i("XIAN","Server connection status : "+serverStatus);
                        //checkServer();

                        tv_connected.setText("Not Connected!");
                        tv_connected.setTextColor(Color.parseColor("#c10023"));
                    }

                }
            }
        });


        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent at = new Intent(getApplicationContext(), Setting.class);
                startActivity(at);

            }
        });
/*
        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkServer();

            }
        });

        */

        btn_Alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerTaskAlarm sta = new ServerTaskAlarm();
                sta.execute();
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        this.finish();
        Intent i = getIntent();
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    class ServerTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                s = new Socket(IP,port);                                        //Connect at the socket
                printWriter = new PrintWriter(s.getOutputStream());             //Set the output stream
                printWriter.write(keyWord+"\n");                             //send the message through the socket
                printWriter.flush();
                printWriter.close();

                s.close();

            }catch (IOException e){
                Log.i("XIAN","line 241 "+e);
            }

            return null;
        }

    }

    class ServerTaskAlarm extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                s = new Socket(IP,port);                                        //Connect at the socket
                printWriter = new PrintWriter(s.getOutputStream());             //Set the output stream
                printWriter.write("ALARM"+"\n");                             //send the message through the socket
                printWriter.flush();
                printWriter.close();

                s.close();

            }catch (IOException e){
                Log.i("XIAN","line 241 "+e);
            }

            return null;
        }

    }

    private String getIP(){
        SharedPreferences mSharedPreferences = getSharedPreferences("IP_PORT", MODE_PRIVATE);
        return mSharedPreferences.getString("IP","192.168.10.10");
    }

    private int getPort(){
        SharedPreferences mSharedPreferences = getSharedPreferences("IP_PORT", MODE_PRIVATE);
        return mSharedPreferences.getInt("PORT",80);
    }

    private void storeStatus(String status){
        SharedPreferences mSharedPreferences = getSharedPreferences("IP_PORT", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("STATUS",status);
        mEditor.apply();
    }

    private String getStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences("IP_PORT", MODE_PRIVATE);
        return mSharedPreferences.getString("STATUS","Bike01_OFF");
    }

    private String getPassword(){
        SharedPreferences mSharedPreferences = getSharedPreferences("Password", MODE_PRIVATE);
        return mSharedPreferences.getString("PASSWORD","0000");
    }

    private void changeUI(){

        Log.i("XIAN","Change called");

        if(serverStatus){
            tv_connected.setText("Connected!");
            tv_connected.setTextColor(Color.parseColor("#00911f"));
        }else{
            tv_connected.setText("Not Connected!");
            tv_connected.setTextColor(Color.parseColor("#c10023"));
        }

    }

    private String getKeyWord(){
        SharedPreferences mSharedPreferences = getSharedPreferences("KeyWord", MODE_PRIVATE);
        return mSharedPreferences.getString("KEYWORD","Bike0001");
    }


    public static boolean isReachableByTcp(String host, int port, int timeout) {
        try {

            Socket socket = new Socket(IP,port);
            //SocketAddress socketAddress = new InetSocketAddress(host, port);
            //socket.connect(socketAddress, timeout);
            printWriter = new PrintWriter(socket.getOutputStream());             //Set the output stream
            printWriter.write("CONNECTED\n");                             //send the message through the socket
            printWriter.flush();
            printWriter.close();
            socket.close();
            Log.i("XIAN","SERVER CONNECTED");
            return true;
        } catch (IOException e) {
            Log.i("XIAN", ""+e);
            return false;
        }
    }

    public static boolean isReachableByTcp00(String host, int port, int timeout) {
        try {
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress, timeout);
            socket.close();
            //Log.i("XIAN","Socket Closed");
            return true;
        } catch (IOException e) {
            Log.i("XIAN", ""+e);
            return false;
        }
    }

    private void checkServer(){

        final Handler h = new Handler();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    serverStatus = isReachableByTcp00(IP,port,100);
                    Log.i("XIAN","Server connection status : "+serverStatus);

                } catch (Exception e) {
                    Log.i("XIAN",""+e);
                }

                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if(serverStatus){
                            Toast.makeText(getApplicationContext(),"Server is connected!",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Server is not connected!",Toast.LENGTH_SHORT).show();
                        }

                        changeUI();
                    }
                });

            }
        };
        thread.start();
    }



    /*class MyServerThread implements Runnable{

        Socket rS ;
        ServerSocket ss;
        InputStreamReader isr;
        BufferedReader bfr;
        String messssssss;

        @Override
        public void run() {

            try {
                ss = new ServerSocket(80);
                while(true){
                    s = ss.accept();
                    isr = new InputStreamReader(s.getInputStream());
                    bfr = new BufferedReader(isr);
                    messssssss = bfr.readLine();
                    Log.i("XIAN","FORM SERVER: : : "+messssssss);
                    if(messssssss.equals("CONNECTED")){
                        serverStatus = true;
                    }
                }
            } catch (IOException e) {
                Log.i("XIAN","Line 335 "+e);
            }

        }
    }*/

}
