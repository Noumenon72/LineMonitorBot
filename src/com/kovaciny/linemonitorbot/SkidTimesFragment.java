package com.kovaciny.linemonitorbot;

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.kovaciny.helperfunctions.HelperFunction;
import com.kovaciny.primexmodel.PrimexModel;
import com.kovaciny.primexmodel.Product;
import com.kovaciny.primexmodel.Skid;
import com.kovaciny.primexmodel.WorkOrder;

public class SkidTimesFragment extends SectionFragment implements
		OnClickListener, OnEditorActionListener, OnItemSelectedListener, View.OnFocusChangeListener, ViewEventResponder	{
	private SkidFinishedBroadcastReceiver mAlarmReceiver;
	private List<Skid<Product>> mSkidList;
	private Button mBtn_enterProduct;
	private Button mBtn_cancelAlarm;
	private Button mBtn_calculateTimes;
	
	private List<View> mEditableGroup;
	private List<TextView> mTimesDisplayList;
	
	private EditText mEdit_currentSkidNumber;
	private EditText mEdit_currentCount;
	private EditText mEdit_totalCountPerSkid;
	private EditText mEdit_numSkidsInJob;

	private TextView mTxt_timePerSkid;
	private TextView mLbl_productsPerMinute;
	private TextView mTxt_productsPerMinute;
	private TextView mLbl_totalProducts;
	private TextView mLbl_products;
	private TextView mTxt_jobFinishTime;
	private TextView mLbl_jobFinishTime;
	private TextView mLbl_timeToMaxson;
	private TextView mTxt_timeToMaxson;
	private TextView mTxt_skidStartTime;
	private TextView mTxt_skidFinishTime;
	
	private long mMillisPerSkid;
	private String mJobFinishText;
	private String mTimeToMaxsonText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAlarmReceiver = new SkidFinishedBroadcastReceiver();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.skid_times_fragment, container,
				false);

		//set up editTexts
		mEditableGroup = new ArrayList<View>(); //TODO make the finish times noneditable since they're not in the group.
		
		
		
		mEdit_currentSkidNumber = (EditText) rootView.findViewById(R.id.edit_skid_number);
		mEditableGroup.add(mEdit_currentSkidNumber);
		
		mEdit_numSkidsInJob = (EditText) rootView
				.findViewById(R.id.edit_num_skids_in_job);
		//needs to be added before skid number so skid number will be in range when checked
		
		mEdit_currentCount = (EditText) rootView
				.findViewById(R.id.edit_current_count);
		mEditableGroup.add(mEdit_currentCount);
		
		mEdit_totalCountPerSkid = (EditText) rootView
				.findViewById(R.id.edit_total_sheets_per_skid);
		mEditableGroup.add(mEdit_totalCountPerSkid);
		
		for (View v : mEditableGroup) {
			EditText etv = (EditText) v;
			etv.setOnEditorActionListener(this);
			etv.setOnFocusChangeListener(this);
		}

		//set up buttons
		mBtn_enterProduct = (Button) rootView
				.findViewById(R.id.btn_enter_product);
		mBtn_enterProduct.setOnClickListener(this);
		
		mBtn_cancelAlarm = (Button) rootView.findViewById(R.id.btn_cancel_alarm);
		mBtn_cancelAlarm.setOnClickListener(this);
				
		mBtn_calculateTimes = (Button) rootView.findViewById(R.id.btn_calculate_times);
		mBtn_calculateTimes.setOnClickListener(this);
		
		//set up textViews
		mTxt_timePerSkid = (TextView) rootView.findViewById(R.id.txt_time_per_skid);		
		mTxt_jobFinishTime = (TextView) rootView.findViewById(R.id.txt_job_finish_time);
		mTxt_timeToMaxson = (TextView) rootView.findViewById(R.id.txt_time_to_maxson);
		mTxt_productsPerMinute = (TextView) rootView.findViewById(R.id.txt_products_per_minute);
		mTxt_skidStartTime = (TextView) rootView.findViewById(R.id.txt_skid_start_time);
		mTxt_skidFinishTime = (TextView) rootView.findViewById(R.id.txt_skid_finish_time);
		
		mTimesDisplayList = Arrays.asList(new TextView[]{mTxt_timePerSkid, mTxt_jobFinishTime, mTxt_timeToMaxson,
				mTxt_productsPerMinute, mTxt_skidStartTime, mTxt_skidFinishTime});
		
		mLbl_productsPerMinute = (TextView) rootView.findViewById(R.id.lbl_products_per_minute);
		mLbl_products = (TextView) rootView.findViewById(R.id.lbl_products);
		mLbl_timeToMaxson = (TextView) rootView.findViewById(R.id.lbl_time_to_maxson);
		mLbl_jobFinishTime = (TextView) rootView.findViewById(R.id.lbl_job_finish_time);
		
		//restore saved state
		SharedPreferences settings = this.getActivity().getPreferences(Context.MODE_PRIVATE);
		boolean visible = settings.getBoolean("cancelAlarmVisible", false);
		String tps = settings.getString("timePerSkid", "");
		String ppm = settings.getString("productsPerMinute", "");
		String jft = settings.getString("jobFinishTime", "");
		String ttm = settings.getString("timeToMaxson", "");
		String sst = settings.getString("skidStartTime", "");
		String sft = settings.getString("skidFinishTime", "");
		if (visible) {
			mBtn_cancelAlarm.setVisibility(Button.VISIBLE);
		} else mBtn_cancelAlarm.setVisibility(Button.INVISIBLE);
		mTxt_timePerSkid.setText(tps);
		mTxt_productsPerMinute.setText(ppm);
		mTxt_jobFinishTime.setText(jft);
		mTxt_timeToMaxson.setText(ttm);
		mTxt_skidStartTime.setText(sst);
		mTxt_skidFinishTime.setText(sft);
		
		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.TextView.OnEditorActionListener#onEditorAction(android
	 * .widget.TextView, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent arg2) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see android.view.View.OnFocusChangeListener#onFocusChange(android.view.View, boolean)
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if ( !hasFocus && mEditableGroup.contains(v)) {
		}
	}
	
	public List<Skid<Product>> getSkidList() {
		return mSkidList;
	}

	public void setSkidList(List<Skid<Product>> skidList) {
		this.mSkidList = skidList;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
			long arg3) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.btn_enter_product):
			mBtn_enterProduct.setError(null);
			((MainActivity)getActivity()).showSheetsPerMinuteDialog();
			break;
		case (R.id.btn_calculate_times):
			//supply default values and validate entries
			if (mEdit_currentSkidNumber.getText().toString().equals("")) {
				mEdit_currentSkidNumber.setText("1");
			}
			String numSkids = mEdit_numSkidsInJob.getText().toString();
			String skidNumber = mEdit_currentSkidNumber.getText().toString();
			if ( numSkids.equals("") || ( Integer.valueOf(skidNumber) > Integer.valueOf(numSkids)) ) {
				mEdit_numSkidsInJob.setText(skidNumber);
			}
			//Process the entries only if all the necessary ones are filled.
			Iterator<View> itr = mEditableGroup.iterator();
			boolean validInputs = true;
			if (mEdit_totalCountPerSkid.getText().toString().equals("0")) {
				mEdit_totalCountPerSkid.setError(getString(R.string.error_need_nonzero));
				validInputs = false;
			} else {
				while (itr.hasNext()) {
					EditText et = (EditText)itr.next();
					if ( et.getText().toString().trim().equals("") ) {
						et.setError(getString(R.string.error_empty_field));
						validInputs = false;
					}
				}	
			}
			if (validInputs) {
				((MainActivity)getActivity()).hideKeyboard();
				mBtn_enterProduct.setError(null);
				try {
					((MainActivity)getActivity()).updateSkidData(
							Integer.valueOf(mEdit_currentSkidNumber.getText().toString()),
							Integer.valueOf(mEdit_currentCount.getText().toString()),
							Integer.valueOf(mEdit_totalCountPerSkid.getText().toString()),
							Integer.valueOf(mEdit_numSkidsInJob.getText().toString())
							);
				} catch (IllegalStateException e) {
					if (e.getCause().getMessage().equals(PrimexModel.ERROR_NO_PRODUCT_SELECTED)) {
						mBtn_enterProduct.setError("Please enter a product");
					} else {
						throw e;
					}
				}
					
				for (View ett : mEditableGroup) {
					((EditText)ett).setError(null);
				}
				mTxt_productsPerMinute.setVisibility(TextView.VISIBLE);
				mTxt_timeToMaxson.setVisibility(TextView.VISIBLE);
				this.mTxt_timePerSkid.setText(mTimeToMaxsonText);
			}
			break;
		case (R.id.btn_cancel_alarm):
			cancelAlarm();
			break;
		}
	}

	public void onetimeTimer(View v, long interval) {

		Context context = getActivity();
		if (mAlarmReceiver != null) {
			mAlarmReceiver.setOnetimeTimer(context, interval);
		} else {
			Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
		}
		mBtn_cancelAlarm.setVisibility(Button.VISIBLE);
	}
	
	public void repeatingTimer(View v, long trigger, long interval) {

		Context context = getActivity();
		if (mAlarmReceiver != null) {
			mAlarmReceiver.setRepeatingTimer(context, trigger, interval);
		} else {
			Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
		}
		mBtn_cancelAlarm.setVisibility(Button.VISIBLE);
	}

	public void cancelAlarm() {
		Context context = getActivity();
		
		Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vib.vibrate(new long[]{0,10},-1);
		vib.cancel();
		
		Intent intent = new Intent(context, SkidFinishedBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        pi.cancel();
        
        mBtn_cancelAlarm.setVisibility(Button.INVISIBLE);
	}
	public void modelPropertyChange (PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		Object newProperty = event.getNewValue();
		
		if (propertyName == PrimexModel.PRODUCT_CHANGE_EVENT) {
			String units = ((Product)newProperty).getUnits();
			StringBuilder capUnits = new StringBuilder(units);
			capUnits.setCharAt(0, Character.toUpperCase(units.charAt(0)));
			this.mLbl_productsPerMinute.setText(capUnits.toString() + " per minute");
			this.mLbl_products.setText(capUnits.toString() + ":");
			
		} else if (propertyName == PrimexModel.PRODUCTS_PER_MINUTE_CHANGE_EVENT) {
			this.mTxt_productsPerMinute.setText(String.valueOf(newProperty));
			this.mTxt_productsPerMinute.setVisibility(TextView.GONE); //Don't show it till user clicks the get times button
			
		} else if (propertyName == PrimexModel.CURRENT_SKID_FINISH_TIME_CHANGE_EVENT) {
			//update finish time for this skid
			SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.US);
			String formattedTime = formatter.format((Date)newProperty);
			mTxt_skidFinishTime.setText(formattedTime);
			
			//set alarm 
			long alarmLeadTime = (long) (1.5 * HelperFunction.ONE_MINUTE_IN_MILLIS); //TODO
			Date curDate = new Date();
			long timeNow = curDate.getTime();
			long timeThen = ((Date)newProperty).getTime();
			Long triggerAtMillis = timeThen - timeNow - alarmLeadTime;
			if (triggerAtMillis > 0) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
				Boolean repeatPref = sharedPref.getBoolean("repeating_timer", false);
				if (repeatPref) {
					repeatingTimer( mTxt_skidFinishTime, triggerAtMillis, mMillisPerSkid );	
				} else {
					onetimeTimer(mTxt_skidFinishTime, triggerAtMillis);
				}
								
			}			
		} else if (propertyName == PrimexModel.CURRENT_SKID_START_TIME_CHANGE_EVENT) {
			SimpleDateFormat formatter2 = new SimpleDateFormat("h:mm a", Locale.US);
			String formattedTime2 = formatter2.format((Date)newProperty);
			mTxt_skidStartTime.setText(formattedTime2);
			
		} else if (propertyName == PrimexModel.MINUTES_PER_SKID_CHANGE_EVENT) {
			long minutes = Math.round((Double)newProperty);
			mTimeToMaxsonText =	HelperFunction.formatMinutesAsHours(minutes);
			mMillisPerSkid = Math.round((Double)newProperty * HelperFunction.ONE_MINUTE_IN_MILLIS);
			
		} else if (propertyName == PrimexModel.NUMBER_OF_SKIDS_CHANGE_EVENT) {
			mEdit_numSkidsInJob.setText(String.valueOf(newProperty));
			
		} else if (propertyName == PrimexModel.JOB_FINISH_TIME_CHANGE_EVENT) {
			Date finishTime = (Date)newProperty;
			SimpleDateFormat formatter3 = new SimpleDateFormat("h:mm a E", Locale.US);

			//"Pace time": Don't show the day of the week if it's before 6 am the next day. 
			Calendar finishDate = new GregorianCalendar(Locale.US);
			finishDate.setTime(finishTime);
			Calendar today = Calendar.getInstance(Locale.US);
			today.add(Calendar.DAY_OF_MONTH, 1);
			today.set(Calendar.HOUR_OF_DAY, 6);
			today.set(Calendar.MINUTE, 0);
			if (finishDate.before(today)) {
				formatter3 = new SimpleDateFormat("h:mm a", Locale.US);
			}
			
			mJobFinishText = formatter3.format(finishTime);
			mTxt_jobFinishTime.setText(mJobFinishText);
			
		} else if (propertyName == PrimexModel.SKID_CHANGE_EVENT) {
			Skid<Product> skid = (Skid<Product>)newProperty;
			mEdit_currentSkidNumber.setText(String.valueOf(skid.getSkidNumber()));
			mEdit_currentCount.setText(String.valueOf(skid.getCurrentItems()));
			mEdit_totalCountPerSkid.setText(String.valueOf(skid.getTotalItems()));
			
		} else if (propertyName == PrimexModel.TIME_TO_MAXSON_CHANGE_EVENT) {
			Double timeToMaxson = (Double)newProperty;
			mTimeToMaxsonText = HelperFunction.formatSecondsAsMinutes(Math.round(timeToMaxson / HelperFunction.ONE_SECOND_IN_MILLIS));
			mTxt_timeToMaxson.setText(mTimeToMaxsonText);
			mTxt_timeToMaxson.setVisibility(TextView.INVISIBLE); //don't show it unless the user clicks the get times button
			
		} else if (propertyName == PrimexModel.NEW_WORK_ORDER_EVENT) {
			for (TextView tv : mTimesDisplayList) {
				tv.setText("");
			}
			
		} else if (propertyName == PrimexModel.SELECTED_WO_CHANGE_EVENT) {
			WorkOrder wo = (WorkOrder)newProperty;
			Skid<Product> skid = wo.getSelectedSkid();
			mEdit_currentSkidNumber.setText(String.valueOf(skid.getSkidNumber()));
		} 
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		//save the user's current state
		super.onSaveInstanceState(outState);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		SharedPreferences settings = this.getActivity().getPreferences(Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
		boolean visible = false;
		if (mBtn_cancelAlarm.getVisibility() == Button.VISIBLE) {
			visible = true;
		}
		editor.putBoolean("cancelAlarmVisible", visible);
		editor.putString("timePerSkid", mTxt_timePerSkid.getText().toString());
		editor.putString("productsPerMinute", mTxt_productsPerMinute.getText().toString());
		editor.putString("jobFinishTime", mTxt_jobFinishTime.getText().toString());
		editor.putString("timeToMaxson", mTxt_timeToMaxson.getText().toString());
		editor.putString("skidStartTime", mTxt_skidStartTime.getText().toString());
		editor.putString("skidFinishTime", mTxt_skidFinishTime.getText().toString());

	    // Commit the edits!
	    editor.commit();
		super.onPause();
	}
}
