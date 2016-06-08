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
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class WorklistViewer extends AppCompatActivity {
    static final String TAG = "[LTS][Worklist]";
    private String member_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worklist_viewer);

        int user_mode = ProfileManager.getUserMode(Session.GetInstance());
        Intent intent = getIntent();
        String worklist = intent.getStringExtra("worklist");
        // TODO: get member_id
        showWorklist(this, user_mode, worklist);
    }

    private void showWorklist(Activity activity, int user_mode, String worklist) {
        final Activity ac = activity;
        Log.d(TAG, "Show Work List");

        // Show title
        TextView title0 = (TextView) findViewById(R.id.worklist_view_title0);
        TextView title1 = (TextView) findViewById(R.id.worklist_view_title1);
        TextView title2 = (TextView) findViewById(R.id.worklist_view_title2);
        title0.setVisibility(View.VISIBLE);
        title1.setVisibility(View.VISIBLE);
        title2.setVisibility(View.VISIBLE);

        // Get each request information and  Fill the Group/Child data of Expandable ListView
        ArrayList<Map<String, String>> mGroupList = new ArrayList<Map<String, String>>();
        ArrayList<ArrayList<Map<String, String>>> mChildList = new ArrayList<ArrayList<Map<String, String>>>();
        String delimiters = null;
        if (worklist.contains(";"))
            delimiters = ";";
        else
            delimiters = " ";
        StringTokenizer st = new StringTokenizer(worklist, delimiters); // Parse new request list (ex. work_list = ";13;52;1;32")
        int idx = 0;
        while (st.hasMoreTokens()) {
            String id_str = st.nextToken();
            Log.d(TAG, "id: " + id_str);

            JSONObject request = RequestManager.getRequestInfo(Session.GetInstance(), Integer.parseInt(id_str));

            Map<String, String> curr = new HashMap<String, String>();
            curr.put("ID", id_str);
            curr.put("SUBJECT", (String)request.get("subject"));
            mGroupList.add(curr);

            ArrayList<Map<String, String>> children = new ArrayList<Map<String, String>>();
            Iterator<Object> itr = request.keySet().iterator();
            while (itr.hasNext()) {
                Object key = itr.next();
                String val = (String) request.get(key);
                String allowedTerm = ItemFilter.GetAllowedTermForRequestColumn((String) key, user_mode);
                if (allowedTerm == null) // Filtering
                    continue;
                Map<String, String> child = new HashMap<String, String>();
                child.put("ITEM", allowedTerm);
                val = val.replace(';', ' ');
                if (((String) key).contains("language"))
                    val = RequestManager.getLanuageString(Integer.parseInt(val));
                child.put("DATA", val);
                children.add(child);
            }
            mChildList.add(children);
        }

        final ExpandableListView mExpListView = (ExpandableListView) findViewById(R.id.worklist_view);
        mExpListView.setAdapter(new customExpandableListAdapter(
                ac.getApplicationContext(),
                mGroupList,
                R.layout.request_list_row,
                new String[]{"ID", "SUBJECT"},
                new int[]{R.id.req_list_item_id, R.id.req_list_item_desc},
                mChildList,
                R.layout.detail_request_list_row,
                new String[]{"ITEM", "DATA"},
                new int[]{R.id.detail_req_list_item, R.id.detail_req_list_data})
                //mExpListView, user_mode)
        );
        Log.d(TAG, "SetAdaptor");
        mExpListView.setVisibility(View.VISIBLE);
    }
    public static class customExpandableListAdapter extends SimpleExpandableListAdapter {
        public customExpandableListAdapter(Context context,
                                           List<? extends Map<String, ?>> groupData, int groupLayout,
                                           String[] groupFrom, int[] groupTo,
                                           List<? extends List<? extends Map<String, ?>>> childData,
                                           int childLayout, String[] childFrom, int[] childTo) {
            super(context, groupData, groupLayout, groupLayout, groupFrom, groupTo, childData,
                    childLayout, childLayout, childFrom, childTo);
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);
            TextView tview = (TextView) view.findViewById(R.id.req_list_item_desc);
            tview.setLayoutParams(new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            return view;
        }
    }
}
