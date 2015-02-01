package com.mioty.fabianbuentello.nrinject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by fabianBuentello on 1/24/15.
 */
public class NodeAdapter extends ArrayAdapter<Nodes> {

    ArrayList<Nodes> nodeList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public NodeAdapter(Context context, int resource, ArrayList<Nodes> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        nodeList = objects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.nName = (TextView) v.findViewById(R.id.nName);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.nName.setText(nodeList.get(position).getnName());
        return v;

    }

    static class ViewHolder {
        public TextView nName;
    }

}