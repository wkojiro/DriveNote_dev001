package jp.techacademy.wakabayashi.kojiro.drivenote_dev001;

/**
 * Created by wkojiro on 2017/04/21.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DestAdapter extends BaseAdapter {

    private SettingActivity activity;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Dest> mDestArrayList;
    private Dest RailsRealm;
    private Dest checked_id;
    private Integer rails_id = -1;
    private Integer selected_id = -1;




    public DestAdapter(Context context, SettingActivity activity) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.activity = activity;
    }

    protected void setDestArrayList(ArrayList<Dest> destArrayList){
        mDestArrayList = destArrayList;
    }




    @Override
    public int getCount() {
        return mDestArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDestArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_dests, parent ,false);
        }

        final TextView textView1 = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView textView2 = (TextView) convertView.findViewById(R.id.addressTextView);
        TextView textView3 = (TextView) convertView.findViewById(R.id.emailTextView);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

        textView1.setText(mDestArrayList.get(position).getDestName() + mDestArrayList.get(position).getPositionId()+ mDestArrayList.get(position).getId());
        textView2.setText(mDestArrayList.get(position).getDestAddress());
        textView3.setText(mDestArrayList.get(position).getDestEmail());

        //memo: 現在保存されている目的地がどれかを確認し、該当するところにCheckする。
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.activity);

        //memo: 現状はPositionKeyを保存しているから、これで条件分岐しているが本来はRailsIdで検索すべき。
        //memo: もし、すでに保存されている目的地があるのであれば、該当する目的地にチェックをつける
        if(sp.getString(Utils.DestnameKEY,"") != "" ){

            rails_id = sp.getInt(Utils.RailsKEY,-1);

            // rails_id が含まれるmDestArrayListの案件を検索する。
            // その案件のpositionを確認する。
            if(mDestArrayList.get(position).getId() == rails_id){
                checkBox.setChecked(true);
            } else {

                checkBox.setChecked(false);
            }
        }

        //memo: CheckBoxが選択された段階で、わかることは何番めがクリックされた、というだけ。
        // ここからどの案件と特定するには、やはり、Position_idで比較するしかない？
        /*



         */
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;

                if(cb.isChecked())
                {
                    //  selected_id = position ;

                    // cb.setChecked(false);

                    // Realm realm = Realm.getDefaultInstance();

                    //memo: destIdで検索して当該のデータを取得 positionは０はじまり、position_idは１はじまりだから＋１する。
                    //memo: Checkされた順番が１だったら、Position_idが１のものもを取得する。
                    //memo: つまり、昇順降順が変わってもPositionは変わらないから、選択したものと異なるものが返ってきてしまう。

                    /*
                     Realm  ASENDING
                       ID  Position_id   ListViewの物理的なPosition
                       　　　　　　　　　　　　
                       1      1　　　　　　　　　　1
                       4      2                 2
                       5      3                 3
                       8      4                 4

                       現状は、positionが１だからPosition_idが１と一致するから、１を導き出す。


                      Realm  DESENDING
                       ID  Position_id   ListViewの物理的なPosition
                       　　　　　　　　　　　　
                       8      4　　　　　　　　　　1 本来であれば「８」を読んで欲しいが「１」を読んでしまう。
                       5      3                 2
                       4      2                 3
                       1      1                 4



                     */

                    //これが正解
                    selected_id = mDestArrayList.get(position).getId();
                    Log.d("destID", String.valueOf(selected_id));



                    /*
                    RailsRealm = realm.where(Dest.class).equalTo("id", selected_id + 1 ).findFirst();
                    realm.close();
                    Log.d("RailsRealm", "物理的なPosition"+position +"+1とRealmで保持しているposition_id"+String.valueOf(RailsRealm.getPositionId())+"が一致した案件は、"+String.valueOf(RailsRealm.getId())+"であった。");
                    Log.d("RailsRealm", String.valueOf(RailsRealm.getId()));
                    Log.d("RailsRealm", String.valueOf(RailsRealm));
                   selected_id = RailsRealm.getId();
                   */


                    activity.addDestination(selected_id);

                }
                else
                {

                    selected_id = -1;
                }
                notifyDataSetChanged();
            }
        });

        return convertView;




    }
}
