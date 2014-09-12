package com.hackthenorth.android.base;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.hackthenorth.android.framework.VisibilityManager;
import com.taplytics.sdk.Taplytics;
import com.taplytics.sdk.TaplyticsCodeExperimentListener;

import java.util.Map;

/**
 * A base class for activities. This is used to determine if our app is in the foreground. If you make a new activity, make sure to extend
 * from this base class, or else implement the operations that are given here.
 *
 * Note: See [1] for reference. [1]:
 * http://stackoverflow.com/questions/18038399/how-to-check-if-activity-is-in-foreground-or-in-visible-background
 */
public class BaseActivity extends Activity {

	String actionbarBackgroundColor = "#181244";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		runColorExperiment();
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(actionbarBackgroundColor)));
	}

	@Override
	public void onResume() {
		super.onResume();
		VisibilityManager.activityResumed();
	}

	@Override
	public void onPause() {
		super.onPause();
		VisibilityManager.activityPaused();
	}

	private void runColorExperiment() {
		Taplytics.runCodeExperiment("actionbar color", new TaplyticsCodeExperimentListener() {
			@Override
			public void baselineVariation(Map<String, Object> stringObjectMap) {
				if (stringObjectMap != null && !stringObjectMap.isEmpty()) {
					actionbarBackgroundColor = (String) stringObjectMap.get("color");
				} else {
					actionbarBackgroundColor = "#181244";
				}
				getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(actionbarBackgroundColor)));
			}

			@Override
			public void experimentVariation(String s, Map<String, Object> stringObjectMap) {
				actionbarBackgroundColor = (String) stringObjectMap.get("color");
				getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(actionbarBackgroundColor)));
			}

			@Override
			public void experimentUpdated() {
				runColorExperiment();
			}
		});
	}
}
