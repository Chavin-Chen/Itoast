package com.chavinchen.itoast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.SoftReference;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by ChavinChen on 2018/08/24 23:39
 * EMAIL: <a href="mailto:chavinchen@hotmail.com">chavinchen@hotmail.com</a>
 */
public class IToast {

    private Request mRequest;
    private IToastView mIToastView;

    private IToast() {
    }

    private void init(Context context, RequestArgument argument) {
        mRequest = new Request(context, argument);
    }

    // ====================================== public static ========================================

    @IntDef({DURATION.SHORT, DURATION.LONG})
    @Retention(RetentionPolicy.CLASS)
    public @interface DURATION {
        int SHORT = 0;
        int LONG = 1;
    }

    @IntDef({GRAVITY.TOP, GRAVITY.CENTER, GRAVITY.BOTTOM})
    @Retention(RetentionPolicy.CLASS)
    public @interface GRAVITY {
        int TOP = 1;
        int CENTER = 2;
        int BOTTOM = 3;
    }

    @IntDef({STRATEGY.ANDROID_FIRST, STRATEGY.CUSTOM_FIRST})
    @Retention(RetentionPolicy.CLASS)
    public @interface STRATEGY {
        int ANDROID_FIRST = 0;
        int CUSTOM_FIRST = 1;
    }

    /**
     * change strategy
     *
     * @param strategy   {@link STRATEGY} first use {@link STRATEGY#ANDROID_FIRST}Android or
     *                   {@link STRATEGY#ANDROID_FIRST} Custom
     * @param controller {@link AppController} null of a controller that can getTopActivity
     */
    public static void setup(@STRATEGY int strategy, @Nullable AppController controller) {
        ConfigHolder.mStrategy = strategy;
        ConfigHolder.mAppController = controller;
    }

    public static IToast makeText(@NonNull Context context, @StringRes int strId,
                                  @DURATION int duration) {
        CharSequence mes = context.getApplicationContext().getText(strId);
        return makeText(context, mes, duration);
    }

    public static IToast makeText(@NonNull Context context, CharSequence message,
                                  @DURATION int duration) {
        return makeText(context, message, duration, GRAVITY.BOTTOM, 0);
    }

    public static IToast makeText(@NonNull Context context, @StringRes int strId,
                                  @DURATION int duration,
                                  @GRAVITY int gravity) {
        CharSequence mes = context.getApplicationContext().getText(strId);
        return makeText(context, mes, duration, gravity);
    }

    public static IToast makeText(@NonNull Context context, CharSequence message,
                                  @DURATION int duration,
                                  @GRAVITY int gravity) {
        return makeText(context, message, duration, gravity, 0);
    }


    public static IToast makeText(@NonNull Context context, CharSequence message,
                                  @DURATION int duration,
                                  @GRAVITY int gravity,
                                  @DrawableRes int icon) {
        final IToast toast = new IToast();
        RequestArgument argument = new RequestArgument(message, duration, gravity, icon, null, new IToastView.ToastDisappearListener() {
            @Override
            public void onDismiss(RequestArgument arg) {
                toast.response();
            }
        });
        toast.init(context, argument);
        return toast;
    }

    public static IToast makeView(@NonNull Context context,
                                  @DURATION int duration,
                                  @NonNull View view) {
        return makeView(context, duration, GRAVITY.BOTTOM, view);
    }

    public static IToast makeView(@NonNull Context context,
                                  @DURATION int duration,
                                  @GRAVITY int gravity,
                                  @NonNull View view) {
        final IToast toast = new IToast();
        RequestArgument argument = new RequestArgument("", duration, gravity, 0, null, new IToastView.ToastDisappearListener() {
            @Override
            public void onDismiss(RequestArgument arg) {
                toast.response();
            }
        });
        toast.init(context, argument);
        return toast;
    }

    public interface AppController {
        FragmentActivity getTopActivity();
    }

    // ====================================== public ===============================================

    public IToast show() {
        if (null == mRequest || ConfigHolder.mRequests.contains(mRequest)) {
            return this;
        }
        request(mRequest);
        return this;
    }

    public void cancel() {
        if (null != mRequest && null != mIToastView) {
            if (mRequest.equals(ConfigHolder.mRequests.peek())) {
                mIToastView.cancel();
                mRequest = null;
            } else {
                mRequest.active = false;
                mRequest = null;
            }
        }
    }

    // ===================================== private ===============================================


    private void request(Request request) {
        ConfigHolder.mRequests.offer(request);
        if (!ConfigHolder.mRequests.isEmpty() && !ConfigHolder.mIsApplying) {
            apply();
        }
    }

