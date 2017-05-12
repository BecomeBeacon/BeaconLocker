package com.example.becomebeacon.beaconlocker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by GW on 2017-04-27.
 */

//리스트관리
public class ItemListViewAdapter extends BaseAdapter {
    /** * ListView에 세팅할 Item 정보들 */
    private List articleList;

    /** * ListView에 Item을 세팅할 요청자의 정보가 들어감 */
    private Context context;

    /** * 생성자 *
     * @param articleList
     * @param context
     * */
    public ItemListViewAdapter(List articleList, Context context) {
        this.articleList = articleList;
        this.context = context;
    }

    /** * ListView에 세팅할 아이템의 갯수 * @return */

    @Override
    public int getCount() {
        return articleList.size();
    }

    /** * position 번째 Item 정보를 가져옴 * @param position * @return */

    @Override
    public Object getItem(int position) {
        return articleList.get(position);
    }

    /** * 아이템의 index를 가져옴 * Item index == position * @param position * @return */

    @Override
    public long getItemId(int position) {
        return position;
    }

    /** * ListView에 Item들을 세팅함 * position 번 째 있는 아이템을 가져와서 converView에 넣은다음 parent에서 보여주면된다.
     * @param position : 현재 보여질 아이템의 인덱스, 0 ~ getCount() 까지 증가
     * @param convertView : ListView의 Item Cell(한 칸) 객체를 가져옴
     * @param parent : ListView
     * @return */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /** * 가장 간단한 방법 * 사용자가 처음으로 Flicking을 할 때, 아래쪽에 만들어지는 Cell(한 칸)은 Null이다. */

        if( convertView == null ) {
            // Item Cell에 Layout을 적용시킬 Inflater 객체를 생성한다.
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

            // Item Cell에 Layout을 적용시킨다.
            // 실제 객체는 이곳에 있다.

            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView tvSubject = (TextView) convertView.findViewById(R.id.tvNickname);
        //TextView tvAuthor = (TextView) convertView.findViewById(R.id.tvIslost);
        TextView tvHitCount = (TextView) convertView.findViewById(R.id.tvMeter);

        ItemData article = (ItemData) getItem(position); tvSubject.setText(article.getNickname());
        //tvAuthor.setText(article.getIslost());
        String str = article.getMeter() + "m";
        tvHitCount.setText(str);
        return convertView;
    }
}
