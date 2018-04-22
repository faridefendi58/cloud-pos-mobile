package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_notification);
        buildMenu();

        final Context ini = this;
        buildTheNotifList(ini);
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

    final ArrayList<String> list_items = new ArrayList<String>();
    final ArrayList<String> list_messages = new ArrayList<String>();
    final ArrayList<String> list_ids = new ArrayList<String>();

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
                                }

                                CustomListAdapter adapter2 = new CustomListAdapter(NotificationActivity.this, list_ids, list_items, list_messages, R.layout.list_view_notification);

                                ListView list_notification = (ListView) findViewById(R.id.list_notification);
                                list_notification.setAdapter(adapter2);
                                itemListener(list_notification);
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
}
