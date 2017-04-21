package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

/**
 * Created by wkojiro on 2017/04/21.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DestAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private List<String> mDestList;

    public DestAdapter(Context context) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDestList(List<String> destList) {
        mDestList = destList;
    }




    @Override
    public int getCount() {
        return mDestList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDestList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        TextView textView1 = (TextView) convertView.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) convertView.findViewById(android.R.id.text2);

        // 後でTaskクラスから情報を取得するように変更する
        textView1.setText(mDestList.get(position));

        return convertView;
    }
}
