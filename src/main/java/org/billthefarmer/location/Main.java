////////////////////////////////////////////////////////////////////////////////
//
//  Location - An Android location app.
//
//  Copyright (C) 2015	Bill Farmer
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
///////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.location;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
// import org.osmdroid.views.overlay.compass.CompassOverlay;
// import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.OSRef;

public class Main extends Activity
    implements View.OnClickListener, LocationListener,
	       GpsStatus.Listener, GpsStatus.NmeaListener
{
    private static final String TAG = "Location";

    private TextView locationView;
    private StatusView statusView;
    private ImageView imageView;
    private MapView map;

    private LocationManager locationManager;
    private DateFormat dateFormat;

    private FileOutputStream os;

    private SimpleLocationOverlay simpleLocation;
    private List<Overlay> overlayList;

    private float accuracy;
    private boolean track;

    private boolean located;
    private boolean zoomed;
    
    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	View v = findViewById(R.id.start);
	if (v != null)
	    v.setOnClickListener(this);

	v = findViewById(R.id.open);
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

	org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants
	    .setUserAgentValue(BuildConfig.APPLICATION_ID);

        map = (MapView)findViewById(R.id.map);
	if (map != null)
	{
	    map.setTileSource(TileSourceFactory.MAPNIK);
	    map.setBuiltInZoomControls(true);
	    map.setMultiTouchControls(true);
	    overlayList = map.getOverlays();

	    ScaleBarOverlay scale = new ScaleBarOverlay(map);
	    scale.setAlignBottom(true);
	    scale.setAlignRight(false);
	    overlayList.add(scale);

	    MinimapOverlay miniMap =
		new MinimapOverlay(this, map.getTileRequestCompleteHandler());
	    miniMap.setZoomDifference(4);
	    overlayList.add(miniMap);

	    // InternalCompassOrientationProvider provider =
	    // 	new InternalCompassOrientationProvider(this);
	    // CompassOverlay compass =
	    // 	new CompassOverlay(this, provider, map);
	    // overlayList.add(compass);
	}

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
	locationManager.addNmeaListener(this);
	locationManager.addGpsStatusListener(this);
    }

    // On pause

    @Override
    protected void onPause()
    {
	super.onPause();

	locationManager.removeUpdates(this);
	locationManager.removeNmeaListener(this);
	locationManager.removeGpsStatusListener(this);
    }

    private void showLocation(Location location)
    {
	double lat = location.getLatitude();
	double lng = location.getLongitude();
	double alt = location.getAltitude();
	float  acc = location.getAccuracy();
	long   tim = location.getTime();

	IMapController mapController = map.getController();
	if (!zoomed)
	{
	    mapController.setZoom(14);
	    zoomed = true;
	}

	if (track || acc < accuracy)
	{
	    accuracy = acc;

	    GeoPoint point = new GeoPoint(location);
	    if (!located)
	    {
		mapController.setCenter(point);
		located = true;
	    }

	    if (simpleLocation == null)
	    {
		Bitmap bitmap = BitmapFactory
		    .decodeResource(getResources(), R.drawable.marker_default);
		simpleLocation = new SimpleLocationOverlay(bitmap);
		overlayList.add(simpleLocation);
	    }

	    simpleLocation.setLocation(point);

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
		east = Float.NaN;
		north = Float.NaN;
		OSString = "N/A";
	    }

	    String date = dateFormat.format(new Date(tim));

	    String format = "Latitude: %s\nLongitude: %s\nAltitude: %1.2fm\n" +
		"Accuracy: %1.0fm\nOSRef: %1.0f, %1.0f\nOSRef: %s\nTime: %s";
	    String text =
		String.format(Locale.getDefault(), format, latString, lngString,
			      alt, acc, east, north, OSString, date);

	    if (locationView != null)
		locationView.setText(text);
	}
    }

    private void showNmea(long timestamp, String nmea)
    {
	try
	{
	    if (os != null)
		os.write(nmea.getBytes());
	}

	catch (Exception e) {}
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
	    located = false;

	    Location location =
		locationManager
		.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	    if (location != null)
		showLocation(location);

	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						   5000, 0, this);
	    locationManager.addNmeaListener(this);
	    locationManager.addGpsStatusListener(this);

	    if (imageView != null)
		imageView.setVisibility(View.VISIBLE);
	    break;

	case R.id.open:
	    try
	    {
		if (os == null)
		{
		    File file = new File(getExternalFilesDir(null), "Nmea.txt");
		    os = new FileOutputStream(file);
		}
	    }

	    catch (Exception e) {}
	    break;

	case R.id.stop:
	    locationManager.removeUpdates(this);
	    locationManager.removeNmeaListener(this);
	    locationManager.removeGpsStatusListener(this);

	    if (imageView != null)
		imageView.setVisibility(View.INVISIBLE);

	    try
	    {
		if (os != null)
		{
		    os.close();
		    os = null;
		}
	    }

	    catch (Exception e) {}
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
    public void onNmeaReceived(long timestamp, String nmea)
    {
	showNmea(timestamp, nmea);
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
