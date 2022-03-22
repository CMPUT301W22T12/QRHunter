package com.example.qrhunter;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class userHandler {
    public userHandler(){}

    public String getUsername(Context context) throws IOException {
        File userInfoFile = new File(context.getFilesDir(), "userInfo");
        byte[] bytes = new byte[(int) userInfoFile.length()];
        FileInputStream fis = new FileInputStream(userInfoFile);
        fis.read(bytes);
        String username = new String(bytes);
        return username;
    }
}
