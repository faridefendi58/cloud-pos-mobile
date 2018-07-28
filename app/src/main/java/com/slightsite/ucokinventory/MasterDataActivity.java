package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class MasterDataActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;
    static final int NUM_TAB_ITEMS = 3;

    private static final String TAG = MasterDataActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private ListView list_product;
    private JSONArray product_data;
    private ArrayList<String> product_items = new ArrayList<String>();
    private AlertDialog dialog;

    // for dialog form
    private EditText input_product_title;
    private EditText input_product_code;
    private EditText input_product_unit;
    private EditText input_product_description;

    private MasterDataActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_master_data);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(NUM_TAB_ITEMS);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                intUi();
                getProductList();
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
            return inflater.inflate(R.layout.tab_fragment_master_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_master_2, container, false);
        }
    }

    public static class TabFragment3 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_master_3, container, false);
        }
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

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
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

    private void intUi() {
        list_product = (ListView) findViewById(R.id.list_product);
    }

    /**
     * Building the list of the product
     */
    private void getProductList() {

        Map<String, String> params = new HashMap<String, String>();
        //params.put("simply", "1");

        product_items.clear();

        String wh_url = Server.URL + "product/list?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                wh_url, params, true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                product_data = jObj.getJSONArray("data");
                                for(int n = 0; n < product_data.length(); n++)
                                {
                                    JSONObject data_n = product_data.getJSONObject(n);
                                    product_items.add(data_n.getString("title"));
                                }

                                reloadProductList();
                                productItemListener();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Reloading listview of the product
     */
    private void reloadProductList() {
        ArrayAdapter<String> productAdapter = new ArrayAdapter<String>(
                MasterDataActivity.this,
                android.R.layout.simple_list_item_activated_1,
                product_items);

        list_product.setAdapter(productAdapter);
    }

    /**
     * action closing the active dialog
     * @param view
     */
    public void closeDialog(View view) {
        dialog.hide();
    }

    /**
     * Add product dialog
     * @param view
     */
    public void addProduct(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MasterDataActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);

        builder.setView(mView);
        dialog = builder.create();

        input_product_title = (EditText) mView.findViewById(R.id.input_product_title);
        input_product_code = (EditText) mView.findViewById(R.id.input_product_code);
        input_product_unit = (EditText) mView.findViewById(R.id.input_product_unit);
        input_product_description = (EditText) mView.findViewById(R.id.input_product_description);

        trigger_product_dialog_button(mView);

        dialog.show();
    }

    /**
     * Submit and delete button execution for product dialog
     * @param mView
     */
    private void trigger_product_dialog_button(final View mView) {
        Button btn_dialog_submit = (Button) mView.findViewById(R.id.btn_dialog_submit);
        btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int error = 0;
                if (input_product_title.getText().toString().length() == 0
                        && input_product_code.getText().toString().length() == 0
                        && input_product_unit.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.msg_product_empty_field),
                            Toast.LENGTH_LONG).show();
                    error = error + 1;
                }

                if (error == 0) {
                    // submision
                    Map<String, String> post_params = new HashMap<String, String>();
                    post_params.put("admin_id", sharedpreferences.getString("id", null));
                    post_params.put("title", input_product_title.getText().toString());
                    post_params.put("code", input_product_code.getText().toString());
                    post_params.put("unit", input_product_unit.getText().toString());
                    post_params.put("description", input_product_description.getText().toString());

                    product_items.add(post_params.get("title"));
                    reloadProductList();

                    dialog.hide();
                }
            }
        });

        Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);
        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void productItemListener() {
        list_product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MasterDataActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);

                //String title = list_product.getItemAtPosition(i).toString();

                builder.setView(mView);
                dialog = builder.create();

                input_product_title = (EditText) mView.findViewById(R.id.input_product_title);
                input_product_code = (EditText) mView.findViewById(R.id.input_product_code);
                input_product_unit = (EditText) mView.findViewById(R.id.input_product_unit);
                input_product_description = (EditText) mView.findViewById(R.id.input_product_description);
                TextView dialog_title = (TextView) mView.findViewById(R.id.dialog_title);

                dialog_title.setText(getResources().getString(R.string.dialog_update_product));
                try {
                    input_product_title.setText(product_data.getJSONObject(i).getString("title"));
                    input_product_code.setText(product_data.getJSONObject(i).getString("code"));
                    input_product_unit.setText(product_data.getJSONObject(i).getString("unit"));
                    input_product_description.setText(product_data.getJSONObject(i).getString("description"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.show();
            }
        });
    }
}
