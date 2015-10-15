package com.bbk.lling.multitaskdownload.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @Class:
 * @Description:
 * @author: lling(www.cnblogs.com/liuling)
 * @Date: 2015/10/14
 */
public class DownloadService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
