package kent.group8.senseplateandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class BackgroundWorker extends AsyncTask<String,Void,String> {
    Context context;
    TaskListener taskListener;

    BackgroundWorker(Context context, TaskListener taskListener) {
        this.context = context;
        this.taskListener = taskListener;
    }

    public BackgroundWorker(View.OnClickListener onClickListener, View.OnClickListener onClickListener1) {
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String type = params[0];
            if (type.equals("edamam_api")) {
                String api_url = params[1];
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(api_url).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(String settings) {
        try {
            taskListener.onTaskCompleted(settings);
        } catch (NullPointerException e) {

        }

    }
    public interface TaskListener {
        void onTaskCompleted(String settings);
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
        super.onProgressUpdate(values);
    }
}



