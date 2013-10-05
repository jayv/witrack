package at.fhstp.wificompass.activities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.fhstp.wificompass.Logger;
import at.fhstp.wificompass.R;
import at.fhstp.wificompass.model.SensorData;
import at.fhstp.wificompass.model.helper.DatabaseHelper;
import at.fhstp.wificompass.userlocation.StepDetection;
import at.fhstp.wificompass.userlocation.StepDetectionProvider;
import at.fhstp.wificompass.userlocation.StepDetector;
import at.fhstp.wificompass.userlocation.StepTrigger;
import at.fhstp.wificompass.view.PaintBoxHistory;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

/**
 * <p>
 * This Activity is used to calibrate the parameters concerning step detection
 * </p>
 * <p>
 * Original by Paul Smith, adjusted and extended by Paul Woelfel
 * </p>
 * 
 * @author Paul Smith, Paul Woelfel
 */
public class CalibratorActivity extends Activity implements StepTrigger, OnClickListener {

	/**
	 * @uml.property name="stepDetection"
	 * @uml.associationEnd
	 */
	private StepDetection stepDetection;

	/**
	 * @uml.property name="svHistory"
	 * @uml.associationEnd
	 */
	PaintBoxHistory svHistory;

	// GUI
	TextView tvPeak = null;

	TextView tvFilter = null;

	TextView tvTimeout = null;

	SeekBar sbPeak = null;

	SeekBar sbFilter = null;

	SeekBar sbTimeout = null;

	SeekBar sbStepSize = null;

	float peak; // threshold for step detection

	float filter; // value for low pass filter

	int step_timeout_ms; // distance in ms between each step

	float stepSize;

	protected boolean autoCalibrationRunning = false, showSensorData = true;

	protected DatabaseHelper databaseHelper;

	protected Dao<SensorData, Integer> sensorDao;

	protected AutoCalibrateTask calibrateTask;

	protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	protected Runnable runnable;

	protected ScheduledFuture<?> scheduledTask = null;

	public static final String ACCELOREMETER_STRING = "acc", STEP_STRING = "step";

	public static final int STEP_TYPE = 42;

	// filter range 0.05 to 0.80 in 0.05 steps
	public static final float FILTER_MIN = 0.05f, FILTER_MAX = 0.8f, FILTER_INTERVAL = 0.05f;

	// peak range 0.2 to 3.5 in 0.1 steps
	public static final float PEAK_MIN = 0.2f, PEAK_MAX = 3.0f, PEAK_INTERVAL = 0.1f;

	// step timeout from 100ms to 500ms in 25ms steps
	public static final int TIMEOUT_MIN = 200, TIMEOUT_MAX = 500, TIMEOUT_INTERVAL = 50;

	public static final int STEP_DETECTED_REWARD = 1, STEP_FALSE_DETECTED_PUNISH = -2, STEP_NOT_DETECTED_PUNISH = -1;

	protected int windowSize = 500;

	public static final String BUNDLE_SCORE = "score", BUNDLE_PCT = "percantage", BUNDLE_FILTER = "filter", BUNDLE_PEAK = "peak",
			BUNDLE_FOUND = "found", BUNDLE_NOTFOUND = "notfound", BUNDLE_FALSEFOUND = "falsefound", BUNDLE_ALLFOUND = "allfound",
			BUNDLE_TIMEOUT = "timeout", BUNDLE_DEF_SCORE = "defScore", BUNDLE_DEF_SCORE_PCT = "defScorePct", BUNDLE_DEF_DETECTED = "defDetected",
			BUNDLE_DEF_NOT_DETECTED = "defNotDetected", BUNDLE_DEF_FALSE_DETECTED = "defFalseDetected", BUNDLE_REAL_STEP_COUNT = "stepCount",EXTRA_START_MODE="startMode";
	
	public static final int START_MODE_NORMAL=1,START_MODE_AUTO_CONFIG=2;

