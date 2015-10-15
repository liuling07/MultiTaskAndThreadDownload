package com.bbk.lling.multithreaddownload;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity2 extends Activity {

    private static final String TAG = "MainActivity";
    private static final int DEFAULT_POOL_SIZE = 1;
    private static final int GET_LENGTH_SUCCESS = 1;
    //下载路径
    private String downloadPath = Environment.getExternalStorageDirectory() +
            File.separator + "download";

//    private String mUrl = "http://ftp.neu.edu.cn/mirrors/eclipse/technology/epp/downloads/release/juno/SR2/eclipse-java-juno-SR2-linux-gtk-x86_64.tar.gz";
        private String mUrl = "http://p.gdown.baidu.com/c4cb746699b92c9b6565cc65aa2e086552651f73c5d0e634a51f028e32af6abf3d68079eeb75401c76c9bb301e5fb71c144a704cb1a2f527a2e8ca3d6fe561dc5eaf6538e5b3ab0699308d13fe0b711a817c88b0f85a01a248df82824ace3cd7f2832c7c19173236";
    private ProgressBar mProgressBar;
    private TextView mPercentTV;
    SharedPreferences mSharedPreferences = null;
    long mFileLength = 0;
    Long mCurrentLength = 0L;

    private InnerHandler mHandler = new InnerHandler();

    //创建线程池
    private Executor mExecutor = Executors.newCachedThreadPool();

    private List<DownloadAsyncTask> mTaskList = new ArrayList<DownloadAsyncTask>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mPercentTV = (TextView) findViewById(R.id.percent_tv);
        mSharedPreferences = getSharedPreferences("download", Context.MODE_PRIVATE);
        //开始下载
        findViewById(R.id.begin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        //创建存储文件夹
                        File dir = new File(downloadPath);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        //获取文件大小
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet(mUrl);
                        HttpResponse response = null;

                        try {
                            response = client.execute(request);
                            mFileLength = response.getEntity().getContentLength();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            if (request != null) {
                                request.abort();
                            }
                        }
                        Message.obtain(mHandler, GET_LENGTH_SUCCESS).sendToTarget();
                    }
                }.start();
            }
        });

        //暂停下载
        findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DownloadAsyncTask task : mTaskList) {
                    if (task != null && task.getStatus() == AsyncTask.Status.RUNNING ) {
                        task.stop();
                    }
                }
                mTaskList.clear();
            }
        });
    }

    /**
     * 开始下载
     * 根据待下载文件大小计算每个线程下载位置，并创建AsyncTask
     */
    private void beginDownload() {
        mCurrentLength = 0L;
        mPercentTV.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
        long blockLength = mFileLength / DEFAULT_POOL_SIZE;
        for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
            long beginPosition = i * blockLength;//每条线程下载的开始位置
            long endPosition = (i + 1) * blockLength;//每条线程下载的结束位置
            if (i == (DEFAULT_POOL_SIZE - 1)) {
                endPosition = mFileLength;//如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
            }
            DownloadAsyncTask task = new DownloadAsyncTask(beginPosition, endPosition);
            mTaskList.add(task);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrl, String.valueOf(i));
        }
    }

    /**
     * 更新进度条
     */
    synchronized public void updateProgress() {
        int percent = (int) Math.ceil((float)mCurrentLength / (float)mFileLength * 100);
        Log.i(TAG, "downloading  " + mCurrentLength + "," + mFileLength + "," + percent);
        if(percent > mProgressBar.getProgress()) {
            mProgressBar.setProgress(percent);
            mPercentTV.setText("下载进度：" + percent + "%");
            if (mProgressBar.getProgress() == mProgressBar.getMax()) {
                Toast.makeText(MainActivity2.this, "下载结束", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        for(DownloadAsyncTask task: mTaskList) {
            if(task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
            mTaskList.clear();
        }
        super.onDestroy();
    }

    /**
     * 下载的AsyncTask
     */
    private class DownloadAsyncTask extends AsyncTask<String, Integer , Long> {
        private static final String TAG = "DownloadAsyncTask";
        private long beginPosition = 0;
        private long endPosition = 0;

        private long current = 0;

        private String currentThreadIndex;
        private boolean isStop = false;

        public DownloadAsyncTask(long beginPosition, long endPosition) {
            this.beginPosition = beginPosition;
            this.endPosition = endPosition;
        }

        public void stop() {
            isStop = true;
        }


        @Override
        protected Long doInBackground(String... params) {
            Log.i(TAG, "downloading");
            String url = params[0];
            currentThreadIndex = url + params[1];
            if(url == null) {
                return null;
            }
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = null;
            InputStream is = null;
            RandomAccessFile fos = null;
            OutputStream output = null;

            try {
                //本地文件
                File file = new File(downloadPath + File.separator + url.substring(url.lastIndexOf("/") + 1));

                //获取之前下载保存的信息，从之前结束的位置继续下载
                //这里加了判断file.exists()，判断是否被用户删除了，如果文件没有下载完，但是已经被用户删除了，则重新下载
                long downedPosition = mSharedPreferences.getLong(currentThreadIndex, 0);
                if(file.exists() && downedPosition != 0) {
                    beginPosition = beginPosition + downedPosition;
                    current = downedPosition;
                    synchronized (mCurrentLength) {
                        mCurrentLength += downedPosition;
                    }
                }

                //设置下载的数据位置beginPosition字节到endPosition字节
                Header header_size = new BasicHeader("Range", "bytes=" + beginPosition + "-" + endPosition);
                request.addHeader(header_size);
                //执行请求获取下载输入流
                response = client.execute(request);
                is = response.getEntity().getContent();

                //创建文件输出流
                fos = new RandomAccessFile(file, "rw");
                //从文件的size以后的位置开始写入，其实也不用，直接往后写就可以。有时候多线程下载需要用
                fos.seek(beginPosition);

                byte buffer [] = new byte[1024];
                int inputSize = -1;
                while((inputSize = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, inputSize);
                    current += inputSize;
                    synchronized (mCurrentLength) {
                        mCurrentLength += inputSize;
                    }
                    Log.e(TAG, isStop + "");
                    if (isStop) {
                        Log.e(TAG, "OA! I am stoped." + this.toString());
                        break;
                    }
                    this.publishProgress();
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally{
                try{
//                    request.abort();
                    if(output != null) {
                        output.close();
                    }
                    if(fos != null) {
                        fos.close();
                    }
//                    is.close();
                    EofSensorInputStream sd = null;
                } catch(Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "download begin ");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //更新界面进度条
            updateProgress();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            Log.i(TAG, "download success ");
            super.onPostExecute(aLong);
            if(isStop) {
                customCancelMethod();
            } else {
                mSharedPreferences.edit().remove(currentThreadIndex).commit();
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "download cancelled ");
            super.onCancelled();
            customCancelMethod();
        }

        @Override
        protected void onCancelled(Long aLong) {
            Log.i(TAG, "download cancelled(Long aLong)");
            super.onCancelled(aLong);
            customCancelMethod();
        }

        protected void customCancelMethod() {
            Log.i(TAG, "customCancelMethod");
            mSharedPreferences.edit().putLong(currentThreadIndex, current).commit();
        }
    }

    private class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LENGTH_SUCCESS :
                    beginDownload();
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
