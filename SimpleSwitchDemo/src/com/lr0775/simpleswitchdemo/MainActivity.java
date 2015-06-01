package com.lr0775.simpleswitchdemo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.lr0775.simpleswitch.R;
import com.lr0775.widget.SimpleSwitch;

public class MainActivity extends ActionBarActivity implements
		OnCheckedChangeListener, OnClickListener {

	private SimpleSwitch mSimpleSwitch;
	private TextView mSimpleSwitchTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSimpleSwitchTv = (TextView) findViewById(R.id.simple_switch_tv);
		mSimpleSwitch = (SimpleSwitch) findViewById(R.id.simple_switch);
		mSimpleSwitch.setOnCheckedChangeListener(this);
		mSimpleSwitch.setChecked(true);

		findViewById(R.id.open_simple_switch_btn).setOnClickListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mSimpleSwitchTv.setText("SimpleSwitch is : "
				+ (isChecked ? "On" : "Off"));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.open_simple_switch_btn:
			mSimpleSwitch.setChecked(!mSimpleSwitch.isChecked());
			break;
		default:
			break;
		}
	}
}
