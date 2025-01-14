package noorg.nothing.nope.no.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import noorg.nothing.nope.no.BuildConfig;
import noorg.nothing.nope.no.util.NotificationUtil;
import noorg.nothing.nope.no.util.json.JSONUtils;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        new android.os.Handler().postDelayed(() -> {
            if (!isOnline(context)) return;

            new CheckUpdates().execute();
        }, 60 * 60 * 1000 /*60 min*/);
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private static class CheckUpdates extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String jsonString = JSONUtils.getFileContent(JSONUtils.JSON_LINK).replace("%XPOSED_ZIP%", "");

                String newApkVersion = new JSONObject(jsonString).getJSONObject("apk").getString("version");

                Integer a = BuildConfig.VERSION_CODE;
                Integer b = Integer.valueOf(newApkVersion);

                if (a.compareTo(b) < 0) {
                    NotificationUtil.showInstallerUpdateNotification();
                }
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
            return null;
        }

    }
}
