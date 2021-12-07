package com.amazon.tv.ftvstylekb

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.amazon.tv.ftvstylekb.SettingsActivity.Companion.getLauncherIconShown
import com.amazon.tv.ftvstylekb.SettingsActivity.Companion.setLauncherIconShown

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val enabled = getLauncherIconShown(context, SettingsActivity::class.java)
        val pref = findPreference<CheckBoxPreference>(context.getString(R.string.show_app_label_pref_key))
        if (pref != null) pref.isChecked = enabled
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        val context = requireContext()
        if (preference?.key == context.getString(R.string.show_app_label_pref_key)) {
            val hide = !getLauncherIconShown(context, SettingsActivity::class.java)
            setLauncherIconShown(context, SettingsActivity::class.java, hide)
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }
}