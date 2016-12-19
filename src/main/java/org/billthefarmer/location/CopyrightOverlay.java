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

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

// CopyrightOverlay

public class CopyrightOverlay extends Overlay
{
    private Paint paint;
    private String copyright;

    // Constructor

    public CopyrightOverlay(Context context, int id)
    {
	super();

	// Get the string
	Resources resources = context.getResources();
	copyright = resources.getString(id);

	paint = new Paint();
    }

    // Draw

    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow)
    {
	int width = canvas.getWidth();
	int height = canvas.getHeight();

	float length = paint.measureText(copyright);
	float size = paint.getTextSize();

	// Set text size to make text fill half canvas width
	paint.setTextSize(size * (width / 3) / length);
	paint.setAntiAlias(true);

	// Draw the text bottom left
	if (!shadow)
	    canvas.drawText(copyright, 8, height - 8, paint);
    }
}
