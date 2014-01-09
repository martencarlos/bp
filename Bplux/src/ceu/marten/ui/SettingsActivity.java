package ceu.marten.ui;

import ceu.marten.bplux.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	public static final String KEY_PREF_CONF_CONFIG = "ask_confirmation_configuration";
	public static final String KEY_PREF_CONF_REC = "ask_confirmation_recording";
	
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_preference);
    }
}