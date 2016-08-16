package cn.com.dareway.dwandroidlib.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Created by ggg on 2016/8/12.
 */
public class DWTextView extends TextView {
    public DWTextView(Context context) {
        this(context, null);
    }

    public DWTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DWTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置textview显示的指定格式的日期
     * @param date 传入的日期，可以是Date类型的对象；也可以是String类型的对象（字符串）；
     *             还可以是Long类型
     *             当日期为字符串时，字符串的格式为yyyyMMdd或yyyyMMddHHmmss或者HHmmss。
     * @param mask 传入返回生成日期字符串的格式
     */
    public void setDate(Object date, String mask) {
        try {
            String finalText = "";
            Date tempDate = null;
            SimpleDateFormat format = new SimpleDateFormat(mask);
            SimpleDateFormat tempFormat = null;
            if (date instanceof String) {//传入的时间值为字符串类型
                if (((String) date).length() == 8) {//yyyyMMdd
                    tempFormat = new SimpleDateFormat("yyyyMMdd");
                    tempDate = tempFormat.parse((String) date);
                } else if (((String) date).length() == 6) {//HHmmss
                    tempFormat = new SimpleDateFormat("HHmmss");
                    tempDate = tempFormat.parse((String) date);
                } else if (((String) date).length() == 14) {//yyyyMMddHHmmss
                    tempFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    tempDate = tempFormat.parse((String) date);
                }
                finalText = format.format(tempDate);
            } else if (date instanceof Date) {//传入的时间值为Date类型
                finalText = format.format(date);
            } else if (date instanceof Long) {
                //传入的时间值为Long类型
                tempDate = new Date((Long) date);
                finalText = format.format(tempDate);
            }
            this.setText(finalText);
            this.invalidate();
        } catch (Exception e) {
            this.setText(e.getMessage());
            e.printStackTrace();
        }
    }

    /**设置textview显示的指定格式的钱数
     * @param number 传入的钱数，可以是String类型，Double，int 等数字类型
     * @param locale  钱的单位，例如： Locale.CHINA 表示 ￥（元），为null时，不显示单位
     */
    public void setNumber(Object number, Locale locale) {
        String finalText = "";
        NumberFormat format = null;
        if (locale != null) {
            format = NumberFormat.getCurrencyInstance(locale);

        } else {
            format = NumberFormat.getCurrencyInstance();
        }
        Double numberDouble = 0.0;
        if (number instanceof String) {
            numberDouble = Double.parseDouble((String) number);
        } else {
            numberDouble = (Double) number;
        }
        // 把转换后的货币String类型返回
        finalText = format.format(numberDouble);
        if (locale != null) {
            this.setText(finalText);
        } else {
            this.setText(finalText.substring(1));
        }
        this.invalidate();
    }
}
