package com.example.android.temperaturesample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.temperature.CPUTemperatureListener;
import android.temperature.CPUTemperatureManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TemperatureSampleActivity extends Activity implements CPUTemperatureListener {

	// Constants.
	private final static String TEMPERATURE_SUFIX = " " + (char)0x00B0 + "C";
	
	private final static int ACTION_CHANGE_TEXT_YELLOW = 0;
	private final static int ACTION_CHANGE_TEXT_WHITE = 1;
	
	private final static String HOT_TEMPERATURE_TITLE = "Hot CPU Temperature";
	private final static String HOT_TEMPERATURE_DESCRIPTION = "Hot CPU temperature is the temperature" +
			" limit at which system will reduce CPU frequency to avoid system overheating. ";
	private final static String CRITICAL_TEMPERATURE_TITLE = "Critical CPU Temperature";
	private final static String CRITICAL_TEMPERATURE_DESCRIPTION = "Critical CPU temperature is the " +
			"temperature limit at which system will halt to avoid system damage caused by overheating. " +
			"This always occurs after reaching hot temperature. ";

	// Variables.
	private Button subscribeButton;
	private Button unsubscribeButton;

	private EditText timeText;

	private TextView statusText;
	private TextView hotTemperatureText;
	private TextView criticalTemperatureText;
	private TextView currentTemperatureText;
	
	private ImageButton hotTemperatureButton;
	private ImageButton criticalTemperatureButton;

	private CPUTemperatureManager temperatureManager;

	/**
	 * Handler to manage UI calls from different threads.
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ACTION_CHANGE_TEXT_YELLOW:
				currentTemperatureText.setTextColor(getResources().getColor(R.color.yellow));
				break;
			case ACTION_CHANGE_TEXT_WHITE:
				currentTemperatureText.setTextColor(getResources().getColor(R.color.white));
				break;
			}
		};
	};
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get CPU Temperature System Service.
		temperatureManager = (CPUTemperatureManager)getSystemService(Context.TEMPERATURE_SERVICE);

		// Initialize UI.
		initializeUIComponents();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	protected void onStop() {
		super.onStop();
		// Remove listener.
		handleUnsubscribePressed();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume();
		readTemperatures();
	}

	/**
	 * Initializes all the UI components and sets the corresponding event listeners.
	 */
	private void initializeUIComponents() {
		// Initialize subscribe button.
		subscribeButton = (Button)findViewById(R.id.subscribe_button);
		subscribeButton.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			public void onClick(View v) {
				handleSubscribePressed();
			}
		});
		// Initialize unsubscribe button.
		unsubscribeButton = (Button)findViewById(R.id.unsubscribe_button);
		unsubscribeButton.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			public void onClick(View v) {
				handleUnsubscribePressed();
			}
		});
		// Initialize time interval text.
		timeText = (EditText)findViewById(R.id.time_text);
		// Initialize status text.
		statusText = (TextView)findViewById(R.id.status_text);
		// Initialize temperature labels.
		hotTemperatureText = (TextView)findViewById(R.id.hot_temp_label);
		criticalTemperatureText = (TextView)findViewById(R.id.critical_temp_label);
		currentTemperatureText = (TextView)findViewById(R.id.current_temp_label);
		// Hot temperature Help button.
		hotTemperatureButton = (ImageButton)findViewById(R.id.hot_temp_help_button);
		hotTemperatureButton.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			public void onClick(View v) {
				showPopupDialog(HOT_TEMPERATURE_TITLE, HOT_TEMPERATURE_DESCRIPTION);
			}
		});
		// Critical temperature Help button.
		criticalTemperatureButton = (ImageButton)findViewById(R.id.critical_temp_help_button);
		criticalTemperatureButton.setOnClickListener(new OnClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			public void onClick(View v) {
				showPopupDialog(CRITICAL_TEMPERATURE_TITLE, CRITICAL_TEMPERATURE_DESCRIPTION);
			}
		});
	}

	/**
	 * Reads CPU service temperatures and fills UI components.
	 */
	private void readTemperatures() {
		// Read temperatures from service.
		hotTemperatureText.setText(temperatureManager.getHotTemperature() + TEMPERATURE_SUFIX);
		criticalTemperatureText.setText(temperatureManager.getCriticalTemperature() + TEMPERATURE_SUFIX);
		currentTemperatureText.setText(temperatureManager.getCurrentTemperature() + TEMPERATURE_SUFIX);
	}

	/**
	 * Handles what happens when the subscribe button is pressed.
	 */
	private void handleSubscribePressed() {
		try {
			long timeout = Long.valueOf(timeText.getText().toString());
			temperatureManager.requestTemperatureUpdates(timeout, this);
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
		temperatureManager.removeUpdates(this);
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
	 * @param title Pupup dialog title.
	 * @param message Popup dialog message.
	 */
	private void showPopupDialog(String title, String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setCancelable(true);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});
		// Set the Icon for the Dialog
		alertDialog.setIcon(R.drawable.help_image);
		alertDialog.show();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.temperature.CPUTemperatureListener#onTemperatureUpdate(float)
	 */
	public void onTemperatureUpdate(float arg0) {
		currentTemperatureText.setText(arg0 + TEMPERATURE_SUFIX);
		// Add a mark effect to the text.
		handler.sendEmptyMessage(ACTION_CHANGE_TEXT_YELLOW);
		handler.sendEmptyMessageDelayed(ACTION_CHANGE_TEXT_WHITE, 400);
	}
}