	protected ArrayList<SensorData> accelerometerSensorValues, stepValues;

	protected static final int SCHEDULER_TIME = 25;
	
	protected boolean autoConfig=false;

	OnSeekBarChangeListener sbListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {
			switch (sb.getId()) {
			case R.id.calibrator_sbPeak:
				setProgressValue(sb.getId(), sbPeak.getProgress() / 10.0f);
				break;
			case R.id.calibrator_sbFilter:
				setProgressValue(sb.getId(), sbFilter.getProgress() / 100.0f);
				break;
			case R.id.calibrator_sbTimeout:
				setProgressValue(sb.getId(), sbTimeout.getProgress());
				break;
			case R.id.calibrator_step_size:
				setProgressValue(sb.getId(), sbStepSize.getProgress() / 100.0f);
				break;
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras=this.getIntent().getExtras();
		
		if(extras!=null&&extras.containsKey(EXTRA_START_MODE)&&extras.getInt(EXTRA_START_MODE)==START_MODE_AUTO_CONFIG){
			autoConfig=true;
			Logger.i("started for auto config");
		}
		
		// Load settings after creation of GUI-elements, to set their values
		initLogic();
		initUI();

		runnable = new Runnable() {

			@Override
			public void run() {
				if (showSensorData)
					svHistory.getMessageHandler().sendEmptyMessage(PaintBoxHistory.MESSAGE_REFRESH_HISTORY);
			}

		};
		
		
	}

	protected void initLogic() {
		stepDetection = new StepDetection(this, this, filter, peak, step_timeout_ms);

		// create PaintBox (-24.0 to 24.0, 100 entries)

		databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		try {
			sensorDao = databaseHelper.getDao(SensorData.class);
		} catch (SQLException e) {
			Logger.e("could not initialize dao for sensorData", e);
		}

		accelerometerSensorValues = new ArrayList<SensorData>();
		stepValues = new ArrayList<SensorData>();
	}

	protected void initUI() {
		setContentView(R.layout.calibrator);

		tvPeak = (TextView) findViewById(R.id.calibrator_tvPeak);
		tvFilter = (TextView) findViewById(R.id.calibrator_tvFilter);
		tvTimeout = (TextView) findViewById(R.id.calibrator_tvTimeout);

		sbPeak = (SeekBar) findViewById(R.id.calibrator_sbPeak);
		sbFilter = (SeekBar) findViewById(R.id.calibrator_sbFilter);
		sbTimeout = (SeekBar) findViewById(R.id.calibrator_sbTimeout);
		sbStepSize = (SeekBar) findViewById(R.id.calibrator_step_size);

		// Add OnSeekBarChangeListener after creation of step detection, because object is used
		sbPeak.setOnSeekBarChangeListener(sbListener);
		sbFilter.setOnSeekBarChangeListener(sbListener);
		sbTimeout.setOnSeekBarChangeListener(sbListener);
		sbStepSize.setOnSeekBarChangeListener(sbListener);

		svHistory = (PaintBoxHistory) findViewById(R.id.calibrator_history_paintbox);
		if (svHistory == null) {
			Logger.e("svHistory must not be null!?!?!?!");
		} else {
			svHistory.setOnClickListener(this);
		}

		if (!showSensorData) {
			((ViewGroup) findViewById(R.id.calibrator_LinearLayout01)).removeView(svHistory); // remove surface view
		}

		ToggleButton autoScanning = (ToggleButton) findViewById(R.id.calibrator_auto_calibrate);
		autoScanning.setOnClickListener(this);
		autoScanning.setChecked(autoCalibrationRunning);

		((ToggleButton) findViewById(R.id.calibrator_toggle_graph)).setOnClickListener(this);
		((ToggleButton) findViewById(R.id.calibrator_toggle_graph)).setChecked(showSensorData);

		((Button) findViewById(R.id.calibrator_step_button)).setOnClickListener(this);

		// ((Button) findViewById(R.id.calibrator_analyze_data)).setOnClickListener(this);

		loadSettings();
		
		if(autoConfig){
			toggleAutoCalibration();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		saveSettings();
		if (scheduledTask != null) {
			scheduledTask.cancel(false);
			scheduledTask = null;
		}
		stepDetection.unload();
	}

	@Override
	public void onDestroy() {
		OpenHelperManager.releaseHelper();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		loadSettings();
		// if(showSensor)
		scheduledTask = scheduler.scheduleWithFixedDelay(runnable, 0, SCHEDULER_TIME, TimeUnit.MILLISECONDS);

		stepDetection.load(SensorManager.SENSOR_DELAY_FASTEST);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initUI();
	}

	private void loadSettings() {
		filter = getSharedPreferences(StepDetectionProvider.CALIB_DATA, 0).getFloat(StepDetectionProvider.FILTER,
				StepDetectionProvider.FILTER_DEFAULT);
		peak = getSharedPreferences(StepDetectionProvider.CALIB_DATA, 0).getFloat(StepDetectionProvider.PEAK, StepDetectionProvider.PEAK_DEFAULT);
		step_timeout_ms = getSharedPreferences(StepDetectionProvider.CALIB_DATA, 0).getInt(StepDetectionProvider.TIMEOUT,
				StepDetectionProvider.TIMEOUT_DEFAULT);
		stepSize = getSharedPreferences(StepDetectionProvider.CALIB_DATA, 0).getFloat(StepDetectionProvider.STEP, StepDetectionProvider.STEP_DEFAULT);

		// Update GUI elements
		setProgressValue(R.id.calibrator_sbPeak, peak);
		setProgressValue(R.id.calibrator_sbFilter, filter);
		setProgressValue(R.id.calibrator_sbTimeout, step_timeout_ms);
		setProgressValue(R.id.calibrator_step_size, stepSize);
	}

	protected boolean setProgressValue(int id, float value) {
		boolean ret = true;
		switch (id) {
		case R.id.calibrator_sbPeak:
			peak = value;
			stepDetection.setPeak(peak);
			sbPeak.setProgress((int) (value * 10));
			tvPeak.setText(getString(R.string.calibrator_peak_text, value, StepDetectionProvider.PEAK_DEFAULT));
			break;
		case R.id.calibrator_sbFilter:
			filter = value;
			stepDetection.setA(filter);
			sbFilter.setProgress((int) (value * 100));
			tvFilter.setText(getString(R.string.calibrator_filter_text, value, StepDetectionProvider.FILTER_DEFAULT));
			break;
		case R.id.calibrator_sbTimeout:
			step_timeout_ms = (int) value;
			stepDetection.setStep_timeout_ms(step_timeout_ms);
			tvTimeout.setText(getString(R.string.calibrator_step_timeout_text, (int) value, StepDetectionProvider.TIMEOUT_DEFAULT));
			break;
		case R.id.calibrator_step_size:
			stepSize = value;

			((TextView) (findViewById(R.id.calibrator_tv_step_size))).setText(getString(R.string.calibrator_step_size_text, value,
					StepDetectionProvider.STEP_DEFAULT));
			break;
		default:
			ret = false;
			break;
		}
		if (ret) {
			saveSettings();
		}

		return ret;
	}

	private void saveSettings() {
		// Save current values to settings
		SharedPreferences settings = getSharedPreferences(StepDetectionProvider.CALIB_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat(StepDetectionProvider.FILTER, filter);
		editor.putFloat(StepDetectionProvider.PEAK, peak);
		editor.putInt(StepDetectionProvider.TIMEOUT, step_timeout_ms);
		editor.putFloat(StepDetectionProvider.STEP, stepSize);
		// Apply changes
		editor.commit();
	}

	@Override
	public void onAccelerometerDataReceived(long nowMs, double x, double y, double z) {
		if (autoCalibrationRunning && sensorDao != null) {
			// save data for calculations.
			// try {
			// sensorDao.create(new SensorData(ACCELOREMETER_STRING, Sensor.TYPE_ACCELEROMETER, (float) x, (float) y, (float) z, 0));
			// } catch (SQLException e) {
			// Logger.e("could not save sensor data", e);
			// }
			accelerometerSensorValues.add(new SensorData(ACCELOREMETER_STRING, Sensor.TYPE_ACCELEROMETER, (float) x, (float) y, (float) z, 0));
		}
	}

	@Override
	public void onCompassDataReceived(long nowMs, double x, double y, double z) {
	}

	@Override
	public void onTimerElapsed(long nowMs, double[] acc, double[] comp) {
		if (showSensorData)
			svHistory.addTriple(nowMs, acc);
	}

	@Override
	public void onStepDetected(long nowMs, double compDir) {
		if (!autoCalibrationRunning && showSensorData)
			svHistory.addStepTS(nowMs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View paramView) {
		switch (paramView.getId()) {
		case R.id.calibrator_auto_calibrate:
			toggleAutoCalibration();
			break;

		// case R.id.calibrator_analyze_data:
		// showCalibrationDialog();
		// break;

		case R.id.calibrator_step_button:
		case R.id.calibrator_history_paintbox:
			if (autoCalibrationRunning) {

				// persistent sensor values
				// try {
				// sensorDao.create(new SensorData(STEP_STRING, STEP_TYPE));
				// } catch (SQLException e) {
				// Logger.e("could not save step", e);
				// }
				// not persistent
				stepValues.add(new SensorData(STEP_STRING, STEP_TYPE));
				if (showSensorData)
					svHistory.addStepTS(System.currentTimeMillis());
			}
			break;

		case R.id.calibrator_toggle_graph:

			Logger.d("disableing graph");
			LinearLayout linLayout = (LinearLayout) findViewById(R.id.calibrator_LinearLayout01);
			if (showSensorData) {
				// hide svHistory
				linLayout.removeView(svHistory);
				scheduledTask.cancel(false);
				scheduledTask = null;
			} else {
				// show svHistory
				linLayout.addView(svHistory, linLayout.getChildCount() - 1, svHistory.getLayoutParams());
				scheduledTask = scheduler.scheduleWithFixedDelay(runnable, 0, SCHEDULER_TIME, TimeUnit.MILLISECONDS);
			}
			showSensorData = !showSensorData;
			break;

		}

	}

	/**
	 * 
	 */
	protected void toggleAutoCalibration() {
		if (autoCalibrationRunning) {
			// stop calibration
			autoCalibrationRunning = false;
			showCalibrationDialog();

		} else {
			// start calibration
			// clear saved sensor data
			try {
				sensorDao.delete(sensorDao.deleteBuilder().prepare());
			} catch (SQLException e) {
				Logger.e("could not delete all sensordata entries", e);
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.calibrator_auto_config_info_title);
			builder.setMessage(R.string.calibrator_auto_config_info_message);

			final Activity activity = this;

			builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					accelerometerSensorValues = new ArrayList<SensorData>();
					stepValues = new ArrayList<SensorData>();
					autoCalibrationRunning = true;
					((ToggleButton) activity.findViewById(R.id.calibrator_auto_calibrate)).setChecked(autoCalibrationRunning);
				}
			});

			builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});

			builder.create().show();
		}

