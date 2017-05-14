package com.example.jia.noteapplication.Activity;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jia.noteapplication.MyNoteDatabaseHelper;
import com.example.jia.noteapplication.NoteMessage;
import com.example.jia.noteapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity {
    private EditText etd_phone;//编辑电话
    private EditText edt_code;//编辑验证码
    private TextView tv_sendCode;//发送验证码
    private FloatingActionButton btn_saveToBomb;//保存到Bmob
    private Button btn_return_main;//返回主界面
    private MyNoteDatabaseHelper myNoteDatabaseHelper=new MyNoteDatabaseHelper(this,"NoteDatabase.db",null,1);//数据库名指定NoteDatabase.db
    private int success=0;//用来显示成功添加数据的条数
    private ArrayList<NoteMessage> saveToNet=new ArrayList<>();
    private ProgressDialog progressDialog = null;//为了防止保存到网络上时间过长，而导致ANR，需要弹出进度条
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSqlMessage();//打开数据库，获取所有的数据，存到list中
        initViews();

    }

    private void progressShowUp() {
        progressDialog=ProgressDialog.show(LoginActivity.this, "请稍等...", "保存中...", true);
    }

    private void initViews() {
        etd_phone= (EditText) findViewById(R.id.edt_phone);
        edt_code= (EditText) findViewById(R.id.edt_code);
        tv_sendCode= (TextView) findViewById(R.id.tv_sendCode);
        btn_saveToBomb= (FloatingActionButton) findViewById(R.id.btn_saveToBomb);
        btn_return_main= (Button) findViewById(R.id.btn_return_main);
        tv_sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,"=======",Toast.LENGTH_SHORT).show();
            }
        });
        tv_sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);//让tv_sendCode处于不可点击状态
                timer.start();//开启倒计时

            }
        });
        btn_saveToBomb.setOnClickListener(new View.OnClickListener() { //将数据保存到网络上
            @Override
            public void onClick(View v) { //点击所有数据保存到Bmob平台上
                saveToNet.clear();
                getSqlMessage();
                String phoneNum=etd_phone.getText().toString();//获取编辑框的手机号码,记得用正则表达式验证是否符合手机格式
                String codePass=edt_code.getText().toString();//获取编辑框的验证码,记得证明验证码的正确性
                if(phoneNum.length()==0){
                    Toast.makeText(LoginActivity.this,"电话号码输入不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(codePass.length()==0){
                    Toast.makeText(LoginActivity.this,"验证码输入不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isMobile(phoneNum)!=true){
                    Toast.makeText(LoginActivity.this,"电话号码不合法",Toast.LENGTH_SHORT).show();
                    return;
                }
                progressShowUp();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int x=0;x<saveToNet.size();x++){
                            saveToNet.get(x).save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if(e==null){
                                        success=+1;

                                    }else{
                                        //  Toast.makeText(LoginActivity.this,"添加数据失败",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        Message m=new Message();
                        m.arg1=520;
                        messageListener.sendMessage(m);
                    }
                }).start();

            }
        });

        btn_return_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.finish();//结束本界面，回到主界面
            }
        });

    }


    CountDownTimer timer=new CountDownTimer(10000, 1000) {// millisInFuture 总时长， countDownInterval 时间间隔
        @Override
        public void onTick(long millisUntilFinished) {
            tv_sendCode.setText(millisUntilFinished/1000+"秒后重新发送验证码");
        }

        @Override
        public void onFinish() {
            tv_sendCode.setEnabled(true);
            tv_sendCode.setText("发送验证码");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();//结束时，取消倒计时
    }
    /**
     * 验证手机格式
     */
    public static boolean isMobile(String number) {
    /*
    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、180、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
    */
        String num = "[1][358]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(number)) {
            return false;
        } else {
            //matches():字符串是否在给定的正则表达式匹配
            return number.matches(num);
        }

    }

    private void getSqlMessage() {

        SQLiteDatabase db= myNoteDatabaseHelper.getWritableDatabase();
        Cursor cursor=db.query("Note",null,null,null,null,null,null);
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){ //便利cursor，将数据存到list
            NoteMessage n=new NoteMessage();
            String tittle=cursor.getString(cursor.getColumnIndex("Title"));
            String kind=cursor.getString(cursor.getColumnIndex("Kind"));
            String plane=cursor.getString(cursor.getColumnIndex("Plane"));
            String SqlTime=cursor.getString(cursor.getColumnIndex("Time"));//time是date类型的，需要转换
            String ChangedTime=cursor.getString(cursor.getColumnIndex("ChangedTime"));
            byte[] picture=cursor.getBlob(cursor.getColumnIndex("Picture"));//获取的是图片二进制数据
            Bitmap bmp= BitmapFactory.decodeByteArray(picture, 0, picture.length);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");

            try {
                Date dateTime=format.parse(SqlTime);
                Date change=format.parse(ChangedTime);
                n.setTittle(tittle);
                n.setKind(kind);
                n.setPlane(plane);
                n.setCreate_time(dateTime);
                n.setChange_time(change);
                n.setBitmap(bmp);
                saveToNet.add(n);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        cursor.close();

        //saveToNet.
    }
    private Handler messageListener =new Handler(new Handler.Callback() {

        public boolean handleMessage(Message msg) {
            if(msg.arg1==520){
                progressDialog.dismiss();
                edt_code.setText("");
                Toast.makeText(LoginActivity.this,"共有"+success+"条成功保存到Bmob",Toast.LENGTH_SHORT).show();
                return  true;
            }
            return false;
        }
    });



}
