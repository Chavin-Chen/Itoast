package com.chavinchen.itoast;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ChavinChen on 2018/08/25 0:31
 * EMAIL: <a href="mailto:chavinchen@hotmail.com">chavinchen@hotmail.com</a>
 */
public class IToastView extends DialogFragment implements DialogInterface.OnShowListener{

    private static final String KEY_MESSAGE = "KEY_MESSAGE";
    private static final String KEY_DURATION = "KEY_DURATION";
    private static final String KEY_GRAVITY = "KEY_GRAVITY";
    private static final String KEY_ICON = "KEY_ICON";

    private static final CharSequence DEFAULT_MESSAGE = "Empty Message! {.Chavin}";

    public static final String TAG = "I_TOAST";


    public static IToastView newInstance(@NonNull IToast.RequestArgument argument) {
        if (TextUtils.isEmpty(argument.message)) {
            argument.message = DEFAULT_MESSAGE;
        }
        IToastView iToastView = new IToastView();

        Bundle args = new Bundle();
        args.putCharSequence(KEY_MESSAGE, argument.message);
        args.putInt(KEY_DURATION, argument.duration);
        args.putInt(KEY_GRAVITY, argument.gravity);
        args.putInt(KEY_ICON, argument.icon);
        iToastView.setArguments(args);

        iToastView.setCustomView(argument.view);
        iToastView.setListener(argument.listener);

        iToastView.setArgument(argument);
        return iToastView;
    }


    static final long LENGTH_LONG = 3500L;
    static final long LENGTH_SHORT = 2000L;

    private static final int WHAT_HIDE = 0x001;
    private Handler mHandler;

    private IToast.RequestArgument mArgument;

    @Nullable
    private View mCustomView;

    private ToastDisappearListener mListener;

    private boolean mActive = true;
    private boolean mIsShow = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_INPUT, 0);
        setCancelable(false);
        setShowsDialog(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != mCustomView) {
            return mCustomView;
        }

        CharSequence mes = DEFAULT_MESSAGE;

        @DrawableRes
        int icon = 0;

        @SuppressLint("ShowToast")
        Toast toast = Toast.makeText(getActivity(), mes, Toast.LENGTH_SHORT);

        Bundle args = getArguments();
        if (null != args) {
            mes = args.getCharSequence(KEY_MESSAGE, DEFAULT_MESSAGE);
            icon = args.getInt(KEY_ICON, 0);
        }

        toast.setText(mes);

        View view = toast.getView();
        TextView textView = (TextView) view.findViewById(android.R.id.message);
        if (0 != icon) {
            Drawable dwLeft = ContextCompat.getDrawable(view.getContext(), icon);
            if (null != dwLeft) {
                dwLeft.setBounds(0, 0, (int) textView.getTextSize(), (int) textView.getTextSize());
                textView.setCompoundDrawables(dwLeft, null, null, null);
                textView.setCompoundDrawablePadding((int) textView.getTextSize() / 2);
            }
        }

        return view;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (null != window) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER_HORIZONTAL;

            @SuppressLint("ShowToast")
            Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
            int mYOffset = toast.getYOffset();

            int gravity = IToast.GRAVITY.BOTTOM;
            Bundle args = getArguments();
            if (null != args) {
                gravity = args.getInt(KEY_GRAVITY, IToast.GRAVITY.BOTTOM);
            }
            switch (gravity) {
                case IToast.GRAVITY.BOTTOM:
                    params.gravity = params.gravity | Gravity.BOTTOM;
                    params.y = mYOffset;
                    break;
                case IToast.GRAVITY.CENTER:
                    params.gravity = params.gravity | Gravity.CENTER_VERTICAL;
                    break;
                case IToast.GRAVITY.TOP:
                    params.gravity = params.gravity | Gravity.TOP;
                    params.y = mYOffset;
                    break;
            }
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = android.R.style.Animation_Toast;
            params.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;// 1000
        }
        dialog.setOnShowListener(this);
        return dialog;
    }


    @Override
    public void onPause() {
        super.onPause();
        clearWindowAnim();
    }

    @Override
    public void onStop() {
        super.onStop();
        clearDuration();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mIsShow = true;
        if(!mActive){
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (null != mListener) {
            mListener.onDismiss(mArgument);
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        applyDuration();
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        int res = super.show(transaction, tag);
        applyDuration();
        return res;
    }

    public IToastView show(FragmentManager manager, boolean active) {
        mActive = active;
        show(manager, TAG);
        return this;
    }

    public void cancel() {
        mActive = false;
        dismissAllowingStateLoss();
    }

    private void applyDuration() {
        if (null == mHandler) {
            mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == WHAT_HIDE) {
                        dismissAllowingStateLoss();
                        return true;
                    }
                    return false;
                }
            });
        }

        int duration = IToast.DURATION.SHORT;
        Bundle args = getArguments();
        if (null != args) {
            duration = args.getInt(KEY_DURATION, duration);
        }
        switch (duration) {
            case IToast.DURATION.LONG:
                mHandler.sendEmptyMessageDelayed(WHAT_HIDE, LENGTH_LONG);
                break;
            case IToast.DURATION.SHORT:
                mHandler.sendEmptyMessageDelayed(WHAT_HIDE, LENGTH_SHORT);
                break;
        }
    }

    private void clearDuration() {
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        dismissAllowingStateLoss();
    }

    private void clearWindowAnim() {
        Dialog dialog = getDialog();
        if (null != dialog) {
            Window window = dialog.getWindow();
            if (null != window) {
                window.setWindowAnimations(0);
            }
        }
    }

    private void setArgument(IToast.RequestArgument argument) {
        mArgument = argument;
    }

    private void setCustomView(View view) {
        mCustomView = view;
    }

    private void setListener(ToastDisappearListener listener) {
        mListener = listener;
    }

    interface ToastDisappearListener {
        void onDismiss(IToast.RequestArgument arg);
    }

}