    private void apply() {
        ConfigHolder.mIsApplying = true;
        final Request request = ConfigHolder.mRequests.peek();
        final Context context;
        if (null == request
                || null == request.context
                || null == (context = request.context.get())
                || !request.active) {
            response();
            return;
        }
        if (STRATEGY.ANDROID_FIRST == ConfigHolder.mStrategy) {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                applyAToast(context, request);
            } else if (context instanceof FragmentActivity) {
                applyCToast((FragmentActivity) context, request);
            } else {
                if (null != ConfigHolder.mAppController && null != ConfigHolder.mAppController.getTopActivity()) {
                    applyCToast(ConfigHolder.mAppController.getTopActivity(), request);
                }
            }
        } else {
            if (context instanceof FragmentActivity) {
                applyCToast((FragmentActivity) context, request);
            } else if (null != ConfigHolder.mAppController
                    && null != ConfigHolder.mAppController.getTopActivity()) {

                applyCToast(ConfigHolder.mAppController.getTopActivity(), request);

            } else if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                applyAToast(context, request);
            }
        }
    }

    private void response() {
        ConfigHolder.mIsApplying = false;
        ConfigHolder.mRequests.poll();
        destroy();
        if (ConfigHolder.mRequests.isEmpty()) {
            return;
        }
        apply();
    }

    private void applyAToast(final Context context, final Request request) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                showAToast(context.getApplicationContext(), request.argument);
            }
        };
        if (isMainThread()) {
            r.run();
        } else {
            new Handler(Looper.getMainLooper()).post(r);
        }
    }

    private void applyCToast(final FragmentActivity activity, final Request request) {
        if (activity.isRestricted()
                || activity.getSupportFragmentManager().isDestroyed()
                || activity.getSupportFragmentManager().isStateSaved()) {
            response();
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mIToastView = IToastView.newInstance(request.argument).show(activity.getSupportFragmentManager());
            }
        };
        if (isMainThread()) {
            r.run();
        } else {
            new Handler(Looper.getMainLooper()).post(r);
        }
    }

    @SuppressLint("ShowToast")
    private void showAToast(Context context, RequestArgument argument) {
        Toast toast;
        if (null != argument.view) {
            toast = new Toast(context.getApplicationContext());
            toast.setView(argument.view);
        } else {
            toast = Toast.makeText(context.getApplicationContext(),
                    argument.message, Toast.LENGTH_SHORT);
            if (0 != argument.icon) {
                View view = toast.getView();
                TextView textView = (TextView) view.findViewById(android.R.id.message);
                Drawable dwLeft = ContextCompat.getDrawable(view.getContext(), argument.icon);
                if (null != dwLeft) {
                    dwLeft.setBounds(0, 0, (int) textView.getTextSize(), (int) textView.getTextSize());
                    textView.setCompoundDrawables(dwLeft, null, null, null);
                    textView.setCompoundDrawablePadding((int) textView.getTextSize() / 2);
                }
            }
        }
        long delay;
        switch (argument.duration) {
            case DURATION.SHORT:
                toast.setDuration(Toast.LENGTH_SHORT);
                delay = IToastView.LENGTH_SHORT;
                break;
            case DURATION.LONG:
                toast.setDuration(Toast.LENGTH_LONG);
                delay = IToastView.LENGTH_LONG;
                break;
            default:
                toast.setDuration(Toast.LENGTH_SHORT);
                delay = IToastView.LENGTH_SHORT;
        }
        switch (argument.gravity) {
            case GRAVITY.TOP:
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                        toast.getXOffset(), toast.getYOffset());
                break;
            case GRAVITY.CENTER:
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL,
                        toast.getXOffset(), toast.getYOffset());
                break;
            case GRAVITY.BOTTOM:
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                        toast.getXOffset(), toast.getYOffset());
                break;
            default:
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                        toast.getXOffset(), toast.getYOffset());

        }
        toast.show();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                response();
            }
        }, delay);
    }

    private void destroy() {
        mRequest = null;
        mIToastView = null;
    }

    private boolean isMainThread() {
        Looper myLooper = Looper.myLooper();
        Looper mainLooper = Looper.getMainLooper();
        if (mainLooper.equals(myLooper)) {
            return true;
        }
        return false;
    }

    // ===================================== classes ===============================================

    private static final class ConfigHolder {
        private static final int MAX_REQUEST = 50;

        private static volatile boolean mIsApplying = false;

        private static volatile AppController mAppController;

        @STRATEGY
        private static volatile int mStrategy = STRATEGY.ANDROID_FIRST;

        private static Queue<Request> mRequests = new ArrayBlockingQueue<>(MAX_REQUEST);
    }

    static class RequestArgument {

        int contextHashCode;

        @Nullable
        CharSequence message;
        @DURATION
        int duration;
        @GRAVITY
        int gravity;
        @DrawableRes
        int icon;
        @Nullable
        View view;
        @Nullable
        IToastView.ToastDisappearListener listener;

        RequestArgument(@Nullable CharSequence message,
                        int duration, int gravity, int icon,
                        @Nullable View view, @Nullable IToastView.ToastDisappearListener listener) {
            this.message = message;
            this.duration = duration;
            this.gravity = gravity;
            this.icon = icon;
            this.view = view;
            this.listener = listener;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RequestArgument)) return false;

            RequestArgument that = (RequestArgument) o;

            if (contextHashCode != that.contextHashCode) return false;
            if (duration != that.duration) return false;
            if (gravity != that.gravity) return false;
            if (icon != that.icon) return false;
            if (message != null ? !message.equals(that.message) : that.message != null)
                return false;
            if (view != null ? !view.equals(that.view) : that.view != null) return false;
            return listener != null ? listener.equals(that.listener) : that.listener == null;
        }

        @Override
        public int hashCode() {
            int result = contextHashCode;
            result = 31 * result + (message != null ? message.hashCode() : 0);
            result = 31 * result + duration;
            result = 31 * result + gravity;
            result = 31 * result + icon;
            result = 31 * result + (view != null ? view.hashCode() : 0);
            result = 31 * result + (listener != null ? listener.hashCode() : 0);
            return result;
        }
    }

    static class Request {

        SoftReference<Context> context;
        boolean active = true;

        RequestArgument argument;

        Request(Context ctx, RequestArgument arg) {
            context = new SoftReference<>(ctx);
            argument = arg;
            argument.contextHashCode = ctx.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Request)) return false;

            Request request = (Request) o;

            if (active != request.active) return false;
            if (context != null ? !context.equals(request.context) : request.context != null)
                return false;
            return argument != null ? argument.equals(request.argument) : request.argument == null;
        }

        @Override
        public int hashCode() {
            int result = context != null ? context.hashCode() : 0;
            result = 31 * result + (active ? 1 : 0);
            result = 31 * result + (argument != null ? argument.hashCode() : 0);
            return result;
        }
    }

}