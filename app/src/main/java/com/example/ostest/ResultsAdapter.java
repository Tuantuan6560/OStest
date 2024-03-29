package com.example.ostest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ResultsAdapter extends BaseAdapter {
    private Context context;
    private List<PCB> list;

    public ResultsAdapter(Context context,List<PCB> list){
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
            convertView= LayoutInflater.from(context).inflate(R.layout.item_result,parent,false);
            viewHolder=new ViewHolder();
            viewHolder.tv_id=convertView.findViewById(R.id.tv_id);
            viewHolder.tv_result=convertView.findViewById(R.id.tv_result);

            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_id.setText("id:"+pcb.id+"\t");
        viewHolder.tv_result.setText("结果："+pcb.variables);
        return convertView;
    }
    static class ViewHolder {
        TextView tv_id;
        TextView tv_result;
    }
}
