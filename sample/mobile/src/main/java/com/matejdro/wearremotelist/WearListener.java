package com.matejdro.wearremotelist;

import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.matejdro.wearremotelist.parcelables.CompressedParcelableBitmap;
import com.matejdro.wearremotelist.parcelables.StringParcelableWraper;
import com.matejdro.wearremotelist.providerside.conn.ListWearableListenerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WearListener extends ListWearableListenerService
{
    private List<ImageWithText> appList = new ArrayList<>();
    @Override
    public void onCreate()
    {
        List<ApplicationInfo> installedApps = getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo app : installedApps)
        {
            String name = getPackageManager().getApplicationLabel(app).toString();

            Bitmap icon = null;
            Drawable appIconDrawable = getPackageManager().getApplicationIcon(app);
            if (appIconDrawable instanceof BitmapDrawable)
                icon = ((BitmapDrawable) appIconDrawable).getBitmap();

            appList.add(new ImageWithText(icon, name));
        }

        Collections.sort(appList);

        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent)
    {
        super.onMessageReceived(messageEvent);
    }

    @Override
    public int getRemoteListSize(String listPath)
    {
        if (!listPath.equals(AppList.IMAGE_PATH) && !listPath.equals(AppList.TEXT_PATH))
            return  -1;

        return 10000;
    }

    @Override
    public Parcelable getItem(String listPath, int position)
    {
        int appIndex = position % appList.size();

        switch (listPath)
        {
            case AppList.IMAGE_PATH:
                return new CompressedParcelableBitmap(appList.get(appIndex).getImage());
            case AppList.TEXT_PATH:
                return new StringParcelableWraper(appList.get(appIndex).getText());
            default:
                throw new IllegalStateException("Unknown list: " + listPath + ", this call should not have happened.");
        }

    }

    @Override
    public void onError(String listPath, @TransferError int errorCode)
    {
        Log.e("WearListener", "ERROR: " + listPath + " " + errorCode);
    }
}
