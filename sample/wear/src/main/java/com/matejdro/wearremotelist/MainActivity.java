package com.matejdro.wearremotelist;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.matejdro.wearremotelist.parcelables.CompressedParcelableBitmap;
import com.matejdro.wearremotelist.parcelables.StringParcelableWraper;
import com.matejdro.wearremotelist.receiverside.RemoteList;
import com.matejdro.wearremotelist.receiverside.RemoteListListener;
import com.matejdro.wearremotelist.receiverside.RemoteListManager;
import com.matejdro.wearremotelist.receiverside.conn.WatchSingleConnection;

public class MainActivity extends Activity implements RemoteListListener, GoogleApiClient.ConnectionCallbacks
{
    private ListAdapter adapter;

    private GoogleApiClient apiClient;
    private RemoteList<CompressedParcelableBitmap> imageList;
    private RemoteList<StringParcelableWraper> textList;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        WearableListView listView = (WearableListView) findViewById(R.id.list);
        adapter = new ListAdapter();
        listView.setAdapter(adapter);

        //Item change is called frequently which makes our list blink. Disable change animations to fix that.
        listView.getItemAnimator().setSupportsChangeAnimations(false);

        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();
        apiClient.connect();

    }

    @Override
    public void onConnected(Bundle bundle)
    {
        RemoteListManager listManager = new RemoteListManager(new WatchSingleConnection(apiClient), this);

        /*
            Use two lists, one for text that is transferred very fast and one for images that is slower.
            Text one is set to higher priority to make sure user can scroll fast through the list.
            After user stops scrolling, text list stops requesting data and image list transfers all images.
         */
        imageList = listManager.createRemoteList(AppList.IMAGE_PATH, CompressedParcelableBitmap.CREATOR, 200, 5);
        textList = listManager.createRemoteList(AppList.TEXT_PATH, StringParcelableWraper.CREATOR, 1000, 40);
        textList.setPriority(1);
        imageList.setPriority(0);
    }


    @Override
    public void onListSizeChanged(String listPath)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void newEntriesTransferred(String listPath, final int from, final int to)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                adapter.notifyItemRangeChanged(from, to - from + 1);
            }
        });
    }

    @Override
    public void onError(String listPath, @TransferError int errorCode)
    {
        Log.e("MainActivity", "ERROR: " + listPath + " " + errorCode);
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    private class ListAdapter extends WearableListView.Adapter
    {

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            ListItemHolder holder = new ListItemHolder(view);
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            holder.textView = (TextView) view.findViewById(R.id.name);

            return holder;
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder baseHolder, int position)
        {

            ListItemHolder holder = (ListItemHolder) baseHolder;

            StringParcelableWraper text = textList.get(position);

            if (text == null)
            {
                holder.textView.setText(null);
                holder.imageView.setImageDrawable(null);
            }
            else
            {
                holder.textView.setText(text.getString());

                //Make sure there is plenty of text items loaded around in case user scrolls fast.
                textList.fillAround(position, 20);

                CompressedParcelableBitmap image = imageList.get(position);
                if (image == null)
                    holder.imageView.setImageDrawable(null);
                else
                    holder.imageView.setImageBitmap(image.getBitmap());
            }
        }

        @Override
        public int getItemCount()
        {
            if (imageList == null || textList == null)
                return 0;

            return Math.min(textList.size(), imageList.size());
        }
    }

    private static class ListItemHolder extends WearableListView.ViewHolder
    {
        public ImageView imageView;
        public TextView textView;

        public ListItemHolder(View itemView)
        {
            super(itemView);
        }
    }
}
