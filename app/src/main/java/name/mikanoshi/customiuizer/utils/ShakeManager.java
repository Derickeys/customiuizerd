package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import de.robv.android.xposed.XposedBridge;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.mods.Launcher;

public class ShakeManager implements SensorEventListener {
	
	private float xAccel;
	private float yAccel;
	private float zAccel;

	private float xPreviousAccel;
	private float yPreviousAccel;
	private float zPreviousAccel;

	private boolean firstUpdate = true;
	private boolean shakeInitiated = false;
	private long lastShakeEvent = System.currentTimeMillis();
	
	private Context helperContext;
	
	public ShakeManager(Context helpercontext) {
		this.helperContext = helpercontext;
	}
	
	public void reset() {
		xAccel = 0;
		yAccel = 0;
		zAccel = 0;
		xPreviousAccel = 0;
		yPreviousAccel = 0;
		zPreviousAccel = 0;
		firstUpdate = true;
		shakeInitiated = false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//Don't care...
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		updateAccelParameters(se.values[0], se.values[1], se.values[2]);
		if (!shakeInitiated && isAccelerationChanged())
			shakeInitiated = true;
		else if (shakeInitiated && isAccelerationChanged())
			executeShakeActionDelayed();
		else if (shakeInitiated && (!isAccelerationChanged()))
			shakeInitiated = false;
	}

	private void updateAccelParameters(float xNewAccel, float yNewAccel, float zNewAccel) {
		if (firstUpdate) {
			xPreviousAccel = xNewAccel;
			yPreviousAccel = yNewAccel;
			zPreviousAccel = zNewAccel;
			firstUpdate = false;
		} else {
			xPreviousAccel = xAccel;
			yPreviousAccel = yAccel;
			zPreviousAccel = zAccel;
		}
		xAccel = xNewAccel;
		yAccel = yNewAccel;
		zAccel = zNewAccel;
	}
	
	private boolean isAccelerationChanged() {
		float deltaX = Math.abs(xPreviousAccel - xAccel);
		float deltaY = Math.abs(yPreviousAccel - yAccel);
		float deltaZ = Math.abs(zPreviousAccel - zAccel);
		float shakeThresholdX = 4f;
		float shakeThresholdY = 4f;
		float shakeThresholdZ = 8f;
		return (deltaX > shakeThresholdX && deltaY > shakeThresholdY)
				|| (deltaX > shakeThresholdX && deltaZ > shakeThresholdZ)
				|| (deltaY > shakeThresholdY && deltaZ > shakeThresholdZ);
	}
	
	private void executeShakeActionDelayed() {
		long now = System.currentTimeMillis();
		int shakeEventThrottle = 750;
		if (now - lastShakeEvent > shakeEventThrottle) {
			lastShakeEvent = now;
			executeShakeAction();
		}
	}
	
	private void executeShakeAction() {
		//MainModule.pref.reload();
		Launcher.handleAction(MainModule.pref.getInt("pref_key_launcher_shake_action", 1), 5, MainModule.pref.getInt("pref_key_launcher_shake_toggle", 0), helperContext);
	}
}
