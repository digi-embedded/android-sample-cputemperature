/*
 * Copyright (c) 2014-2021, Digi International Inc. <support@digi.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.digi.android.sample.cputemperature;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.digi.android.system.cpu.CPUManager;
import com.digi.android.system.cpu.ICPUTemperatureListener;
import com.digi.android.system.cpu.exception.CPUTemperatureException;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * CPU Temperature sample application.
 *
 * <p>This example displays the current CPU temperature using the CPU
 * Temperature API.</p>
 *
 * <p>For a complete description on the example, refer to the 'README.md' file
 * included in the example directory.</p>
 */
public class TemperatureSampleActivity extends Activity implements ICPUTemperatureListener {

	// Constants.
	private final static String TEMP_FORMAT = "%.1f " + (char)0x00B0 + "C";

	private final static int ACTION_CHANGE_TEXT_YELLOW = 0;
	private final static int ACTION_CHANGE_TEXT_WHITE = 1;

	private final static String HOT_TEMP_TITLE = "Hot CPU Temperature";
	private final static String HOT_TEMP_DESC = "Hot CPU temperature is the limit temperature at "
			+ "which system will reduce CPU frequency to avoid system overheating.";
	private final static String CRITICAL_TEMP_TITLE = "Critical CPU Temperature";
	private final static String CRITICAL_TEMP_DESC = "Critical CPU temperature is the limit "
			+ "temperature at which system will halt to avoid system damage caused by overheating. "
			+ "This always occurs after reaching hot temperature.";

	// Variables.
	private EditText timeText;

	private TextView statusText;
	private TextView hotTemperatureText;
	private TextView criticalTemperatureText;
	private TextView currentTemperatureText;

	private CPUManager cpuManager;

	private final IncomingHandler handler = new IncomingHandler(this);

	/**
	 * Handler to manage UI calls from different threads.
	 */
	static class IncomingHandler extends Handler {
		private final WeakReference<TemperatureSampleActivity> wActivity;

		IncomingHandler(TemperatureSampleActivity activity) {
			wActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			TemperatureSampleActivity activity = wActivity.get();

			if (activity != null)
				activity.updateTemperatureTextStatus(msg.what);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get CPU System Service.s
		cpuManager = new CPUManager(this);

		// Initialize UI.
		initializeUIComponents();
	}

	@Override
	public void onTemperatureUpdate(float value) {
		currentTemperatureText.setText(String.format(Locale.getDefault(), TEMP_FORMAT, value));

		// Add a mark effect to the text.
		handler.sendEmptyMessage(ACTION_CHANGE_TEXT_YELLOW);
		handler.sendEmptyMessageDelayed(ACTION_CHANGE_TEXT_WHITE, 400);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Remove listener.
		handleUnsubscribePressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		readTemperatures();
	}

	/**
	 * Initializes all the UI components and sets the corresponding event listeners.
	 */
	private void initializeUIComponents() {
		// Initialize subscribe button.
		Button subscribeButton = findViewById(R.id.subscribe_button);
		subscribeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleSubscribePressed();
			}
		});

		// Initialize unsubscribe button.
		Button unsubscribeButton = findViewById(R.id.unsubscribe_button);
		unsubscribeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleUnsubscribePressed();
			}
		});

		// Initialize time interval text.
		timeText = findViewById(R.id.time_text);

		// Initialize status text.
		statusText = findViewById(R.id.status_text);

		// Initialize temperature labels.
		hotTemperatureText = findViewById(R.id.hot_temp_label);
		criticalTemperatureText = findViewById(R.id.critical_temp_label);
		currentTemperatureText = findViewById(R.id.current_temp_label);

		// Hot temperature Help button.
		ImageButton hotTemperatureButton = findViewById(R.id.hot_temp_help_button);
		hotTemperatureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showPopupDialog(HOT_TEMP_TITLE, HOT_TEMP_DESC);
			}
		});

		// Critical temperature Help button.
		ImageButton criticalTemperatureButton = findViewById(R.id.critical_temp_help_button);
		criticalTemperatureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showPopupDialog(CRITICAL_TEMP_TITLE, CRITICAL_TEMP_DESC);
			}
		});
	}

	/**
	 * Reads CPU service temperatures and fills UI components.
	 */
	private void readTemperatures() {
		// Read temperatures from service.
		try {
			hotTemperatureText.setText(
					String.format(Locale.getDefault(), TEMP_FORMAT, cpuManager.getHotTemperature()));
			criticalTemperatureText.setText(
					String.format(Locale.getDefault(), TEMP_FORMAT, cpuManager.getCriticalTemperature()));
			currentTemperatureText.setText(
					String.format(Locale.getDefault(), TEMP_FORMAT, cpuManager.getCurrentTemperature()));
		} catch (CPUTemperatureException e) {
			showToast("Error reading temperatures: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Handles what happens when the subscribe button is pressed.
	 */
	private void handleSubscribePressed() {
		try {
			long timeout = Long.parseLong(timeText.getText().toString());
			cpuManager.registerListener(this, timeout);
			setTextSubscribed();
		} catch (NumberFormatException e) {
			showToast("Invalid timeout value");
		} catch (Exception e) {
			showToast("Error subscribing for temperature updates: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Handles what happens when the unsubscribe button is pressed.
	 */
	private void handleUnsubscribePressed() {
		cpuManager.unregisterListener(this);
		setTextUnsubscribed();
	}

	/**
	 * Changes the status text to display subscribed message in green.
	 */
	private void setTextSubscribed() {
		statusText.setText(R.string.subscribed);
		statusText.setTextColor(getResources().getColor(R.color.light_green));
	}

	/**
	 * Changes the status text to display unsubscribed message in red.
	 */
	private void setTextUnsubscribed() {
		statusText.setText(R.string.unsubscribed);
		statusText.setTextColor(getResources().getColor(R.color.light_red));
	}

	/**
	 * Updates the status of the current temperature text.
	 *
	 * @param status New status value.
	 *
	 * @see #ACTION_CHANGE_TEXT_YELLOW
	 * @see #ACTION_CHANGE_TEXT_WHITE
	 */
	private void updateTemperatureTextStatus(int status) {
		switch (status) {
			case ACTION_CHANGE_TEXT_YELLOW:
				currentTemperatureText.setTextColor(getResources().getColor(R.color.yellow));
				break;
			case ACTION_CHANGE_TEXT_WHITE:
				currentTemperatureText.setTextColor(getResources().getColor(R.color.white));
				break;
			default:
				break;
		}
	}

	/**
	 * Displays a toast with the given message.
	 *
	 * @param message Message to display in the toast.
	 */
	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Displays a popup dialog with the given title and message.
	 *
	 * @param title Popup dialog title.
	 * @param message Popup dialog message.
	 */
	private void showPopupDialog(String title, String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setCancelable(true);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});

		// Set the icon for the dialog.
		alertDialog.setIcon(R.drawable.help_image);
		alertDialog.show();
	}
}