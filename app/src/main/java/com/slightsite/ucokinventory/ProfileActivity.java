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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProfileActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;
    static final int NUM_TAB_ITEMS = 2;

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private ProfileActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_profile);
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
                initUi();
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
            return NUM_TAB_ITEMS;
        }
    }

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_profile_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_profile_2, container, false);
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

    private EditText input_name;
    private EditText input_email;
    private EditText input_username;
    private EditText input_phone;

    private EditText input_old_password;
    private EditText input_new_password;
    private EditText input_new_password_confirm;

    private void initUi() {
        input_name = (EditText) findViewById(R.id.input_name);
        input_email = (EditText) findViewById(R.id.input_email);
        input_username = (EditText) findViewById(R.id.input_username);
        input_phone = (EditText) findViewById(R.id.input_phone);

        input_name.setText(sharedpreferences.getString("name", null));
        input_email.setText(sharedpreferences.getString("email", null));
        input_username.setText(sharedpreferences.getString("username", null));
        if (sharedpreferences.getString("phone", null) != null) {
            input_phone.setText(sharedpreferences.getString("phone", null));
        }

        input_old_password = (EditText) findViewById(R.id.input_old_password);
        input_new_password = (EditText) findViewById(R.id.input_new_password);
        input_new_password_confirm = (EditText) findViewById(R.id.input_new_password_confirm);
    }

    public void updateProfile(View view) {
        Map<String, String> post_params = new HashMap<String, String>();
        post_params.put("admin_id", sharedpreferences.getString("id", null));
        post_params.put("name", input_name.getText().toString());
        post_params.put("email", input_email.getText().toString());
        post_params.put("username", input_username.getText().toString());
        post_params.put("phone", input_phone.getText().toString());

        int error = 0;
        if (post_params.get("name").length() <= 0
                || post_params.get("email").length() <= 0
                || post_params.get("username").length() <= 0) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.msg_password_empty_field), Toast.LENGTH_LONG).show();
            error = error + 1;
        }

        Log.e(TAG, "post_params : "+ post_params.toString());

        if (error == 0) {
            String url = Server.URL + "user/update?api-key=" + Server.API_KEY;
            _string_request(
                    Request.Method.POST,
                    url,
                    post_params, true,
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
        }
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

    public void updatePassword(View view) {
        Map<String, String> post_params = new HashMap<String, String>();
        post_params.put("admin_id", sharedpreferences.getString("id", null));
        post_params.put("old_password", input_old_password.getText().toString());
        post_params.put("new_password", input_new_password.getText().toString());

        Log.e(TAG, "post_params : "+ post_params.toString());

        int error = 0;
        if (post_params.get("old_password").length() <= 0
                || post_params.get("old_password").length() <= 0
                || post_params.get("old_password").length() <= 0) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.msg_require_all_field), Toast.LENGTH_LONG).show();
            error = error + 1;
        }

        if (!input_new_password_confirm.getText().toString().equals(input_new_password.getText().toString())) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.msg_password_different), Toast.LENGTH_LONG).show();
            error = error + 1;
        }

        if (error == 0) {
            String url = Server.URL + "user/change-password?api-key=" + Server.API_KEY;
            _string_request(
                    Request.Method.POST,
                    url,
                    post_params, true,
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
        }
    }
}
