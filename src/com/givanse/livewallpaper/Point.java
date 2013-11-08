package com.givanse.livewallpaper;

public class Point {
	
	  String text;
	  private float x;
	  private float y;

	  public Point(String text, float x, float y) {
		  this.text = text;
		  this.x = x;
		  this.y = y;
	  }
	  
	  public float getX() {
		  return this.x;
	  }
	  
	  public float getY() {
		  return this.y;
	  }
}
