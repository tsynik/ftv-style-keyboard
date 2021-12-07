package com.amazon.tv.ftvstylekb

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    // var enabled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
    }

    //    @Override
    //    public void onResume() {
    //        super.onResume();
    //        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    //        enabled = prefs.getBoolean("show_app_label", true);
    //        setLauncherIconShown(this, SettingsActivity.class, enabled);
    //    }

    companion object {
        fun setLauncherIconShown(context: Context?, activityClass: Class<*>?, shown: Boolean) {
            val pm = context?.packageManager
            val component = ComponentName(context!!, activityClass!!)
            pm?.setComponentEnabledSetting(
                    component,
                    if (shown) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            )
        }

        fun getLauncherIconShown(context: Context?, activityClass: Class<*>?): Boolean {
            val pm = context?.packageManager
            val component = ComponentName(context!!, activityClass!!)
            return pm?.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
    }
}