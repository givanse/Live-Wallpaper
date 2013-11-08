package com.givanse.livewallpaper;

import java.util.ArrayList;
import java.util.List;

import com.givanse.livewallpaper.R;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveWallpaperService extends WallpaperService {

	int x, y;
   
	@Override
    public void onCreate() {
            super.onCreate();
    }

	@Override
    public void onDestroy() {
            super.onDestroy();
    }

	@Override
    public Engine onCreateEngine() {
		return new LiveWallpaperEngine();
    }

    /**
     * This objects handles the lifecycle events, animations and 
     * drawings of the wallpaper.
     */
    class LiveWallpaperEngine extends Engine {
    	private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
                @Override
                public void run() {
                    draw();
                }
            };
            
        private boolean visible = true;
        public Bitmap image1, backgroundImage;

        private List<Point> circles;
        private Paint paint = new Paint();
        private int width;
        private int height;
        private int maxNumber;
        private boolean touchEnabled;

        LiveWallpaperEngine() {
        	// get the fish and background image references
            image1 = BitmapFactory.decodeResource(getResources(), R.drawable.fish);
            backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            x=-130; // initialize x position
            y=200;  // initialize y position   

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
            handler.post(drawRunner);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
        	this.visible = visible;
            // if screen wallpaper is visible then draw the image otherwise do not draw
            if(visible) {
                handler.post(drawRunner);
            } else {
            	handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
        	super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, 
                                     int format, int width, int height) {
            this.width = width;
            this.height = height;
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

        public void onOffsetsChanged(float xOffset, float yOffset, 
        		                     float xStep, float yStep, 
        		                     int xPixels, int yPixels) {
        	draw();
        }

        void draw() {
        	final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null; 
            try {
            	canvas = holder.lockCanvas();
                // clear the canvas
                canvas.drawColor(Color.BLACK);
                if (canvas != null) {
                    // draw the background image
                    canvas.drawBitmap(backgroundImage, 0, 0, null);
                    // draw the fish
                    canvas.drawBitmap(image1, x,y, null);
                    // get the width of canvas
                    int width=canvas.getWidth();
                   
                    // if x crosses the width means  x has reached to right edge
                    if(x>width+100) {  
                        // assign initial value to start with
                        x=-130;
                    }
                    // change the x position/value by 1 pixel
                    x=x+1;

                    if (circles.size() >= maxNumber) {
                        circles.clear();
                    }
                    int x = (int) (width * Math.random());
                    int y = (int) (height * Math.random());
                    circles.add(new Point(String.valueOf(circles.size() + 1), x, y));
                    drawCircles(canvas, circles);
                }
            } finally {
            	if(canvas != null)
            		holder.unlockCanvasAndPost(canvas);
            }

            handler.removeCallbacks(drawRunner);

            if (visible) {
            	handler.postDelayed(drawRunner, 100); /* milliseconds */
            }   
        } // draw()

        // Surface view requires that all elements are drawn completely
        private void drawCircles(Canvas canvas, List<Point> circles) {
        	paint.setColor(Color.BLUE);
            for (Point point : circles) {
            	canvas.drawCircle(point.getX(), point.getY(), 20.0f, paint);
            }
        }

    } // class LiveWallpaperEngine
}
