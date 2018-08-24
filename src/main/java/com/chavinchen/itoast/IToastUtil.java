package com.chavinchen.itoast;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Created by ChavinChen on 2018/08/24 23:39
 * EMAIL: <a href="mailto:chavinchen@hotmail.com">chavinchen@hotmail.com</a>
 */
public class IToastUtil {

    private static final int ICON_NONE = 0;

    private static Context mApplicationContext;

    private static IToast sItoast;

    public static void init(Context context, @IToast.STRATEGY int strategy, @NonNull IToast.AppController controller) {
        mApplicationContext = context.getApplicationContext();
        IToast.setup(strategy, controller);
    }

    public static void showShort(@StringRes int strId) {
        show(ICON_NONE, strId, IToast.DURATION.SHORT, IToast.GRAVITY.BOTTOM);
    }

    public static void showShort(CharSequence mes) {
        show(ICON_NONE, mes, IToast.DURATION.SHORT, IToast.GRAVITY.BOTTOM);
    }

    public static void showLong(@StringRes int strId) {
        show(ICON_NONE, strId, IToast.DURATION.LONG, IToast.GRAVITY.BOTTOM);
    }

    public static void showLong(CharSequence mes) {
        show(ICON_NONE, mes, IToast.DURATION.LONG, IToast.GRAVITY.BOTTOM);
    }

    public static void showShort(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId, strId, IToast.DURATION.SHORT, IToast.GRAVITY.BOTTOM);
    }

    public static void showShort(@DrawableRes int iconId, CharSequence mes) {
        show(iconId, mes, IToast.DURATION.SHORT, IToast.GRAVITY.BOTTOM);
    }

    public static void showLong(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId, strId, IToast.DURATION.LONG, IToast.GRAVITY.BOTTOM);
    }

    public static void showLong(@DrawableRes int iconId, CharSequence mes) {
        show(iconId,mes, IToast.DURATION.LONG, IToast.GRAVITY.BOTTOM);
    }

    public static void showShortBottom(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId,strId, IToast.DURATION.SHORT, IToast.GRAVITY.BOTTOM);
    }

    public static void showShortBottom(@DrawableRes int iconId, CharSequence mes) {
        show(iconId,mes, IToast.DURATION.SHORT, IToast.GRAVITY.BOTTOM);
    }

    public static void showShortCenter(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId,strId, IToast.DURATION.SHORT, IToast.GRAVITY.CENTER);
    }

    public static void showShortCenter(@DrawableRes int iconId, CharSequence mes) {
        show(iconId,mes, IToast.DURATION.SHORT, IToast.GRAVITY.CENTER);
    }

    public static void showShortTop(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId,strId, IToast.DURATION.SHORT, IToast.GRAVITY.TOP);
    }

    public static void showShortTop(@DrawableRes int iconId, CharSequence mes) {
        show(iconId,mes, IToast.DURATION.SHORT, IToast.GRAVITY.TOP);
    }

    public static void showLongBottom(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId,strId, IToast.DURATION.LONG, IToast.GRAVITY.BOTTOM);
    }

    public static void showLongBottom(@DrawableRes int iconId, CharSequence mes) {
        show(iconId,mes, IToast.DURATION.LONG, IToast.GRAVITY.BOTTOM);
    }

    public static void showLongCenter(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId,strId, IToast.DURATION.LONG, IToast.GRAVITY.CENTER);
    }

    public static void showLongCenter(@DrawableRes int iconId, CharSequence mes) {
        show(iconId,mes, IToast.DURATION.LONG, IToast.GRAVITY.CENTER);
    }

    public static void showLongTop(@DrawableRes int iconId, @StringRes int strId) {
        show(iconId, strId, IToast.DURATION.LONG, IToast.GRAVITY.TOP);
    }

    public static void showLongTop(@DrawableRes int iconId, CharSequence mes) {
        show(iconId,mes, IToast.DURATION.LONG, IToast.GRAVITY.TOP);
    }

    public static void show(@DrawableRes int iconId, @StringRes int strId,
                            @IToast.DURATION int duration, @IToast.GRAVITY int gravity) {
        show(iconId, mApplicationContext.getText(strId), duration,gravity);
    }

    public static void show(@DrawableRes int iconId, CharSequence mes,
                            @IToast.DURATION int duration, @IToast.GRAVITY int gravity) {
        if(null != sItoast){ // keep no repeat
            sItoast.cancel();
        }
        sItoast = IToast.makeText(mApplicationContext, mes, duration, gravity, iconId).show();
    }

}
