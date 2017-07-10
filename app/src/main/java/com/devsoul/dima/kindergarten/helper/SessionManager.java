package com.devsoul.dima.kindergarten.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 *  This class maintains session data across the app using the SharedPreferences.
 *  We store a boolean flag isLoggedIn in shared preferences to check the login status,
 *  and store an int flag type in shared preferences to check user type.
 */
public class SessionManager
{
    public static final String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;
    Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "LoginApi";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_TYPE = "type";

    // Constructor
    public SessionManager(Context context)
    {
        this._context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

    }

    /**
     * Set login to true or false in the session
     * @param isLoggedIn - true if user logged in, false if user isn't logged in
     */
    public void setLogin(boolean isLoggedIn)
    {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        // commit changes
        editor.commit();
        Log.d(TAG,"user login modified in pref");
    }

    /**
     * Get if user is logged in or isn't
     * @return true if user logged in, false if user isn't logged in
     */
    public boolean isLoggedIn()
    {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    /**
     * Set user type in the session
     * @param type 1 - Teacher, 2 - Parent
     */
    public void setType(int type)
    {
        editor.putInt(KEY_TYPE, type);
        // commit changes
        editor.commit();
        Log.d(TAG,"user type modified in pref");
    }

    /**
     * Get user type from session
     * @return type: 1 - Teacher, 2 - Parent, 0 - Default
     */
    public int getType()
    {
        return pref.getInt(KEY_TYPE, 0);
    }
}
