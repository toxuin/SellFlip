package ru.toxuin.sellflip.library;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import ru.toxuin.sellflip.R;

import java.util.LinkedList;
import java.util.List;

public class TaggingAdapter<T> extends BaseAdapter implements SpinnerAdapter {
    // Caption, Something
    private final List<Pair<String, T>> content;
    private LayoutInflater inflater;

    public TaggingAdapter(Activity context) {
        content = new LinkedList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(Pair pair) {
        if (!(pair.first instanceof String)) throw new IllegalStateException("Pair must be of type Pair<String, YourObject>");
        else content.add(pair);
    }

    public void clear() {
        content.clear();
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public Pair<String, T> getItem(int position) {
        return content.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public T getItemContents(int position) {
        return content.get(position).second;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View spinView = convertView!=null ? convertView : inflater.inflate(R.layout.button_spinner_item, parent, false);
        TextView tv = (TextView) spinView.findViewById(android.R.id.text1);
        tv.setText(content.get(position).first);
        return spinView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView!=null ? convertView : inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        CheckedTextView tv = (CheckedTextView) view.findViewById(android.R.id.text1);
        tv.setText(content.get(position).first);
        return view;
    }
}
