package com.airbnb.lottie;

import android.util.JsonReader;

import java.io.IOException;

class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {
  AnimatableScaleValue(LottieComposition composition) {
    super(composition);
    initialValue = new ScaleXY();
  }

  AnimatableScaleValue(JsonReader reader, int frameRate, LottieComposition composition,
      boolean isDp) throws IOException {
    super(reader, composition, isDp);
  }

  @Override public ScaleXY valueFromObject(JsonReader reader, float scale) throws IOException {
    int i = 0;
    float scaleX = 1f;
    float scaleY = 1f;
    reader.beginArray();
    while (reader.hasNext()) {
      if (i == 0) {
        scaleX = (float) reader.nextDouble() / 100f * scale;
      } else if (i == 1) {
        scaleY = (float) reader.nextDouble() / 100f * scale;
      } else {
        reader.skipValue();
      }
      i++;
    }
    reader.endArray();
    return new ScaleXY(scaleX, scaleY);
  }

  @Override public KeyframeAnimation<ScaleXY> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    ScaleKeyframeAnimation animation =
        new ScaleKeyframeAnimation(getDuration(), composition, keyTimes, keyValues, interpolators);
    animation.setStartDelay(delay);
    return animation;
  }
}
