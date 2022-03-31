package com.example.qrhunter;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class for handling user information
 */
public class userHandler {
    public userHandler(){}

    /**
     * Returns the username of the current logged in user on device by checking the relevant userInfo file
     * @param context context of the calling activity
     * @return String of the username
     * @throws IOException
     */
    public String getUsername(Context context) throws IOException {
        //Check userInfo file and retrieve username
        File userInfoFile = new File(context.getFilesDir(), "userInfo");
        byte[] bytes = new byte[(int) userInfoFile.length()];
        FileInputStream fis = new FileInputStream(userInfoFile);
        fis.read(bytes);
        String username = new String(bytes);
        return username;
    }

    public boolean getAdminStatus(Context context) throws IOException {
        File adminUserFile = new File(context.getFilesDir(), "adminUser");
        byte[] bytes = new byte[(int) adminUserFile.length()];
        FileInputStream fis = new FileInputStream(adminUserFile);
        fis.read(bytes);
        String adminPassword = new String(bytes);
        ScoringHandler hashHandle = new ScoringHandler();
        String adminHash = hashHandle.sha256("admin_password");
        if(adminPassword.equals(adminHash)){
            return true;
        }
        return false;
    }
}
