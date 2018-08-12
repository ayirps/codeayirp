package Helper;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by User on 11/08/2018.
 */
public class VolleyRequestHandlerSingleton {
    private static VolleyRequestHandlerSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private VolleyRequestHandlerSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyRequestHandlerSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequestHandlerSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
