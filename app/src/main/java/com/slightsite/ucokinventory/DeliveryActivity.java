package com.slightsite.ucokinventory;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class DeliveryActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = DeliveryActivity.class.getSimpleName();
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

        setDinamicContent(R.layout.app_bar_delivery);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        buildThePreOrderList();
        buildTheDeliveryOrderList();
    }

    public void buildMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View v = navigationView.getHeaderView(0);
        TextView txt_full_name = (TextView) v.findViewById(R.id.txt_full_name);
        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        txt_full_name.setText( sharedpreferences.getString(TAG_NAME, null) );
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_delivery, container, false);
            LinearLayout layout_1 = (LinearLayout) rootView.findViewById(R.id.layout_1);
            LinearLayout layout_2 = (LinearLayout) rootView.findViewById(R.id.layout_2);
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                layout_1.setVisibility(View.VISIBLE);
                layout_2.setVisibility(View.GONE);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                layout_2.setVisibility(View.VISIBLE);
                layout_1.setVisibility(View.GONE);
            }
            return rootView;
        }
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
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
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

    final ArrayList<String> list_items = new ArrayList<String>();
    final ArrayList<String> list_descs = new ArrayList<String>();
    final ArrayList<String> list_ids = new ArrayList<String>();

    public void buildThePreOrderList() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("status", "pending");
        params.put("is_pre_order", "1");

        final ArrayList<String> descs = new ArrayList<String>();
        _string_request(
                Request.Method.GET,
                Server.URL + "purchase/list?api-key=" + Server.API_KEY,
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
                                JSONObject origins = new JSONObject(jObj.getString("origin"));
                                JSONObject destinations = new JSONObject(jObj.getString("destination"));

                                for(int n = 0; n < data.length(); n++)
                                {
                                    list_items.add(data.getString(n));
                                    list_ids.add(data.getString(n));
                                    String desc = "Pengadaan dari " + origins.getString(data.getString(n)) +
                                            " dengan tujuan area warehouse " + destinations.getString(data.getString(n));
                                    list_descs.add(desc);
                                }

                                CustomListAdapter adapter2 = new CustomListAdapter(DeliveryActivity.this, list_ids, list_items, list_descs, R.layout.list_view_notification);

                                ListView list_pre_order = (ListView) findViewById(R.id.list_pre_order);
                                list_pre_order.setAdapter(adapter2);
                                itemListener(list_pre_order);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
                detail_desc.setText(list_descs.get(i));
                LinearLayout layout_1 = (LinearLayout) findViewById(R.id.layout_1);
                layout_1.setVisibility(View.GONE);
                LinearLayout layout_1_detail = (LinearLayout) findViewById(R.id.layout_1_detail);
                layout_1_detail.setVisibility(View.VISIBLE);
            }
        });

        Button btn_pre_order_back = (Button) findViewById(R.id.btn_pre_order_back);
        btn_pre_order_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout_1 = (LinearLayout) findViewById(R.id.layout_1);
                layout_1.setVisibility(View.VISIBLE);
                LinearLayout layout_1_detail = (LinearLayout) findViewById(R.id.layout_1_detail);
                layout_1_detail.setVisibility(View.GONE);
            }
        });
    }

    final ArrayList<String> list_do_items = new ArrayList<String>();
    final ArrayList<String> list_do_descs = new ArrayList<String>();
    final ArrayList<String> list_do_ids = new ArrayList<String>();

    public void buildTheDeliveryOrderList() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);

        _string_request(
                Request.Method.GET,
                Server.URL + "delivery/list?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of delivery: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                JSONObject po_data = new JSONObject(jObj.getString("po_data"));
                                JSONObject origins = new JSONObject(jObj.getString("po_origin"));
                                JSONObject destinations = new JSONObject(jObj.getString("po_destination"));

                                for(int n = 0; n < data.length(); n++)
                                {
                                    list_do_items.add(data.getString(n));
                                    list_do_ids.add(data.getString(n));
                                    String desc = "Dikirim dari " + origins.getString(data.getString(n)) +
                                            " dengan tujuan area warehouse " + destinations.getString(data.getString(n));
                                    list_do_descs.add(desc);
                                }

                                Log.e(TAG, "List do items: " + list_do_items.toString());
                                Log.e(TAG, "List do ids: " + list_do_ids.toString());
                                Log.e(TAG, "List do descs: " + list_do_descs.toString());

                                //CustomListAdapter adapter3 = new CustomListAdapter(DeliveryActivity.this, list_do_ids, list_do_items, list_do_descs, R.layout.list_view_notification);
                                ArrayAdapter adapter3 = new ArrayAdapter<String>(DeliveryActivity.this,
                                        R.layout.activity_list_view, list_do_items);

                                ListView list_do_status = (ListView) findViewById(R.id.list_do_status);
                                list_do_status.setAdapter(adapter3);
                                //itemListener(list_pre_order);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
