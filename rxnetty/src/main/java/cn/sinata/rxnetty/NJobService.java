package cn.sinata.rxnetty;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.List;

/**
 *
 * Created by liaoxiang on 17/1/22.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NJobService extends android.app.job.JobService {

    @Override
    public void onCreate() {
        super.onCreate();
        jobScheduler();
    }

    public void jobScheduler() {
        try {
            int id = 2010;
            JobInfo.Builder builder = new JobInfo.Builder(id,
                    new ComponentName(getPackageName(), NJobService.class.getName()));
            builder.setPeriodic(60000);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED) {
                builder.setPersisted(true);
            }
            JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.err.println("-----p name ---0--->"+getPackageName());
        //如果app已经没有运行，停止
        if (!isAppRun(this, getPackageName())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (Build.VERSION.SDK_INT >= 24) {
            if (!isServiceWork(this, "cn.sinata.rxnetty.CoreService")) {
                this.startService(new Intent(this.getApplicationContext(), CoreService.class));
//                this.jobFinished(params, false);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        System.err.println("-----p name --1---->"+getPackageName());
        //如果app已经没有运行，停止
        if (!isAppRun(this, getPackageName())) {
            stopSelf();
            return false;
        }
        if (!isServiceWork(this, "cn.sinata.rxnetty.CoreService")) {
            this.startService(new Intent(this.getApplicationContext(), CoreService.class));
            this.jobFinished(params, false);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    // 判断服务是否正在运行
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (myAM == null) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    // 判断app是否正在运行
    public boolean isAppRun(Context mContext, String packegeName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (myAM == null) {
            return false;
        }
        List<ActivityManager.RunningTaskInfo> myList = myAM.getRunningTasks(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).baseActivity.getPackageName();
            if (mName.equals(packegeName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.err.println("-------onDestroy--job-->");
    }
}
