package com.wewow;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suncjs on 2017/3/13.
 */

public class UserInfo {
    private Long id;
    private String open_id;
    private String nickname;
    private String desc;
    private String reg_type;
    private String reg_time;
    private String token;
    private String background_id;

    public static UserInfo getAnonymouUser() {
        UserInfo anonymous = new UserInfo();
        anonymous.id = 0L;
        anonymous.nickname = "Anonymous User";
        return anonymous;
    }

    private static final String TAG = "Userinfo";
    private static final String PREFERENCE_USERINFO = "PREFERENCE_USERINFO";
    private static final String PREFERENCE_USERINFO_KEY = "PREFERENCE_USERINFO_KEY";

    private UserInfo() {

    }

    public Long getId() {
        return id;
    }


    public String getOpen_id() {
        return open_id;
    }

    public String getNickname() {
        return nickname;
    }


    public String getDesc() {
        return desc;
    }

    public String getReg_type() {
        return reg_type;
    }

    public String getReg_time() {
        return reg_time;
    }

    public String getToken() {
        return token;
    }

    private String serialize() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("id", this.id);
            jobj.put("nickname", this.nickname);
            jobj.put("desc", this.desc);
            jobj.put("open_id", this.open_id);
            jobj.put("reg_time", this.reg_time);
            jobj.put("reg_type", this.reg_type);
            jobj.put("token", this.token);
            jobj.put("background_id", this.background_id);
            return jobj.toString();
        } catch (JSONException e) {
            Log.w(TAG, "serialize fail");
            return null;
        }
    }

    public void saveUserInfo(Context cxt) {
        SharedPreferences sp = cxt.getSharedPreferences(PREFERENCE_USERINFO, Context.MODE_PRIVATE);
        String s = this.serialize();
        if (s != null) {
            Editor ed = sp.edit();
            ed.putString(UserInfo.PREFERENCE_USERINFO_KEY, s);
            ed.commit();
        }
    }

    public static UserInfo getCurrentUser(Context cxt) {
        SharedPreferences sp = cxt.getSharedPreferences(PREFERENCE_USERINFO, Context.MODE_PRIVATE);
        String s = sp.getString(PREFERENCE_USERINFO_KEY, "{}");
        try {
            JSONObject jobj = new JSONObject(s);
            return UserInfo.getUserInfo(jobj);
        } catch (JSONException e) {
            Log.e(TAG, "saved userinfo corrupted");
            return null;
        }
    }

    public static boolean isUserLogged(Context cxt) {
        return UserInfo.getCurrentUser(cxt) != null;
    }

    public static UserInfo getUserInfo(JSONObject jobj) {
        UserInfo ui = new UserInfo();
        try {
            ui.desc = jobj.getString("desc");
            ui.open_id = jobj.getString("open_id");
            ui.id = jobj.getLong("id");
            ui.nickname = jobj.getString("nickname");
            ui.reg_time = jobj.optString("reg_time");
            ui.reg_type = jobj.optString("reg_type");
            ui.token = jobj.getString("token");
            ui.background_id = jobj.optString("background_id", "1");
        } catch (JSONException e) {
            Log.w(TAG, "json to userinfo fail");
            return null;
        }
        return ui;
    }

    public static void logout(Context cxt) {
        SharedPreferences sp = cxt.getSharedPreferences(PREFERENCE_USERINFO, Context.MODE_PRIVATE);
        Editor ed = sp.edit();
        ed.remove(UserInfo.PREFERENCE_USERINFO_KEY);
        ed.commit();
    }

    public void setNickName(String nickname) {
        this.nickname = nickname;
    }

    public void setSignature(String sig) {
        this.desc = sig;
    }

    public String getBackground_id() {
        return background_id;
    }

    public void setBackground_id(String background_id) {
        this.background_id = background_id;
    }
}
