package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wkojiro on 2017/05/01.
 */

public class DestRecAdapter extends RecyclerView.Adapter<DestItemViewHolder>{

    private final List<DestItem> data;

    public DestRecAdapter(List<DestItem> data) {
        this.data = data;
    }

    @Override
    public DestItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dest, parent, false);
        return new DestItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DestItemViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
