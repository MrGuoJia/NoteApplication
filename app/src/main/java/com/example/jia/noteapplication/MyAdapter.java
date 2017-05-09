package com.example.jia.noteapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jia.noteapplication.Listener.DeleteListener;
import com.example.jia.noteapplication.Listener.MyOnItemClickListener;
import com.example.jia.noteapplication.Listener.MyOnItemLongClickListener;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by jia on 2017/5/3.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<NoteMessage> messagesList;
    private MyOnItemClickListener itemClickListener=null;
    private MyOnItemLongClickListener itemLongClickListener=null;
    DeleteListener listener;


    static class ViewHolder  extends  RecyclerView.ViewHolder{
        ImageView kind;//拍的照，默认ic_launcher
        TextView msg;//输入标题
        TextView edt_kind;//输入类别
        TextView tv_time;//创建的时间
        TextView edt_plane;//输入备注
        TextView tv_UpdateTime;//获取修改的时间

        public ViewHolder(View itemView) {
            super(itemView);
            kind= (ImageView) itemView.findViewById(R.id.img_kindResult);
            msg= (TextView) itemView.findViewById(R.id.edt_msgResult);
            edt_kind= (TextView) itemView.findViewById(R.id.edt_kindResult);
            tv_time= (TextView) itemView.findViewById(R.id.tv_createTime);
            edt_plane= (TextView) itemView.findViewById(R.id.edt_planeResult);
            tv_UpdateTime= (TextView) itemView.findViewById(R.id.tv_UpdateTime);

        }

    }
    public  MyAdapter(List<NoteMessage> messagesList,DeleteListener listener){
        this.messagesList=messagesList;
        this.listener=listener;

    }
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View n= LayoutInflater.from(parent.getContext()).inflate(R.layout.note_message_result,parent,false);
        RecyclerView.ViewHolder holder=new ViewHolder(n);

        return (ViewHolder) holder;
    }

    @Override
    public void onBindViewHolder(final MyAdapter.ViewHolder holder, final int position) {

        NoteMessage noteMessage=messagesList.get(position);
        holder.msg.setText(noteMessage.getTittle());
        holder.edt_kind.setText(noteMessage.getKind());

        SimpleDateFormat sDateFormat   =   new   SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");//Date类型格式转换String
        String   date   =   sDateFormat.format(noteMessage.getCreate_time());
        String updateTime=sDateFormat.format(noteMessage.getChange_time());
        holder.tv_time.setText(date);
        holder.tv_UpdateTime.setText(updateTime);
        holder.edt_plane.setText(noteMessage.getPlane());
        holder.kind.setImageBitmap(noteMessage.getBitmap());
        if(itemClickListener!=null){/*自定义item的点击事件不为null，设置监听事件*/
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.OnItemClickListener(holder.itemView,holder.getLayoutPosition());
                }
            });
        }
        if(itemLongClickListener != null){//自定义item的长按事件不为null，设置监听事件
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemLongClickListener.OnItemLongClickListener(holder.itemView,holder.getLayoutPosition());
                    return true;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }
    /**
     * 列表点击事件
     *
     * @param itemClickListener
     */
    public void setOnItemClickListener(MyOnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    /**
     * 列表长按事件
     *
     * @param itemLongClickListener
     */
    public void setOnItemLongClickListener(MyOnItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

}