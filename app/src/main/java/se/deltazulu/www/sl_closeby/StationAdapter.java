package se.deltazulu.www.sl_closeby;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dexter Zetterman on 2017-05-04.
 */

public class StationAdapter extends ArrayAdapter {

    Context context;
    ArrayList<Station> items;

    StationAdapter stationAdapter;

    private class ViewHolder{
        TextView name;
        TextView distance;
    }

    public StationAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<Station> items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View converterView, @NonNull ViewGroup parent){
        ViewHolder viewHolder;
        if(converterView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            converterView = layoutInflater.inflate(R.layout.item_station,null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) converterView.findViewById(R.id.item_name);
            viewHolder.distance = (TextView) converterView.findViewById(R.id.item_distance);
            converterView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) converterView.getTag();
        }
        Station row = this.items.get(position);
        viewHolder.name.setText(row.getName());
        viewHolder.distance.setText("Avstånd från din position: "+row.getDist()+"m");
        return converterView;
    }
}
