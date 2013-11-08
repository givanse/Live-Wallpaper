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
    	private final Handler handler = new Handler();

        private boolean visible = true;                 /* current visibility */

        /**
          * Called at each iteration to draw one single frame and schedule
          * the next iteration.
          */
        private final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    iteration();
                    drawFrame();
                }
            };
            
        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(runnable);       /* stop the animation */
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
            	handler.removeCallbacks(runnable);
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
            handler.removeCallbacks(runnable);       /* stop the animation */
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
            handler.removeCallbacks(runnable);
            if (visible) {
                handler.postDelayed(runnable, 1000 / 25);
            }
        }
    } // inner class AnimationEngine
}
