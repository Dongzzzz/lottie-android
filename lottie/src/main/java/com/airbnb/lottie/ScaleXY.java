package com.airbnb.lottie;

class ScaleXY {
  private float scaleX = 1f;
  private float scaleY = 1f;

  ScaleXY() {
  }

  ScaleXY(float scaleX, float scaleY) {
    this.scaleX = scaleX;
    this.scaleY = scaleY;
  }

  ScaleXY scale(float sx, float sy) {
    this.scaleX = sx;
    this.scaleY = sy;
    return this;
  }

  float getScaleX() {
    return scaleX;
  }

  float getScaleY() {
    return scaleY;
  }

  boolean isDefault() {
    return scaleX == 1f && scaleY == 1f;
  }

  @Override public String toString() {
    return getScaleX() + "x" + getScaleY();
  }
}
