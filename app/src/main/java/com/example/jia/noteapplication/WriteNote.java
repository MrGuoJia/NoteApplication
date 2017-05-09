package com.example.jia.noteapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WriteNote extends AppCompatActivity {
    private ImageView view;
    private EditText editTittle;
    private EditText editPlane;
    private Button btn_getImage;
    private Button brn_save;
    private static final int CHOOSE_PHOTO=0;
    private Bitmap imgMap;
   // private Spinner spinner;
    private String KIND="未选类型";
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_message);

        initChoices();//初始化下拉框

        initViews();
    }
    private void initChoices() {
        List<String> list=new ArrayList<String>();
        list.add("生活"); list.add("娱乐");  list.add("休闲");  list.add("工作");
        Spinner spinner= (Spinner) findViewById(R.id.spinner1);

        adapter=new ArrayAdapter<String>(WriteNote.this,android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);  /*adapter设置一个下拉列表样式，参数为系统子布局*/

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    KIND=adapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initViews() {

        view= (ImageView) findViewById(R.id.img_kind);
        editTittle= (EditText) findViewById(R.id.edt_msg);
        editPlane= (EditText) findViewById(R.id.edt_plane);
        btn_getImage= (Button) findViewById(R.id.btn_getImage);
        brn_save= (Button) findViewById(R.id.btn_save);
        view.setImageResource(R.mipmap.timg);
        imgMap=convertViewToBitmap(view);//如果不选择相片，则默认为这张图片
        btn_getImage.setOnClickListener(new View.OnClickListener() { //拍照
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });
        brn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String  tittle=editTittle.getText().toString().trim();
                String plane=editPlane.getText().toString().trim();
                if(tittle.length()==0){
                    tittle="无标题";//如果标题不写默认为 tittle= 无标题

                } if(plane.length()==0){
                    plane="无备注";
                }

                if(tittle.length()==0||plane.length()==0 ||imgMap==null){
                    Toast.makeText(WriteNote.this,"请填写备注信息",Toast.LENGTH_SHORT).show();
                    return;
                }


                byte buff[] = new byte[1024*1024];//看你图有多大..自己看着改
                buff = Bitmap2Bytes(imgMap);//这里的LZbitmap是Bitmap类的,跟第一个方法不同

                SimpleDateFormat sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");
                Date time = new Date();
                String   date   =   sDateFormat.format(time);


                //在intent传递时:
                Intent myIntent = new Intent();
                myIntent.putExtra("bitmap",buff);//图像
                myIntent.putExtra("tittle",tittle);//标题
                myIntent.putExtra("date",date);//时间
                myIntent.putExtra("kind",KIND);//类型
                myIntent.putExtra("plane",plane);//详细计划
                WriteNote.this.setResult(1001,myIntent);
                finish();
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = null;
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4及以上系统使用这个方法处理图片
                        bitmap = ImgUtil.handleImageOnKitKat(this, data);        //ImgUtil是自己实现的一个工具类
                        imgMap=bitmap;//用来传图片
                    } else {
                        //4.4以下系统使用这个方法处理图片
                        bitmap = ImgUtil.handleImageBeforeKitKat(this, data);
                        imgMap=bitmap;
                    }
                    view.setImageBitmap(bitmap);
                }
                break;
            default:
                break;
        }
    }
    private byte[] Bitmap2Bytes(Bitmap bm){//将图片转化为2进制数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    public  Bitmap convertViewToBitmap(View view) {//将View转化为Bitmap图片的方法
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }
}
