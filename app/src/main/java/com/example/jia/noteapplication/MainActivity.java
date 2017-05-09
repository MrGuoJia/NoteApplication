package com.example.jia.noteapplication;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.jia.noteapplication.Listener.DeleteListener;
import com.example.jia.noteapplication.Listener.MyOnItemClickListener;
import com.example.jia.noteapplication.Listener.MyOnItemLongClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DeleteListener {
    private FloatingActionButton btn_add;
    private SwipeMenuRecyclerView recyclerView;
    private List<NoteMessage> messagesList=new ArrayList<>();//接收备忘录信息
    private MyAdapter adapter;
    private MyNoteDatabaseHelper myNoteDatabaseHelper;//创建数据库表
    private int itemNum=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        myNoteDatabaseHelper=new MyNoteDatabaseHelper(this,"NoteDatabase.db",null,1);//数据库名指定NoteDatabase.db
        setSupportActionBar(toolbar);//设置顶部为搜索框，并修改文字
        initList();//初始化recyclerView
        initViews();
        getSqlMessage();//获取数据库的信息，使得一打开就展示列表中的信息
        LayoutItemClick();//recyclerView的每个子布局点击事件与recyclerView长按响应事件
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
        btn_add= (FloatingActionButton) findViewById(R.id.btn_add);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,WriteNote.class);
                startActivityForResult(i,1001);
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
                Toast.makeText(MainActivity.this,"搜索的功能，还没做，吐司一下",Toast.LENGTH_SHORT).show();
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
            //*明天将修改的值写入到数据库中，并且把list的数据set一下啊，更新详细见P222*//*
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

}