package com.campusbondhu.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "campusbondhu_session";
    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String uid, String role, String name) {
        prefs.edit().putString("uid", uid).putString("role", role)
            .putString("name", name).apply();
    }
    public String getUid() { return prefs.getString("uid", null); }
    public String getRole() { return prefs.getString("role", null); }
    public String getName() { return prefs.getString("name", null); }
    public void clear() { prefs.edit().clear().apply(); }
}
