package com.example.jia.noteapplication.Activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.example.jia.noteapplication.Listener.DeleteListener;
import com.example.jia.noteapplication.Listener.MyOnItemClickListener;
import com.example.jia.noteapplication.Listener.MyOnItemLongClickListener;
import com.example.jia.noteapplication.Adapter.MyAdapter;
import com.example.jia.noteapplication.MyNoteDatabaseHelper;
import com.example.jia.noteapplication.NoteMessage;
import com.example.jia.noteapplication.R;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.Bmob;

/*
*百度语音合成用的
* App ID: 9627960

API Key: MKUquSz153Nv5Zn1oMC9ZHw7

Secret Key: 370b696fa2e1ca47f2f7527f990b1c63
* */
public class MainActivity extends AppCompatActivity implements DeleteListener,SpeechSynthesizerListener {
    private FloatingActionButton btn_add;
    private SwipeMenuRecyclerView recyclerView;
    private List<NoteMessage> messagesList=new ArrayList<>();//接收备忘录信息
    private MyAdapter adapter;
    private MyNoteDatabaseHelper myNoteDatabaseHelper;//创建数据库表
    private int itemNum=0;
    private ImageView speak;
    //以下为声音的资源文件
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";
    private static final String LICENSE_FILE_NAME = "temp_license_2017-03-30";
    // 语音合成客户端
    private SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;//用来写资源文件的路径
    private FloatingActionButton btn_send_to_net;//跳转到新界面用 bomb保存到网络
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBmob();//默认初始化Bmob
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        myNoteDatabaseHelper=new MyNoteDatabaseHelper(this,"NoteDatabase.db",null,1);//数据库名指定NoteDatabase.db
        setSupportActionBar(toolbar);//设置顶部为搜索框，并修改文字
        initList();//初始化recyclerView
        initViews();
        getSqlMessage();//获取数据库的信息，使得一打开就展示列表中的信息
        LayoutItemClick();//recyclerView的每个子布局点击事件与recyclerView长按响应事件
        startTTS();//实现语音合成
        initialEnv();
        initGetVoice();
    }

    private void initBmob() {
        Bmob.initialize(this, "6e7349e42c098dd87b16cdad0e3e5912");
    }

    private void initGetVoice() {
        speak= (ImageView) findViewById(R.id.img_speak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
              final View layout = inflater.inflate(R.layout.voice_speak_dialog, (ViewGroup) findViewById(R.id.dialog));
              new AlertDialog.Builder(MainActivity.this).setTitle("当前列表有"+messagesList.size()+"条备忘录").setView(layout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edtNum= (EditText) layout.findViewById(R.id.edt_num);//获取数字，编辑框已经被我设置为只能输入数字的属性
                        int num=Integer.parseInt(edtNum.getText().toString());//list下标从0开始，而我们认为第一个其实是第0个
                        if(num<=0||num>messagesList.size() ){
                            Toast.makeText(MainActivity.this,"备忘录没有这么长哦",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String voice=messagesList.get(num-1).getPlane();
                        if(voice.length()==0){
                            Toast.makeText(MainActivity.this,"备忘录无详细计划，无法转换为语音",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mSpeechSynthesizer.speak(voice);

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"已取消语音合成",Toast.LENGTH_SHORT).show();
                    }
                }).show();

            }
        });

    }

    private void initialEnv() {
        if(mSampleDirPath==null){
            String sdcardPath= Environment.getExternalStorageDirectory().toString();
            mSampleDirPath=sdcardPath + "/" + SAMPLE_DIR_NAME;
        }File file = new File(mSampleDirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    private void LayoutItemClick() {
        adapter.setOnItemClickListener(new MyOnItemClickListener() {
            @Override
            public void OnItemClickListener(View view, int position) {//点击修改
                itemNum=position;
              //  Toast.makeText(MainActivity.this,"这是第"+(position+1)+"个备忘录~,长按点击删除哦",Toast.LENGTH_SHORT).show();
                Intent a=new Intent();
                a.setClass(MainActivity.this,UpdateActivity.class);
                byte buff[] = new byte[1024*1024];//看你图有多大..自己看着改
                buff = Bitmap2Bytes(messagesList.get(position).getBitmap());//这里的LZbitmap是Bitmap类的,跟第一个方法不同
                a.putExtra("bitmap",buff);//图像
                a.putExtra("tittle",messagesList.get(position).getTittle());//标题

                a.putExtra("kind",messagesList.get(position).getKind());//类型
                a.putExtra("plane",messagesList.get(position).getPlane());//详细计划
                startActivityForResult(a,1000);//

            }
        });
        adapter.setOnItemLongClickListener(new MyOnItemLongClickListener() {
            @Override
            public void OnItemLongClickListener(View view, final int position) {//长按删除功能
                final int n=position;
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("确定删除此备忘录吗?");
                builder.setTitle("提示");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db=myNoteDatabaseHelper.getWritableDatabase();
                        /*db.delete("Note","Plane=? and Title=? and Kind=? "
                                ,new String[]{messagesList.get(n).getPlane()
                                        ,messagesList.get(n).getTittle()
                                        ,messagesList.get(n).getKind()} );//多条件删除*/
                        SimpleDateFormat sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");//将时间改为字符格式
                        String   date   =   sDateFormat.format(messagesList.get(n).getCreate_time());
                        db.delete("Note","Time=?",new String[]{date});
                        adapter.notifyDataSetChanged();//
                        messagesList.clear();
                        getSqlMessage();
                        dialog.dismiss();

                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }


    private void initList() {
        recyclerView= (SwipeMenuRecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true); //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        adapter=new MyAdapter(messagesList,MainActivity.this);
        recyclerView.setAdapter(adapter);
    }

    private void initViews() {
        btn_add= (FloatingActionButton) findViewById(R.id.btn_add);//语音合成功能
        speak= (ImageView) findViewById(R.id.img_speak);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,WriteNote.class);
                startActivityForResult(i,1001);
            }
        });
        btn_send_to_net= (FloatingActionButton) findViewById(R.id.btn_sendToSave);
        btn_send_to_net.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(MainActivity.this,"搜索的功能",Toast.LENGTH_SHORT).show();
                Intent search=new Intent(MainActivity.this,SearchActivity.class);
                startActivity(search);//传递到搜索界面，并在该在界面展示搜索的数据
                break;
        }
        return true;
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //接收回调的数据
        super.onActivityResult(requestCode, resultCode, data);



        if(requestCode==1001){
            byte bmpBuff[]= (byte[]) data.getSerializableExtra("bitmap");

            String tittle=data.getStringExtra("tittle");
            String kind=data.getStringExtra("kind");
            String date=data.getStringExtra("date");
            String plane=data.getStringExtra("plane");

            SQLiteDatabase db= myNoteDatabaseHelper.getWritableDatabase();   //生成表
            ContentValues values=new ContentValues();
            values.put("Time",date);
            values.put("Title",tittle);
            values.put("Picture",bmpBuff);//图片以字节的形式存储
            values.put("Kind",kind);
            values.put("Plane",plane);
            values.put("ChangedTime",date);
            db.insert("Note",null,values);//插入数据库
            messagesList.clear();//先清除掉前面生成的所有list的数据，不然展示的时候会变成两倍
            getSqlMessage();
        }
        else if(requestCode==1000){
            SQLiteDatabase db= myNoteDatabaseHelper.getWritableDatabase();   //生成表
            ContentValues values=new ContentValues();
            byte bmpBuff[]= (byte[]) data.getSerializableExtra("bitmap");
            String tittle=data.getStringExtra("tittle");
            String kind=data.getStringExtra("kind");
            String update_date=data.getStringExtra("update");
            String plane=data.getStringExtra("plane");

            Date create_time=messagesList.get(itemNum).getCreate_time();//将创造时间作为主键
            SimpleDateFormat sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");//将时间改为字符格式
            String   create_date   =   sDateFormat.format(create_time);


            values.put("ChangedTime",update_date);
            values.put("Title",tittle);
            values.put("Picture",bmpBuff);//图片以字节的形式存储
            values.put("Kind",kind);
            values.put("Plane",plane);
            db.update("Note",values,"Time=?",new String[]{create_date});
            messagesList.clear();//先清除掉前面生成的所有list的数据，不然展示的时候会变成两倍
            getSqlMessage();
            refresh();

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
            Bitmap bmp=BitmapFactory.decodeByteArray(picture, 0, picture.length);
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
                messagesList.add(n);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        cursor.close();

    }
    private byte[] Bitmap2Bytes(Bitmap bm){//将图片转化为2进制数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    public void refresh() {
        adapter.notifyDataSetChanged();

    }
    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
    private void startTTS() {
        // 获取语音合成对象实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        // 设置context
        mSpeechSynthesizer.setContext(this);
        // 设置语音合成状态监听器
        mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 设置在线语音合成授权，需要填入从百度语音官网申请的api_key和secret_key
        mSpeechSynthesizer.setApiKey("MKUquSz153Nv5Zn1oMC9ZHw7", "370b696fa2e1ca47f2f7527f990b1c63");
        // 设置离线语音合成授权，需要填入从百度语音官网申请的app_id
        mSpeechSynthesizer.setAppId("9627960");
        // 设置语音合成文本模型文件
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE,  mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 设置语音合成声音模型文件
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,  mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 设置语音合成声音授权文件
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE,  mSampleDirPath + "/"
                + LICENSE_FILE_NAME);
        // 获取语音合成授权信息
        AuthInfo authInfo = mSpeechSynthesizer.auth(TtsMode.MIX);
        // 判断授权信息是否正确，如果正确则初始化语音合成器并开始语音合成，如果失败则做错误处理
        if (authInfo.isSuccess()) {
            mSpeechSynthesizer.initTts(TtsMode.MIX);
           // mSpeechSynthesizer.speak("点击语音图标,开始语音读备忘录");
        } else {
            // 授权失败
            Toast.makeText(MainActivity.this,"授权失败",Toast.LENGTH_SHORT).show();
        }
        mSpeechSynthesizer.initTts(TtsMode.MIX); // 引擎初始化tts接口
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
    }
    public void onError(String arg0, SpeechError arg1) {
        // 监听到出错，在此添加相关操作
    }
    public void onSpeechFinish(String arg0) {
        // 监听到播放结束，在此添加相关操作
    }
    public void onSpeechProgressChanged(String arg0, int arg1) {
        // 监听到播放进度有变化，在此添加相关操作
    }
    public void onSpeechStart(String arg0) {
        // 监听到合成并播放开始，在此添加相关操作
    }
    public void onSynthesizeDataArrived(String arg0, byte[] arg1, int arg2) {
        // 监听到有合成数据到达，在此添加相关操作
    }
    public void onSynthesizeFinish(String arg0) {
        // 监听到合成结束，在此添加相关操作
    }
    public void onSynthesizeStart(String arg0) {
        // 监听到合成开始，在此添加相关操作
    }
    /**
     * 将工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    public void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpeechSynthesizer.release();

    }

}