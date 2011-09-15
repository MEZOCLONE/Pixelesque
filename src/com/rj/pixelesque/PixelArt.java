package com.rj.pixelesque;

import processing.core.PApplet;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.rj.processing.mt.Cursor;
import com.rj.processing.mt.MTManager;
import com.rj.processing.mt.Point;
import com.rj.processing.mt.TouchListener;

public class PixelArt extends PApplet implements TouchListener {
	
	public MTManager mtManager;
	PixelData art;
	private ScaleGestureDetector mScaleDetector;
	private PixelArtState state;
	
	
	
	
	public int sketchWidth() { return this.screenWidth; }
	public int sketchHeight() { return this.screenHeight; }
	public String sketchRenderer() { return PApplet.OPENGL; }
	public boolean keepTitlebar() { return true; }
	
	
	
	View bbbar;
	PixelArtStateView buttonbar;
	
	@Override
	public void onCreate(final Bundle savedinstance) {
		super.onCreate(savedinstance);
		bbbar = getLayoutInflater().inflate(com.rj.pixelesque.R.layout.buttonbar, null);
		this.addContentView(bbbar, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		buttonbar = (PixelArtStateView)bbbar.findViewById(com.rj.pixelesque.R.id.buttonbarz);
	}
	
	
	
	@Override
	public void setup() {
		hint(DISABLE_DEPTH_TEST);
		hint(DISABLE_OPENGL_ERROR_REPORT);
		hint(PApplet.DISABLE_ACCURATE_TEXTURES);
		hint(PApplet.DISABLE_DEPTH_MASK);
		hint(PApplet.DISABLE_DEPTH_SORT);
	    frameRate(60);
	    	    
	    debug();
	    
	    
	    mtManager = new MTManager();
	    mtManager.addTouchListener(this);

	    mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
	    
	    actualsetup();
	}
		
	
	public void debug() {
		  // Place this inside your setup() method
		  final DisplayMetrics dm = new DisplayMetrics();
		  getWindowManager().getDefaultDisplay().getMetrics(dm);
		  final float density = dm.density; 
		  final int densityDpi = dm.densityDpi;
		  println("density is " + density); 
		  println("densityDpi is " + densityDpi);
		  println("HEY! the screen size is "+width+"x"+height);
	}
	
	
	float origx;
	float origy;
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	    	float[] coords = art.getDataCoordsFloatFromXY(PixelArt.this, detector.getFocusX(), detector.getFocusY());

	    	
	    	
	        art.scale *= detector.getScaleFactor();
	        // Don't let the object get too small or too large.
	        art.scale = Math.max(1f, Math.min(art.scale, 5.0f));
	        
	        if (art.scale < 1.1f && art.scale > 0.9f) {
	        	art.scale = 1f;
	        }
	        
	        float[] postcoords = art.getXYFromDataCoordsFloat(PixelArt.this, coords[0], coords[1]);
	        float diffx = detector.getFocusX() -  postcoords[0];
	        float diffy = detector.getFocusY() -  postcoords[1];
	        //Log.d("Pixelesque", "SCALE: moving: "+diffx+", "+diffy  + "   orig:"+coords[0]+","+coords[1]+ "     post: "+postcoords[0]+","+postcoords[1]);
	        moveArt(diffx, diffy);

	        
	        return true;
	    }
	    
