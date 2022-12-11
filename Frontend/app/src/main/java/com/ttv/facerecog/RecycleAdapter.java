package com.ttv.facerecog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.drive.model.File;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Nandagopal on 12/1/16.
 */
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.GridItemViewHolder> {
    private Context context;
    private List<File> mItemList;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    /**
     * Called when a grid adapter has been called
     *
     * @param context   The context of main activity
     * @param mItemList Detail List of recyclerview grid that contains data
     */
    public RecycleAdapter(Context context, List<File> mItemList) {
        this.context = context;
        this.mItemList = mItemList;
    }


    /**
     * Called when RecyclerView needs a new {@link GridItemViewHolder} of the given type to represent
     * an item.
     * <p/>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p/>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(GridItemViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param position The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(GridItemViewHolder, int)
     */
    @Override
    public GridItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new GridItemViewHolder(itemView, this);
    }


    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link GridItemViewHolder#itemView} to reflect the item at the given
     * position.
     * <p/>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link GridItemViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p/>
     * Override {@link #onBindViewHolder(GridItemViewHolder, int)} instead if Adapter can
     * handle effcient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(GridItemViewHolder holder, int position) {
        File items = mItemList.get(position);
//        new DownloadImageTask(holder.mImage)
//                .execute(items.getIconLink());
        if (items.getMimeType().contains("folder")){
            holder.mImage.setImageResource(R.drawable.folder_img);
        } else {
            holder.mImage.setImageResource(R.drawable.file_img);
        }

        holder.mName.setText("" + items.getName());
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    /**
     * Called when a grid item has been called
     *
     * @param onItemClickListener The view that was clicked.
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(GridItemViewHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }


    public class GridItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mImage ;
        public TextView mName;
        public RecycleAdapter mAdapter;

        public GridItemViewHolder(View itemView, RecycleAdapter mAdapter) {
            super(itemView);
            this.mAdapter = mAdapter;
            mImage = (ImageView) itemView.findViewById(R.id.image_id);
            mName = (TextView) itemView.findViewById(R.id.file_name);
            itemView.setOnClickListener(this);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}