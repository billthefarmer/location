package org.billthefarmer.location;

import android.app.ActionBar;
import android.app.Activity;
import android.location.Location;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.OSRef;

public class Main extends Activity
    implements View.OnClickListener, LocationListener, GpsStatus.Listener
{
    private TextView locationView;
    private StatusView statusView;
    private ImageView imageView;

    private LocationManager locationManager;
    private DateFormat dateFormat;

    private float accuracy;
    private boolean track;

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

	v = findViewById(R.id.track);
	if (v != null)
	    v.setOnClickListener(this);

	locationView = (TextView)findViewById(R.id.location);
	statusView = (StatusView)findViewById(R.id.status);

	// Acquire a reference to the system Location Manager
	locationManager =
	    (LocationManager)getSystemService(LOCATION_SERVICE);

	// Add custom view to action bar

	ActionBar actionBar = getActionBar();
	actionBar.setCustomView(R.layout.custom);
	actionBar.setDisplayShowCustomEnabled(true);
	imageView = (ImageView)actionBar.getCustomView();

	dateFormat = DateFormat.getDateTimeInstance();
    }

    @Override
    protected void onResume()
    {
	super.onResume();

	accuracy = 1000;

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
	double lng = location.getLongitude();
	double alt = location.getAltitude();
	float  acc = location.getAccuracy();
	long   tim = location.getTime();

	if (track || acc < accuracy)
	{
	    accuracy = acc;

	    String latString = Location.convert(lat, Location.FORMAT_SECONDS);
	    String lngString = Location.convert(lng, Location.FORMAT_SECONDS);

	    LatLng coord = new LatLng(lat, lng);
	    coord.toOSGB36();
	    OSRef OSCoord = coord.toOSRef();

	    double east;
	    double north;
	    String OSString;

	    if (OSCoord.isValid())
	    {
		east = OSCoord.getEasting();
		north = OSCoord.getNorthing();
		OSString = OSCoord.toSixFigureString();
	    }

	    else
	    {
		east = 0;
		north = 0;
		OSString = "N/A";
	    }

	    String date = dateFormat.format(new Date(tim));

	    String format = "Latitude: %s\nLongitude: %s\nAltitude: %1.2fm\n" +
		"Accuracy: %1.0fm\nOSRef: %1.0f, %1.0f\nOSRef: %s\nTime: %s";
	    String text =
		String.format(format, latString, lngString, alt, acc,
			      east, north, OSString, date);

	    if (locationView != null)
		locationView.setText(text);
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
	    accuracy = 1000;

	    Location location =
		locationManager
		.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	    if (location != null)
		showLocation(location);

	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						   5000, 0, this);
	    locationManager.addGpsStatusListener(this);

	    if (imageView != null)
		imageView.setVisibility(View.VISIBLE);
	    break;

	case R.id.stop:
	    locationManager.removeUpdates(this);
	    locationManager.removeGpsStatusListener(this);

	    if (imageView != null)
		imageView.setVisibility(View.INVISIBLE);
	    break;

	case R.id.track:
	    track = !track;
	    break;
	}
    }

    @Override
    public void onLocationChanged(Location location)
    {
	showLocation(location);
    }

    @Override
    public void onGpsStatusChanged(int event)
    {
	GpsStatus status = null;

	switch (event)
	{
	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	    status = locationManager.getGpsStatus(null);

	    if (statusView != null)
		statusView.updateStatus(status);

	    break;
	}
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