		((ToggleButton) findViewById(R.id.calibrator_auto_calibrate)).setChecked(autoCalibrationRunning);

	}

	protected void showCalibrationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.calibrator_auto_config_setting_title);
		builder.setMessage(getString(R.string.calibrator_auto_config_setting_message, windowSize));

		final SeekBar sb = new SeekBar(this);
		sb.setMax(1500);
		sb.setProgress(windowSize);

		builder.setView(sb);

		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				windowSize = sb.getProgress();
				startCalibrationCalculation();
			}
		});

		builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		final AlertDialog dialog = builder.create();

		final Context ctx = this;

		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				dialog.setMessage(ctx.getString(R.string.calibrator_auto_config_setting_message, progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		});

		dialog.show();
	}

	/**
	 * 
	 */
	protected void startCalibrationCalculation() {
		// do the magic
		final ProgressDialog calibratingProgress = new ProgressDialog(this);
		calibratingProgress.setTitle(R.string.calibrator_auto_config_progress_title);
		calibratingProgress.setMessage(getString(R.string.calibrator_auto_config_progress_message));
		calibratingProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		calibratingProgress.setButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
				if (calibrateTask != null) {
					calibrateTask.cancel(true);
				}
			}
		});

		calibrateTask = new AutoCalibrateTask(this, calibratingProgress);

		calibratingProgress.show();
		calibrateTask.execute();
	}

	protected class AutoCalibrateTask extends AsyncTask<Void, Integer, Bundle> {

		protected CalibratorActivity parent;

		protected ProgressDialog progressDialog;

		protected boolean running = true;

		public AutoCalibrateTask(final CalibratorActivity calibrator, final ProgressDialog progress) {
			this.parent = calibrator;
			this.progressDialog = progress;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Bundle doInBackground(Void... paramArrayOfParams) {
			Bundle result = new Bundle();

			// try {
			// QueryBuilder<SensorData, Integer> accQuery = sensorDao.queryBuilder();
			// accQuery.where().eq(SensorData.FIELD_TYPE, Sensor.TYPE_ACCELEROMETER);
			// accQuery.orderBy(SensorData.FIELD_TIMESTAMP, true);
			// List<SensorData> accelerometerValues = accQuery.query();
			List<SensorData> accelerometerValues = accelerometerSensorValues;

			// QueryBuilder<SensorData, Integer> stepQuery = sensorDao.queryBuilder();
			// stepQuery.where().eq(SensorData.FIELD_TYPE, STEP_TYPE);
			// stepQuery.orderBy(SensorData.FIELD_TIMESTAMP, true);
			// List<SensorData> steps = stepQuery.query();
			List<SensorData> steps = stepValues;

			progressDialog.setMax((int) (((PEAK_MAX - PEAK_MIN) / PEAK_INTERVAL) * ((FILTER_MAX - FILTER_MIN) / FILTER_INTERVAL)));
			progressDialog.setProgress(0);

			float bestFilter = 0f, bestPeak = 0f;
			int bestScore = Integer.MIN_VALUE, bestStepFound = 0, bestStepNotFound = 0, bestStepFalseFound = 0, bestTimeout = 0;

			int defScore = 0, defStepFound = 0, defStepNotFound = 0, defStepFalseFound = 0;

			// long halfWindow = StepDetection.INTERVAL_MS * StepDetector.WINDOW / 2;

			// the user must tap on the screen in less than a half second
			long halfWindow = windowSize / 2;

			int allDetected = 1;

			int progress = 0;

			// calculate the best values for lowpass filter l and peak p
			// filter range 0.05 to 0.80 in 0.05 steps
			// peak range 0.2 to 3 in 0.05 steps

			// synchronize the list arrays, we don't want the arrays to be modified, while we cycle through them.
			// happened two times on a Galaxy S2, even if the values shouldn't be saved any more...
			this.parent.autoCalibrationRunning = false;

			synchronized (accelerometerValues) {
				synchronized (steps) {

					if (accelerometerValues.size() > 0) {

						// for (int t = TIMEOUT_MIN; running && t <= TIMEOUT_MAX; t += TIMEOUT_INTERVAL)

						// cycle over peak values
						for (float p = PEAK_MIN; running && p <= PEAK_MAX; p += PEAK_INTERVAL) {

							// cycle over filter values
							for (float l = FILTER_MIN; running && l <= FILTER_MAX; l += FILTER_INTERVAL) {

								// Logger.d("searching for steps with peak p=" + p + " and filter l=" + l);

								int score = 0;
								StepDetector detector = new StepDetector(l, p, step_timeout_ms);
								detector.setLogSteps(false);
								Iterator<SensorData> stepIterator = steps.iterator();
								long stepTime = 0;

								if (stepIterator.hasNext())
									stepTime = stepIterator.next().getTimestamp();

								long lastTimer = 0;

								int stepFound = 0, stepNotFound = 0, stepFalseFound = 0;

								// cycle through accelerometerValues
								for (SensorData acc : accelerometerValues) {

									// add sensor values
									detector.addSensorValues(acc.getTimestamp(), new float[] { acc.getValue0(), acc.getValue1(), acc.getValue2() });

									// only all INTERVAL_MS
									if (acc.getTimestamp() > lastTimer + StepDetection.INTERVAL_MS) {
										lastTimer = acc.getTimestamp();

										// check if a step has been detected
										if (detector.checkForStep()) {

											// check if we have missed some steps:
											// are more steps saved?
											// is the current StepTime before the sensor value timesteamp minus half the window size
											while (stepIterator.hasNext() && stepTime < acc.getTimestamp() - halfWindow) {
												// Logger.d("step "+ stepTime +" has not been found, getting next one");
												stepTime = stepIterator.next().getTimestamp();
												stepNotFound++;
												score += STEP_NOT_DETECTED_PUNISH;

											}

											// is there a step in the current database
											// is the step timer in the interval sensor timestamp - halfwindow and sensortime + halfwindow

											// Logger.d("stepTime="+stepTime+" acc="+acc.getTimestamp()+" diff="+(stepTime-acc.getTimestamp())+" matched: "+(Math.abs(stepTime - acc.getTimestamp()) <
											// halfWindow?"true":"false"));

											if (Math.abs(stepTime - acc.getTimestamp()) < halfWindow) {
												// that's fine, we found one

												// Logger.d("matched step");
												score += STEP_DETECTED_REWARD;
												stepFound++;
												if (stepIterator.hasNext()) {
													stepTime = stepIterator.next().getTimestamp();
												} else {
													stepTime = 0;
												}
											} else {
												// no, there is none
												score += STEP_FALSE_DETECTED_PUNISH;
												stepFalseFound++;
												// Logger.d("step not matched");
											}
										}

									}
								}
								// finished analyzing all sensor values

								// are there some steps missing?

								while (stepTime != 0) {
									// Logger.d("missed a step="+stepTime);
									// step missing, bad for the score
									if (stepIterator.hasNext())
										stepTime = stepIterator.next().getTimestamp();
									else
										stepTime = 0;
									score += STEP_NOT_DETECTED_PUNISH;
									stepNotFound++;

								}

								if (Logger.isVerboseEnabled())
									Logger.v((score > bestScore ? "better" : "worse") + " score found: " + score + (score > bestScore ? ">" : "<")
											+ bestScore + " : p=" + p + " l=" + l + " found=" + stepFound + " notFound=" + stepNotFound
											+ " falseFound=" + stepFalseFound);

								if (score == bestScore) {
									allDetected++;
								}

								// have we found a better score?
								if (score > bestScore || (score == bestScore && stepFound > bestStepFound)) {
									bestFilter = l;
									bestPeak = p;
									bestTimeout = step_timeout_ms;
									bestScore = score;
									bestStepFound = stepFound;
									bestStepNotFound = stepNotFound;
									bestStepFalseFound = stepFalseFound;
									allDetected = 1;
								}

								if (l > StepDetectionProvider.FILTER_DEFAULT-FILTER_INTERVAL/2 && l<StepDetectionProvider.FILTER_DEFAULT+FILTER_INTERVAL/2
										&& p > StepDetectionProvider.PEAK_DEFAULT-PEAK_INTERVAL/2&& p < StepDetectionProvider.PEAK_DEFAULT+PEAK_INTERVAL/2) {
									// how good do the default values perform?
									defScore = score;
									defStepFound = stepFound;
									defStepNotFound = stepNotFound;
									defStepFalseFound = stepFalseFound;

								}

								// progressDialog.setProgress((int)progressCur);
								this.publishProgress(++progress);
							}
						}

						Logger.i("Best score is: " + bestScore + " " + (((float) bestScore) / steps.size() * 100) + "% filter l=" + bestFilter
								+ " peak p=" + bestPeak);
						result.putInt(BUNDLE_SCORE, bestScore);
						result.putFloat(BUNDLE_PCT, ((float) bestScore) / steps.size());
						result.putFloat(BUNDLE_FILTER, bestFilter);
						result.putFloat(BUNDLE_PEAK, bestPeak);
						result.putInt(BUNDLE_FOUND, bestStepFound);
						result.putInt(BUNDLE_NOTFOUND, bestStepNotFound);
						result.putInt(BUNDLE_FALSEFOUND, bestStepFalseFound);
						result.putInt(BUNDLE_ALLFOUND, allDetected);
						result.putInt(BUNDLE_TIMEOUT, bestTimeout);
						result.putInt(BUNDLE_REAL_STEP_COUNT, steps.size());
						result.putInt(BUNDLE_DEF_SCORE, defScore);
						result.putFloat(BUNDLE_DEF_SCORE_PCT, ((float) defScore) / steps.size());
						result.putInt(BUNDLE_DEF_DETECTED, defStepFound);
						result.putInt(BUNDLE_DEF_NOT_DETECTED, defStepNotFound);
						result.putInt(BUNDLE_DEF_FALSE_DETECTED, defStepFalseFound);

						// } catch (SQLException e) {
						// Logger.e("could not access saved data", e);
						// }

					}
				}
			}

			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Bundle result) {
			progressDialog.dismiss();
			if (running)
				parent.setCalibrationResult(result);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			running = false;
			super.onCancelled();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
		}

		// @Override
		// protected void onPostExecute(final Vector<AccessPointDrawable> result) {
		// progress.dismiss();
		// parent.setCalculatedAccessPoints(result);
		// }
	}

	/**
	 * @param result
	 */
	protected void setCalibrationResult(Bundle result) {
		setProgressValue(R.id.calibrator_sbFilter, result.getFloat(BUNDLE_FILTER, StepDetectionProvider.FILTER_DEFAULT));
		setProgressValue(R.id.calibrator_sbPeak, result.getFloat(BUNDLE_PEAK, StepDetectionProvider.FILTER_DEFAULT));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.calibrator_auto_config_title);

		builder.setMessage(getString(R.string.calibrator_auto_config_message, result.getInt(BUNDLE_SCORE, 0), result.getFloat(BUNDLE_PCT, 0) * 100,
				result.getFloat(BUNDLE_FILTER, StepDetectionProvider.FILTER_DEFAULT),
				result.getFloat(BUNDLE_PEAK, StepDetectionProvider.PEAK_DEFAULT), result.getInt(BUNDLE_FOUND, 0), result.getInt(BUNDLE_NOTFOUND, 0),
				result.getInt(BUNDLE_FALSEFOUND, 0), result.getInt(BUNDLE_ALLFOUND, 0), result.getInt(BUNDLE_TIMEOUT, 0),
				result.getInt(BUNDLE_REAL_STEP_COUNT), result.getInt(BUNDLE_DEF_SCORE), result.getFloat(BUNDLE_DEF_SCORE_PCT)*100,
				result.getInt(BUNDLE_DEF_DETECTED), result.getInt(BUNDLE_DEF_NOT_DETECTED), result.getInt(BUNDLE_DEF_FALSE_DETECTED)));


		builder.setCancelable(false);
		builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				paramDialogInterface.dismiss();
				if(autoConfig){
					finish();
				}
			}

		});
		builder.create().show();
	}

}