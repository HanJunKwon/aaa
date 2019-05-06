package com.example.rhinspeak.NaverAPI;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;

import com.example.rhinspeak.R;


public class NaverSynthesis {
    private static final String TAG = NaverSynthesis.class.getSimpleName();
    private Handler mHandler;

    /**
     *
     * @param context : 메세지를 전달받을 handler의 소유권을 갖고 있는 Activity
     * @param handler : 메세지를 전달받아서 실행할 handler
     */
    public NaverSynthesis(Context context, Handler handler){
        this.mHandler = handler;
    }

    @WorkerThread
    public void synthesisConnect(){
        Message msg = Message.obtain(mHandler, R.id.SynthesisConnect);
        msg.sendToTarget();
    }

    @WorkerThread
    public void sendText(){
        Message msg = Message.obtain(mHandler, R.id.SendText);
        msg.sendToTarget();
    }

    @WorkerThread
    public void startRecord(){
        Message msg = Message.obtain(mHandler, R.id.ResultRecordStart);
        msg.sendToTarget();
    }
}
