package com.slightsite.ucokinventory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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

/**
 * Created by mrsvette on 26/03/18.
 */

public class StockActivity extends MainActivity {
    Intent intent;
    ProgressDialog pDialog;
    int success;

    private static final String TAG = StockActivity.class.getSimpleName();
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
    private StockActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_stock);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // delay build the form after tabs fully finished
                buildTheStockList();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        intent = new Intent(StockActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final StockActivity.VolleyCallback callback) {
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

    private ArrayList get_list_warehouse() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "warehouse/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new StockActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                Log.e(TAG, "Response: " + data.toString());
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
            return 1;
        }
    }

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_stock_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_stock_2, container, false);
        }
    }

    private void buildTheStockList()
    {
        /*ArrayList get_list_warehouse = get_list_warehouse();
        Log.e(TAG, "Size : "+ get_list_warehouse.size());

        TableLayout table_layout = (TableLayout) findViewById(R.id.table_layout);


        for (int i = 0; i < get_list_warehouse.size(); i++) {
            TableRow row = new TableRow(this);
            //row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            TextView wh = new TextView(this);
            wh.setText("cuk "+ i);

            table_layout.addView(row);
            row.addView(wh);
        }*/
        final TableLayout table_layout = (TableLayout) findViewById(R.id.table_layout);
        final Context ini = this;

        Map<String, String> params = new HashMap<String, String>();

        final ArrayList<String> items = new ArrayList<String>();

        String stock_url = Server.URL + "stock/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, stock_url, params, false,
                new StockActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONObject data = jObj.getJSONObject("data");
                                Iterator<String> iter = data.keys();
                                Integer idx = 0;
                                while (iter.hasNext()) {
                                    String key = iter.next();
                                    try {
                                        String DK = "0";
                                        String DD = "0";
                                        String PC = "0";
                                        String DC = "0";
                                        if ( data.get(key) instanceof JSONObject ) {
                                            JSONObject iterate_data = new JSONObject(data.get(key).toString());
                                            JSONObject wh_data = new JSONObject(iterate_data.getString("wh_data"));
                                            if (iterate_data.getString("stock_data").length() > 2) {
                                                JSONArray stock_data = iterate_data.getJSONArray("stock_data");
                                                //Log.e(TAG, "Stock data : "+ stock_data.toString());
                                                for(int n = 0; n < stock_data.length(); n++)
                                                {
                                                    JSONObject stock_data_n = stock_data.getJSONObject(n);
                                                    if (stock_data_n.getString("code").equals("DK")) {
                                                        DK = stock_data_n.getString("quantity");
                                                    }
                                                    if (stock_data_n.getString("code").equals("DD")) {
                                                        DD = stock_data_n.getString("quantity");
                                                    }
                                                    if (stock_data_n.getString("code").equals("PC")) {
                                                        PC = stock_data_n.getString("quantity");
                                                    }
                                                    if (stock_data_n.getString("code").equals("DC")) {
                                                        DC = stock_data_n.getString("quantity");
                                                    }
                                                }
                                            }

                                            TableRow row = new TableRow(ini);
                                            TextView wh = new TextView(ini);
                                            wh.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                                    TableRow.LayoutParams.WRAP_CONTENT));

                                            wh.setGravity(Gravity.LEFT);
                                            wh.setPadding(5, 15, 0, 15);

                                            wh.setText(wh_data.getString("title"));

                                            //table_layout.addView(row);
                                            row.addView(wh);

                                            for(int n = 0; n < 4; n++)
                                            {
                                                TextView tv1 = new TextView(ini);
                                                tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

                                                tv1.setGravity(Gravity.LEFT);
                                                tv1.setPadding(0, 15, 0, 15);

                                                if (n == 0) {
                                                    tv1.setText(DK);
                                                } else if (n == 1) {
                                                    tv1.setText(DD);
                                                } else if (n == 2) {
                                                    tv1.setText(PC);
                                                } else if (n == 3) {
                                                    tv1.setText(DC);
                                                }

                                                row.addView(tv1);
                                            }
                                            idx ++;
                                            if ((idx % 2) == 0) {
                                                row.setBackgroundColor(Color.parseColor("#ebebeb"));
                                            }
                                            table_layout.addView(row);
                                        }
                                    } catch (JSONException e) {
                                        // Something went wrong!
                                        e.printStackTrace();
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
