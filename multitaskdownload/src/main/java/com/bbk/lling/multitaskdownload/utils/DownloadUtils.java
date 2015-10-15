package com.bbk.lling.multitaskdownload.utils;

import android.os.Environment;

import com.bbk.lling.multitaskdownload.beans.AppContent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Class:
 * @Description:
 * @author: lling(www.cnblogs.com/liuling)
 * @Date: 2015/10/13
 */
public class DownloadUtils {

    /**
     * 获取下载文件存储目录
     * @return
     */
    public static String getDownloadPath() {
        String downloadPath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            downloadPath = Environment.getExternalStorageDirectory() +
                    File.separator + "download";
        }
        return downloadPath;
    }

    /**
     * 获取测试数据
     * @return
     */
    public static List<AppContent> getTestData() {
        List<AppContent> list = new ArrayList<AppContent>();
        AppContent app1 = new AppContent("奇趣营", "http://219.128.78.33/apk.r1.market.hiapk.com/data/upload/apkres/2015/9_25/18/com.lling.qiqu_060245.apk");
        list.add(app1);
        AppContent app2 = new AppContent("云老师", "http://183.57.28.39/apk.r1.market.hiapk.com/data/upload/apkres/2015/9_30/9/com.bbk.cloudteacher_095422.apk");
        list.add(app2);
        AppContent app3 = new AppContent("开心消消乐", "http://183.62.114.247/m.wdjcdn.com/apk.wdjcdn.com/2/65/61103e72de9d8f95465e68b535559652.apk");
        list.add(app3);
        AppContent app4 = new AppContent("欢乐斗地主", "http://119.147.254.64/dd.myapp.com/16891/F5151080C8F69D2D992047D19B24C39D.apk");
        list.add(app4);
        AppContent app5 = new AppContent("天天酷跑", "http://183.61.182.28/m.wdjcdn.com/apk.wdjcdn.com/b/eb/295abecdc49760ed4b74fbc0d36c3ebb.apk");
        list.add(app5);
        AppContent app6 = new AppContent("中国象棋", "http://183.61.182.28/m.wdjcdn.com/apk.wdjcdn.com/8/0b/d7b904d26558ae7e22d1e6ebdf4830b8.apk");
        list.add(app6);
        AppContent app7 = new AppContent("消灭星星", "http://14.18.142.20/m.wdjcdn.com/apk.wdjcdn.com/b/a0/e8c01dc875f0c72f4952ce800f3f6a0b.apk");
        list.add(app7);
        AppContent app8 = new AppContent("墨迹天气", "http://125.88.65.248/m.wdjcdn.com/apk.wdjcdn.com/3/8d/19e04e8c921e30eaae2dd5835697e8d3.apk");
        list.add(app8);
        AppContent app9 = new AppContent("暗黑黎明", "http://183.61.182.28/m.wdjcdn.com/apk.wdjcdn.com/4/7d/410a086fe71834660e01cecdde55d7d4.apk");
        list.add(app9);
        AppContent app10 = new AppContent("有道词典", "http://14.18.142.20/m.wdjcdn.com/apk.wdjcdn.com/b/4a/ab2001cd8ec89687e34dab51119064ab.apk");
        list.add(app10);
        AppContent app11 = new AppContent("虎扑跑步", "http://14.18.142.20/m.wdjcdn.com/apk.wdjcdn.com/1/7c/f0276757ab38391ba24489e224ca87c1.apk");
        list.add(app11);
        AppContent app12 = new AppContent("虎扑跑步", "http://14.18.142.20/m.wdjcdn.com/apk.wdjcdn.com/1/7c/f0276757ab38391ba24489e224ca87c1.apk");
        list.add(app12);
        AppContent app13 = new AppContent("大众点评", "http://125.88.65.248/m.wdjcdn.com/apk.wdjcdn.com/6/43/e5f01e24b83295ef6bd04c454377b436.apk");
        list.add(app13);
        AppContent app14 = new AppContent("一号店", "http://125.88.65.248/m.wdjcdn.com/apk.wdjcdn.com/6/55/188dd88849b9901e3547d9cb104df556.apk");
        list.add(app14);
        return list;
    }
}
