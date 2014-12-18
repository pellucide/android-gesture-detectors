package com.almeros.android.multitouch.sample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.almeros.android.multitouch.MoveGestureDetector;
import com.almeros.android.multitouch.RotateGestureDetector;
import com.almeros.android.multitouch.Sensors;
import com.almeros.android.multitouch.ShoveGestureDetector;

/**
 * Test activity for testing the different GestureDetectors.
 *
 * @author Almer Thie (code.almeros.com)
 * Copyright (c) 2013, Almer Thie (code.almeros.com)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 *  in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
public class TouchActivity extends Activity implements OnTouchListener, Sensors.Listener  {
	private boolean LOG_SHOW_DETAILS=false;
	public static final String LOG_TAG= TouchActivity.class.getSimpleName();

    private  Matrix mMatrix = new Matrix();
    private float mScaleFactor;
    private float mRotationDegrees;
    private float mFocusX, mFocusY;
    private int mAlpha;
    private int mImageHeight, mImageWidth;
	public com.almeros.android.multitouch.Sensors sensors;

    private ImageView mImageView;
    private TextView mHeading, mPitch, mRoll,mInclination;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    private ShoveGestureDetector mShoveDetector;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set this class as touchListener to the ImageView
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnTouchListener(this);

        // Determine dimensions of 'earth' image
        Drawable d = getResources().getDrawable(R.drawable.parc);
        mImageHeight = d.getIntrinsicHeight();
        mImageWidth = d.getIntrinsicWidth();

        // Setup Gesture Detectors
        mGestureDetector = new GestureDetector(getApplicationContext(), new GestureListener());
        mScaleDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());
        mRotateDetector = new RotateGestureDetector(getApplicationContext(), new RotateListener());
        mMoveDetector = new MoveGestureDetector(getApplicationContext(), new MoveListener());
        mShoveDetector = new ShoveGestureDetector(getApplicationContext(), new ShoveListener());
        mHeading = (TextView) findViewById(R.id.heading);
        mPitch = (TextView) findViewById(R.id.pitch);
        mRoll = (TextView) findViewById(R.id.roll);
        mInclination = (TextView) findViewById(R.id.inclination);

        resetImageView();
    	sensors = new Sensors(getApplicationContext());
		sensors.registerListener(this);
    }

    @SuppressWarnings("deprecation")
    public boolean onTouch(View v, MotionEvent event) {
    	if (LOG_SHOW_DETAILS)
    		Log.d(LOG_TAG, "onTouch(v: " + v + " event: " + event + ")");
    	else
    		Log.d(LOG_TAG, "onTouch()");
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);
        mShoveDetector.onTouchEvent(event);

        updateImageView();
        return true; // indicate event was handled
    }
     
    @Override
    public void onResume() {
    	super.onResume();
		sensors.start();
    }

    @Override
    public void onPause() {
    	super.onPause();
    	sensors.stop();
    }

    float mRotateX, mRotateXPrev = 0.0f;
    float mRotateY, mRotateYPrev = 0.0f;
    Camera cam = new Camera();

    private void updateImageView() {
        float scaledImageCenterX = (mImageWidth * mScaleFactor) / 2;
        float scaledImageCenterY = (mImageHeight * mScaleFactor) / 2;

        Transformation trans = new Transformation();
        Transformation camTrans = new Transformation();
        mMatrix = trans.getMatrix();
        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postRotate(mRotationDegrees, scaledImageCenterX, scaledImageCenterY);

        Matrix camMatrix = camTrans.getMatrix();
        cam.rotateX(-mRotateXPrev + mRotateX);
        cam.rotateY(-mRotateYPrev + mRotateY);
        cam.getMatrix(camMatrix);

        mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);

        trans.compose(camTrans);
        mMatrix = trans.getMatrix();

        mImageView.setImageMatrix(mMatrix);
        mImageView.setAlpha(mAlpha);
    }

    private void updateImageView1() {
        float scaledImageCenterX = (mImageWidth * mScaleFactor) / 2;
        float scaledImageCenterY = (mImageHeight * mScaleFactor) / 2;

        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postRotate(mRotationDegrees, scaledImageCenterX, scaledImageCenterY);
        mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY - scaledImageCenterY);

        mImageView.setImageMatrix(mMatrix);
        mImageView.setAlpha(mAlpha);
    }

    private void resetImageView() {
        // Determine the center of the screen to center 'earth'
        Display display = getWindowManager().getDefaultDisplay();
        mFocusX = display.getWidth() / 2f;
        mFocusY = display.getHeight() / 2f;
        mScaleFactor = .4f;
        mRotationDegrees = 0f;
        mAlpha = 255;
        updateImageView();
    }

    private void applyScaleDelta(float scale) {
        mScaleFactor *= scale; // scale change since previous event
        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "GestureListener.onSingleTapConfirmed(e: " + e + ")");
        	else
        		Log.d(LOG_TAG, "GestureListener.onSingleTapConfirmed()");
            resetImageView();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "GestureListener.onDoubleTapEvent(e: " + e + ")");
        	else
        		Log.d(LOG_TAG, "GestureListener.onDoubleTapEvent()");
            if (e.getActionMasked() != MotionEvent.ACTION_UP)
            	return false;
            applyScaleDelta(2f);
            resetImageView();
            return true;
        }
		@Override
		public boolean onDown(MotionEvent e) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "GestureListener.onDown(e: " + e + ")");
        	else
        		Log.d(LOG_TAG, "GestureListener.onDown()");
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "GestureListener.onShowPress(e: " + e + ")");
        	else
        		Log.d(LOG_TAG, "GestureListener.onShowPress()");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "GestureListener.onSingleTapUp(e: " + e + ")");
        	else
        		Log.d(LOG_TAG, "GestureListener.onSingleTapUp()");
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	if (LOG_SHOW_DETAILS)
				Log.d(LOG_TAG, "GestureListener.onScroll(e1:"+e1+" e2:"+e2+"distanceX: "+distanceX+" distanceY:"+distanceY+")");
			else
        		Log.d(LOG_TAG, "GestureListener.onScroll()");
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "GestureListener.onLongPress(e: " + e + ")");
        	else
        		Log.d(LOG_TAG, "GestureListener.onLongPress()");
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	//if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "GestureListener.onFling(e1:"+e1+" e2:"+e2+" velocityX:"+velocityX+" velocityY:"+velocityY+")");
        	//else
        		//Log.d(LOG_TAG, "GestureListener.onFling()");

        	Runnable r = new BouncyInvalidator(velocityX, velocityY);
			mImageView.post(r);
			return true;
		}
    }
    
    private class BouncyInvalidator implements Runnable {
    	float velocityX, velocityY;
    	private long time;
		public BouncyInvalidator(float velocityX, float velocityY) {
			super();
			this.velocityX = velocityX/14;
			this.velocityY = velocityY/14;
		}

		@Override
		public void run() {
			if ((Math.abs(velocityX) < 1) && (Math.abs(velocityY) < 1))
				return;
			else {
				if (time==0)
					time=System.currentTimeMillis();
				long time1=System.currentTimeMillis();
				long diff = time1 - time;
				mFocusX += diff * velocityX;
				mFocusY += diff * velocityY;
				updateImageView();
				velocityX = velocityX*0.9f;
				velocityY = velocityY*0.9f;

				Display display = getWindowManager().getDefaultDisplay();
				int width =  (display.getWidth() / 2);
				int height = (display.getHeight() / 2);
				if ((mFocusX<0) || (mFocusX > width) ||
					(mFocusY<0) || (mFocusY > height) )
				{
				}
				else {
					mImageView.post(this);
				}
			}
		}
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "ScaleListener.onScale(detector: " + detector + ")");
        	else
        		Log.d(LOG_TAG, "ScaleListener.onScale()");

            applyScaleDelta(detector.getScaleFactor());
            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "RotateListener.onRotate(detector: " + detector + ")");
        	else
        		Log.d(LOG_TAG, "RotateListener.onRotate()");
            mRotationDegrees += detector.getRotationDegreesDelta();
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "MoveListener.onMove(detector: " + detector + ")");
        	else
        		Log.d(LOG_TAG, "MoveListener.onMove()");
            PointF d = detector.getFocusDelta();
            mFocusX += d.x;
            mFocusY += d.y;

            // mFocusX = detector.getFocusX();
            // mFocusY = detector.getFocusY();
            return true;
        }
    }

    private class ShoveListener extends ShoveGestureDetector.SimpleOnShoveGestureListener {
        @Override
        public boolean onShove(ShoveGestureDetector detector) {
        	if (LOG_SHOW_DETAILS)
        		Log.d(LOG_TAG, "ShoveListener.onShove(detector: " + detector + ")");
        	else
        		Log.d(LOG_TAG, "ShoveListener.onShove()");
            mAlpha += detector.getShovePixelsDelta()/2;
            mAlpha = Math.max(0, Math.min(mAlpha, 255));
            return true;
        }
    }

	@Override
	public void onSensorsStateChangeMagAcc() {
		mHeading.setText(String.valueOf(sensors.Heading));
		mPitch.setText(String.valueOf(sensors.Pitch));
		mRoll.setText(String.valueOf(sensors.Roll));

		mInclination.setText(String.valueOf(sensors.inclination));
		
		mRotationDegrees = sensors.Heading;

		mRotateXPrev = mRotateX;
		mRotateX = sensors.Pitch;

		mRotateYPrev = mRotateY;
		mRotateY = sensors.Roll;

		updateImageView();
		//mHeading.setText(String.valueOf(sensors.yaw));
		//mPitch.setText(String.valueOf(sensors.pitch));
		//mRoll.setText(String.valueOf(sensors.roll));
	}

	@Override
	public void onSensorsStateGPSLocationChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorsStateGPSStatusChange() {
		// TODO Auto-generated method stub
		
	}
}