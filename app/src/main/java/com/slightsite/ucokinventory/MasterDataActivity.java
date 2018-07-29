package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.Dialog;
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

    private ListView list_supplier;
    private JSONArray supplier_data;
    private ArrayList<String> supplier_items = new ArrayList<String>();

    private ListView list_shipment;
    private JSONArray shipment_data;
    private ArrayList<String> shipment_items = new ArrayList<String>();

    private AlertDialog dialog;

    // for dialog form
    private EditText input_product_title;
    private EditText input_product_code;
    private EditText input_product_unit;
    private EditText input_product_description;
    private EditText input_supplier_name;
    private EditText input_supplier_address;
    private EditText input_supplier_phone;
    private EditText input_supplier_notes;
    private EditText input_shipment_title;
    private EditText input_shipment_description;

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
                getSupplierList();
                getShipmentList();
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
        list_supplier = (ListView) findViewById(R.id.list_supplier);
        list_shipment = (ListView) findViewById(R.id.list_shipment);
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

        trigger_product_dialog_button(mView, false);

        dialog.show();
    }

    /**
     * Submit and delete button execution for product dialog
     * @param mView
     */
    private void trigger_product_dialog_button(final View mView, final Boolean is_update) {
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);

        if (is_update) {
            btn_dialog_cancel.setVisibility(View.GONE);
            btn_dialog_delete.setVisibility(View.VISIBLE);
        }

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

                    if (!is_update) {
                        product_items.add(post_params.get("title"));
                        // build the product data
                        JSONObject additional_data = new JSONObject();
                        try {
                            additional_data.put("id", 0);
                            additional_data.put("code", post_params.get("code"));
                            additional_data.put("title", post_params.get("title"));
                            additional_data.put("unit", post_params.get("unit"));
                            additional_data.put("description", post_params.get("description"));

                            product_data.put(additional_data);
                            // execute create data on server

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        product_items.set(selected_list_product, post_params.get("title"));
                        try {
                            product_data.getJSONObject(selected_list_product).put("code", post_params.get("code"));
                            product_data.getJSONObject(selected_list_product).put("title", post_params.get("title"));
                            product_data.getJSONObject(selected_list_product).put("unit", post_params.get("unit"));
                            product_data.getJSONObject(selected_list_product).put("description", post_params.get("description"));
                            if (product_data.getJSONObject(selected_list_product).getInt("id") > 0) {
                                post_params.put("id", product_data.getJSONObject(selected_list_product).getString("id"));
                                // execute update on server
                                //Boolean update = _execute();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    reloadProductList();

                    dialog.hide();
                }
            }
        });

        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        MasterDataActivity.this);
                quitDialog.setTitle(getResources().getString(R.string.dialog_remove_confirm));
                quitDialog.setPositiveButton(getResources().getString(R.string.btn_remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            executingDeleteProduct();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        reloadProductList();
                    }
                });

                quitDialog.setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mdialog, int which) {
                        dialog.show();
                    }
                });
                quitDialog.show();
            }
        });
    }

    private int selected_list_product;

    /**
     * Onclick product items
     */
    private void productItemListener() {
        list_product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MasterDataActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);

                //String title = list_product.getItemAtPosition(i).toString();
                selected_list_product = i;

                builder.setView(mView);
                dialog = builder.create();

                input_product_title = (EditText) mView.findViewById(R.id.input_product_title);
                input_product_code = (EditText) mView.findViewById(R.id.input_product_code);
                input_product_unit = (EditText) mView.findViewById(R.id.input_product_unit);
                input_product_description = (EditText) mView.findViewById(R.id.input_product_description);
                TextView dialog_title = (TextView) mView.findViewById(R.id.dialog_title);

                dialog_title.setText(getResources().getString(R.string.dialog_update_product));
                try {
                    JSONObject product_detail = product_data.getJSONObject(i);
                    input_product_title.setText(product_detail.getString("title"));
                    input_product_code.setText(product_detail.getString("code"));
                    input_product_unit.setText(product_detail.getString("unit"));
                    input_product_description.setText(product_detail.getString("description"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                trigger_product_dialog_button(mView, true);

                dialog.show();
            }
        });
    }

    /**
     * Delete product execution
     */
    private void executingDeleteProduct() {
        Map<String, String> post_params = new HashMap<String, String>();
        post_params.put("admin_id", sharedpreferences.getString("id", null));
        try {
            post_params.put("id", product_data.getJSONObject(selected_list_product).getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Integer.parseInt(post_params.get("id")) > 0) {
            // execute delete in server
        }

        product_data.remove(selected_list_product);
        product_items.remove(selected_list_product);
    }

    /**
     * Create, Update, Delete execution
     * @param url
     * @param params
     * @return
     */
    private boolean _execute(String url, Map params) {
        _string_request(
                Request.Method.POST,
                url,
                params,
                true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                Toast.makeText(getApplicationContext(),
                                        jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return (success > 0)? true : false;
    }

    /**
     * Geting suplier data
     */
    private void getSupplierList() {

        Map<String, String> params = new HashMap<String, String>();
        //params.put("simply", "1");

        supplier_items.clear();

        String url = Server.URL + "supplier/list?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                supplier_data = jObj.getJSONArray("data");
                                for(int n = 0; n < supplier_data.length(); n++)
                                {
                                    JSONObject data_n = supplier_data.getJSONObject(n);
                                    supplier_items.add(data_n.getString("name"));
                                }

                                reloadSupplierList();
                                supplierItemListener();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Reloading listview of the supplier
     */
    private void reloadSupplierList() {
        ArrayAdapter<String> supplierAdapter = new ArrayAdapter<String>(
                MasterDataActivity.this,
                android.R.layout.simple_list_item_activated_1,
                supplier_items);

        list_supplier.setAdapter(supplierAdapter);
    }

    /**
     * Add supplier dialog
     * @param view
     */
    public void addSupplier(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MasterDataActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_supplier, null);

        builder.setView(mView);
        dialog = builder.create();

        input_supplier_name = (EditText) mView.findViewById(R.id.input_supplier_name);
        input_supplier_address = (EditText) mView.findViewById(R.id.input_supplier_address);
        input_supplier_phone = (EditText) mView.findViewById(R.id.input_supplier_phone);
        input_supplier_notes = (EditText) mView.findViewById(R.id.input_supplier_notes);

        trigger_supplier_dialog_button(mView, false);

        dialog.show();
    }

    /**
     * Submit and delete button execution for product dialog
     * @param mView
     */
    private void trigger_supplier_dialog_button(final View mView, final Boolean is_update) {
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);

        if (is_update) {
            btn_dialog_cancel.setVisibility(View.GONE);
            btn_dialog_delete.setVisibility(View.VISIBLE);
        }

        Button btn_dialog_submit = (Button) mView.findViewById(R.id.btn_dialog_submit);
        btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int error = 0;
                if (input_supplier_name.getText().toString().length() == 0
                        && input_supplier_address.getText().toString().length() == 0
                        && input_supplier_phone.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.msg_supplier_empty_field),
                            Toast.LENGTH_LONG).show();
                    error = error + 1;
                }

                if (error == 0) {
                    // submision
                    Map<String, String> post_params = new HashMap<String, String>();
                    post_params.put("admin_id", sharedpreferences.getString("id", null));
                    post_params.put("name", input_supplier_name.getText().toString());
                    post_params.put("address", input_supplier_address.getText().toString());
                    post_params.put("phone", input_supplier_phone.getText().toString());
                    post_params.put("notes", input_supplier_notes.getText().toString());

                    if (!is_update) {
                        supplier_items.add(post_params.get("name"));
                        // build the product data
                        JSONObject additional_data = new JSONObject();
                        try {
                            additional_data.put("id", 0);
                            additional_data.put("name", post_params.get("name"));
                            additional_data.put("address", post_params.get("address"));
                            additional_data.put("phone", post_params.get("phone"));
                            additional_data.put("notes", post_params.get("notes"));

                            supplier_data.put(additional_data);
                            // execute create data on server

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        supplier_items.set(selected_list_supplier, post_params.get("name"));
                        try {
                            supplier_data.getJSONObject(selected_list_supplier).put("name", post_params.get("name"));
                            supplier_data.getJSONObject(selected_list_supplier).put("address", post_params.get("address"));
                            supplier_data.getJSONObject(selected_list_supplier).put("phone", post_params.get("phone"));
                            supplier_data.getJSONObject(selected_list_supplier).put("notes", post_params.get("notes"));
                            if (supplier_data.getJSONObject(selected_list_supplier).getInt("id") > 0) {
                                post_params.put("id", supplier_data.getJSONObject(selected_list_supplier).getString("id"));
                                // execute update on server
                                //Boolean update = _execute();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    reloadSupplierList();

                    dialog.hide();
                }
            }
        });

        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        MasterDataActivity.this);
                quitDialog.setTitle(getResources().getString(R.string.dialog_remove_confirm));
                quitDialog.setPositiveButton(getResources().getString(R.string.btn_remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            executingDeleteSupplier();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        reloadSupplierList();
                    }
                });

                quitDialog.setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mdialog, int which) {
                        dialog.show();
                    }
                });
                quitDialog.show();
            }
        });
    }

    private int selected_list_supplier;

    /**
     * Onclick product items
     */
    private void supplierItemListener() {
        list_supplier.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MasterDataActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_supplier, null);

                //String title = list_product.getItemAtPosition(i).toString();
                selected_list_supplier = i;

                builder.setView(mView);
                dialog = builder.create();

                input_supplier_name = (EditText) mView.findViewById(R.id.input_supplier_name);
                input_supplier_address = (EditText) mView.findViewById(R.id.input_supplier_address);
                input_supplier_phone = (EditText) mView.findViewById(R.id.input_supplier_phone);
                input_supplier_notes = (EditText) mView.findViewById(R.id.input_supplier_notes);
                TextView dialog_title = (TextView) mView.findViewById(R.id.dialog_title);

                dialog_title.setText(getResources().getString(R.string.dialog_update_supplier));
                try {
                    JSONObject supplier_detail = supplier_data.getJSONObject(i);
                    input_supplier_name.setText(supplier_detail.getString("name"));
                    input_supplier_address.setText(supplier_detail.getString("address"));
                    input_supplier_phone.setText(supplier_detail.getString("phone"));
                    input_supplier_notes.setText(supplier_detail.getString("notes"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                trigger_supplier_dialog_button(mView, true);

                dialog.show();
            }
        });
    }

    /**
     * Delete supplier execution
     */
    private void executingDeleteSupplier() {
        Map<String, String> post_params = new HashMap<String, String>();
        post_params.put("admin_id", sharedpreferences.getString("id", null));
        try {
            post_params.put("id", supplier_data.getJSONObject(selected_list_supplier).getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Integer.parseInt(post_params.get("id")) > 0) {
            // execute delete in server
        }

        supplier_data.remove(selected_list_supplier);
        supplier_items.remove(selected_list_supplier);
    }

    /**
     * Geting shipment data
     */
    private void getShipmentList() {

        Map<String, String> params = new HashMap<String, String>();
        //params.put("simply", "1");

        shipment_items.clear();

        String url = Server.URL + "shipment/list?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                shipment_data = jObj.getJSONArray("data");
                                for(int n = 0; n < shipment_data.length(); n++)
                                {
                                    JSONObject data_n = shipment_data.getJSONObject(n);
                                    shipment_items.add(data_n.getString("title"));
                                }

                                reloadShipmentList();
                                shipmentItemListener();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Reloading listview of the shipment
     */
    private void reloadShipmentList() {
        ArrayAdapter<String> shipmentAdapter = new ArrayAdapter<String>(
                MasterDataActivity.this,
                android.R.layout.simple_list_item_activated_1,
                shipment_items);

        list_shipment.setAdapter(shipmentAdapter);
    }

    /**
     * Add shipment dialog
     * @param view
     */
    public void addShipment(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MasterDataActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_shipment, null);

        builder.setView(mView);
        dialog = builder.create();

        input_shipment_title = (EditText) mView.findViewById(R.id.input_shipment_title);
        input_shipment_description = (EditText) mView.findViewById(R.id.input_shipment_description);

        trigger_shipment_dialog_button(mView, false);

        dialog.show();
    }

    /**
     * Submit and delete button execution for shipment dialog
     * @param mView
     */
    private void trigger_shipment_dialog_button(final View mView, final Boolean is_update) {
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);

        if (is_update) {
            btn_dialog_cancel.setVisibility(View.GONE);
            btn_dialog_delete.setVisibility(View.VISIBLE);
        }

        Button btn_dialog_submit = (Button) mView.findViewById(R.id.btn_dialog_submit);
        btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int error = 0;
                if (input_shipment_title.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.msg_shipment_empty_field),
                            Toast.LENGTH_LONG).show();
                    error = error + 1;
                }

                if (error == 0) {
                    // submision
                    Map<String, String> post_params = new HashMap<String, String>();
                    post_params.put("admin_id", sharedpreferences.getString("id", null));
                    post_params.put("title", input_shipment_title.getText().toString());
                    post_params.put("description", input_shipment_description.getText().toString());

                    if (!is_update) {
                        shipment_items.add(post_params.get("title"));
                        // build the product data
                        JSONObject additional_data = new JSONObject();
                        try {
                            additional_data.put("id", 0);
                            additional_data.put("title", post_params.get("title"));
                            additional_data.put("description", post_params.get("description"));

                            shipment_data.put(additional_data);
                            // execute create data on server

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        shipment_items.set(selected_list_shipment, post_params.get("title"));
                        try {
                            shipment_data.getJSONObject(selected_list_shipment).put("title", post_params.get("title"));
                            shipment_data.getJSONObject(selected_list_shipment).put("description", post_params.get("description"));
                            if (shipment_data.getJSONObject(selected_list_shipment).getInt("id") > 0) {
                                post_params.put("id", shipment_data.getJSONObject(selected_list_shipment).getString("id"));
                                // execute update on server
                                //Boolean update = _execute();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    reloadShipmentList();

                    dialog.hide();
                }
            }
        });

        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                        MasterDataActivity.this);
                quitDialog.setTitle(getResources().getString(R.string.dialog_remove_confirm));
                quitDialog.setPositiveButton(getResources().getString(R.string.btn_remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            executingDeleteShipment();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        reloadShipmentList();
                    }
                });

                quitDialog.setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mdialog, int which) {
                        dialog.show();
                    }
                });
                quitDialog.show();
            }
        });
    }

    private int selected_list_shipment;

    /**
     * Onclick shipment items
     */
    private void shipmentItemListener() {
        list_shipment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MasterDataActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_shipment, null);

                selected_list_shipment = i;

                builder.setView(mView);
                dialog = builder.create();

                input_shipment_title = (EditText) mView.findViewById(R.id.input_shipment_title);
                input_shipment_description = (EditText) mView.findViewById(R.id.input_shipment_description);
                TextView dialog_title = (TextView) mView.findViewById(R.id.dialog_title);

                dialog_title.setText(getResources().getString(R.string.dialog_update_shipment));
                try {
                    JSONObject shipment_detail = shipment_data.getJSONObject(i);
                    input_shipment_title.setText(shipment_detail.getString("title"));
                    input_shipment_description.setText(shipment_detail.getString("description"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                trigger_shipment_dialog_button(mView, true);

                dialog.show();
            }
        });
    }

    /**
     * Delete shipment execution
     */
    private void executingDeleteShipment() {
        Map<String, String> post_params = new HashMap<String, String>();
        post_params.put("admin_id", sharedpreferences.getString("id", null));
        try {
            post_params.put("id", shipment_data.getJSONObject(selected_list_shipment).getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Integer.parseInt(post_params.get("id")) > 0) {
            // execute delete in server
        }

        shipment_data.remove(selected_list_shipment);
        shipment_items.remove(selected_list_shipment);
    }
}
