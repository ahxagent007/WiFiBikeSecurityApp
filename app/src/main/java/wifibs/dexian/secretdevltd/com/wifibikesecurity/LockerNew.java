package wifibs.dexian.secretdevltd.com.wifibikesecurity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class LockerNew extends AppCompatActivity {


    private ImageView iv_status,iv_connected,iv_alarm;
    private TextView tv_statusText;//, tv_connected;
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

    //Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //User
    UserData ud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_locker);
        //TESTING
        //startActivity(new Intent(getApplicationContext(),Login.class));
        //TESTING

        IP = getIP();
        port = getPort();
        password = getPassword();
        status = getStatus();
        keyWord = getKeyWord();

        iv_status = findViewById(R.id.IV_newStatus);
        tv_statusText = findViewById(R.id.tv_newStatusText);
        //tv_connected = findViewById(R.id.tv_connected);
        btn_setting = findViewById(R.id.btn_neSetting);
        //btn_Connect = findViewById(R.id.btn_Connect);
        iv_alarm = findViewById(R.id.IV_newMakeBeep);
        iv_connected = findViewById(R.id.IV_newConnected);

        /*Thread ReceiveThread = new Thread(new MyServerThread());
        ReceiveThread.start();*/


        //Connecting to Firebase
        firebaseConnection();


        if(status.equals("Bike01_ON")){
            iv_status.setImageResource(R.drawable.new_locked);
            tv_statusText.setText("Touch to unlock your bike");
        }else if(status.equals("Bike01_OFF")){
            iv_status.setImageResource(R.drawable.new_unlocked);
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

                            AlertDialog.Builder myBuilder = new AlertDialog.Builder(LockerNew.this);
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

                                        LockerNew.ServerTask st = new LockerNew.ServerTask();
                                        st.execute();

                                        storeStatus(status);

                                        iv_status.setImageResource(R.drawable.new_unlocked);
                                        tv_statusText.setText("Touch to lock your bike");

                                        Dialog.cancel();

                                    }else{
                                        Toast.makeText(getApplicationContext(),"Password don't match!",Toast.LENGTH_SHORT).show();
                                        et_password.setText("");
                                    }
                                }
                            });

                        }else{

                            AlertDialog.Builder myBuilder = new AlertDialog.Builder(LockerNew.this);
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

                                        LockerNew.ServerTask st = new LockerNew.ServerTask();
                                        st.execute();

                                        storeStatus(status);

                                        iv_status.setImageResource(R.drawable.new_locked);
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
                        iv_connected.setImageResource(R.drawable.new_disconnected);
                        //tv_connected.setText("Not Connected!");
                        //tv_connected.setTextColor(Color.parseColor("#c10023"));
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

        iv_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockerNew.ServerTaskAlarm sta = new LockerNew.ServerTaskAlarm();
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
            iv_connected.setImageResource(R.drawable.new_connected);
            //tv_connected.setText("Connected!");
            //tv_connected.setTextColor(Color.parseColor("#00911f"));
        }else{
            iv_connected.setImageResource(R.drawable.new_disconnected);
            //tv_connected.setText("Not Connected!");
            //tv_connected.setTextColor(Color.parseColor("#c10023"));
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


    private void storePort(int port){
        SharedPreferences mSharedPreferences = getSharedPreferences("IP_PORT", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("PORT",port);
        mEditor.apply();
    }

    private void storeIP(String IP){
        SharedPreferences mSharedPreferences = getSharedPreferences("IP_PORT", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("IP",IP);
        mEditor.apply();
    }

    private void storeKeyWord(String key){
        SharedPreferences mSharedPreferences = getSharedPreferences("KeyWord", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("KEYWORD",key);
        mEditor.apply();
        Log.i("XIAN","new Password store : "+key);
    }

    private void storePassword(String pass){
        SharedPreferences mSharedPreferences = getSharedPreferences("Password", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("PASSWORD",pass);
        mEditor.apply();
        Log.i("XIAN","new Password store : "+pass);
    }


    public void firebaseConnection(){

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fu = mAuth.getCurrentUser();
        String uID = fu.getUid();

        /*UserData ud = new UserData("XIAN","liya@liya.com","key0001","199.22.66.33",1000,"passs","10 JUNE 2018");

        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(uID);*/


        //Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(uID);


        Log.i("XIAN","FIREBASE DATABASE "+mDatabase.child(uID).getKey());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ud = dataSnapshot.getValue(UserData.class);

                Log.i("XIAN", "usER NAME IS :: " + ud.username);
                Log.i("XIAN"," "+dataSnapshot.toString());

                storeIP(ud.getIp());
                storePort(ud.getPort());
                storeKeyWord(ud.getKey());
                storePassword(ud.getPass());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
