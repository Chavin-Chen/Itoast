# IToast

This is for Android Toast.

As you know android.widget.toast is dependence on OS NotificationManager.
But after API-19(Android KitKat), Our users can close app Notification Permission.
Then our toast cannot show any message~

I use a Dialog on the TopActivity to show toast.


How to Use It?
-----------------------------------
### Add gradle dependence

```Java
    repositories {
      jcenter()
    }

    dependencies {
      implementation 'com.chavinchen:itoast:1.0.1'
    }
```

### Use in Project
Use can use IToast to show toast , just like use android.widget.Toast, eg:
    
    IToast.makeText(context, message, IToast.DURATION.SHORT).show();
    
And also we have a no-repeat toast util which has more easily API, but first we need init it first:
You can init it in YourApplication#onCreate like this:

```Java
    IToastUtil.init(getApplicationContext(), IToast.STRATEGY.CUSTOM_FIRST, new IToast.AppController() {
        @Override
        public FragmentActivity getTopActivity() {
            // FIXME Here you need return Top Activity
            return null;
        }
    });
```

there are three arguments: 

1. context, 
2. the strategy tell IToast use custom toast first or android toast first, 
3. when android toast can't show (IToast can know it in runtime) We need a top activity to show dialog instead,

Of course you can write a better util based on IToast, so IToast give you another way to init:

 ```Java
    IToast.setup(IToast.STRATEGY.ANDROID_FIRST, new IToast.AppController() {
        @Override
        public FragmentActivity getTopActivity() {
            // FIXME Here you need return Top Activity
            return null;
        }
    });
```

Then you can show toast like this:

```Java
    IToastUtil.showShort("Hello I am Chavin :)");
```

the above.



###### 25/08/2018 on guangzhou.





