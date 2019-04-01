package com.fsck.k9.activity.setup;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.fsck.k9.DaoSession;
import com.fsck.k9.DrunkMode;
import com.fsck.k9.K9;
import com.fsck.k9.activity.K9PreferenceActivity;
import com.fsck.k9.activity.SetDrunkModeTime;

import com.fsck.k9.R;

import java.util.Calendar;
import java.util.Date;

public class DrunkModeSettings extends K9PreferenceActivity {

    private CheckBoxPreference isDrunkCheckbox;
    private Preference setDrunkTimePreference;
    private DaoSession daoSession;
    private DrunkMode drunkModeSettings;
    private String strStart;
    private String strEnd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null &&
                savedInstanceState.getBoolean("refresh needed", false)){
            Bundle noUpdate = new Bundle();
            noUpdate.putBoolean("refresh needed", false);
            getIntent().replaceExtras(noUpdate);
            recreate();
        }

        // Always picks the single entry
        daoSession = ((K9)getApplication()).getDaoSession();
        drunkModeSettings = daoSession.getDrunkModeDao().loadByRowId(1);

        addPreferencesFromResource(R.xml.drunk_mode_settings_preferences);

        setDrunkTimePreference = findPreference("drunk_mode_settings_time");
        isDrunkCheckbox = (CheckBoxPreference)findPreference("drunk_mode_settings_toggle");
        strStart = dateToCalendarFormat(drunkModeSettings.getStartTime());
        strEnd = dateToCalendarFormat(drunkModeSettings.getEndTime());
        setDrunkTimePreference.setSummary("Start Time: " + strStart + "; End Time: " + strEnd);

        isDrunkCheckbox.setChecked(drunkModeSettings.getIsDrunk());
        setDrunkTimePreference.setEnabled(drunkModeSettings.getIsDrunk());

        isDrunkCheckbox.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Boolean isDrunk = !drunkModeSettings.getIsDrunk();
                        setDrunkTimePreference.setEnabled(isDrunk);
                        isDrunkCheckbox.setChecked(isDrunk);
                        drunkModeSettings.setIsDrunk(isDrunk);
                        return true;
                    }
                }
        );

        setDrunkTimePreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getApplicationContext(), SetDrunkModeTime.class));
                        return true;
                    }
                }
        );
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        daoSession.getDrunkModeDao().update(drunkModeSettings);
        daoSession.clear();
    }

    private String dateToCalendarFormat(Date time){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(time);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String strTime = (hourOfDay%12) + ":" + ((minute < 10) ? "0" + minute : minute) + (hourOfDay > 12 ? " PM" : " AM");
        return strTime;
    }

}
