package cn.com.dareway.dwandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public interface MyCallBack{
        void onSuccess(Dataobject);
        void onError();
    }

    public MyCallBack myCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            final DataObject result = DataObject.parsJson(response);
            myCallBack.onSuccess(result);

            Volley.doPost(url, new VolleyCallBack(){
                onResponse(){
                    myCallBack.onSuccess(result);
                };
                onError();
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
