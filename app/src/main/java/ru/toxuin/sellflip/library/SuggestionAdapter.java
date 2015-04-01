package ru.toxuin.sellflip.library;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.toxuin.sellflip.R;

import java.util.List;

public class SuggestionAdapter extends CursorAdapter {
    private List<String> adapterItems;
    private TextView textView;

    public SuggestionAdapter(Context context, Cursor cursor, List<String> items) {
        super(context, cursor, true);
        this.adapterItems = items;
    }

    public String getItemAt(int position) {
        return adapterItems.get(position);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.search_suggestion_item, viewGroup, false);
        textView = (TextView) view.findViewById(R.id.text);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        textView.setText(adapterItems.get(cursor.getPosition()));
    }
}
