package cn.com.dareway.dwandroidlib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import cn.com.dareway.dwandroidlib.viewhelper.LoadDialog;


/**
 * 对 volley的StringRequest进行的封装
 * Created by Administrator on 2016/8/16.
 */
public class DWRequest {
    private int method;
    private String url;
    private DWCallBack mCallBack;
    private Context mContext;
    private Map<String, String> parames;
    private LoadDialog dialog;
    private RequestQueue queue;
    private String mCharSet;
    private boolean isShow = true;
    private static final String tokenShareKey = "tokenkey";
    private static final String shortTokenKey = "shortTokenKey";
    private static final String longTokenKey = "longTokenKey";
    private String mLongTokenUrl;
    private SharedPreferences preferences;
    private View view;
    private String methodname;
    private static final String TAG=DWRequest.class.getSimpleName();
    public static int TIMELONG = 300000;

    /**
     * DWRequest的构造方法
     *
     * @param context             ：上下文对象
     * @param method              ：请求方式 （例如：post请求或get请求），
     *                            可从DWRequest.Method中获取（例如：DWRequest.Method.POST)
     * @param url                 :请求的url
     * @param charSet：指定返回数据的字符编码
     * @param cerId：raw文件下证书的id
     */
    public DWRequest(Context context, int method, String url, String charSet, int cerId) {
        this.mContext = context;
        this.method = method;
        this.url = url;
        this.mCharSet = charSet;
        this.parames = new HashMap<>();
        dialog = new LoadDialog();
        queue = Volley.newRequestQueue(context, null, true, cerId);
        preferences = context.getSharedPreferences(tokenShareKey, Context.MODE_APPEND);
    }

