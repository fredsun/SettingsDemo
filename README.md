### 两种创建 PreferenceActivity 的方案
简述:
* 第一种方案 来自 Android Studio new 出来的 demo.
activity 和 fragment 布局都来自R.xml.
监听通过 OnPreferenceChangeListener, 对表单选项单独注册监听
* 第二种方案 来自 github 上的 [Twidere](https://github.com/TwidereProject/Twidere-Android)
首先谷歌官方对于PreferenceActivity的[建议](https://developer.android.com/guide/topics/ui/settings.html#Fragment)
  ```
  如果您在开发针对 Android 3.0（API 级别 11）及更高版本的应用，则应使用 PreferenceFragment 显示 Preference 对象的列表。您可以将 PreferenceFragment 添加到任何 Activity，而不必使用 PreferenceActivity。
  ```
  所以用的 activity 是普通的 xml, 用 listview 实现了列表,
  fragment布局使用第一种方案的R.xml

先来看第一种
##第一种方案
**完全是Android Studio的Demo，只需知道第二种方案用了第一种方案，也就是官方Demo里自带的 R.xml.xxx 的 preference 布局文, 其余可跳过**
1. 资源下新建 xml 文件夹, 新建布局
activity的布局:
```
<preference-headers>
  <header/>
  <header/>
</preference-headers>
```
fragment 的布局框架:
```
<PreferenceScreen>
  <PreferenceCategory/>分类标题
  <Preference 条目
    title 条目名
    summary 条目内容/>
  <Preference/>
```
一个具体的 fragment 布局,添加了 key, 用于监听
```
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android">
    <SwitchPreference android:title="节省流量"
        android:key="save_data"
        android:summary="在使用收费网络时，禁用媒体预览" />

</PreferenceScreen>
```
2. 新建Activity(基本)
```
public class SettingActivity extends PreferenceActivity {
  //内部嵌套Fragment1
  public static class NetWorkPreferenceFragment extends PreferenceFragment{

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_network);
        }
    }
  //内部嵌套Fragment2
  public static class AboutMePreferenceFragment extends PreferenceFragment{
      @Override
      public void onCreate(@Nullable Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          addPreferencesFromResource(R.xml.pref_me);
      }
  }
  //加入布局
  @Override
   public void onBuildHeaders(List<Header> target) {
       super.onBuildHeaders(target);
       loadHeadersFromResource(R.xml.pref_headers, target);
   }

   //加入Fragment
   @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)||
                NetWorkPreferenceFragment.class.getName().equals(fragmentName)||
                AboutMePreferenceFragment.class.getName().equals(fragmentName);
    }
}
```

3. 加入对偏好修改的监听
```
//原registerOnSharedPreferenceChangeListener是弱引用注册，会被回收，所以全局变量
    static Preference.OnPreferenceChangeListener bindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener(){

        //ListPreference 和 RingtonePreference 的 summary 过多，分开处理
        //stolen from AndroidStudio Sample Project
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference){
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            }else if (preference instanceof RingtonePreference){
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            }else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };
```

4. 抽出添加监听操作
```
private static void bindPreferenceSummaryToValue(Preference preference, Boolean isBoolean){
        //绑定listener
        preference.setOnPreferenceChangeListener(bindPreferenceSummaryToValueListener);
        if (isBoolean){
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(),false));
        }else {
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(),""));
        }

    }
```

5. 给Fragment里的具体key添加监听
```
public static class NetWorkPreferenceFragment extends PreferenceFragment{

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_network);
            bindPreferenceSummaryToValue(findPreference("test_key"), false);
            bindPreferenceSummaryToValue(findPreference("save_data"), true);
        }
    }
```

接着来看第二种
## 第二种方案
首先致敬 [Twidere](https://github.com/TwidereProject/Twidere-Android) 项目的完全开源.才得以看到源码, 虽然是kotlin的。。= =
以及被我删了部分后转成的java项目[Github](https://github.com/sunxlfred/SettingsDemo)
1. 构建左侧 listview 的条目 item 的类
2. 创建左侧 listview 的条目 item 的 xml, 以及点击效果的 selector
3. 构建 adapter, 把 xml 和创建的 item 类的实体关联起来
4. 开始 onCreate, 初始化 listview 和条目 item
5. 构建右侧详细条目Fragment
6. 补充 onCreate, intent 中如果没指定具体条目, 打开的是第一个详细条目, 并添加左侧 listview的点击监听
7. 切记!, style 里补充
```
<item name="preferenceTheme">@style/PreferenceThemeOverlay.v14.Material</item>
```
否则报错 Must specify preferenceTheme in theme
8. 监听back, log打印sp里存储的设置参数
9. 进阶 右侧详细条目还需要二次跳转, 重写 onPreferenceStartFragment(注意v7包还是v14包, 和Fragment里的preference一致就行)
10. 进阶2 back键时判断是 activity 还是 fragment 跳出

#### Must specify preferenceTheme in theme
在style里新建
```
<item name="preferenceTheme">@style/PreferenceThemeOverlay.v14.Material</item>
```
针对4.4以下的可能还需另外配置

## 关于SharePreference
#### getSharedPreferences(name , mode)
mode的选择:
1. Context.MODE_PRIVATE:
2. Context.MODE_WORLD_READABLE: API17后不建议使用, 全局可读文件很危险
3. Context.MODE_MULTI_PROCESS: 不要用来跨进程, 还是用ContentProvider

#### SharedPreference 的使用 tips[参考](http://weishu.me/2016/10/13/sharedpreference-advices/)
1. 别存大 key 和 value(一口气加载时会卡住)
2. commit在当前线程别在主线程
3. 别用来跨进程
4. 不要存放JSON(特殊符号解析浪费时间)
5. edit和apply尽量一次搞定
6. apply还是commit. 异步commit.
7. 如何命名一个独立的sp:
    ```
    getPreferenceManager().setSharedPreferencesName("preference");
    ```

#### OnSharedPreferenceChangeListener
1. OnSharedPreferenceChangeListener 和 OnPreferenceChangeListener比较, 前者是 preference 有变化就收到，后者只针对用 onPreferenceChange 绑定过的 key.[参考](https://stackoverflow.com/questions/13321637/whats-different-between-onpreferencechangelistener-and-onsharedpreferencechange)
2. OnSharedPreferenceChangeListener 是弱引用, 需要在生命周期里注册监听[参考](http://droidyue.com/blog/2014/11/29/why-onsharedpreferencechangelistener-was-not-called/)

#### 提交用apply还是commit[参考](http://www.cloudchou.com/android/post-988.html)
apply 调用 QueuedWork.add(awaitCommit), 如果任务过多, 等待时间过久, 且开始了onPause, 会导致onPause 因为 QueuedWork.waitToFinish()被apply过久而ANR. 最优方案是开启一个线程去 commit.
且 commit 有返回值, 可以补救


#### Preference 中 inflate xml 使用 addPreferencesFromResource(redId) 还是 setPreferencesFromResource(redId, rootkey)
其实没有太大的差别, 虽然官方的PreferenceFragment 是在 onCreate 中调用了 addPreferencesFromResource[链接](https://developer.android.com/reference/android/preference/PreferenceFragment.html), 而PreferenceFragmentCompat的demo中, onCreatePreferences 里用的是 setPreferencesFromResource [链接](https://developer.android.com/reference/android/support/v7/preference/PreferenceFragmentCompat.html),
来看 setPreferencesFromResource 的源码
```
@Override
public void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);
   final TypedValue tv = new TypedValue();
   getActivity().getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
   final int theme = tv.resourceId;
   if (theme == 0) {
       throw new IllegalStateException("Must specify preferenceTheme in theme");
   }
   mStyledContext = new ContextThemeWrapper(getActivity(), theme);
   mPreferenceManager = new PreferenceManager(mStyledContext);
   mPreferenceManager.setOnNavigateToScreenListener(this);
   final Bundle args = getArguments();
   final String rootKey;
   if (args != null) {
       rootKey = getArguments().getString(ARG_PREFERENCE_ROOT);
   } else {
       rootKey = null;
   }
   onCreatePreferences(savedInstanceState, rootKey);
}
```
可以看到 rootKey 是根据 oncreate 时传进来的 Bundle 设置的

那么问题来了, 怎么传进来了, 再来看谷歌的一个例子[LeanbackPreferenceFragment](https://developer.android.com/reference/android/support/v17/preference/LeanbackPreferenceFragment.html)
其中
```
@Override
public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
    final Fragment f = new PrefsFragment();
    final Bundle args = new Bundle(1);
    args.putString(PreferenceFragment.ARG_PREFERENCE_ROOT, pref.getKey());
    f.setArguments(args);
    startPreferenceFragment(f);
    return true;
}
```
通过重写 onPreferenceStartScreen 方法, 自己配置了bundle.

**结论:** 从 intent 里传进来 PreferenceFragment.ARG_PREFERENCE_ROOT. 不然就重写 onPreferenceStartScreen

LeanbackSettingsFragment

> 参考
> [github-Twidere项目](https://github.com/TwidereProject/Twidere-Android)
> [OnSharedPreferenceChangeListeners设计-弱引用](http://droidyue.com/blog/2014/11/29/why-onsharedpreferencechangelistener-was-not-called/)
> [SharedPreference使用tips](http://weishu.me/2016/10/13/sharedpreference-advices/)
