package com.example.jia.noteapplication.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jia.noteapplication.Listener.DeleteListener;
import com.example.jia.noteapplication.Listener.MyOnItemClickListener;
import com.example.jia.noteapplication.Listener.MyOnItemLongClickListener;
import com.example.jia.noteapplication.MyNoteDatabaseHelper;
import com.example.jia.noteapplication.NoteMessage;
import com.example.jia.noteapplication.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by jia on 2017/5/3.
 */

public class MySearchAdapter extends RecyclerView.Adapter<MySearchAdapter.ViewHolder> {
    private List<NoteMessage> SearchMessagesList;

    static class ViewHolder  extends  RecyclerView.ViewHolder{
        ImageView kind;//拍的照，默认ic_launcher
        TextView msg;//输入标题
        TextView edt_kind;//输入类别
        TextView tv_time;//创建的时间
        TextView edt_plane;//输入备注
        TextView tv_UpdateTime;//获取修改的时间

        public ViewHolder(View itemView) {
            super(itemView);
            kind= (ImageView) itemView.findViewById(R.id.search_img_kindResult);
            msg= (TextView) itemView.findViewById(R.id.search_edt_msgResult);
            edt_kind= (TextView) itemView.findViewById(R.id.search_edt_kindResult);
            tv_time= (TextView) itemView.findViewById(R.id.search_tv_createTime);
            edt_plane= (TextView) itemView.findViewById(R.id.search_edt_planeResult);
            tv_UpdateTime= (TextView) itemView.findViewById(R.id.search_tv_UpdateTime);

        }

    }
    public MySearchAdapter(List<NoteMessage> messagesList){
        SearchMessagesList=messagesList;
    }
    @Override
    public MySearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View n= LayoutInflater.from(parent.getContext()).inflate(R.layout.search_message_result,parent,false);
        RecyclerView.ViewHolder holder=new ViewHolder(n);
        return (ViewHolder) holder;
    }

    @Override
    public void onBindViewHolder(final MySearchAdapter.ViewHolder holder, final int position) {

        NoteMessage noteMessage=SearchMessagesList.get(position);
        holder.msg.setText(noteMessage.getTittle());
        holder.edt_kind.setText(noteMessage.getKind());
        SimpleDateFormat sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");//Date类型格式转换String
        String   date   =   sDateFormat.format(noteMessage.getCreate_time());
        String updateTime=sDateFormat.format(noteMessage.getChange_time());
        holder.tv_time.setText(date);
        holder.tv_UpdateTime.setText(updateTime);
        holder.edt_plane.setText(noteMessage.getPlane());
        holder.kind.setImageBitmap(noteMessage.getBitmap());

    }


    @Override
    public int getItemCount() {
        return SearchMessagesList.size();
    }
}