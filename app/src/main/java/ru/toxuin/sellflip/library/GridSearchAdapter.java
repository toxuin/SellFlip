package ru.toxuin.sellflip.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.LayerDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import ru.toxuin.sellflip.BaseActivity;
import ru.toxuin.sellflip.BuildConfig;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.SingleAdFragment;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.views.PrescalableImageView;
import ru.toxuin.sellflip.restapi.spicerequests.SingleAdThumbRequest;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GridSearchAdapter extends BaseAdapter {
    private static final String TAG = "SEARCH_GRID_ADAPTER";
    private Context context;
    private final List<SingleAd> itemsList;

    private final LayoutInflater mLayoutInflater;

    protected SpiceManager spiceManager;

    public GridSearchAdapter(Context context, SpiceManager manager) {
        this.spiceManager = manager;
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.itemsList = new LinkedList<>();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        SingleAd ad = itemsList.get(position);
        SearchResultViewHolder vh = null;
        if (convertView != null) {
            vh = (SearchResultViewHolder) convertView.getTag();
        }

        if (convertView == null || (vh != null && !vh.id.equals(ad.getId()))) {
            convertView = mLayoutInflater.inflate(R.layout.search_result_item, parent, false);
            vh = new SearchResultViewHolder(context, convertView);
            vh.setSpiceManager(spiceManager);
            vh.bind(ad);

            // THUMBNAIL
            if (ad.getVideoWidth() > 0 && ad.getVideoHeight() > 0) {
                float ratio = (float) ad.getVideoWidth() / ad.getVideoHeight();
                vh.thumbnail.setRatio(ratio);
            }

            convertView.setTag(vh);
        }

        return convertView;
    }






    public void clear() {
        itemsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    @Override
    public SingleAd getItem(int position) {
        return itemsList.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(SingleAd ad) {
        if (!itemsList.contains(ad)) itemsList.add(ad);
        notifyDataSetChanged();
    }

    public void addAll(SingleAd.List ads) {
        for (SingleAd ad : ads) {
            add(ad);
        }
    }

    public static class SearchResultViewHolder implements View.OnClickListener, View.OnLongClickListener {
        Context context;
        LinearLayout cardContainer;
        TextView title;
        TextView price;
        TextView date;
        PrescalableImageView thumbnail;
        String id;
        SpiceManager spiceManager;

        public SearchResultViewHolder(Context cntx, View itemView) {
            this.context = cntx;
            this.cardContainer = (LinearLayout) itemView.findViewById(R.id.item_card);
            this.title = (TextView) itemView.findViewById(R.id.item_title);
            this.price = (TextView) itemView.findViewById(R.id.item_price);
            this.date = (TextView) itemView.findViewById(R.id.item_date);
            this.thumbnail = (PrescalableImageView) itemView.findViewById(R.id.item_thumbnail);
            cardContainer.setOnClickListener(this);
            cardContainer.setOnLongClickListener(this);
        }

        public void setSpiceManager(SpiceManager manager) {
            this.spiceManager = manager;
        }

        public void bind(SingleAd ad) {
            // BACKGROUND
            //if (position % 2 == 0) viewHolder.cardContainer.setBackgroundColor(context.getResources().getColor(R.color.even_card));

            // ID
            id = ad.getId();

            // TITLE
            title.setText(ad.getTitle());

            // DATE
            String dateAgo;
            Date past = ad.getDate();
            Date now = new Date();
            long secondsAgo = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutesAgo = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hoursAgo = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            if (secondsAgo < 60) dateAgo = secondsAgo + " " + context.getString(R.string.seconds_ago);
            else if (minutesAgo < 60)
                dateAgo = minutesAgo + " " + context.getString(R.string.minutes_ago);
            else if (hoursAgo < 24) {
                if (hoursAgo == 1) dateAgo = "1 hour ago";
                else dateAgo = hoursAgo + " " + context.getString(R.string.hours_ago);
            }
            else dateAgo = DateFormat.getDateFormat(context.getApplicationContext()).format(past);
            date.setText(dateAgo);

            // PRICE

            if (ad.getPrice() == 0) price.setText("Free");
            else if (ad.getPrice() == -1) price.setText("Please contact");
            else {
                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                price.setText(formatter.format(ad.getPrice()));
            }

            // LOADING:
            thumbnail.setImageResource(R.drawable.loading);
            LayerDrawable progressAnimation = (LayerDrawable) thumbnail.getDrawable();
            ((Animatable) progressAnimation.getDrawable(0)).start();
            ((Animatable) progressAnimation.getDrawable(1)).start();

            // GET ACTUAL THUMBNAIL:
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            int thumbWidth = size.x / context.getResources().getInteger(R.integer.search_result_columns);
            final SingleAdThumbRequest thumbRequest = new SingleAdThumbRequest(context, ad.getId(), thumbWidth);
            spiceManager.execute(thumbRequest, new RequestListener<Bitmap>() {
                @Override
                public void onRequestSuccess(Bitmap bitmap) {
                    if (bitmap != null) thumbnail.setImageBitmap(bitmap);
                    else Log.e(TAG, "ERROR: IMAGE = NULL");
                }

                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    thumbnail.setImageResource(R.drawable.no_image);
                    if (BuildConfig.DEBUG) spiceException.printStackTrace();
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (id == null) return;
            SingleAdFragment adFragment = new SingleAdFragment();
            adFragment.setAdId(id);
            BaseActivity.setContent(adFragment);
        }

        @Override
        public boolean onLongClick(View v) {
            if (id == null) return false;
            SharedPreferences spref = context.getSharedPreferences(context.getString(R.string.app_preference_key), Context.MODE_PRIVATE);
            Set<String> favs = spref.getStringSet("favoriteAds", new HashSet<String>());
            SharedPreferences.Editor edit = spref.edit();
            if (favs.contains(id)) {
                favs.remove(id);
                SuperToast superToast = new SuperToast(context, Style.getStyle(Style.ORANGE, SuperToast.Animations.POPUP));
                superToast.setDuration(SuperToast.Duration.VERY_SHORT);
                superToast.setText("Removed from favorites!");
                superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                superToast.show();
            } else {
                favs.add(id);
                SuperToast superToast = new SuperToast(context, Style.getStyle(Style.PURPLE, SuperToast.Animations.POPUP));
                superToast.setDuration(SuperToast.Duration.VERY_SHORT);
                superToast.setText("Added to favorites!");
                superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                superToast.show();
            }
            edit.putStringSet("favoriteAds", favs);
            edit.apply();
            return true;
        }
    }
}
