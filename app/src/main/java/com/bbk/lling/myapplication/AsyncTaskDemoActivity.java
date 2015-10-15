package com.bbk.lling.myapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;

public class AsyncTaskDemoActivity extends Activity {

    private ProgressBar progressBar;
    //下载路径
    private String downloadPath = Environment.getExternalStorageDirectory() +
            File.separator + "download";
    private DownloadAsyncTask downloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async_task_demo);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        //开始下载
        findViewById(R.id.begin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 一个AsyncTask只能被执行一次，否则会抛异常
                 * java.lang.IllegalStateException: Cannot execute task: the task is already running.
                 * 如果要重新开始任务的话要重新创建AsyncTask对象
                 */
                if(downloadTask != null && !downloadTask.isCancelled()) {
                    return;
                }
                downloadTask = new DownloadAsyncTask("http://bbk-lewen.u.qiniudn.com/3d5b1a2c-4986-4e4a-a626-b504a36e600a.flv");
                downloadTask.execute();
            }
        });

        //暂停下载
        findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadTask != null && downloadTask.getStatus() == AsyncTask.Status.RUNNING) {
                    downloadTask.cancel(true);
                }
            }
        });

    }

    /**
     * 下载的AsyncTask
     */
    private class DownloadAsyncTask extends AsyncTask<String, Integer, Long> {
        private static final String TAG = "DownloadAsyncTask";
        private String mUrl;

        public DownloadAsyncTask(String url) {
            this.mUrl = url;
        }

        @Override
        protected Long doInBackground(String... params) {
            Log.i(TAG, "downloading");
            if(mUrl == null) {
                return null;
            }
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(mUrl);
            HttpResponse response = null;
            InputStream is = null;
            RandomAccessFile fos = null;
            OutputStream output = null;

            try {
                //创建存储文件夹
                File dir = new File(downloadPath);
                if(!dir.exists()) {
                    dir.mkdir();
                }
                //本地文件
                File file = new File(downloadPath + File.separator + mUrl.substring(mUrl.lastIndexOf("/") + 1));
                if(!file.exists()){
                    //创建文件输出流
                    output = new FileOutputStream(file);
                    //获取下载输入流
                    response = client.execute(request);
                    is = response.getEntity().getContent();
                    //写入本地
                    file.createNewFile();
                    byte buffer [] = new byte[1024];
                    int inputSize = -1;
                    //获取文件总大小，用于计算进度
                    long total = response.getEntity().getContentLength();
                    int count = 0; //已下载大小
                    while((inputSize = is.read(buffer)) != -1) {
                        output.write(buffer, 0, inputSize);
                        count += inputSize;
                        //更新进度
                        this.publishProgress((int) ((count / (float) total) * 100));
                        //一旦任务被取消则退出循环，否则一直执行，直到结束
                        if(isCancelled()) {
                            output.flush();
                            return null;
                        }
                    }
                    output.flush();
                } else {
                    long readedSize = file.length(); //文件大小，即已下载大小
                    //设置下载的数据位置XX字节到XX字节
                    Header header_size = new BasicHeader("Range", "bytes=" + readedSize + "-");
                    request.addHeader(header_size);
                    //执行请求获取下载输入流
                    response = client.execute(request);
                    is = response.getEntity().getContent();
                    //文件总大小=已下载大小+未下载大小
                    long total = readedSize + response.getEntity().getContentLength();

                    //创建文件输出流
                    fos = new RandomAccessFile(file, "rw");
                    //从文件的size以后的位置开始写入，其实也不用，直接往后写就可以。有时候多线程下载需要用
                    fos.seek(readedSize);
                    //这里用RandomAccessFile和FileOutputStream都可以，只是使用FileOutputStream的时候要传入第二哥参数true,表示从后面填充
//                    output = new FileOutputStream(file, true);

                    byte buffer [] = new byte[1024];
                    int inputSize = -1;
                    int count = (int)readedSize;
                    while((inputSize = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, inputSize);
//                        output.write(buffer, 0, inputSize);
                        count += inputSize;
                        this.publishProgress((int) ((count / (float) total) * 100));
                        if(isCancelled()) {
//                            output.flush();
                            return null;
                        }
                    }
//                    output.flush();
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally{
                try{
                    if(is != null) {
                        is.close();
                    }
                    if(output != null) {
                        output.close();
                    }
                    if(fos != null) {
                        fos.close();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "download begin ");
            Toast.makeText(AsyncTaskDemoActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i(TAG, "downloading  " + values[0]);
            //更新界面进度条
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            Log.i(TAG, "download success " + aLong);
            Toast.makeText(AsyncTaskDemoActivity.this, "下载结束", Toast.LENGTH_SHORT).show();
            super.onPostExecute(aLong);
        }
    }

}
