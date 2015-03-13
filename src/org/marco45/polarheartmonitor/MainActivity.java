package org.marco45.polarheartmonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.SimpleXYSeries;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This program connect to a bluetooth polar heart rate monitor and display data
 * @author Marco
 *
 */
public class MainActivity extends Activity  implements OnItemSelectedListener, Observer {

	private int MAX_SIZE = 60; //graph max size
	
	boolean searchBt = true;
	//ConnectThread reader;
	BluetoothAdapter mBluetoothAdapter;
	Set<BluetoothDevice> pairedDevices;
	boolean menuBool = false;
	//int i =0;
	private XYPlot plot;
	//SimpleXYSeries series1;
	Tracker t;//Set the Tracker
	boolean h7 = false;
	boolean normal = false;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DataHandler.getInstance().addObserver(this);
		
		AdView mAdView = (AdView) findViewById(R.id.adView);

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
        
        if(	android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
        	h7=true;
        }
        
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(DataHandler.getInstance().newValue){

			//Verify if bluetooth if activated, if not activate it
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
			if (!mBluetoothAdapter.isEnabled()) {
				new AlertDialog.Builder(this)
				.setTitle(R.string.bluetooth)
				.setMessage(R.string.bluetoothOff)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						mBluetoothAdapter.enable();
						try {Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						listBT();
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						searchBt = false;
					}
				})
				.show();

			}
			else{
				listBT();
			}



			// Create Graph
			plot = (XYPlot) findViewById(R.id.dynamicPlot);
			if(plot.getSeriesSet().size()==0){
				Number[] series1Numbers = {};
				DataHandler.getInstance().setSeries1(new SimpleXYSeries(Arrays.asList(series1Numbers),SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Heart Rate")); 
			}
			DataHandler.getInstance().setNewValue(false);

		}
		else
		{
			listBT();
			plot = (XYPlot) findViewById(R.id.dynamicPlot);

		}
		//LOAD Graph
		LineAndPointFormatter series1Format = new LineAndPointFormatter( Color.rgb(0, 0, 255), Color.rgb(200, 200, 200), null, null ); 
		series1Format.setPointLabelFormatter(new PointLabelFormatter());
		plot.addSeries(DataHandler.getInstance().getSeries1(), series1Format); 
		plot.setTicksPerRangeLabel(3);
		plot.getGraphWidget().setDomainLabelOrientation(-45);

		//ANALYTIC
		t = GoogleAnalytics.getInstance(this).newTracker("UA-51478243-1");
		t.setScreenName("Polar main page");
		t.send(new HitBuilders.AppViewBuilder().build());

	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		DataHandler.getInstance().deleteObserver(this);
	}

	public void onStart(){
		super.onStart();



	}

	/**
	 * Run on startup to list bluetooth paired device
	 */
	public void listBT(){
		if(searchBt){
			//Discover bluetooth devices
			List<String> list = new ArrayList<String>();
			list.add("");
			pairedDevices = mBluetoothAdapter.getBondedDevices();
			// If there are paired devices
			if (pairedDevices.size() > 0) {
				// Loop through paired devices
				for (BluetoothDevice device : pairedDevices) {
					// Add the name and address to an array adapter to show in a ListView
					list.add(device.getName() + "\n" + device.getAddress());
				}
			}


			//Populate drop down
			Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, list);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner1.setOnItemSelectedListener(this);
			spinner1.setAdapter(dataAdapter);
			
			if(DataHandler.getInstance().getID()!=0)
				spinner1.setSelection(DataHandler.getInstance().getID());
		}
	}

	/**
	 * When menu button are pressed
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			DataHandler.getInstance().getReader().cancel();
			//DataHandler.getInstance().setReader(null);
			System.out.println("menu pes�");
			menuBool=false;
			return true;
		}
		else if (id==R.id.about){
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {



		if(arg2!=0){
			//Actual work
			DataHandler.getInstance().setID(arg2);
			if(!h7 && ((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID()-1]).getName().contains("H7"))
			{
				DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID()-1], this));
				h7=true;
			}
			else{
				DataHandler.getInstance().setReader(new ConnectThread((BluetoothDevice) pairedDevices.toArray()[arg2-1], this));
				DataHandler.getInstance().getReader().start();
				normal=true;
			}
			menuBool=true;

		}

	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		//menu.findItem(R.id.action_settings).setEnabled(menuBool);
		menu.findItem(R.id.action_settings).setVisible(false);//MENU OFF NOT WORKING
		return true;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {


	}

	/**
	 * Called when bluetooth connection failed
	 */
	public void connectionError(){
		menuBool=false;
		final MainActivity ac = this;
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getBaseContext(),getString(R.string.couldnotconnect),Toast.LENGTH_SHORT).show();
				TextView rpm = (TextView) findViewById(R.id.rpm);
				rpm.setText("0 BMP");
				Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
				spinner1.setSelection(DataHandler.getInstance().getID());
				
				if(h7==false){
					DataHandler.getInstance().setReader(null);
					DataHandler.getInstance().setH7(new H7ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID()-1], ac));
					h7=true;
				}
				else if(normal==false){
					DataHandler.getInstance().setH7(null);
					DataHandler.getInstance().setReader(new ConnectThread((BluetoothDevice) pairedDevices.toArray()[DataHandler.getInstance().getID()-1], ac));
					DataHandler.getInstance().getReader().start();
					normal=true;
				}
			}
		});
		
	}

	@Override
	public void update(Observable observable, Object data) {
		receiveData();		
	}

	/**
	 * Update Gui with new value from receiver
	 */
	public void receiveData(){		
		//ANALYTIC
		//t.setScreenName("Polar Bluetooth Used");
		//t.send(new HitBuilders.AppViewBuilder().build());

		runOnUiThread(new Runnable() {
			public void run() {
				menuBool=true;
				TextView rpm = (TextView) findViewById(R.id.rpm);
				rpm.setText(DataHandler.getInstance().getLastValue()+" BPM");

				if(DataHandler.getInstance().getLastValue()!=0){
					DataHandler.getInstance().getSeries1().addLast(0, DataHandler.getInstance().getLastValue());
					if(DataHandler.getInstance().getSeries1().size()>MAX_SIZE)
						DataHandler.getInstance().getSeries1().removeFirst();//Prevent graph to overload data.
					plot.redraw();
				}

				TextView min = (TextView) findViewById(R.id.min);
				min.setText("Min "+DataHandler.getInstance().getMin()+" BPM");

				TextView avg = (TextView) findViewById(R.id.avg);
				avg.setText("Avg "+DataHandler.getInstance().getAvg()+" BPM");

				TextView max = (TextView) findViewById(R.id.max);
				max.setText("Max "+DataHandler.getInstance().getMax()+" BPM");				
			}
		});
	}

}
