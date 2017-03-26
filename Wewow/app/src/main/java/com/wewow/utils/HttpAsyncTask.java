package com.wewow.utils;

import android.os.AsyncTask;
import android.util.Pair;

import java.util.List;

/**
 * Created by suncjs on 2017/3/26.
 */

public class HttpAsyncTask extends AsyncTask<Object, Integer, byte[]> {
    public interface TaskDelegate {
        public void taskCompletionResult(byte[] result);
    }

    private TaskDelegate delegate;

    @Override
    protected byte[] doInBackground(Object... objects) {
        String url = (String) objects[0];
        this.delegate = objects.length > 1 ? (TaskDelegate) objects[1] : null;
        WebAPIHelper.HttpMethod method = objects.length > 2 ? (WebAPIHelper.HttpMethod) objects[2] : WebAPIHelper.HttpMethod.GET;
        byte[] buf = objects.length > 3 ? (byte[]) objects[3] : null;
        WebAPIHelper wpi = WebAPIHelper.getWewowWebAPIHelper();
        List<Pair<String, String>> headers = objects.length > 4 ? (List<Pair<String, String>>) objects[4] : null;
        return wpi.callWebAPI(url, method, buf, headers);
    }

    @Override
    protected void onPostExecute(byte[] result) {
        this.delegate.taskCompletionResult(result);
    }

}
