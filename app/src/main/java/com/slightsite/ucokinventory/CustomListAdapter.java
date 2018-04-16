package com.slightsite.ucokinventory;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomListAdapter extends BaseAdapter {

    Activity activity;
    ArrayList customListDataModelArrayList = new ArrayList<>();
    ArrayList customListDescArrayList = new ArrayList<>();
    LayoutInflater layoutInflater = null;

    public CustomListAdapter(Activity activity, ArrayList customListDataModelArray, ArrayList customListDescArrayList){
        this.activity=activity;
        this.customListDataModelArrayList = customListDataModelArray;
        this.customListDescArrayList = customListDescArrayList;
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return customListDataModelArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return customListDataModelArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private static class ViewHolder{
        TextView list_title,list_desc;

    }
    ViewHolder viewHolder = null;


    // this method  is called each time for arraylist data size.
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View vi=view;
        final int pos = position;
        if(vi == null){

            // create  viewholder object for list_rowcell View.
            viewHolder = new ViewHolder();
            // inflate list_rowcell for each row
            vi = layoutInflater.inflate(R.layout.list_view_receipt,null);
            viewHolder.list_title = (TextView) vi.findViewById(R.id.list_title);
            viewHolder.list_desc = (TextView) vi.findViewById(R.id.list_desc);
            /*We can use setTag() and getTag() to set and get custom objects as per our requirement.
            The setTag() method takes an argument of type Object, and getTag() returns an Object.*/
            vi.setTag(viewHolder);
        }else {

            /* We recycle a View that already exists */
            viewHolder= (ViewHolder) vi.getTag();
        }

        viewHolder.list_title.setText(customListDataModelArrayList.get(pos).toString());
        viewHolder.list_desc.setText(customListDescArrayList.get(pos).toString());


        return vi;
    }
}
