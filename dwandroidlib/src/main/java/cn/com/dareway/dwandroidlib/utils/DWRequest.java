package cn.com.dareway.dwandroidlib.utils;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import cn.com.dareway.dwandroidlib.ui.LoadDialog;


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
    private boolean isShow=true;

    /**
     *  DWRequest的构造方法
     * @param context ：上下文对象
     * @param method ：请求方式 （例如：post请求或get请求），
     *               可从DWRequest.Method中获取（例如：DWRequest.Method.POST)
     * @param url :传入的url
     * @param charSet：指定返回数据的字符编码
     * @param cerId：raw文件下证书的id
     */
    public DWRequest(final Context context, int method, String url, String charSet, int cerId) {
        this.mContext = context;
        this.method = method;
        this.url = url;
        this.mCharSet = charSet;
        this.parames = new HashMap<>();
        dialog = new LoadDialog();
        queue = Volley.newRequestQueue(context, null, true, cerId);
    }

    /**
     * 发送参数请求数据
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
            protected Map<String, String> getParams() throws AuthFailureError {
                return parames;
            }
        };
        request.setCharSet(mCharSet);
        queue.add(request);
        dialog.showDialog(mContext, request, null);
    }

    /**
     * 添加发送的参数
     * @param key
     * @param value
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
     * @param isShow
     */
    public void setToastShow(boolean isShow)
    {
        this.isShow=isShow;
    }

    /**
     * 无发送参数请求网络
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
        request.setCharSet(mCharSet);
        queue.add(request);
        dialog.showDialog(mContext, request, null);
    }

    /**
     * 显示默认的Toast提示
     * @param message
     */
   private void showToast(String message)
    {
        if(isShow) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
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
