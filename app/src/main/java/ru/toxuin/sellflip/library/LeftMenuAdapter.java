package ru.toxuin.sellflip.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;

import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.entities.LeftMenuItem;

public class LeftMenuAdapter extends ArrayAdapter<LeftMenuItem> {

    public LeftMenuAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.left_menu_item, parent, false);
            holder = new ViewHolder();
            holder.slide_menu_icon = (FontAwesomeText) convertView.findViewById(R.id.slide_menu_icon);
            holder.slide_menu_title = (TextView) convertView.findViewById(R.id.slide_menu_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.slide_menu_icon.setIcon(getItem(position).getIconName());
        holder.slide_menu_title.setText(getItem(position).getName());

        return convertView;
    }

    static class ViewHolder {
        FontAwesomeText slide_menu_icon;
        TextView slide_menu_title;
    }
}