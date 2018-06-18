package com.test.stroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 로그인 유지를 위한 class
 * Created by Administrator on 2017-06-02.
 */

public class SaveSharedPreference {

    static final String PREF_USER_ID="userid";

    static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUserId(Context context, String userId)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_ID, userId);
        editor.commit();
        Log.d("PREF_USER_ID값",userId);
    }

    public static String getUserId(Context context)
    {
        return getSharedPreferences(context).getString(PREF_USER_ID,"");
    }
}
