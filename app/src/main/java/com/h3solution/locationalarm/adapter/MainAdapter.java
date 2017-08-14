package com.h3solution.locationalarm.adapter;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.h3solution.locationalarm.R;
import com.h3solution.locationalarm.activity.CreateAlarmActivity;
import com.h3solution.locationalarm.base.BaseActivity;
import com.h3solution.locationalarm.model.Area;
import com.h3solution.locationalarm.util.H3Application;
import com.h3solution.locationalarm.util.UtilFunctions;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Adapter of MainActivity
 * Created by HHHai on 21-05-2017.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ItemViewHolder> {

    private BaseActivity context;
    private ArrayList<Area> itemList;

    public MainAdapter(BaseActivity context, ArrayList<Area> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        Area area = itemList.get(position);
        holder.layoutItem.setTag(area);
        holder.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Area thisArea = (Area) v.getTag();
                context.startActivity(new Intent(context, CreateAlarmActivity.class));
                EventBus.getDefault().post(thisArea);
            }
        });

        holder.txtItemTitle.setText(area.getTitle());
        holder.swItemMain.setOnCheckedChangeListener(null);
        holder.swItemMain.setChecked(area.isEnabled());
        holder.swItemMain.setTag(area);
        holder.swItemMain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Area thisArea = (Area) buttonView.getTag();

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Area result = realm.where(Area.class)
                        .equalTo("id", thisArea.getId())
                        .findFirst();
                result.setEnabled(isChecked);
                realm.commitTransaction();
                realm.close();


                if (isChecked) {
                    H3Application.getInstance().startGetLocation();
                    UtilFunctions.enabledAreas.add(thisArea);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_item)
        ConstraintLayout layoutItem;
        @BindView(R.id.txt_item_title)
        TextView txtItemTitle;
        @BindView(R.id.sw_item_main)
        Switch swItemMain;

        ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}