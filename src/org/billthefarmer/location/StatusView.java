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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.util.AttributeSet;
import android.view.View;

public class StatusView extends View
{
    private GpsStatus status;
    private Iterable<GpsSatellite> satellites;

    private int width;
    private int height;

    private int maxSatellites;
    private float maxPrn = 32;

    private Paint paint;

    private int red;
    private int blue;
    private int white;

    // StatusView

    public StatusView(Context context, AttributeSet attrs)
    {
	super(context, attrs);

	paint = new Paint();

	Resources resources = context.getResources();

	red = resources.getColor(android.R.color.holo_red_dark);
	blue = resources.getColor(android.R.color.holo_blue_bright);
	white = resources.getColor(android.R.color.white);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
	super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	// Get offered dimension

	int w = MeasureSpec.getSize(widthMeasureSpec);

	// Set wanted dimensions

	setMeasuredDimension(w, w / 12);
    }

    // On size changed

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);

	// Get dimensions

	width = w;
	height = h;
    }

    // On draw

    @Override
    protected void onDraw(Canvas canvas)
    {
	canvas.translate(0, height - 2);

	float rectWidth = width / (maxPrn + 1);

	paint.setColor(white);
	canvas.drawLine(0, 1, width, 1, paint);

	if (satellites != null)
	{
	    for (GpsSatellite satellite: satellites)
	    {
		float x = rectWidth * satellite.getPrn();
		float y = satellite.getSnr() / 36f * height;

		if (satellite.usedInFix())
		    paint.setColor(blue);

		else
		    paint.setColor(red);

		canvas.drawRect(x + 1, -y, x + rectWidth - 1, 0, paint);
	    }
	}
    }

    // Update status

    public void updateStatus(GpsStatus status)
    {
	this.status = status;

	maxSatellites = status.getMaxSatellites();
	satellites = status.getSatellites();

	for (GpsSatellite satellite: satellites)
	    if (maxPrn < satellite.getPrn())
		maxPrn = satellite.getPrn();

	invalidate();
    }
}
