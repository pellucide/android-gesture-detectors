package com.almeros.android.multitouch.sample;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import com.almeros.android.multitouch.MoveGestureDetector;
import com.almeros.android.multitouch.RotateGestureDetector;
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
public class TouchActivity extends Activity implements OnTouchListener {

    private final Matrix mMatrix = new Matrix();
    private float mScaleFactor;
    private float mRotationDegrees;
    private float mFocusX, mFocusY;
    private int mAlpha;
    private int mImageHeight, mImageWidth;

    private ImageView mImageView;

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

        resetImageView();
    }

    @SuppressWarnings("deprecation")
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);
        mShoveDetector.onTouchEvent(event);

        updateImageView();

        return true; // indicate event was handled
    }

    private void updateImageView() {
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
            resetImageView();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getActionMasked() != MotionEvent.ACTION_UP) return false;
            applyScaleDelta(2f);
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            applyScaleDelta(detector.getScaleFactor());
            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            mRotationDegrees += detector.getRotationDegreesDelta();
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
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
            mAlpha += detector.getShovePixelsDelta()/2;
            mAlpha = Math.max(0, Math.min(mAlpha, 255));
            return true;
        }
    }

}