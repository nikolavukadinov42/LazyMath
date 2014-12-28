package sc.lazymath.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import sc.lazymath.R;
import sc.lazymath.entities.WolframAlphaPod;


/**
 * Created by dejan on 27.12.2014..
 */
public class PodExpendableListAdapter extends BaseExpandableListAdapter {

    private List<String> headerData;
    private Map<String, List<WolframAlphaPod>> childData;
    private Context context;

    public PodExpendableListAdapter(Context context, Map<String, List<WolframAlphaPod>> childData, List<String> headerData) {
        this.childData = childData;
        this.headerData = headerData;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return headerData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(headerData.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headerData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(headerData.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String title = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.pod_header, parent, false);
        }
        TextView titleTV = (TextView) convertView.findViewById(R.id.podListHeaderTV);
        titleTV.setTypeface(null, Typeface.BOLD);
        titleTV.setText(title);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        WolframAlphaPod wolframAlphaPod = (WolframAlphaPod) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.pod_content, parent, false);
        }
        TextView contentTV = (TextView) convertView.findViewById(R.id.podContentTV);
        contentTV.setText(wolframAlphaPod.getText());

        ImageView plot = (ImageView) convertView.findViewById(R.id.plotImageView);
        plot.setImageBitmap(wolframAlphaPod.getImage());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}

