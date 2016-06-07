package com.kaist.lts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class SelectWorkerActivity extends AppCompatActivity {
    static final String TAG = "[LTS][SelWorker]";
    private int mRequest_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_worker);
        int user_mode = ProfileManager.getUserMode(Session.GetInstance());
        Intent intent = getIntent();
        String candidates = intent.getStringExtra("candidates_list");
        mRequest_id = Integer.parseInt((String)intent.getStringExtra("request_id"));
        showCandidatesList(this, user_mode, candidates);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showCandidatesList(Activity activity, int user_mode, String candidates_id) {
        final Activity ac = activity;
        Log.d(TAG, "Show Request List");

        // Show title
        TextView title0 = (TextView)findViewById(R.id.select_worker_view_title0);
        TextView title1 = (TextView)findViewById(R.id.select_worker_view_title1);
        TextView title2 = (TextView)findViewById(R.id.select_worker_view_title2);
        title0.setVisibility(View.VISIBLE);
        title1.setVisibility(View.VISIBLE);
        title2.setVisibility(View.VISIBLE);

        // Get each request information and  Fill the Group/Child data of Expandable ListView
        ArrayList<Map<String, String>> mGroupList = new ArrayList<Map<String, String>>();
        ArrayList<ArrayList<Map<String, String>>> mChildList = new ArrayList<ArrayList<Map<String, String>>>();
        StringTokenizer st = new StringTokenizer(candidates_id, ";"); // Parse new request list (ex. work_list = ";13;52;1;32")
        int idx = 0;
        while (st.hasMoreTokens()) {
            String id_str = st.nextToken();
            Log.d(TAG, "id: " + id_str);

            JSONObject member_info = ProfileManager.getMemberInfo(Session.GetInstance(), id_str);

            Map<String, String> curr = new HashMap<String, String>();
            curr.put("NUM", Integer.toString(idx++));
            curr.put("MEMBER_ID", id_str);
            mGroupList.add(curr);

            ArrayList<Map<String, String>> children = new ArrayList<Map<String, String>>();
            Iterator<Object> itr = member_info.keySet().iterator();
            while (itr.hasNext()) {
                Object key = itr.next();
                String val = (String)member_info.get(key);
                if (val.equals("id"))
                    continue;
                Map<String, String> child = new HashMap<String, String>();
                child.put("ITEM", (String) key);
                child.put("DATA", val);
                children.add(child);
            }
            mChildList.add(children);
        }

        final ExpandableListView mExpListView = (ExpandableListView)findViewById(R.id.select_worker_view);
        mExpListView.setAdapter(new customExpandableListAdapter(
                ac.getApplicationContext(),
                mGroupList,
                R.layout.request_list_row,
                new String[]{"NUM", "MEMBER_ID"},
                new int[]{R.id.req_list_item_id, R.id.req_list_item_desc},
                mChildList,
                R.layout.detail_request_list_row,
                new String[]{"ITEM", "DATA"},
                new int[]{R.id.detail_req_list_item, R.id.detail_req_list_data},
                mExpListView, user_mode)
        );
        Log.d(TAG, "SetAdaptor");
        mExpListView.setVisibility(View.VISIBLE);
    }

    public class customExpandableListAdapter extends SimpleExpandableListAdapter {
        final private ExpandableListView mExpListView;
        final private int mUserMode;
        final List<? extends List<? extends Map<String, ?>>> mChildData;
        
        public customExpandableListAdapter(Context context,
                                           List<? extends Map<String, ?>> groupData, int groupLayout,
                                           String[] groupFrom, int[] groupTo,
                                           List<? extends List<? extends Map<String, ?>>> childData,
                                           int childLayout, String[] childFrom, int[] childTo,
                                           ExpandableListView expListView, int userMode) {
            super(context, groupData, groupLayout, groupLayout, groupFrom, groupTo, childData,
                    childLayout, childLayout, childFrom, childTo);
            mChildData = childData;
            mExpListView = expListView;
            mUserMode = userMode;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);
            Button select_btn = (Button) view.findViewById(R.id.request_apply_btn);

            Map<String, String> groupdata = (Map<String, String>) super.getGroup(groupPosition);
            select_btn.setTag(groupdata.get("MEMBER_ID"));
            select_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "select_btn onClick");
                    if (!RequestManager.employ(Session.GetInstance(), mRequest_id, (String)view.getTag()))
                        Log.d(TAG, "Failed to Employ, req_id:" + mRequest_id + ", member_id: " + (String)view.getTag());
                    finish();
                }
            });

            enableButton(select_btn, "Select");
            select_btn.setVisibility(View.VISIBLE);
            return view;
        }

        private void enableButton(final Button btn, String text) {
            btn.setText(text);
            btn.setBackgroundColor(0xFF54C7B8);
            btn.setClickable(true);
        }
    }
}
