package com.slightsite.ucokinventory;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PurchaseReportActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseReportActivity.class.getSimpleName();
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
    private PurchaseReportActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_purchase_report);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        final Context ini = this;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // delay build the form after tabs fully finished
                String issue_number = getIntent().getStringExtra("issue_number");
                if (!TextUtils.isEmpty(issue_number)) {
                    Log.e(TAG, "Ada kiriman nomor issue : "+ issue_number);
                    set_detail_issue(issue_number);
                }
            }
        }, 1000);
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

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_p_detail_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_p_detail_2, container, false);
        }
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
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
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

    public interface VolleyCallback {
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

    private void set_detail_issue(String issue_number) {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("issue_number", issue_number);

        _string_request(
                Request.Method.GET,
                Server.URL + "purchase/detail?api-key=" + Server.API_KEY,
                params, true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONObject data = jObj.getJSONObject("data");

                                TextView txt_po_number = (TextView) findViewById(R.id.txt_po_number);
                                txt_po_number.setText(data.getString("po_number"));
                                TextView txt_supplier_name = (TextView) findViewById(R.id.txt_supplier_name);
                                txt_supplier_name.setText(data.getString("supplier_name"));
                                TextView txt_wh_group_name = (TextView) findViewById(R.id.txt_wh_group_name);
                                txt_wh_group_name.setText(data.getString("wh_group_name"));
                                TextView txt_shipment_name = (TextView) findViewById(R.id.txt_shipment_name);
                                txt_shipment_name.setText(data.getString("shipment_name"));
                                TextView txt_created_at = (TextView) findViewById(R.id.txt_created_at);
                                txt_created_at.setText(data.getString("created_at"));
                                TextView txt_created_by = (TextView) findViewById(R.id.txt_created_by);
                                txt_created_by.setText(data.getString("created_by_name"));

                                TextView txt_status = (TextView) findViewById(R.id.txt_status);
                                txt_status.setText(data.getString("status"));

                                JSONArray items = jObj.getJSONArray("items");
                                ArrayList<String> po_items = new ArrayList<String>();

                                for(int n = 0; n < items.length(); n++)
                                {
                                    JSONObject data_n = new JSONObject(items.getString(n));
                                    String item_title = data_n.getString("title")+' '+data_n.getString("quantity")
                                            + " " + data_n.getString("unit");
                                    po_items.add(item_title);
                                }

                                ArrayAdapter adapter = new ArrayAdapter<String>(PurchaseReportActivity.this, R.layout.activity_list_view, po_items);

                                ListView txt_list_items = (ListView) findViewById(R.id.txt_list_items);
                                txt_list_items.setAdapter(adapter);
                                DeliveryActivity.updateListViewHeight(txt_list_items, 20);

                                JSONArray history = jObj.getJSONArray("history");
                                ArrayList<String> list_timeline_ids = new ArrayList<String>();
                                ArrayList<String> list_timeline_titles = new ArrayList<String>();
                                ArrayList<String> list_timeline_descs = new ArrayList<String>();

                                for(int m = 0; m < history.length(); m++)
                                {
                                    JSONObject data_n = new JSONObject(history.getString(m));
                                    list_timeline_ids.add(data_n.getString("date"));
                                    list_timeline_titles.add(data_n.getString("title"));

                                    if (!TextUtils.isEmpty(data_n.getString("notes"))) {
                                        list_timeline_descs.add(data_n.getString("notes"));
                                    } else {
                                        list_timeline_descs.add("-");
                                    }
                                }

                                CustomListAdapter adapter2 = new CustomListAdapter(PurchaseReportActivity.this, list_timeline_ids, list_timeline_titles, list_timeline_descs, R.layout.list_view_timeline);

                                ListView txt_list_timelines = (ListView) findViewById(R.id.txt_list_timelines);
                                txt_list_timelines.setAdapter(adapter2);
                                DeliveryActivity.updateListViewHeight(txt_list_timelines, 120);

                                Log.e(TAG, "Titles : "+ list_timeline_titles.toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
