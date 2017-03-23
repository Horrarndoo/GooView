package com.zyw.horrarndoo.gooview.view;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zyw.horrarndoo.gooview.R;
import com.zyw.horrarndoo.gooview.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Horrarndoo on 2017/3/20.
 */
public class GooViewAapter extends BaseAdapter {
    private Context mContext;
    //记录已经remove的position
    private HashSet<Integer> mRemoved = new HashSet<Integer>();
    private List<String> list = new ArrayList<String>();

    public GooViewAapter(Context mContext, List<String> list) {
        super();
        this.mContext = mContext;
        this.list = list;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_goo, null);
        }
        ViewHolder holder = ViewHolder.getHolder(convertView);
        holder.mContent.setText(list.get(position));
        //item固定小红点layout
        LinearLayout pointLayout = holder.mPointLayout;
        //item固定小红点
        final TextView point = holder.mPoint;

        boolean visiable = !mRemoved.contains(position);
        pointLayout.setVisibility(visiable ? View.VISIBLE : View.GONE);
        if (visiable) {
            point.setText(String.valueOf(position));
            pointLayout.setTag(position);
            GooViewListener mGooListener = new GooViewListener(mContext, pointLayout) {
                @Override
                public void onDisappear(PointF mDragCenter) {
                    super.onDisappear(mDragCenter);
                    mRemoved.add(position);
                    notifyDataSetChanged();
                    Utils.showToast(mContext, "position " + position + " disappear.");
                }

                @Override
                public void onReset(boolean isOutOfRange) {
                    super.onReset(isOutOfRange);
                    notifyDataSetChanged();//刷新ListView
                    Utils.showToast(mContext, "position " + position + " reset.");
                }
            };
            //在point父布局内的触碰事件都进行监听
            pointLayout.setOnTouchListener(mGooListener);
        }
        return convertView;
    }

    static class ViewHolder {

        public ImageView mImage;
        public TextView mPoint;
        public LinearLayout mPointLayout;
        public TextView mContent;

        public ViewHolder(View convertView) {
            mImage = (ImageView) convertView.findViewById(R.id.iv_head);
            mPoint = (TextView) convertView.findViewById(R.id.point);
            mPointLayout = (LinearLayout) convertView.findViewById(R.id.ll_point);
            mContent = (TextView) convertView.findViewById(R.id.tv_content);
        }

        public static ViewHolder getHolder(View convertView) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            return holder;
        }
    }
}
