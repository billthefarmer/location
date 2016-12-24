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
import android.util.DisplayMetrics;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

// CopyrightOverlay

public class CopyrightOverlay extends Overlay
{
    private Paint paint;
    private String copyright;

    protected boolean alignBottom = false;
    protected boolean alignRight  = false;
    
    // Constructor

    public CopyrightOverlay(Context context, int id)
    {
	super();

	// Get the string
	Resources resources = context.getResources();
	copyright = resources.getString(id);

	// Get the display metrics
	final DisplayMetrics dm = resources.getDisplayMetrics();

	// Get paint
	paint = new Paint();
	paint.setAntiAlias(true);
 	paint.setTextSize(dm.density * 12);
   }

    // Set alignBottom

    public void setAlignBottom(boolean alignBottom)
    {
	this.alignBottom = alignBottom;
    }

    // Set alignRight

    public void setAlignRight(boolean alignRight)
    {
	this.alignRight = alignRight;
    }

    // Draw

    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow)
    {
	int width = canvas.getWidth();
	int height = canvas.getHeight();

	// float length = paint.measureText(copyright);
	// float size = paint.getTextSize();

	// Set text size to make text fill one third canvas width
	// paint.setTextSize(size * (width / 3) / length);
	// paint.setAntiAlias(true);

	float x = 0;
	float y = 0;

	if (alignRight)
	{
	    x = width - 8;
	    paint.setTextAlign(Paint.Align.RIGHT);
	}

	else
	{
	    x = 8;
	    paint.setTextAlign(Paint.Align.LEFT);
	}

	if (alignBottom)
	    y = height - 8;

	else
	    y = paint.getTextSize();

	// Draw the text
	if (!shadow)
	    canvas.drawText(copyright, x, y, paint);
    }
}
