package edu.mit.media.funf.probe.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.Probe.ContinuousProbe;
import edu.mit.media.funf.probe.Probe.DataListener;
import edu.mit.media.funf.probe.Probe.State;
import edu.mit.media.funf.probe.Probe.StateListener;
import edu.mit.media.funf.probe.ProbeFactory;


public class TestLocationProbes extends AndroidTestCase {

	public static final String TAG = "FunfTest";
	
	private DataListener listener = new DataListener() {
		@Override
		public void onDataReceived(Uri completeProbeUri, JsonObject data) {
			Log.i(TAG, "DATA: " + completeProbeUri.toString() + " " + data.toString());
		}

		@Override
		public void onDataCompleted(Uri completeProbeUri, JsonElement checkpoint) {
			Log.i(TAG, "COMPLETE: " + completeProbeUri.toString());
		}
	};
	
	private StateListener stateListener = new StateListener() {

		@Override
		public void onStateChanged(Probe probe, State previousState) {
			Log.i(TAG, probe.getClass().getName() + ": " + probe.getState());
			Log.i(TAG, Probe.Identifier.getCompleteProbeUri(probe).toString());
		}
		
	};
	
	@SuppressWarnings("rawtypes")
	public static final Class[] ALL_PROBES = {
		LocationProbe.class,
		SimpleLocationProbe.class
	};
	
	
	@SuppressWarnings("unchecked")
	public void testAll() throws ClassNotFoundException, IOException, InterruptedException {
		Log.i(TAG,"Running");
		List<Class<? extends Probe>> allProbeClasses = Arrays.asList((Class<? extends Probe>[])ALL_PROBES);
		
		// Run one at a time
		ProbeFactory factory = ProbeFactory.BasicProbeFactory.getInstance(getContext());
		for (Class<? extends Probe> probeClass : allProbeClasses) {
			JsonObject config = new JsonObject();
			config.addProperty("maxWaitTime", 1);
			config.addProperty("asdf", 1);
			config.addProperty("zzzz", "__");
			Probe probe = factory.getProbe(probeClass, config);
			probe.addStateListener(stateListener);
			probe.registerListener(listener);
			Thread.sleep(100L);
			if (probe instanceof ContinuousProbe) {
				((ContinuousProbe)probe).unregisterListener(listener);
			}
		}
		// Run simultaneously
		List<Probe> probes = new ArrayList<Probe>();
		for (Class<? extends Probe> probeClass : allProbeClasses) {
			JsonObject config = new JsonObject();
			config.addProperty("maxWaitTime", 8);
			config.addProperty("asdf", 1);
			config.addProperty("zzzz", "__");
			Probe probe = factory.getProbe(probeClass, config);
			probes.add(probe);
		}
		for (Probe probe : probes) {
			probe.addStateListener(stateListener);
			probe.registerListener(listener);
		}
		Thread.sleep(10000L);
		for (Probe probe : probes) {
			if (probe instanceof ContinuousProbe) {
				((ContinuousProbe)probe).unregisterListener(listener);
			}
		}
		
		Thread.sleep(1000L); // Give probes time stop
	}
}
