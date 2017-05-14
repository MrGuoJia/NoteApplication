package com.example.jia.noteapplication;

import android.graphics.Bitmap;

import java.util.Date;

import cn.bmob.v3.BmobObject;


/**
 * Created by jia on 2017/5/3.
 */

public class NoteMessage extends BmobObject {
    private  String tittle;//备注标题
    private String kind;//备注类型
    private String plane;//备注信息
    private Date Create_time;
    private Date change_time;
    private Bitmap bitmap;
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }



    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getPlane() {
        return plane;
    }

    public void setPlane(String plane) {
        this.plane = plane;
    }
    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public Date getCreate_time() {
        return Create_time;
    }

    public void setCreate_time(Date create_time) {
        Create_time = create_time;
    }

    public Date getChange_time() {
        return change_time;
    }

    public void setChange_time(Date change_time) {
        this.change_time = change_time;
    }
}
