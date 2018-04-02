package com.example.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class ReaderAdapter extends RecyclerView.Adapter<ViewHolder>{
    private static final String TAG = ReaderAdapter.class.toString();

    private Cursor mCursor;
    private Context mContext;
    private AdapterItemListener adapterItemListener;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    public ReaderAdapter(Context context, AdapterItemListener adapterItemListener) {
        this.mContext = context;
        this.adapterItemListener = adapterItemListener;
    }

    public ReaderAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        final ViewHolder vh = new ViewHolder(view);
        return vh;

//            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
//            final ViewHolder vh = new ViewHolder(view);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(mContext, ArticleDetailActivity.class);
//                    intent.putExtra(EXTRA_STARTING_ITEM_POSITION,
//                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())));
//                    if (!mIsDetailsActivityStarted) {
//                        mIsDetailsActivityStarted = true;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ArticleListActivity.this,
//                                    vh.thumbnailView, vh.thumbnailView.getTransitionName()).toBundle());
//                        }
//                    } else {
//                        intent = new Intent(Intent.ACTION_VIEW,
//                                ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())));
//                        startActivity(intent);
//                        Log.d(TAG, "intent started to fragment");
//                    }
//                }
//            });
//
//            return vh;

    }


    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }
//            holder.thumbnailView.setImageUrl(
//                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
//                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
//            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        String article_image= mCursor.getString(ArticleLoader.Query.THUMB_URL);
        Picasso.with(mContext)
                .load(article_image)
                .placeholder(R.drawable.empty_detail)
                .error(R.drawable.empty_detail)
                .fit().centerCrop()
                .into(holder.thumbnailView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.thumbnailView.setTransitionName(String.valueOf(position));
        }

        holder.thumbnailView.setTag(String.valueOf(position));
        holder.thumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterItemListener != null) {
                    adapterItemListener.onItemClick(ItemsContract.Items.buildItemUri(getItemId(position)), position, holder.thumbnailView);
                }
            }
        });
    }

    public void swapCursor(final Cursor cursor) {
        this.mCursor = cursor;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();    }

    public interface AdapterItemListener {
        void onItemClick(Uri uri, int position, View v);
    }
}

 class ViewHolder extends RecyclerView.ViewHolder{
    public ImageView thumbnailView;
    public TextView titleView;
    public TextView subtitleView;

    public ViewHolder(View view) {
        super(view);
        thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        titleView = (TextView) view.findViewById(R.id.article_title);
        subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
    }



}