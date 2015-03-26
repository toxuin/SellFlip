package ru.toxuin.sellflip.library;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import ru.toxuin.sellflip.BaseActivity;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.SingleAdFragment;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.SearchResultAdapter.SearchResultViewHolder;
import ru.toxuin.sellflip.restapi.ApiConnector;

public class SearchResultAdapter extends Adapter<SearchResultViewHolder> {
    private static final String TAG = "SEARCH_RESULT_ADAPTER";
    private static Context context;
    private final List<SingleAd> itemsList;
    private int totalServerItems = 0;
    public OnScrollListener searchResultsEndlessScrollListener = new OnScrollListener() {
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + ApiConnector.getItemsOnPage())) {
                if (currentPage * ApiConnector.getItemsOnPage() < totalServerItems) {
                    requestData(currentPage);
                    loading = true;
                }
            }
        }
    };
    private LinearLayoutManager layoutManager;

    public SearchResultAdapter(Context context) {
        super();
        SearchResultAdapter.context = context;
        this.itemsList = new LinkedList<>();
    }

    public void requestData(int page) {
        ApiConnector api = ApiConnector.getInstance(context);
        api.requestTopAdsPaged(page, new Callback<List<SingleAd>>() {
            @Override
            public void success(List<SingleAd> allAds, Response response) {
                for (Header header : response.getHeaders()) {
                    try {
                        if (header.getName().equals("X-Total-Items")) {
                            totalServerItems = Integer.parseInt(header.getValue());
                            Log.d(TAG, "GOT TOTAL ITEMS: " + totalServerItems);
                        }
                    } catch (NullPointerException e) {
                        // NO ACTION
                    }
                }
                itemsList.addAll(allAds);
                notifyDataSetChanged();
                Log.d("LIST_ADAPTER", "GOT " + allAds.size() + " ITEMS!");
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new SearchResultViewHolder(
                inflater.inflate(R.layout.search_result_item, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder viewHolder, int position) {
        viewHolder.bind(itemsList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public void setLayoutManager(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public static class SearchResultViewHolder extends ViewHolder implements OnClickListener {
        LinearLayout cardContainer;
        TextView title;
        TextView price;
        TextView date;
        ImageView thumbnail;
        String id;

        public SearchResultViewHolder(View itemView) {
            super(itemView);
            this.cardContainer = (LinearLayout) itemView.findViewById(R.id.item_card);
            this.title = (TextView) itemView.findViewById(R.id.item_title);
            this.price = (TextView) itemView.findViewById(R.id.item_price);
            this.date = (TextView) itemView.findViewById(R.id.item_date);
            this.thumbnail = (ImageView) itemView.findViewById(R.id.item_thumbnail);
            cardContainer.setOnClickListener(this);
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
            long minutesAgo = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long hoursAgo = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            if (secondsAgo < 60) dateAgo = secondsAgo + context.getString(R.string.seconds_ago);
            else if (minutesAgo < 60)
                dateAgo = minutesAgo + context.getString(R.string.minutes_ago);
            else if (hoursAgo < 24) dateAgo = hoursAgo + context.getString(R.string.hours_ago);
            else dateAgo = DateFormat.getDateFormat(context.getApplicationContext()).format(past);
            date.setText(dateAgo);

            // PRICE

            if (ad.getPrice() == 0) price.setText("Free");
            else {
                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                price.setText(formatter.format(ad.getPrice()));
            }

            // THUMBNAIL
            // LOADING:
            thumbnail.setImageResource(R.drawable.loading);
            LayerDrawable progressAnimation = (LayerDrawable) thumbnail.getDrawable();
            ((Animatable) progressAnimation.getDrawable(0)).start();
            ((Animatable) progressAnimation.getDrawable(1)).start();

            // GET ACTUAL THUMBNAIL:
            new BitmapDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);  // ANDROID 3.0+ ONLY
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "CLICKED " + view.getClass().getSimpleName());
            if (id == null) return;
            SingleAdFragment adFragment = new SingleAdFragment();
            adFragment.setAdId(id);
            BaseActivity.setContent(adFragment);
        }
    }
}
