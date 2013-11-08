package com.givanse.livewallpaper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.givanse.livewallpaper.R;

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
        
        public Bitmap imgFish, backgroundImage;

        private List<Point> circles;
        private Paint paint = new Paint();
        private int width;
        private int height;
        private int maxNumber;
        private boolean touchEnabled;

        LiveWallpaperEngine() {
        	// get the fish and background image references
            imgFish = BitmapFactory.decodeResource(getResources(), R.drawable.fish);
            backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            xFish=-130; // initialize xFish position
            yFish=200;  // initialize yFish position   

            SharedPreferences prefs = PreferenceManager
                          .getDefaultSharedPreferences(LiveWallpaperService.this);
            maxNumber = Integer
                              .valueOf(prefs.getString("numberOfCircles", "4"));
            touchEnabled = prefs.getBoolean("touch", false);
            circles = new ArrayList<Point>();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(10f);
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
        	 
        	for (int i = 0; i < 20; i++) {
        		this.createRandomCircle();
        	}
        	 
        	super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (touchEnabled) {
                float x = event.getX();
                float y = event.getY();
                SurfaceHolder holder = getSurfaceHolder();
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.BLACK);
                        circles.clear();
                        circles.add(new Point(String.valueOf(circles.size() + 1), x, y));
                        drawCircles(canvas, circles);
                    }
                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
                super.onTouchEvent(event);
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, 
        		                     float xStep, float yStep, 
        		                     int xPixels, int yPixels) {
        	// store the offsets
        	this.offsetX = xOffset;
        	this.offsetY = yOffset;
        	 
        	super.onOffsetsChanged(xOffset, yOffset, xStep, yStep,
        	                       xPixels, yPixels);
        }
        
        @Override
        public Bundle onCommand(String action, int x, int y, int z,
            Bundle extras, boolean resultRequested) {
            if ("android.wallpaper.tap".equals(action)) {
                createCircle(x - this.offsetX, y - this.offsetY);
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
           synchronized (circles) {
              for (Iterator<RainbowCircle> it = rCircles.iterator(); it.hasNext();) {
                 RainbowCircle circle = it.next();
                 circle.tick();
                 if (circle.isDone())
                    it.remove();
              }
              iterationCount++;
              if (isPreview() || iterationCount % 2 == 0)
                 createRandomCircle();
           }
         
           super.iteration();
        }
        
        void drawRC(Canvas c) {
    	   c.save();
    	   c.drawColor(0xff000000);
    	 
    	   synchronized (rCircles) {
    	      for (RainbowCircle circle : rCircles) {
    	         if (circle.alpha == 0)
    	            continue;
    	 
    	         // intersects with the screen?
    	         float minX = circle.x - circle.radius;
    	         if (minX > (-this.offsetX + this.visibleWidth)) {
    	            continue;
    	         }
    	         float maxX = circle.x + circle.radius;
    	         if (maxX < -this.offsetX) {
    	            continue;
    	         }
    	 
    	         paint.setAntiAlias(true);
    	 
    	         // paint the fill
    	         paint.setColor(Color.argb(circle.alpha, Color
    	               .red(circle.color), Color.green(circle.color),
    	               Color.blue(circle.color)));
    	         paint.setStyle(Paint.Style.FILL_AND_STROKE);
    	         c.drawCircle(circle.x + this.offsetX, circle.y
    	               + this.offsetY, circle.radius, paint);
    	 
    	         // paint the contour
    	         paint.setColor(Color.argb(circle.alpha, 63 + 3 * Color
    	               .red(circle.color) / 4, 63 + 3 * Color
    	               .green(circle.color) / 4, 63 + 3 * Color
    	               .blue(circle.color) / 4));
    	         paint.setStyle(Paint.Style.STROKE);
    	         paint.setStrokeWidth(3.0f);
    	         c.drawCircle(circle.x + this.offsetX, circle.y
    	               + this.offsetY, circle.radius, paint);
    	      }
    	   }
    	   drawFish(c);
    	   c.restore();
    	}

        void drawFish(Canvas canvas) {
    	    // draw the fish
            canvas.drawBitmap(imgFish, xFish, yFish, null);
           
            // if xFish crosses the width means  xFish has reached to right edge
            if(xFish > canvas.getWidth() + 100) {  
                // assign initial value to start with
                xFish = -130;
            }
            // change the xFish position/value by 1 pixel
            xFish += 1;

            if (circles.size() >= maxNumber) {
                circles.clear();
            }
            int x = (int) (canvas.getWidth() * Math.random());
            int y = (int) (canvas.getHeight() * Math.random());
            circles.add(new Point(String.valueOf(circles.size() + 1), x, y));
            drawCircles(canvas, circles);
        }

        // Surface view requires that all elements are drawn completely
        private void drawCircles(Canvas canvas, List<Point> circles) {
        	paint.setColor(Color.BLUE);
            for (Point point : circles) {
            	canvas.drawCircle(point.getX(), point.getY(), 20.0f, paint);
            }
        }
        
        void createRandomCircle() {
        	   int x = (int) (width * Math.random());
        	   int y = (int) (height * Math.random());
        	   createCircle(x, y);
        	}
        	 
        	int getColor(float yFraction) {
        	   return Color.HSVToColor(new float[] { 360.0f * yFraction, 1.0f,
        	         1.0f });
        	}
        	 
        	void createCircle(float x, float y) {
        	   float radius = (float) (40 + 20 * Math.random());
        	 
        	   float yFraction = (float) y / (float) height;
        	   yFraction = yFraction + 0.05f - (float) (0.1f * (Math.random()));
        	   if (yFraction < 0.0f)
        	      yFraction += 1.0f;
        	   if (yFraction > 1.0f)
        	      yFraction -= 1.0f;
        	   int color = getColor(yFraction);
        	 
        	   int steps = 40 + (int) (20 * Math.random());
        	   RainbowCircle circle = new RainbowCircle(x, y, radius,
        	         color, steps);
        	   synchronized (this.circles) {
        	      this.rCircles.add(circle);
        	   }
        	}

    } // class LiveWallpaperEngine
}
