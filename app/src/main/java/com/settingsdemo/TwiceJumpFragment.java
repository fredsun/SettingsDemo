package com.settingsdemo;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * 9. 进阶，右侧详细条目还需要二次跳转, 给个 Preference 添加 Fragment标签 详见 R.xml.pref_network
 * Created by fred on 2018/1/11.
 */

public class TwiceJumpFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_twice_jump);
    }
}
