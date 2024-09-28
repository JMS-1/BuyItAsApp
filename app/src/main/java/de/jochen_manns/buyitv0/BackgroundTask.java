package de.jochen_manns.buyitv0;

import android.app.Activity;

// Replacement for deprecated AsyncTask.
public abstract class BackgroundTask<TResult> {
    // The activity we are working for.
    protected final Activity Context;

    public BackgroundTask(Activity activity) {
        this.Context = activity;
    }

    // Start processing in background using a separate thread.
    public void execute() {
        new Thread(() -> {
            // Calculate result in separate thread.
            TResult result = doInBackground();

            // But process on the UI thread.
            Context.runOnUiThread(() -> onPostExecute(result));
        }).start();
    }

    // Calculate the result on a background thread.
    protected abstract TResult doInBackground();

    // Process the result on the UI thread.
    protected abstract void onPostExecute(TResult result);

}