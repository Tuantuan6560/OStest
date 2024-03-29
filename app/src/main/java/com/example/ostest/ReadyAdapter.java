package com.example.ostest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ReadyAdapter extends BaseAdapter {

    private Context context;
    private List<PCB> list;

    public ReadyAdapter(Context context,List<PCB> list){
        this.context=context;
        this.list=list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PCB pcb= (PCB) getItem(position);
        ViewHolder viewHolder;
        if (convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_ready,parent,false);

            viewHolder=new ViewHolder();
            viewHolder.tv_id=convertView.findViewById(R.id.tv_id);

            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_id.setText("id:"+pcb.id+"\t");
        return convertView;
    }

    static class ViewHolder {
        TextView tv_id;

    }
}
