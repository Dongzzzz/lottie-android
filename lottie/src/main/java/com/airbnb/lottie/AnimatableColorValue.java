package com.airbnb.lottie;

import android.graphics.Color;
import android.util.JsonReader;

import java.io.IOException;

class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {
  AnimatableColorValue(JsonReader reader, LottieComposition composition) throws IOException{
    super(reader, composition, false);
  }

  @Override public Integer valueFromObject(JsonReader reader, float scale) throws IOException {
    double[] colors = new double[4];
    int i = 0;
    boolean shouldUse255 = true;
    reader.beginArray();
    while (reader.hasNext()) {
      if (i >= 4) {
        reader.skipValue();
        continue;
      }
      double channel = reader.nextDouble();
      if (channel > 1f) {
        shouldUse255 = false;
      }
      colors[i++] = channel;
    }
    reader.endArray();

    if (i != 4) {
      return Color.BLACK;
    }

    float multiplier = shouldUse255 ? 255f : 1f;
    return Color.argb(
        (int) (colors[3] * multiplier),
        (int) (colors[0] * multiplier),
        (int) (colors[1] * multiplier),
        (int) (colors[2] * multiplier));
  }


  @Override public KeyframeAnimation<Integer> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }
    ColorKeyframeAnimation animation =
        new ColorKeyframeAnimation(getDuration(), composition, keyTimes, keyValues, interpolators);
    animation.setStartDelay(getDelay());
    return animation;
  }

  @Override public String toString() {
    return "AnimatableColorValue{" + "initialValue=" + initialValue + '}';
  }
}
