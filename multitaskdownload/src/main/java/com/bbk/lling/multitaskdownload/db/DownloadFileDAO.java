package com.bbk.lling.multitaskdownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.bbk.lling.multitaskdownload.beans.AppContent;

import java.util.ArrayList;
import java.util.List;

/**
 * @Class: DownloadFileDAO
 * @Description: 每个文件下载状态记录的数据库操作类
 * @author: lling(www.cnblogs.com/liuling)
 * @Date: 2015/10/13
 */
public class DownloadFileDAO {
    private static final String TAG = "DownloadFileDAO";
    private static DownloadFileDAO dao=null;
    private Context context;
    private DownloadFileDAO(Context context) {
        this.context=context;
    }

    synchronized public static DownloadFileDAO getInstance(Context context){
        if(dao==null){
            dao=new DownloadFileDAO(context);
        }
        return dao;
    }

    /**
     * 获取数据库连接
     * @return
     */
    public SQLiteDatabase getConnection() {
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase= new DBHelper(context).getReadableDatabase();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return sqliteDatabase;
    }

    /**
     * 插入数据
     * @param appContent
     */
    public void insertDownloadFile(AppContent appContent) {
        if(appContent == null) {
            return;
        }
        //如果本地已经存在，直接修改
        if(getAppContentByUrl(appContent.getUrl()) != null) {
            updateDownloadFile(appContent);
            return;
        }
        SQLiteDatabase database = getConnection();
        try {
            String sql = "insert into download_file(app_name, url, download_percent, status) values (?,?,?,?)";
            Object[] bindArgs = { appContent.getName(), appContent.getUrl(), appContent.getDownloadPercent()
                    , appContent.getStatus().getValue()};
            database.execSQL(sql, bindArgs);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

    /**
     * 根据url获取下载文件信息
     * @param url
     * @return
     */
    public AppContent getAppContentByUrl(String url) {
        if(TextUtils.isEmpty(url)) {
            return null;
        }
        SQLiteDatabase database = getConnection();
        AppContent appContent = null;
        Cursor cursor = null;
        try {
            String sql = "select * from download_file where url=?";
            cursor = database.rawQuery(sql, new String[] { url });
            if (cursor.moveToNext()) {
                appContent = new AppContent(cursor.getString(1), cursor.getString(2));
                appContent.setDownloadPercent(cursor.getInt(3));
                appContent.setStatus(AppContent.Status.getByValue(cursor.getInt(4)));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (null != database) {
                database.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return appContent;
    }

    /**
     * 更新下载信息
     * @param appContent
     */
    public void updateDownloadFile(AppContent appContent) {
        if(appContent == null) {
            return;
        }
        SQLiteDatabase database = getConnection();
        try {
            Log.e(TAG, "update download_file,app name:" + appContent.getName() + ",url:" + appContent.getUrl()
                    + ",percent" + appContent.getDownloadPercent() + ",status:" + appContent.getStatus().getValue());
            String sql = "update download_file set app_name=?, url=?, download_percent=?, status=? where url=?";
            Object[] bindArgs = {appContent.getName(), appContent.getUrl(), appContent.getDownloadPercent()
                    , appContent.getStatus().getValue(), appContent.getUrl()};
            database.execSQL(sql, bindArgs);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

    /**
     * 获取所有下载文件记录
     * @return
     */
    public List<AppContent> getAll() {
        SQLiteDatabase database = getConnection();
        List<AppContent> list = new ArrayList<AppContent>();
        Cursor cursor = null;
        try {
            String sql = "select * from download_file";
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                AppContent appContent = new AppContent(cursor.getString(1), cursor.getString(2));
                appContent.setDownloadPercent(cursor.getInt(3));
                appContent.setStatus(AppContent.Status.getByValue(cursor.getInt(4)));
                list.add(appContent);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (null != database) {
                database.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 根据url删除记录
     * @param url
     */
    public void delByUrl(String url) {
        if(TextUtils.isEmpty(url)) {
            return;
        }
        SQLiteDatabase database = getConnection();
        try {
            String sql = "delete from download_file where url=?";
            Object[] bindArgs = { url };
            database.execSQL(sql, bindArgs);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

}
