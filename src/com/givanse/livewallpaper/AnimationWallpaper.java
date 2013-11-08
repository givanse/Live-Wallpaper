package com.givanse.livewallpaper;

import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public abstract class AnimationWallpaper extends WallpaperService {

    /**
     * This objects handles the lifecycle events, animations and 
     * drawings of the wallpaper.
     */
    protected abstract class AnimationEngine extends Engine {

        /**
          * Handle the message queue associated with the main thread. 
          * Used to wait for the next iteration.
          */
    	private final Handler mHandler = new Handler();

        /**
          * Called at each iteration to draw one single frame and schedule
          * the next iteration.
          */
        private final Runnable mIteration = new Runnable() {
                @Override
                public void run() {
                    iteration();
                    drawFrame();
                }
            };
            
        private boolean visible = true;                 /* current visibility */

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mIteration);       /* stop the animation */
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
        	this.visible = visible;
            if(visible) {
                iteration();
                drawFrame();
            } else {
            	/**
                  * Suspend animation, stop drawing, save CPU cycles. 
                  * Stops the animation loop, removing the animation and
                  * redraw requests.
                  */
            	mHandler.removeCallbacks(mIteration);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, 
                                     int format, int width, int height) {
            iteration();
            drawFrame();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
        	super.onSurfaceDestroyed(holder);
            this.visible = false;
            mHandler.removeCallbacks(mIteration);       /* stop the animation */
        }

        public void onOffsetsChanged(float xOffset, float yOffset, 
        		                     float xStep, float yStep, 
        		                     int xPixels, int yPixels) {
        	iteration();
            drawFrame();
        }

        protected abstract void drawFrame();
        
        protected void iteration() {
            /* Reschedule the next redraw in 40ms */
            mHandler.removeCallbacks(mIteration);
            if (visible) {
                mHandler.postDelayed(mIteration, 1000 / 25);
            }
        }

    }
}
