package cn.com.dareway.dwandroidlib.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * Created by ggg on 2016/8/12.
 * 实现网络加载时弹出弹出框，
 */
public class LoadDialog implements DialogInterface.OnDismissListener {
    private ProgressDialog dialog;
    private Context context;
    private Request request;

    /**
     * 显示弹出框，在Request对象创建之后调用
     *
     * @param context 所在Activity的对象
     * @param request Request的对象
     * @param view    自定义的弹出框的view
     */
    public void showDialog(Context context, Request request, View view) {
        this.context = context;
        this.request = request;
        dialog = new ProgressDialog(context);
        if (view != null) {
            dialog.setView(view);
        }
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    /**
     * 隐藏弹出框
     */
    public void hideDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        /*
        * 隐藏dialog时，取消网络请求*/
        if (request != null) {
            request.cancel();

        }
    }

}
