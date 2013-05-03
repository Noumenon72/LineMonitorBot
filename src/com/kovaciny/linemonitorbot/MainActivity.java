package com.kovaciny.linemonitorbot;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	MenuItem mJobPicker;
	MenuItem mLinePicker;
	MenuItem mDebugDisplay;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		mJobPicker = (MenuItem) menu.findItem(R.id.action_pick_job);
		mLinePicker = (MenuItem) menu.findItem(R.id.action_pick_line);
		mDebugDisplay = (MenuItem) menu.findItem(R.id.debug_display);
		mDebugDisplay.setVisible(false);
		
		//populate the line picker with lines
		new PopulateMenusTask().execute();
		Menu pickLineSubMenu = mLinePicker.getSubMenu();
		pickLineSubMenu.clear();
		String lineList[] = getResources().getStringArray(R.array.line_list);
		for (int i=0; i<lineList.length; i++) {
			pickLineSubMenu.add(lineList[i]);
		}
		
		//populate the job picker with jobs
		Menu pickJobSubMenu = mJobPicker.getSubMenu();
		pickJobSubMenu.clear();
		String jobList[]= {"Wo #1", "Wo #2"};
		int jobListGroup = 0;
		for (int i=0; i<jobList.length; i++) {
			int menuId = i;
			pickJobSubMenu.add(jobListGroup, menuId, Menu.FLAG_APPEND_TO_GROUP, jobList[i]);
		}
		int jobOptionsGroup = 1;
		pickJobSubMenu.add(jobOptionsGroup, R.id.new_wo, Menu.FLAG_APPEND_TO_GROUP, "+ New");
		pickJobSubMenu.add(jobOptionsGroup, R.id.clear_wos, Menu.FLAG_APPEND_TO_GROUP, "Clear");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.new_wo:
	        item.setTitle("new wo");
	        return true;
	    }
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
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
						
			// Return a SectionFragment with the page number as its lone argument.
			Fragment fragment;
			switch(position){
			case 0:
				fragment = new SkidTimesFragment();
				break;
			case 1: 
				fragment = new RatesFragment();
				break;
			case 2:
				fragment = new DrainingFragment();
				break;
			default:
				fragment = new SectionFragment();
			}
			
			Bundle args = new Bundle();
			args.putInt(SectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}
	
	public class PopulateMenusTask extends AsyncTask<Void,Void,Void> {
		
		//Context mContext;
		
		public PopulateMenusTask() {
			super();
			//this.mContext = getApplicationContext();
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			// using mContext
		    SystemClock.sleep(5000);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			mLinePicker.setTitle("Async complete");
			super.onPostExecute(result);
		}
		
	
	}

}
