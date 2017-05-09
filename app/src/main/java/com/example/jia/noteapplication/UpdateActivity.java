package com.example.jia.noteapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateActivity extends AppCompatActivity {
    private ImageView view_update;
    private EditText editTittle_update;
    private EditText editPlane_update;
    private Button btn_getImage_update;
    private Button brn_save_update;
    private static final int CHOOSE_PHOTO=0;
    private Bitmap imgMap_update;
  //  private Spinner spinner_update;
    private ArrayAdapter<String> adapter_update;
    private String Tittle="";
    private String Kind="未选类型";
    private String Plane="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        initItemChoose();

       initChoices();
        initViews();
        getItemMessages();


    }


    private void initItemChoose() {//获取传过来的默认下拉框选项的值,与图片
        Intent a=getIntent();
        byte bmpBuff[]= (byte[]) a.getSerializableExtra("bitmap");
        Kind=a.getStringExtra("kind");
        imgMap_update=BitmapFactory.decodeByteArray(bmpBuff, 0, bmpBuff.length);
    }

    private void getItemMessages() {
        Intent cx=getIntent();//将recyclerView 中的值传过来修改

        Tittle=cx.getStringExtra("tittle");
        Kind=cx.getStringExtra("kind");
        Plane=cx.getStringExtra("plane");


        editTittle_update.setText(Tittle);
        editPlane_update.setText(Plane);
        view_update.setImageBitmap(imgMap_update);

    }

    private void initChoices() {
        List<String> list_update=new ArrayList<String>();
        list_update.add("生活"); list_update.add("娱乐");  list_update.add("休闲");  list_update.add("工作");

        Spinner spinner_update= (Spinner) findViewById(R.id.spinner1_update);

        adapter_update=new ArrayAdapter<String>(UpdateActivity.this,android.R.layout.simple_spinner_item,list_update);

        adapter_update.setDropDownViewResource(android.R.layout.simple_spinner_item);
      //adapter设置一个下拉列表样式，参数为系统子布局

        spinner_update.setAdapter(adapter_update);
        for(int i=0;i<list_update.size();i++){//对比传过来的默认下拉框选项，将选项一致
            if(Kind.equals(list_update.get(i))){
                spinner_update.setSelection(i);
            }
        }

        spinner_update.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Kind=adapter_update.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    private void initViews() {
        view_update= (ImageView) findViewById(R.id.img_kind_update);
        editTittle_update= (EditText) findViewById(R.id.edt_msg_update);
        editPlane_update= (EditText) findViewById(R.id.edt_plane_update);
        btn_getImage_update= (Button) findViewById(R.id.btn_getImage_update);
        brn_save_update= (Button) findViewById(R.id.btn_save_update);
        btn_getImage_update= (Button) findViewById(R.id.btn_getImage_update);
        brn_save_update= (Button) findViewById(R.id.btn_save_update);
        btn_getImage_update.setOnClickListener(new View.OnClickListener() { //拍照
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");

                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });
        brn_save_update.setOnClickListener(new View.OnClickListener() {//保存
            @Override
            public void onClick(View v) {
                Tittle=editTittle_update.getText().toString();
                Plane=editPlane_update.getText().toString();
                //在点击时，将title等数据在组建上get到，修改的值
                Intent i = new Intent();
                //将imgMap_update转为二进制
                byte buff[] = new byte[1024*1024];//看你图有多大..自己看着改
                buff = Bitmap2Bytes(imgMap_update);
                SimpleDateFormat sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");
                Date time = new Date();
                String   update   =   sDateFormat.format(time);//取得的是当前的修改时间，记得传回去加到数据库中


                i.putExtra("bitmap",buff);//图像
                i.putExtra("tittle",Tittle);//标题
                i.putExtra("update",update);//修改时间传主activity过去，在1000接收那写入数据库
                i.putExtra("kind",Kind);//类型
                i.putExtra("plane",Plane);//详细计划
                UpdateActivity.this.setResult(1000,i);
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
                        bitmap = ImgUtil.handleImageOnKitKat(this, data); //ImgUtil是自己实现的一个工具类
                        imgMap_update=bitmap;
                    } else {
                        //4.4以下系统使用这个方法处理图片
                        bitmap = ImgUtil.handleImageBeforeKitKat(this, data);
                        imgMap_update=bitmap;

                    }
                    view_update.setImageBitmap(bitmap);//重选的图片
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
}
