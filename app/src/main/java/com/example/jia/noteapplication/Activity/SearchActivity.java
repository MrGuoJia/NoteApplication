package com.example.jia.noteapplication.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jia.noteapplication.Adapter.MyAdapter;
import com.example.jia.noteapplication.Adapter.MySearchAdapter;
import com.example.jia.noteapplication.MyNoteDatabaseHelper;
import com.example.jia.noteapplication.NoteMessage;
import com.example.jia.noteapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText edt_msg;
    private ImageView search_img;
    private RecyclerView search_recyclerView;
    private Button btn_return;
    private List<NoteMessage> SearchMessagesList=new ArrayList<>();//接收备忘录检索信息
    private MyNoteDatabaseHelper myNoteDatabaseHelper=new MyNoteDatabaseHelper(this,"NoteDatabase.db",null,1);//数据库名指定NoteDatabase.db
    private MySearchAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();
        getSearch();//获取查询的信息来检索
    }



    private void initViews() {
        edt_msg= (EditText) findViewById(R.id.edt_search);
        search_img= (ImageView) findViewById(R.id.img_search);
        search_recyclerView= (RecyclerView) findViewById(R.id.search_recyclerView);
        btn_return= (Button) findViewById(R.id.btn_return);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        search_recyclerView.setLayoutManager(layoutManager);
        search_recyclerView.setHasFixedSize(true); //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        adapter=new MySearchAdapter(SearchMessagesList);
        search_recyclerView.setAdapter(adapter);
    }

    private void getSearch() {
        search_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchMessagesList.clear();//每次点击查询，先把列表清空
                String msg=edt_msg.getText().toString().trim();
                if(msg.length()==0){
                    Toast.makeText(SearchActivity.this,"请输入检索信息",Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("=========","没有-2");
                SQLiteDatabase db=myNoteDatabaseHelper.getWritableDatabase();
                String sql  = " Title like ? or Kind like ? or Plane like ? ";//多条件模糊查询

                String [] selectionArgs  = new String[]{"%" + msg + "%",
                        "%" + msg + "%",
                        "%" + msg + "%"};
             Cursor cursor=db.query("Note",null,sql,selectionArgs,null,null,null);
                if (cursor==null){
                    Log.e("=========","没有符合检索的数据");
                    return;
                }
                for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
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
                        SearchMessagesList.add(n);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
                cursor.close();
                if(SearchMessagesList.size()==0){
                    Toast.makeText(SearchActivity.this,"无符合数据",Toast.LENGTH_SHORT).show();
                }else if(SearchMessagesList.size()>=1){
                    Toast.makeText(SearchActivity.this,"搜索到"+SearchMessagesList.size()+"条数据",Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }
        });

        btn_return.setOnClickListener(new View.OnClickListener() {//返回主界面
            @Override
            public void onClick(View v) {
                Intent returnMain=new Intent(SearchActivity.this,MainActivity.class);
                startActivity(returnMain);
                SearchActivity.this.finish();
            }
        });

    }


}
