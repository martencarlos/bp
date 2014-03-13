package ceu.marten.ui.dialogs;

import ceu.marten.bplux.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;

/**
 * 
 * @author Carlos Marten
 *
 */
public class SeekBarPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	public static final String KEY_ZOOM_VALUE = "zoomSummary";
	
	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private Context mContext;

	private String mDialogMessage;
	private int mDefault, mMax, mValue = 0;
	private SharedPreferences sharePref = null;
	private int changedValue;

	/**
	 * Default constructor called before displaying on settings activity
	 * @param context
	 * @param attrs
	 */
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		sharePref = PreferenceManager.getDefaultSharedPreferences(mContext);
		int value = 500;
		if(sharePref.contains(KEY_ZOOM_VALUE))
			value = sharePref.getInt(KEY_ZOOM_VALUE, 500);
		String summary = mContext
				.getString(R.string.sa_zoom_summary_message)
				+ " "
				+ String.valueOf(value)
				+ " "
				+ mContext.getString(R.string.sa_zoom_unit_measure);
		
		setSummary(summary);
		mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
		
		

	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(8, 8, 8, 8);

		mSplashText = new TextView(mContext);
		if (mDialogMessage != null)
			mSplashText.setText(mDialogMessage);
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(22);
		mValueText.setPadding(0, 0, 0, 15);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setPadding(30, 0, 30, 15);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		if (shouldPersist())
			mValue = getPersistedInt(mDefault);

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		
		
		
		return layout;
	}

	@Override
	protected void onBindDialogView(View dialogView) {
		super.onBindDialogView(dialogView);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}

	/**
	 * Updates textView value with current seekbar value
	 */
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String currentValue = String.valueOf(value);
		changedValue = value;
		mValueText.setText(currentValue + " " + mContext.getString(R.string.sa_zoom_unit_measure) );
				
		callChangeListener(Integer.valueOf(value));
	}

	// Required by seekBarListener
	public void onStartTrackingTouch(SeekBar seek) {}
	public void onStopTrackingTouch(SeekBar seek) {}

	
	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setProgress(int progress) {
		mValue = progress;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress);
	}

	public int getProgress() {
		return mValue;
	}
	
	
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult && shouldPersist()){
			persistInt(changedValue);
			String summary = mContext
					.getString(R.string.sa_zoom_summary_message)
					+ " "
					+ String.valueOf(changedValue)
					+ " "
					+ mContext.getString(R.string.sa_zoom_unit_measure);
			Editor edit = sharePref.edit();
			edit.putInt(KEY_ZOOM_VALUE, changedValue);
			edit.commit();
			setSummary(summary);
		}
		
		super.onDialogClosed(positiveResult);
	}

}