package com.settingsdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by fred on 2018/1/11.
 */

public class SettingEXActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    ListView listview;
    EntriesAdapter mEntriesAdapter;
    static final String TAG = "SetActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //4. 开始onCreate, 初始化listView和item
        listview = findViewById(R.id.category_list);
        mEntriesAdapter = new EntriesAdapter(this);
        listview.setAdapter(mEntriesAdapter);
        //设置后才会显示点击效果
        listview.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        initEntries();

        //6. 补充onCreate, 如果intent没指定要打开哪个Fragment, 就开第一个PreferenceEntry
        if (savedInstanceState == null){
            Uri data = getIntent().getData();
            String initialTag = null;
            if (data!=null){
                initialTag = data.getAuthority();
            }
            int initialItem = -1;
            int firstEntry = -1;

            for (int i = 0; i< mEntriesAdapter.getCount(); i++){
                Object item = mEntriesAdapter.getItem(i);
                //打开时显示第一个PreferenceEntry
                if (item instanceof PreferenceEntry){
                    if (firstEntry == -1){
                        firstEntry = i;
                    }
                    //如果有在authority里设置tag,则指定tagPreferenceScreen
                    if (initialTag!= null && initialTag == ((PreferenceEntry) item).tag){
                        initialItem = i;
                        break;
                    }

                }
            }
            if (initialItem == -1){
                initialItem = firstEntry;
            }
            if (initialItem != -1){
                openDetails(initialItem);
                listview.setItemChecked(initialItem, true);

            }
        }
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        openDetails(i);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        Fragment instantiate = Fragment.instantiate(this, pref.getFragment(), pref.getExtras());
        fragmentTransaction.replace(R.id.detail_fragment_container, instantiate);
        fragmentTransaction.addToBackStack(pref.getTitle().toString());
        fragmentTransaction.commit();
        return true;
    }

    //1. 创建条目item类
    interface Entry{
        void bind(View view);
    }

    //1.1 创建具体的条目类
    class PreferenceEntry implements Entry {
        String tag;
        int icon;
        String title;
        int preference;
        String fragment;
        Bundle args;

        public PreferenceEntry(String tag, int icon, String title, int preference, String fragment, Bundle args) {
            this.tag = tag;
            this.icon = icon;
            this.title = title;
            this.preference = preference;
            this.fragment = fragment;
            this.args = args;
        }

        @Override
        public void bind(View view) {
            ImageView imageView;
            imageView = ((ImageView)view.findViewById(android.R.id.icon));
            imageView.setImageResource(icon);
            ((TextView)view.findViewById(android.R.id.title)).setText(title);
        }
    }

    //1.2 创建条目名的类，如功能, 关于, 这样的category
    class HeadEntry implements Entry{
        String title;

        public HeadEntry(String title) {
            this.title = title;
        }

        @Override
        public void bind(View view) {
            ((TextView)view.findViewById(android.R.id.title)).setText(title);
        }
    }
    //2. 构造item的xml和selector点击效果
    //3. 创建Adapter, 把item类和xml关联起来
    private class EntriesAdapter extends BaseAdapter {
        final int VIEW_TYPE_PREFERENCE_ENTRY = 0;
        final int VIEW_TYPE_HEADER_ENTRY = 1;
        Context context;
        LayoutInflater layoutInflater;
        ArrayList<Entry> entries = new ArrayList<>();

        public EntriesAdapter(Context context) {
            this.context = context;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return entries.size();
        }

        @Override
        public Object getItem(int position) {
            return entries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return (long)getItem(position).hashCode();
        }



        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == VIEW_TYPE_PREFERENCE_ENTRY;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Entry item = (Entry) getItem(position);
            if (item instanceof PreferenceEntry){
                return VIEW_TYPE_PREFERENCE_ENTRY;
            }else if (item instanceof HeadEntry){
                return VIEW_TYPE_HEADER_ENTRY;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int itemViewType = getItemViewType(position);
            Entry entry = (Entry) getItem(position);
            View itemView;
            if (convertView != null){
                itemView = convertView;
            }else {
                switch (itemViewType){
                    case VIEW_TYPE_PREFERENCE_ENTRY:
                        itemView =  layoutInflater.inflate(R.layout.item_preference_header_item, parent, false);
                        break;
                    case VIEW_TYPE_HEADER_ENTRY:
                        itemView =  layoutInflater.inflate(R.layout.item_preference_header_category, parent, false);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            entry.bind(itemView);
            return itemView;
        }

        void addHeader(String title){
            entries.add(new HeadEntry(title));
            notifyDataSetChanged();
        }

        void addPreference(String tag, @DrawableRes int icon, String title, @XmlRes int preference){
            entries.add(new PreferenceEntry(tag, icon, title, preference, null, null));
            notifyDataSetChanged();
        }

        void addPreference(String tag, @DrawableRes int icon, String title, Class fragment, Bundle args){
            entries.add(new PreferenceEntry(tag, icon, title, 0, fragment.getName(), args));
            notifyDataSetChanged();
        }
    }

    private void initEntries() {
        mEntriesAdapter.addHeader("功能");
        mEntriesAdapter.addPreference("network", R.drawable.ic_wifi_black_24dp, "网络", R.xml.pref_network);
        mEntriesAdapter.addPreference("theme", R.drawable.ic_color_lens_black_24dp, "主题", R.xml.pref_theme);
        mEntriesAdapter.addHeader("关于");
        mEntriesAdapter.addPreference("me", R.drawable.ic_info_black_24dp, "关于", R.xml.pref_me);
    }

    private void openDetails(int position) {
        if(!(mEntriesAdapter.getItem(position) instanceof PreferenceEntry)){
            return;
        }
        PreferenceEntry entry = (PreferenceEntry) mEntriesAdapter.getItem(position);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        supportFragmentManager.popBackStackImmediate(null, 0);
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        if (entry.preference != 0){
            Bundle args = new Bundle();
            args.putInt("resid", entry.preference);
            Fragment instantiate = Fragment.instantiate(this, SettingDetailFragment.class.getName(),args);
            fragmentTransaction.replace(R.id.detail_fragment_container, instantiate);
        }else if (entry.fragment != null){
            fragmentTransaction.replace(R.id.detail_fragment_container, Fragment.instantiate(this, entry.fragment, entry.args));
        }
        fragmentTransaction.setBreadCrumbTitle(entry.title);
        fragmentTransaction.commit();
    }

    //8. 监听back, 在按下时log对应key的内容. 并弹出一个框
    @Override
    public void onBackPressed() {
        DialogFragment dialogFragment = RestartConfirmFragment.newInstance(R.string.restart_confirm_title);
        dialogFragment.show(getSupportFragmentManager(), "restartConfirmDialog");
        SharedPreferences preference = getSharedPreferences(getString(R.string.sp_name_preference), Context.MODE_PRIVATE);
        Log.i(TAG, "test_key1"+preference.getString("test_key", ""));
        Log.i(TAG, "save_data1"+preference.getBoolean("save_data", false));
    }

    public static class RestartConfirmFragment extends DialogFragment{
        public static RestartConfirmFragment newInstance(int title){
            RestartConfirmFragment restartConfirmFragment = new RestartConfirmFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            restartConfirmFragment.setArguments(args);
            return restartConfirmFragment;
        }
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setPositiveButton("保存",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                    Log.i(TAG, "clickpos");
                                }
                            })
                    .setNegativeButton("不保存",
                            new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                    Log.i(TAG, "clickneg");
                                }
                            }
                    )
                    .create();
        }
    }
}

