package com.slightsite.ucokinventory;

import android.os.Bundle;

/**
 * Created by mrsvette on 02/04/18.
 */

public class InventoryActivity extends MainActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_inventory);
        buildMenu();
    }
}
