package com.givanse.livewallpaper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.givanse.livewallpaper.R;
import com.givanse.livewallpaper.engine.AnimationWallpaper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveWallpaperService extends AnimationWallpaper {

	int xFish, yFish;

	@Override
    public Engine onCreateEngine() {
		return new LiveWallpaperEngine();
    }

    class LiveWallpaperEngine extends AnimationEngine {

    	float offsetX;
    	float offsetY;
    	int visibleWidth;
    	Set<RainbowCircle> rCircles = new HashSet<RainbowCircle>();

        int iterationCount = 0;
        
        public Bitmap imgFish;

        private List<Point> donuts;
        private Paint paint = new Paint();
        private int width;
        private int height;
        private int maxNumberOfDonuts;
        private boolean touchEnabled;

        LiveWallpaperEngine() {
        	// get the fish and background image references
            this.imgFish = BitmapFactory.decodeResource(getResources(), R.drawable.fish);
            xFish = -130; // initialize xFish position
            yFish = 200;  // initialize yFish position   

            SharedPreferences prefs = 
            	PreferenceManager.getDefaultSharedPreferences(LiveWallpaperService.this);
            this.maxNumberOfDonuts = 
            	Integer.valueOf(prefs.getString("numberOfCircles", "1"));
            this.touchEnabled = prefs.getBoolean("touch", false);
            this.donuts = new ArrayList<Point>();
            this.paint.setAntiAlias(true);
            this.paint.setColor(Color.WHITE);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeJoin(Paint.Join.ROUND);
            this.paint.setStrokeWidth(10f);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, 
                                     int format, int width, int height) {
        	this.height = height;
        	
        	if (this.isPreview()) {
        		this.width = width;
        	} else {
        		this.width = 2 * width;
        	}
        	this.visibleWidth = width;
        	
            /* Start with 20 random RainbowCircle instances. */ 
        	for (int i = 0; i < 20; i++) {
        		this.addRandomRainbowCircle();
        	}
        	 
        	super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (this.touchEnabled) {
                float x = event.getX();
                float y = event.getY();
                SurfaceHolder holder = getSurfaceHolder();
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.RED);
                        this.donuts.clear();
                        this.donuts.add(
                        	new Point(String.valueOf(this.donuts.size() + 1), x, y));
                        drawDonuts(canvas, this.donuts);
                    }
                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }    
            }
            
            super.onTouchEvent(event);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, 
        		                     float xStep, float yStep, 
        		                     int xPixels, int yPixels) {
        	this.offsetX = xOffset;
        	this.offsetY = yOffset;
        	 
        	super.onOffsetsChanged(xOffset, yOffset, xStep, yStep,
        	                       xPixels, yPixels);
        }
        
        /**
         * 
         */
        @Override
        public Bundle onCommand(String action, int x, int y, int z,
                                Bundle extras, boolean resultRequested) {
        	/* Draw a RainbowCircle on tapped location. */
            if ("android.wallpaper.tap".equals(action)) {
                addRainbowCircle(x - this.offsetX, y - this.offsetY);
            }
            
            return super.onCommand(action, x, y, z, extras, resultRequested);
        }

        @Override
        protected void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
 
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    drawRC(c);
                }
            } finally {
                if (c != null)
                    holder.unlockCanvasAndPost(c);
            }
        }
        
        @Override
        protected void iteration() {
           synchronized (donuts) {
        	  
        	  /* Update the state of every RainbowCircle. */
              for (Iterator<RainbowCircle> it = rCircles.iterator(); it.hasNext();) {
                 RainbowCircle circle = it.next();
                 circle.tick();
                 if (circle.isDone())
                    it.remove();
              }
              
              this.iterationCount++;
              
              if (isPreview() || this.iterationCount % 2 == 0)
                 addRandomRainbowCircle();
           }
         
           super.iteration();
        }
        
        void drawRC(Canvas c) {
    	   c.save();
    	   c.drawColor(0xff000000);
    	 
    	   synchronized (rCircles) {
    	      for (RainbowCircle rnbwCrcl : rCircles) {
    	    	  
    	         if (rnbwCrcl.alpha == 0)
    	            continue;
    	 
    	         // intersects with the screen?
    	         float minX = rnbwCrcl.x - rnbwCrcl.radius;
    	         if (minX > (-this.offsetX + this.visibleWidth)) {
    	            continue;
    	         }
    	         float maxX = rnbwCrcl.x + rnbwCrcl.radius;
    	         if (maxX < -this.offsetX) {
    	            continue;
    	         }
    	 
    	         this.paint.setAntiAlias(true);
    	 
    	         // paint the fill
    	         this.paint.setColor(Color.argb(
    	             rnbwCrcl.alpha, Color.red(rnbwCrcl.color), 
    	             				 Color.green(rnbwCrcl.color),
    	                             Color.blue(rnbwCrcl.color)));
    	         this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
    	         c.drawCircle(rnbwCrcl.x + this.offsetX, 
    	        		 	  rnbwCrcl.y + this.offsetY, 
                              rnbwCrcl.radius, this.paint);
    	 
    	         // paint the contour
    	         this.paint.setColor(Color.argb(
    	             rnbwCrcl.alpha, 63 + 3 * Color.red(rnbwCrcl.color) / 4, 
    	                             63 + 3 * Color.green(rnbwCrcl.color) / 4, 
    	                             63 + 3 * Color.blue(rnbwCrcl.color) / 4));
    	         this.paint.setStyle(Paint.Style.STROKE);
    	         this.paint.setStrokeWidth(3.0f);
    	         c.drawCircle(rnbwCrcl.x + this.offsetX, 
    	        		      rnbwCrcl.y + this.offsetY, 
                              rnbwCrcl.radius, this.paint);
    	      }
    	   }
    	   drawFish(c);
    	   c.restore();
    	}

        void drawFish(Canvas canvas) {
    	    // draw the fish
            canvas.drawBitmap(imgFish, xFish, yFish, null);
           
            // if xFish crosses the width means  xFish has reached to right edge
            if(xFish > this.visibleWidth + 100) {  
                // assign initial value to start with
                xFish = -130;
            }
            // change the xFish position/value by 1 pixel
            xFish += 3;

            if (donuts.size() >= maxNumberOfDonuts) {
                donuts.clear();
            }
            int x = (int) (this.visibleWidth * Math.random());
            int y = (int) (canvas.getHeight() * Math.random());
            donuts.add(new Point(String.valueOf(donuts.size() + 1), x, y));
            drawDonuts(canvas, donuts);
        }

        // Surface view requires that all elements are drawn completely
        private void drawDonuts(Canvas canvas, List<Point> donuts) {
        	paint.setColor(Color.BLUE);
            for (Point point : donuts) {
            	canvas.drawCircle(point.getX(), point.getY(), 20.0f, paint);
            }
        }
        
        void addRandomRainbowCircle() {
        	int x = (int) (this.width * Math.random());
        	int y = (int) (this.height * Math.random());
        	addRainbowCircle(x, y);
        }
        	 
        void addRainbowCircle(float x, float y) {
            RainbowCircle rCircle = new RainbowCircle(x, y, this.height);
            synchronized (this.donuts) {
                this.rCircles.add(rCircle);
            }
        }

    } // inner class LiveWallpaperEngine
}
