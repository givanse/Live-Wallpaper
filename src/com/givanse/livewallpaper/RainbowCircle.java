package com.givanse.livewallpaper;

public class RainbowCircle {

	float origRadius, deltaRadius, radius;
	   
	float origX, deltaX, x;
	 
	float origY, deltaY, y;
	 
	int color;
	 
	int alpha;
	 
	int steps;
	 
	int currentStep;
	   
	public RainbowCircle(float xCenter, float yCenter, float radius,
	                     int color, int steps) {
	      this.x = xCenter;
	      this.origX = xCenter;
	      this.deltaX = (float) (40.0 * Math.random() - 20.0);
	 
	      this.y = yCenter;
	      this.origY = yCenter;
	      this.deltaY = (float) (40.0 * Math.random() - 20.0);
	 
	      this.origRadius = radius;
	      this.radius = radius;
	      this.deltaRadius = 0.5f * radius;
	 
	      this.color = color;
	      this.alpha = 0;
	 
	      this.steps = steps;
	   }
	 
	   void tick() {
	      this.currentStep++;
	 
	      float fraction = (float) this.currentStep / (float) this.steps;
	 
	      this.radius = this.origRadius + fraction * this.deltaRadius;
	      this.x = this.origX + fraction * this.deltaX;
	      this.y = this.origY + fraction * this.deltaY;
	 
	      this.alpha = (fraction <= 0.25f) ? (int) (128 * 4.0f * fraction) : (int) (-128 * (fraction - 1) / 0.75f);
	   }
	 
	   boolean isDone() {
	       return this.currentStep > this.steps;
	   }
}
