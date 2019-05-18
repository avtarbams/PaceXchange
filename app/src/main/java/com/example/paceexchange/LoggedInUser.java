package com.example.paceexchange;
/***Utility class to fetch Logged in User ***/
public class LoggedInUser {

    public String mLoogedInUser;

    private static LoggedInUser loggedIn_instance = null;

    private LoggedInUser() {
        this.mLoogedInUser = "";
    }

    public static LoggedInUser getInstance()
    {
        if (loggedIn_instance == null)
            loggedIn_instance = new LoggedInUser();

        return loggedIn_instance;
    }

    public String getmLoogedInUser() {
        return mLoogedInUser;
    }

    public void setmLoogedInUser(String mLoogedInUser) {
        this.mLoogedInUser = mLoogedInUser;
    }
}
