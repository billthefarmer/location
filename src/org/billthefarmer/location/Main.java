package org.billthefarmer.location;

import android.app.Activity;
import android.location.Location;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class Main extends Activity
    implements View.OnClickListener, LocationListener, GpsStatus.Listener
{
    private TextView locationView;
    private TextView statusView;

    private LocationManager locationManager;
    private DateFormat dateFormat;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	View v = findViewById(R.id.start);
	if (v != null)
	    v.setOnClickListener(this);

	v = findViewById(R.id.stop);
	if (v != null)
	    v.setOnClickListener(this);

	locationView = (TextView)findViewById(R.id.location);
	statusView = (TextView)findViewById(R.id.status);

	// Acquire a reference to the system Location Manager
	locationManager =
	    (LocationManager)getSystemService(LOCATION_SERVICE);

	dateFormat = DateFormat.getDateTimeInstance();
    }

    @Override
    protected void onResume()
    {
	super.onResume();

	Location location =
	    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	if (location != null)
	    showLocation(location);

	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					       5000, 0, this);
	locationManager.addGpsStatusListener(this);
    }

    // On pause

    @Override
    protected void onPause()
    {
	super.onPause();

	locationManager.removeUpdates(this);
	locationManager.removeGpsStatusListener(this);
    }

    private void showLocation(Location location)
    {
	double lat = location.getLatitude();
	double lon = location.getLongitude();
	double alt = location.getAltitude();
	float  acc = location.getAccuracy();
	long   tim = location.getTime();

	String latString = Location.convert(lat, Location.FORMAT_SECONDS);
	String lonString = Location.convert(lon, Location.FORMAT_SECONDS);

	String date = dateFormat.format(new Date(tim));

	String format = "Latitude: %s\nLongitude: %s\nAltitude: %1.2fm\n" +
	    "Accuracy: %1.0fm\n\nTime: %s";
	String text =
	    String.format(format, latString, lonString, alt, acc, date);

	if (locationView != null)
	    locationView.setText(text);
    }

    private void showStatus(GpsStatus status)
    {

	int used = 0;
	int count = 0;

	if (status != null)
	{
	    Iterable<GpsSatellite> satellites = status.getSatellites();

	    for (GpsSatellite satellite: satellites)
	    {
		if (satellite.usedInFix())
		    used++;

		count++;
	    }
	}

	String format = "Satellites: %d from %d\n";
	String text = String.format(format, used, count);

	if (statusView != null)
	    statusView.setText(text);

	if (status != null)
	{
	    Iterable<GpsSatellite> satellites = status.getSatellites();

	    count = 0;
	    for (GpsSatellite satellite: satellites)
	    {
		float snr = satellite.getSnr();

		text += String.format("%d: %1.0f\n", ++count, snr);
	    }

	    if (statusView != null)
		statusView.setText(text);
	}
    }

    // On click

    @Override
    public void onClick(View v)
    {
	int id = v.getId();

	switch (id)
	{
	case R.id.start:
	    Location location =
		locationManager
		.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	    if (location != null)
		showLocation(location);

	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						   5000, 0, this);
	    locationManager.addGpsStatusListener(this);
	    break;

	case R.id.stop:
	    locationManager.removeUpdates(this);
	    locationManager.removeGpsStatusListener(this);

	    if (locationView != null)
		locationView.setText("");

	    if (statusView != null)
		statusView.setText("");
	    break;
	}
    }

    @Override
    public void onLocationChanged(Location location)
    {
	// Called when a new location is found by the network location
	// provider.

	showLocation(location);
    }

    @Override
    public void onGpsStatusChanged(int event)
    {
	GpsStatus status = null;

	status = locationManager.getGpsStatus(status);

	if (status != null)
	    showStatus(status);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}