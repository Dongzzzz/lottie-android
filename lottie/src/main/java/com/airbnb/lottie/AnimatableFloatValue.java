package com.airbnb.lottie;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;

class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {
  AnimatableFloatValue(LottieComposition composition, Float initialValue) {
    super(composition);
    this.initialValue = initialValue;
  }

  AnimatableFloatValue(JsonReader reader, LottieComposition composition)
      throws IOException{
    this(reader, composition, true);
  }

  AnimatableFloatValue(JsonReader reader, LottieComposition composition,
      boolean isDp) throws IOException {
    super(reader, composition, isDp);
  }

  @Override public Float valueFromObject(JsonReader reader, float scale) throws IOException {
    JsonToken token = reader.peek();

    if (token == JsonToken.BEGIN_ARRAY) {
      reader.beginArray();
      Float value = null;
      while (reader.hasNext()) {
        if (value == null) {
          value = (float) reader.nextDouble() * scale;
        } else {
          reader.skipValue();
        }
      }
      reader.endArray();
    } else if (token == JsonToken.NUMBER){
      return (float) reader.nextDouble() * scale;
    }

    throw new IllegalArgumentException("Can't parse " + token + " into a float.");
  }

  @Override public KeyframeAnimation<Float> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    KeyframeAnimation<Float> animation =
        new NumberKeyframeAnimation<>(getDuration(), composition, keyTimes, Float.class, keyValues,
            interpolators);
    animation.setStartDelay(delay);
    return animation;
  }

  public Float getInitialValue() {
    return initialValue;
  }
}
