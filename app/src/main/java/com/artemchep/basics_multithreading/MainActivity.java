package com.artemchep.basics_multithreading;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artemchep.basics_multithreading.cipher.CipherUtil;
import com.artemchep.basics_multithreading.domain.Message;
import com.artemchep.basics_multithreading.domain.WithMillis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private List<WithMillis<Message>> mList = new ArrayList<>();
    private Map<WithMillis<Message>, Long> queue = new HashMap();

    private MessageAdapter mAdapter = new MessageAdapter(mList);
    private Thread encryptionThread;
    private Looper encryptionLooper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        createThread();

        //showWelcomeDialog();
    }

    private void showWelcomeDialog() {
        new AlertDialog.Builder(this)
                .setMessage("What are you going to need for this task: Thread, Handler.\n" +
                        "\n" +
                        "1. The main thread should never be blocked.\n" +
                        "2. Messages should be processed sequentially.\n" +
                        "3. The elapsed time SHOULD include the time message spent in the queue.")
                .show();
    }

    public void onPushBtnClick(View view) {
        Message message = Message.generate();
        insert(new WithMillis<>(message)); //вставка нового в адаптер без правої частини
    }

    @UiThread
    public void insert(final WithMillis<Message> message) { //вставка,  юай треда
        mList.add(message);
        mAdapter.notifyItemInserted(mList.size() - 1);
        messageProcessing(message);


        // -> нажимаем на кнопку -> получаем новый меседж, инсерт -> отправляем в очередь, засекаем время ...    обновляем меседж
        //                                                                                           ->    обработка

        // How it should look for the end user? Uncomment if you want to see. Please note that
        // you should not use poor decor view to send messages to UI thread.
        //       getWindow().getDecorView().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final Message messageNew = message.value.copy("sample :)");
//                final WithMillis<Message> messageNewWithMillis = new WithMillis<>(messageNew, CipherUtil.WORK_MILLIS);
//                update(messageNewWithMillis);
//            }
//        }, CipherUtil.WORK_MILLIS);
    }

    private void messageProcessing(final WithMillis<Message> message) {
        queue.put(message, System.currentTimeMillis());
        Log.d("ttt", "put");
        new Handler(encryptionLooper).post(new Runnable() {
            @Override
            public void run() {
                Log.d("ttt", "handler");
                final Message messageNew = message.value.copy(CipherUtil.encrypt(message.value.plainText));
                final long workMillis = System.currentTimeMillis() - queue.get(message);
                final WithMillis<Message> messageNewWithMillis = new WithMillis<>(messageNew, workMillis);
                getWindow().getDecorView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        update(messageNewWithMillis);
                    }
                }, workMillis);
            }
        });
    }

    public void createThread() {
        Log.d("ttt", "createThread");
        encryptionThread = new Thread() {
            @Override
            public void run() {
                Log.d("ttt", "new thread");
                Looper.prepare();
                encryptionLooper = Looper.myLooper();
                new Handler(encryptionLooper).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ttt", "post to handler");
                    }
                });
                Looper.loop();
            }
        };
        encryptionThread.start();
    }


    @UiThread
    synchronized public void update(final WithMillis<Message> message) { // оновлення наявного меседжу в листі, оновлення юай

        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).value.key.equals(message.value.key)) {
                mList.set(i, message);
                mAdapter.notifyItemChanged(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

}
