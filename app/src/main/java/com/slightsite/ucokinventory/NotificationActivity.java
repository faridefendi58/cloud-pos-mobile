package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NotificationActivity extends MainActivity {
    Intent intent;
    ProgressDialog pDialog;
    int success;

    private static final String TAG = NotificationActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_notification);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        final Context ini = this;
        buildTheNotifList(ini);
        buildTheArchiveNotifList(ini);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    TabFragment1 tab1 = new TabFragment1();
                    return tab1;
                case 1:
                    TabFragment2 tab2 = new TabFragment2();
                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    final ArrayList<String> list_items = new ArrayList<String>();
    final ArrayList<String> list_messages = new ArrayList<String>();
    final ArrayList<String> list_ids = new ArrayList<String>();

    final Map<String, String> list_issues = new HashMap<String, String>();
    final Map<String, String> list_activities = new HashMap<String, String>();

    private void buildTheNotifList(final Context ini) {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);

        final ArrayList<String> descs = new ArrayList<String>();
        _string_request(
                Request.Method.GET,
                Server.URL + "notification/list?api-key=" + Server.API_KEY,
                params,
                true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response: " + result.toString());
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");

                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = new JSONObject(data.getString(n));
                                    list_items.add(data_n.getString("created_at"));
                                    list_messages.add(data_n.getString("message"));
                                    list_ids.add(data_n.getString("id"));

                                    if (!TextUtils.isEmpty(data_n.getString("rel_detail")) && !data_n.getString("rel_detail").equals("null")) {
                                        JSONObject data_n_detail = new JSONObject(data_n.getString("rel_detail"));
                                        if (!TextUtils.isEmpty(data_n.getString("issue_number"))) {
                                            list_issues.put(data_n.getString("id"), data_n.getString("issue_number"));
                                        } else {
                                            if (data_n.getString("rel_type").equals("purchase_order")) {
                                                list_issues.put(data_n.getString("id"), data_n_detail.getString("po_number"));
                                            } else if (data_n.getString("rel_type").equals("transfer_issue")) {
                                                list_issues.put(data_n.getString("id"), data_n_detail.getString("ti_number"));
                                            } else if (data_n.getString("rel_type").equals("delivery_order")) {
                                                list_issues.put(data_n.getString("id"), data_n_detail.getString("do_number"));
                                            }
                                        }

                                        list_activities.put(data_n.getString("id"), data_n.getString("rel_activity"));
                                    }
                                }

                                CustomListAdapter adapter2 = new CustomListAdapter(NotificationActivity.this, list_ids, list_items, list_messages, R.layout.list_view_notification);

                                ListView list_notification = (ListView) findViewById(R.id.list_notification);
                                list_notification.setAdapter(adapter2);
                                itemListener(list_notification);
                                DeliveryActivity.updateListViewHeight(list_notification, 150);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                url += "&" + pair.getKey() + "=" + pair.getValue();
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener < String > () {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "Request Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                if (show_dialog) {
                    hideDialog();
                }
            }
        })
        {
            // set headers
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void itemListener(final ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String title = list.getItemAtPosition(i).toString();
                TextView id = (TextView) view.findViewById(R.id.list_id);
                TextView title = (TextView) view.findViewById(R.id.list_title);
                TextView desc = (TextView) view.findViewById(R.id.list_desc);

                TextView detail_title = (TextView) findViewById(R.id.detail_title);
                detail_title.setText(list_items.get(i));

                TextView detail_desc = (TextView) findViewById(R.id.detail_desc);
                detail_desc.setText(list_messages.get(i));
                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                step1.setVisibility(View.GONE);
                LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                step2.setVisibility(View.VISIBLE);
                mark_as_viewed(id.getText().toString());

                TextView detail_activity = (TextView) findViewById(R.id.detail_activity);
                detail_activity.setText(list_activities.get(list_ids.get(i)));

                TextView detail_issue_number = (TextView) findViewById(R.id.detail_issue_number);
                detail_issue_number.setText(list_issues.get(list_ids.get(i)));
            }
        });

        Button btn_step2_back = (Button) findViewById(R.id.btn_step2_back);
        btn_step2_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                step2.setVisibility(View.GONE);
                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                step1.setVisibility(View.VISIBLE);
            }
        });

        Button btn_step2_see = (Button) findViewById(R.id.btn_step2_see);
        btn_step2_see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView detail_activity = (TextView) findViewById(R.id.detail_activity);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                if (detail_activity.getText().toString().equals("MainActivity")) {
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                } else if (detail_activity.getText().toString().equals("ReceiptActivity")) {
                    intent = new Intent(getApplicationContext(), ReceiptActivity.class);
                } else if (detail_activity.getText().toString().equals("PurchaseActivity")) {
                    intent = new Intent(getApplicationContext(), PurchaseActivity.class);
                } else if (detail_activity.getText().toString().equals("DeliveryActivity")) {
                    intent = new Intent(getApplicationContext(), DeliveryActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                }

                TextView detail_issue_number = (TextView) findViewById(R.id.detail_issue_number);
                if (!TextUtils.isEmpty(detail_issue_number.getText().toString())) {
                    intent.putExtra("issue_number", detail_issue_number.getText().toString());
                }

                startActivity(intent);
            }
        });
    }

    private void mark_as_viewed(String notification_id)
    {
        Log.e(TAG, "Id : "+ notification_id);
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("notification_id", notification_id);

        _string_request(
                Request.Method.POST,
                Server.URL + "notification/read?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                Log.e(TAG, jObj.getString(TAG_MESSAGE));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_ntf_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_ntf_2, container, false);
        }
    }

    final ArrayList<String> list_archive_items = new ArrayList<String>();
    final ArrayList<String> list_archive_messages = new ArrayList<String>();
    final ArrayList<String> list_archive_ids = new ArrayList<String>();
    final Map<String, String> list_archive_issues = new HashMap<String, String>();
    final Map<String, String> list_archive_activities = new HashMap<String, String>();

    private void buildTheArchiveNotifList(final Context ini) {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("status", "read");

        final ArrayList<String> descs = new ArrayList<String>();
        _string_request(
                Request.Method.GET,
                Server.URL + "notification/list?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Archive Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");

                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = new JSONObject(data.getString(n));
                                    list_archive_items.add(data_n.getString("created_at"));
                                    list_archive_messages.add(data_n.getString("message"));
                                    list_archive_ids.add(data_n.getString("id"));
                                    if (!TextUtils.isEmpty(data_n.getString("rel_detail")) && !data_n.getString("rel_detail").equals("null")) {
                                        JSONObject data_n_detail = new JSONObject(data_n.getString("rel_detail"));
                                        if (data_n.getString("rel_type").equals("purchase_order")) {
                                            list_archive_issues.put(data_n.getString("id"), data_n_detail.getString("po_number"));
                                        } else if (data_n.getString("rel_type").equals("transfer_issue")) {
                                            list_archive_issues.put(data_n.getString("id"), data_n_detail.getString("ti_number"));
                                        } else if (data_n.getString("rel_type").equals("delivery_order")) {
                                            list_archive_issues.put(data_n.getString("id"), data_n_detail.getString("do_number"));
                                        }
                                        list_archive_activities.put(data_n.getString("id"), data_n.getString("rel_activity"));
                                    }
                                }

                                CustomListAdapter adapter2 = new CustomListAdapter(NotificationActivity.this, list_archive_ids, list_archive_items, list_archive_messages, R.layout.list_view_notification);

                                ListView list_archive_notification = (ListView) findViewById(R.id.list_archive_notification);
                                list_archive_notification.setAdapter(adapter2);
                                itemArchiveListener(list_archive_notification);
                                DeliveryActivity.updateListViewHeight(list_archive_notification, 150);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void itemArchiveListener(final ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView id = (TextView) view.findViewById(R.id.list_id);
                TextView title = (TextView) view.findViewById(R.id.list_title);
                TextView desc = (TextView) view.findViewById(R.id.list_desc);

                TextView detail_title = (TextView) findViewById(R.id.detail_archive_title);
                detail_title.setText(list_archive_items.get(i));

                TextView detail_desc = (TextView) findViewById(R.id.detail_archive_desc);
                detail_desc.setText(list_archive_messages.get(i));
                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1_2);
                step1.setVisibility(View.GONE);
                LinearLayout step2 = (LinearLayout) findViewById(R.id.step2_2);
                step2.setVisibility(View.VISIBLE);

                Log.e(TAG, "Archive issue : "+ list_archive_issues.get(list_archive_ids.get(i)));

                TextView detail_archive_activity = (TextView) findViewById(R.id.detail_archive_activity);
                detail_archive_activity.setText(list_archive_activities.get(list_archive_ids.get(i)));

                TextView detail_archive_issue_number = (TextView) findViewById(R.id.detail_archive_issue_number);
                detail_archive_issue_number.setText(list_archive_issues.get(list_archive_ids.get(i)));
            }
        });

        Button btn_step2_2_back = (Button) findViewById(R.id.btn_step2_2_back);
        btn_step2_2_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout step2 = (LinearLayout) findViewById(R.id.step2_2);
                step2.setVisibility(View.GONE);
                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1_2);
                step1.setVisibility(View.VISIBLE);
            }
        });

        Button btn_step2_2_see = (Button) findViewById(R.id.btn_step2_2_see);
        btn_step2_2_see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView detail_archive_activity = (TextView) findViewById(R.id.detail_archive_activity);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                if (detail_archive_activity.getText().toString().equals("MainActivity")) {
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                } else if (detail_archive_activity.getText().toString().equals("ReceiptActivity")) {
                    intent = new Intent(getApplicationContext(), ReceiptActivity.class);
                } else if (detail_archive_activity.getText().toString().equals("PurchaseActivity")) {
                    intent = new Intent(getApplicationContext(), PurchaseActivity.class);
                } else if (detail_archive_activity.getText().toString().equals("DeliveryActivity")) {
                    intent = new Intent(getApplicationContext(), DeliveryActivity.class);
                } else if (detail_archive_activity.getText().toString().equals("TransferActivity")) {
                    intent = new Intent(getApplicationContext(), TransferActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                }

                TextView detail_archive_issue_number = (TextView) findViewById(R.id.detail_archive_issue_number);
                if (!TextUtils.isEmpty(detail_archive_issue_number.getText().toString())) {
                    intent.putExtra("issue_number", detail_archive_issue_number.getText().toString());
                }

                startActivity(intent);
            }
        });
    }
}
