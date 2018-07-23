package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Spinner;
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
    JSONObject po_detail;
    static final int NUM_TAB_ITEMS = 3;

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
        mViewPager.setOffscreenPageLimit(NUM_TAB_ITEMS);

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
                    set_detail_issue(issue_number);
                    initUi();
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
                case 2:
                    TabFragment3 tab3 = new TabFragment3();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_TAB_ITEMS;
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

    public static class TabFragment3 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_p_detail_3, container, false);
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
                                po_detail = jObj.getJSONObject("data");

                                TextView txt_po_number = (TextView) findViewById(R.id.txt_po_number);
                                txt_po_number.setText(po_detail.getString("po_number"));
                                TextView txt_supplier_name = (TextView) findViewById(R.id.txt_supplier_name);
                                txt_supplier_name.setText(po_detail.getString("supplier_name"));
                                TextView txt_wh_group_name = (TextView) findViewById(R.id.txt_wh_group_name);
                                txt_wh_group_name.setText(po_detail.getString("wh_group_name"));
                                TextView txt_shipment_name = (TextView) findViewById(R.id.txt_shipment_name);
                                txt_shipment_name.setText(po_detail.getString("shipment_name"));
                                TextView txt_created_at = (TextView) findViewById(R.id.txt_created_at);
                                txt_created_at.setText(po_detail.getString("created_at"));
                                TextView txt_created_by = (TextView) findViewById(R.id.txt_created_by);
                                txt_created_by.setText(po_detail.getString("created_by_name"));

                                TextView txt_status = (TextView) findViewById(R.id.txt_status);
                                txt_status.setText(po_detail.getString("status"));

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

                                //Log.e(TAG, "Titles : "+ list_timeline_titles.toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void cancelPO(View view) {
        Log.e(TAG, "Detail : " + po_detail.toString());
        String po_status = null;
        try {
            po_status = po_detail.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (po_status.equals("pending")) {
            AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                    PurchaseReportActivity.this);
            quitDialog.setTitle(getResources().getString(R.string.dialog_remove_po));
            quitDialog.setPositiveButton(getResources().getString(R.string.btn_remove), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent newActivity = new Intent(PurchaseReportActivity.this,
                            PurchaseActivity.class);
                    startActivity(newActivity);
                }
            });

            quitDialog.setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            quitDialog.show();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.msg_unable_remove_po),
                    Toast.LENGTH_LONG).show();
        }
    }

    private TextView update_po_number;
    private Spinner supplier_name;
    private Spinner shipment_name;
    private Spinner wh_group_name;

    private ArrayList supplier_list;
    private ArrayList shipment_list;
    private ArrayList<String> shipment_list_id = new ArrayList<String>();
    private ArrayList<String> group_whs = new ArrayList<String>();

    private void initUi() {
        update_po_number = (TextView) findViewById(R.id.update_po_number);
        supplier_name = (Spinner) findViewById(R.id.supplier_name);
        shipment_name = (Spinner) findViewById(R.id.shipment_name);
        wh_group_name = (Spinner) findViewById(R.id.wh_group_name);

        supplier_list = get_list_supplier();
        shipment_list = get_list_shipment();
        list_assigned_wh();
    }

    private ArrayList get_list_supplier() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "supplier/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }

    private ArrayList get_list_shipment() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "shipment/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                    shipment_list_id.add(data_n.getString("id"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }

    private void list_assigned_wh() {

        String roles = sharedpreferences.getString(TAG_ROLES, null);
        try {
            JSONObject jsonObject = new JSONObject(roles);
            JSONArray keys = jsonObject.names();

            for (int i = 0; i < keys.length (); ++i) {
                String key = keys.getString (i); // Here's your key
                String value = jsonObject.getString (key); // Here's your value
                JSONObject data_n = jsonObject.getJSONObject(key);
                if (!group_whs.contains(data_n.getString("warehouse_group_name"))) {
                    group_whs.add(data_n.getString("warehouse_group_name"));
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList get_list_product() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "product/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        //Log.e(TAG, "Response of product request : " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                //Log.e(TAG, "List Product : " + data.toString());
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                    //list_product_items.add(data_n.getString("title"));
                                    //product_names.put(data_n.getString("title"), data_n.getString("id"));
                                    //product_units.put(data_n.getString("title"), data_n.getString("unit"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }

    public void updatePO(View view) {
        mViewPager.setCurrentItem(2, true);

        try {
            update_po_number.setText(po_detail.getString("po_number"));

            // init the spinner of list supplier
            ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(
                    PurchaseReportActivity.this,
                    R.layout.spinner_item, supplier_list);
            supplier_name.setAdapter(spAdapter);
            supplier_name.setSelection(supplier_list.indexOf(po_detail.getString("supplier_name")));

            // init the spinner of list shipment
            ArrayAdapter<String> shAdapter = new ArrayAdapter<String>(
                    PurchaseReportActivity.this,
                    R.layout.spinner_item, shipment_list);
            shipment_name.setAdapter(shAdapter);
            shipment_name.setSelection(shipment_list_id.indexOf(po_detail.getString("shipment_id")));

            // init the spinner of list wh group
            ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                    PurchaseReportActivity.this,
                    R.layout.spinner_item, group_whs);
            wh_group_name.setAdapter(whAdapter);
            wh_group_name.setSelection(group_whs.indexOf(po_detail.getString("wh_group_name")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