    /**
     * 发送参数请求数据,不添加token验证
     *
     * @param callBack
     */
    public void executeWithParam(DWCallBack callBack) {
        this.mCallBack = callBack;
          /*
         使用volley的StringRequest请求网络
         */
        StringRequest request = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                DataObject mData = new DataObject();
                try {
                    mData = DataObject.parseJSON(response);
                } catch (Exception e) {
                    dialog.hideDialog();
                    e.printStackTrace();
                    String eMessage = e.getMessage();
                    mCallBack.Error(eMessage);
                    showToast("网络异常");
                }
                mCallBack.Success(mData);
                dialog.hideDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String eMessage = error.getMessage();
                error.printStackTrace();
                mCallBack.Error(eMessage);
                dialog.hideDialog();
                showToast("网络异常");
            }
        }) {
            @Override
            protected String getParamsEncoding() {
                return "GBK";
            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                return parames;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(TIMELONG,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setCharSet(mCharSet);
        queue.add(request);
        dialog.showDialog(mContext, request, view);
    }

    /**
     * 发送参数请求数据,添加token验证
     * @param LoginClass :登录activity
     * @param longTokenUrl：使用longToken获取shortToken的url
     * @param callBack ：数据返回处理接口
     */
    public void executeWithToken(final Class LoginClass, String longTokenUrl, DWCallBack callBack) {
        this.mCallBack = callBack;
        this.mLongTokenUrl = longTokenUrl;
        methodname="executeWithToken::";
          /*
         使用volley的StringRequest请求网络
         */
        StringRequest request = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.hideDialog();
                DataObject mData = new DataObject();

                Log.i(TAG,"reqestwithToken::"+response.toString());
                try {
                    mData = DataObject.parseJSON(response);
                    String errorCode = mData.getString("errorCode");
                    if (errorCode != null && "8888".equals(errorCode)) {

                        //Token过期
                        getShortTokenByLongToken(LoginClass);

                    } else if (errorCode != null && "9999".equals(errorCode)) {
                        //Token过期
                        getShortTokenByLongToken(LoginClass);

                        //longToken过期
//                        Intent intent = new Intent();
//                        intent.setClass(mContext, LoginClass);
//                        mContext.startActivity(intent);
//                        if(((Activity) mContext)!=null) {
//                            ((Activity) mContext).finish();
//                        }
//                        Toast.makeText(mContext.getApplicationContext(),
//                                "验证信息过期，请重新登录", Toast.LENGTH_SHORT).show();

                    } else {
                        mCallBack.Success(mData);
                    }
                } catch (Exception e) {
                    dialog.hideDialog();
                    e.printStackTrace();
                    String eMessage = e.getMessage();
                    mCallBack.Error(eMessage);
                    showToast("网络异常");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String eMessage = error.getMessage();
                error.printStackTrace();
                mCallBack.Error(eMessage);
                dialog.hideDialog();
                showToast("网络异常");
            }
        }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                getTokenFromHeader(response);

                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String shortToken = preferences.getString(shortTokenKey, "");
                Map<String, String> header = new HashMap<>();
                header.put("Cookie", "TOKEN=" + shortToken);
                return header;
            }

            @Override
            protected String getParamsEncoding() {
                return "GBK";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return parames;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(TIMELONG,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setCharSet(mCharSet);
        queue.add(request);
        dialog.showDialog(mContext, request, view);
    }

    /**
     * 使用 longtoken换取 shorttoken
     * @param LoginClass
     */
    private void getShortTokenByLongToken(final Class LoginClass) {

        final String longToken = preferences.getString(longTokenKey, "");
        StringRequest request = new StringRequest(Request.Method.POST, mLongTokenUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"getShortTokenByLongToken::"+response.toString());
                dialog.hideDialog();
                methodname="getShortTokenByLongToken::";
                try {
                DataObject mData = DataObject.parseJSON(response);
                    String errorCode = mData.getString("errorCode");
                    if (errorCode != null && "8888".equals(errorCode)) {
                        Intent intent = new Intent();
                        intent.setClass(mContext, LoginClass);
                        mContext.startActivity(intent);
                        if(((Activity) mContext)!=null) {
                            ((Activity) mContext).finish();
                        }
                        Toast.makeText(mContext.getApplicationContext(),
                                "验证信息过期，请重新登录", Toast.LENGTH_SHORT).show();


                        //shortToken过期
//                        getShortTokenByLongToken(LoginClass);
                    } else if (errorCode != null && "9999".equals(errorCode)) {
                        //longToken过期
                        Intent intent = new Intent();
                        intent.setClass(mContext, LoginClass);
                        mContext.startActivity(intent);
                        if(((Activity) mContext)!=null) {
                            ((Activity) mContext).finish();
                        }
                        Toast.makeText(mContext.getApplicationContext(),
                                "验证信息过期，请重新登录", Toast.LENGTH_SHORT).show();

                    } else {
                        executeWithToken(LoginClass,mLongTokenUrl,mCallBack);
                    }
                } catch (Exception e) {
                    dialog.hideDialog();
                    e.printStackTrace();
                    String eMessage = e.getMessage();
                    mCallBack.Error(eMessage);
                    showToast("网络异常");
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String eMessage = error.getMessage();
                error.printStackTrace();
                mCallBack.Error(eMessage);
                dialog.hideDialog();
                showToast("网络异常");
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                getTokenFromHeader(response);
                return super.parseNetworkResponse(response);
            }
            @Override
            protected String getParamsEncoding() {
                return "GBK";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString(shortTokenKey,"");
                Map<String, String> header = new HashMap<>();
                header.put("Cookie", "TOKEN=" + token);
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("longtoken", longToken);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(TIMELONG,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setCharSet(mCharSet);
        queue.add(request);
        dialog.showDialog(mContext, request, view);
    }

    /**
     * 添加发送的参数
     *
     * @param key   ：参数键值对的key值
     * @param value ：参数键值对的value值
     * @return
     */
    public DWRequest addParams(String key, String value) {
        if (parames == null) {
            parames = new HashMap<>();
        } else {
            parames.put(key, value);
        }
        return this;
    }

    /**
     * 设置是否显示默认的Toast提示
     *
     * @param isShow
     */
    public void setToastShow(boolean isShow) {
        this.isShow = isShow;
    }

    /**
     * 无发送参数请求网络
     *
     * @param callBack
     */
    public void execute(DWCallBack callBack) {
        this.mCallBack = callBack;
        /*
         使用volley的StringRequest请求网络
         */
        StringRequest request = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                DataObject mData = new DataObject();
                try {
                    mData = DataObject.parseJSON(response);
                } catch (Exception e) {
                    dialog.hideDialog();
                    e.printStackTrace();
                    String eMessage = e.getMessage();
                    mCallBack.Error(eMessage);
                    showToast("网络异常");
                }
                mCallBack.Success(mData);
                dialog.hideDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String eMessage = error.getMessage();
                error.printStackTrace();
                mCallBack.Error(eMessage);
                dialog.hideDialog();
                showToast("网络异常");

            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(TIMELONG,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setCharSet(mCharSet);
        queue.add(request);

        dialog.showDialog(mContext, request, view);
    }

    /**
     * 自定义加载弹出框的view，若不设置，则使用默认的view
     *
     * @param view
     */
    public void setDialogView(View view) {
        this.view = view;
    }

    /**
     * 显示默认的Toast提示
     *
     * @param message
     */
    private void showToast(String message) {
        if (isShow) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 获取返回数据中的token，并保存到sharepreference中
     *
     * @param response
     */
    private void getTokenFromHeader(NetworkResponse response) {
        Map<String, String> headers = response.headers;
//                headers.values("Set-Cookie");
        String shortToken = null;
        String longToken = null;
        for (String key : headers.keySet()) {
            if (key != null && key.contains("Set-Cookie")) {
                if (headers.get(key).contains("LONG_TOKEN") || headers.get(key).contains("SHORT_TOKEN")) {
                    String[] split = headers.get(key).split(";");
                    for (String str : split) {
                        if (str.contains("LONG_TOKEN="))
                            longToken = str.replace("LONG_TOKEN=", "");
                        if (str.contains("SHORT_TOKEN="))
                            shortToken = str.replace("SHORT_TOKEN=", "");
                    }
                }
//                if (headers.get(key).contains("LONG_TOKEN="))
//                    longToken = headers.get(key).replace("LONG_TOKEN=", "");
//                if (headers.get(key).contains("SHORT_TOKEN="))
//                    shortToken = headers.get(key).replace("SHORT_TOKEN=", "");
            }
            SharedPreferences.Editor editor = preferences.edit();
            Log.i(TAG, methodname+"firstquest_shortToken::"+shortToken+";;firstquest_longToken::"+longToken);

            if (shortToken != null) {
                editor.putString(shortTokenKey, shortToken);
                editor.commit();
            }
            if (longToken != null) {
                editor.putString(longTokenKey, longToken);
                editor.commit();
            }

        }
        //TODO TOKEN服务器做法不标准，先拿王居民的进行测试
//        shortToken = "0a6f7e63acb923ba7a859e69d203a4ff";
//        longToken = "6c67bf8c11f943fcf2a67c110cd86131";
    }


    /**
     * 请求网络返回数据时调用的接口
     */
    public interface DWCallBack {

        public void Success(DataObject respones);

        public void Error(String eMessage);
    }


    /**
     * 请求网络的方式
     */
    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

}