	    @Override
	    public boolean onScaleBegin(ScaleGestureDetector detector) {
	    	// TODO Auto-generated method stub
	    	return super.onScaleBegin(detector);
	    }
	}


	float mLastTouchX;
	float mLastTouchY;
	private static final int INVALID_POINTER_ID = -1;
	// The �active pointer� is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;
	public boolean touchEvent(MotionEvent ev) {
	    final int action = ev.getAction();
	    switch (action & MotionEvent.ACTION_MASK) {
	    case MotionEvent.ACTION_DOWN: {
	        final float x = ev.getX();
	        final float y = ev.getY();
	        
	        mLastTouchX = x;
	        mLastTouchY = y;
	        mActivePointerId = ev.getPointerId(0);
	        break;
	    }
	        
	    case MotionEvent.ACTION_MOVE: {
	        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
	        final float x = ev.getX(pointerIndex);
	        final float y = ev.getY(pointerIndex);

	        // Only move if the ScaleGestureDetector isn't processing a gesture.
	        if (!mScaleDetector.isInProgress()) {
	            final float dx = x - mLastTouchX;
	            final float dy = y - mLastTouchY;

	            moveArt(dx,dy);
	        }

	        mLastTouchX = x;
	        mLastTouchY = y;

	        break;
	    }
	        
	    case MotionEvent.ACTION_UP: {
	        mActivePointerId = INVALID_POINTER_ID;
	        break;
	    }
	        
	    case MotionEvent.ACTION_CANCEL: {
	        mActivePointerId = INVALID_POINTER_ID;
	        break;
	    }
	    
	    case MotionEvent.ACTION_POINTER_UP: {
	        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	        final int pointerId = ev.getPointerId(pointerIndex);
	        if (pointerId == mActivePointerId) {
	            // This was our active pointer going up. Choose a new
	            // active pointer and adjust accordingly.
	            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
	            mLastTouchX = ev.getX(newPointerIndex);
	            mLastTouchY = ev.getY(newPointerIndex);
	            mActivePointerId = ev.getPointerId(newPointerIndex);
	        }
	        break;
	    }
	    }
	    
	    return true;
	}

	
	
	@Override
	public boolean surfaceTouchEvent(final MotionEvent event) {
		if (mtManager != null) mtManager.surfaceTouchEvent(event);
	    mScaleDetector.onTouchEvent(event);
	    //touchEvent(event);
	    return super.surfaceTouchEvent(event);
	}
	
	Cursor movingCursor;
	
	@Override
	public void touchAllUp(Cursor c) {
		art.clearCursors();
	}
	@Override
	public void touchDown(Cursor c) {
		if (art.isValid(art.getDataCoordsFromXY(this, c.firstPoint.x, c.firstPoint.y))) {
			art.addCursor(c);
		}
		if (art.cursors.size() <= 1) {
			movingCursor = c;
		}
	}
	
	@Override
	public void touchMoved(Cursor c) {
		Log.d("Pixelesque", "jhlkjhlkjh"+c);

		if (art.hasCursor(c)) {
			int[] coords1 = art.getDataCoordsFromXY(this, c.firstPoint.x, c.firstPoint.y);
			int[] coords2 = art.getDataCoordsFromXY(this, c.currentPoint.x, c.currentPoint.y);
			Log.d("Pixelesque", "MOV: c1:("+coords1[0]+","+coords1[1]+")  c2:("+coords2[0]+","+coords2[1]+")   movcur:"+movingCursor);
			if (coords1[0] == coords2[0] && coords1[1] == coords2[1] && !mScaleDetector.isInProgress()) {
				
			} else {
				art.removeCursor(c);
			}
		} else if (movingCursor != null && movingCursor.curId == c.curId && !mScaleDetector.isInProgress()) {
			if (c.points.size() > 1) {
				Point p1 = c.points.get(c.points.size()-1);
				Point p2 = c.points.get(c.points.size()-2);
				moveArt(p1.x - p2.x, p1.y - p2.y);
			}
		}
		
		
	}
	@Override
	public void touchUp(Cursor c) {
		if (art.hasCursor(c)) {
			int[] coords1 = art.getDataCoordsFromXY(this, c.firstPoint.x, c.firstPoint.y);
			int[] coords2 = art.getDataCoordsFromXY(this, c.currentPoint.x, c.currentPoint.y);
			Log.d("Pixelesque", "UP: c1:("+coords1[0]+","+coords1[1]+")  c2:("+coords2[0]+","+coords2[1]+")");
			if (coords1[0] == coords2[0] && coords1[1] == coords2[1]) {
				int x = coords1[0];
				int y = coords1[1];
				art.flipColor(x, y, state.selectedColor);
			} else {
			}
			art.removeCursor(c);
		}		
		
		buttonbar.updateFromState();
	}


	void actualsetup() {
		art = new PixelData();
		state  = new PixelArtState();
	    buttonbar.setState(state, art);
	}

	@Override
	public void draw() {
		background(0);
		PApplet p = this;
		art.draw(p);
	}
	
	public void moveArt(float dx, float dy) {
        art.topx += dx;
        art.topy += dy;
        
        checkBounds();
	}
	
	public void checkBounds() {
        if (art.topx > 0) art.topx = 0;
        if (art.topy > 0) art.topy = 0;
        if (art.topx + art.getWidth(this) < this.width) art.topx = Math.min(0, (int)(width - art.getWidth(this)));
        if (art.topy + art.getHeight(this) < this.height) art.topy = Math.min(0, (int)(height - art.getHeight(this)));
	}


	@Override
	public void mousePressed() {
//		int[] coords = art.getDataCoordsFromXY(this, mouseX, mouseY);
//		int x = coords[0];
//		int y = coords[1];
//		art.flipColor(x, y);
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	
		
//	@Override
//	public boolean onCreateOptionsMenu(final Menu menu) {
//	    final MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(getMenu(), menu);
//	    return true;
//	}
//	
//	/**
//	 * its 1:42, and where is Jake?
//	 * Rj is goofy as fuck.
//	 */
//	
//	@Override
//	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
//	    switch (item.getItemId()) {
//	    case com.rj.processing.plasmasound.R.id.sequencer_settings:
//	        sequencerSettings();
//	        return true;
//
//	    default:
//	        return super.onOptionsItemSelected(item);
//	    }
//	}
	

}
