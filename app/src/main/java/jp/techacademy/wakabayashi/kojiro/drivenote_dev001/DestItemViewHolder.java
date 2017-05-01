package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by wkojiro on 2017/05/01.
 */

public class DestItemViewHolder extends RecyclerView.ViewHolder {
    private final TextView emailTextView;
    private final TextView nameTextView;
    private final TextView addressTextView;
    private final CheckBox checkBox;

    public DestItemViewHolder(View itemView) {
        super(itemView);

        emailTextView = (TextView) itemView.findViewById(R.id.emailTextView);
        nameTextView = (TextView) itemView.findViewById(R.id.emailTextView);
        addressTextView = (TextView) itemView.findViewById(R.id.addressTextView);
        checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
    }

    public void bind(DestItem item) {
        emailTextView.setText(item.email);
        nameTextView.setText(item.destname);
        addressTextView.setText(item.destname);
        checkBox.setChecked(false);
    }
}
