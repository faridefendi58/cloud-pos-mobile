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
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

/**
 * Created by mrsvette on 02/04/18.
 */

public class InventoryActivity extends MainActivity {
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

        setDinamicContent(R.layout.app_bar_inventory);
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
                // define the product list
                //ArrayList list_products = get_list_product();
                buildTheProductList();
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
            return inflater.inflate(R.layout.tab_fragment_inventory_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_inventory_2, container, false);
        }
    }

    ArrayList<String> list_product_items = new ArrayList<String>();
    Map<String, String> product_names = new HashMap<String, String>();
    Map<String, String> product_units = new HashMap<String, String>();

    private ArrayList get_list_product() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "product/list?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of product request : " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                Log.e(TAG, "List Product : " + data.toString());
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));

                                    //RelativeLayout list_items = (RelativeLayout) findViewById(R.id.list_items);
                                    //buildTheCardView(list_items, data_n.getString("title"), n);

                                    /*list_product_items.add(data_n.getString("title"));
                                    product_names.put(data_n.getString("title"), data_n.getString("id"));
                                    product_units.put(data_n.getString("title"), data_n.getString("unit"));*/
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
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

    private void buildTheCardView(final RelativeLayout mRelativeLayout, String title, Integer i)
    {
        Log.e(TAG, "Loop : "+ i);
        Context mContext = InventoryActivity.this;
        // Initialize a new CardView
        CardView card = new CardView(mContext);

        /*LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);*/

        // Set the CardView layoutParams
        //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(160, 160);
        //params.width = 160;
        //params.height = 160; //ViewPager.LayoutParams.WRAP_CONTENT;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        //params.width = 180;
        params.height = 100;
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins(10, 10 + (i*110), 10, 10);

        card.setLayoutParams(params);

        // Set CardView corner radius
        card.setRadius(9);

        // Set cardView content padding
        card.setContentPadding(15, 25, 15, 25);

        // Set a background color for CardView
        card.setCardBackgroundColor(Color.parseColor("#FFC6D6C3"));
        //card.setCardBackgroundColor(null);

        // Set the CardView maximum elevation
        card.setMaxCardElevation(1);

        // Set CardView elevation
        card.setCardElevation(1);
        //card.setMinimumWidth(180);

        card.setFitsSystemWindows(true);
        card.setClickable(true);

        // Initialize a new TextView to put in CardView
        TextView tv = new TextView(mContext);
        tv.setLayoutParams(params);
        tv.setText(title);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv.setTextColor(Color.RED);

        // Initialize image icon
        ImageView iv = new ImageView(mContext);
        iv.setLayoutParams(params);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));

        // Put the TextView in CardView
        card.addView(tv);
        card.addView(iv);

        // Finally, add the CardView in root layout
        mRelativeLayout.addView(card);
    }

    private void buildTheProductList()
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();

        _string_request(
                Request.Method.GET,
                Server.URL + "product/list?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of product request : " + result.toString());
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

                                ListView listView = (ListView)findViewById(R.id.list_available_product);
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                        InventoryActivity.this,
                                        android.R.layout.simple_list_item_activated_1,
                                        items);

                                listView.setAdapter(arrayAdapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
