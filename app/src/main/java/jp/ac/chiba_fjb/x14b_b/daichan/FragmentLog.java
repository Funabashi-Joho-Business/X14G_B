package jp.ac.chiba_fjb.x14b_b.daichan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.text.SimpleDateFormat;
import java.util.List;

import to.pns.lib.ListView;
import to.pns.lib.LogDB;
import to.pns.lib.LogService;


public class FragmentLog extends Fragment {


    private ListView mListView;
    private BroadcastReceiver mBr;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FragmentLog() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FrameLayout view = new FrameLayout(getContext());
        mBr = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getAction().equals(LogService.UPDATE_NAME))
                {
                    update();
                }
            }
        };
        view.getContext().registerReceiver(mBr, new IntentFilter(LogService.UPDATE_NAME));




        mListView = new ListView(getContext());
        view.addView(mListView);
        mListView.addHeader("ID");
        mListView.addHeader("DATE");
        mListView.addHeader("MESSAGE");
        update();
        return view;
    }

    @Override
    public void onDestroyView() {
        if(mBr != null)
        {
            getView().getContext().unregisterReceiver(mBr);
            mBr = null;
        }
        super.onDestroyView();
    }

    void update()
    {
        mListView.clear();

        LogDB db = new LogDB(getContext());
        if(db == null)
            return;
        List<LogDB.LogData> list = db.getLog();
        db.close();

        for(LogDB.LogData log : list)
        {
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            int index = mListView.addItem(""+log.id);
            mListView.setItem(index,1,df.format(log.date));
            mListView.setItem(index,2,log.msg);
            mListView.setItemGravity(index,0, Gravity.LEFT);
            mListView.setItemGravity(index,1, Gravity.LEFT);
            mListView.setItemGravity(index,2, Gravity.LEFT);
        }
    }
}
