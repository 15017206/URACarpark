package com.example.a15017206.uracarpark;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class SettingsActivity extends AppCompatActivity {
    CheckBox cbCarLotsFreeAfter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbCarLotsFreeAfter = (CheckBox) findViewById(R.id.checkBox);

        cbCarLotsFreeAfter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cbCarLotsFreeAfter.isChecked()){
                    Intent r = new Intent();
                    r.putExtra("showLotsAfter5", true);
                    setResult(Activity.RESULT_OK,r);
                }
            }
        });
    }
}
