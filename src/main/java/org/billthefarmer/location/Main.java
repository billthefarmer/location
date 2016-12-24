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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.OSRef;

public class Main extends Activity
    implements LocationListener, GpsStatus.Listener
{
    private static final String TAG = "Location";

    private TextView locationView;
    private StatusView statusView;
    private ImageView imageView;
    private MapView map;

    private LocationManager locationManager;
    private DateFormat dateFormat;

    private SimpleLocationOverlay simpleLocation;

    private boolean track;

    private boolean located;
    private boolean zoomed;
    
    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	// Get the views
	locationView = (TextView)findViewById(R.id.location);
	statusView = (StatusView)findViewById(R.id.status);

	// Set the user agent
	org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants
	    .setUserAgentValue(BuildConfig.APPLICATION_ID);

	// Get the map
        map = (MapView)findViewById(R.id.map);
	if (map != null)
	{
	    // Set up the map
	    map.setTileSource(TileSourceFactory.MAPNIK);
	    map.setBuiltInZoomControls(true);
	    map.setMultiTouchControls(true);

	    List<Overlay> overlayList = map.getOverlays();

	    // Add the overlays
	    CopyrightOverlay copyright =
		new CopyrightOverlay(this, R.string.copyright);
	    overlayList.add(copyright);
	    copyright.setAlignBottom(true);
	    copyright.setAlignRight(false);

	    ScaleBarOverlay scale = new ScaleBarOverlay(map);
	    scale.setAlignBottom(true);
	    scale.setAlignRight(true);
	    overlayList.add(scale);

	    Bitmap bitmap = BitmapFactory
		.decodeResource(getResources(), R.drawable.marker_default);
	    simpleLocation = new SimpleLocationOverlay(bitmap);
	    overlayList.add(simpleLocation);
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

    // On resume

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

    // On create options menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	// Inflate the menu; this adds items to the action bar if it
	// is present.

	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main, menu);

	return true;
    }

    // On options item selected

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	// Get id

	int id = item.getItemId();
	switch (id)
	{
	case R.id.action_start:
	    located = false;

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

	case R.id.action_stop:
	    locationManager.removeUpdates(this);
	    locationManager.removeGpsStatusListener(this);

	    if (imageView != null)
		imageView.setVisibility(View.INVISIBLE);
	    break;

	case R.id.action_track:
	    track = !track;
	    if (track)
		item.setIcon(R.drawable.ic_action_location_found);

	    else
		item.setIcon(R.drawable.ic_action_location_searching);
	    break;

	default:
	    return false;
	}

	return true;
    }

    // Show location

    private void showLocation(Location location)
    {
	IMapController mapController = map.getController();

	// Zoom map once
	if (!zoomed)
	{
	    mapController.setZoom(14);
	    zoomed = true;
	}

	// Get point
	GeoPoint point = new GeoPoint(location);

	// Centre map once
	if (!located)
	{
	    mapController.setCenter(point);
	    located = true;
	}

	// Unless tracking
	else if (track)
	    mapController.setCenter(point);

	// Set location
	simpleLocation.setLocation(point);

	float  acc = location.getAccuracy();
	double lat = location.getLatitude();
	double lng = location.getLongitude();
	double alt = location.getAltitude();

	String latString = Location.convert(lat, Location.FORMAT_SECONDS);
	String lngString = Location.convert(lng, Location.FORMAT_SECONDS);

	long   time = location.getTime();
	String date = dateFormat.format(new Date(time));

	LatLng coord = new LatLng(lat, lng);
	coord.toOSGB36();
	OSRef OSCoord = coord.toOSRef();

	String text = "";

	if (OSCoord.isValid())
	{
	    double east = OSCoord.getEasting();
	    double north = OSCoord.getNorthing();
	    String OSString = OSCoord.toSixFigureString();

	    String format =
		"Latitude: %s\nLongitude: %s\nAltitude: %1.2fm\n" +
		"Accuracy: %1.0fm\nOSRef: %1.0f, %1.0f\nOSRef: %s\n" +
		"Time: %s";
	    text = String.format(Locale.getDefault(), format, latString,
				 lngString, alt, acc, east, north, OSString,
				 date);
	}

	else
	{
	    String format =
		"Latitude: %s\nLongitude: %s\nAltitude: %1.2fm\n" +
		"Accuracy: %1.0fm\nTime: %s";
	    text = String.format(Locale.getDefault(), format, latString,
				 lngString, alt, acc, date);
	}

	if (locationView != null)
	    locationView.setText(text);
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
