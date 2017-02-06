package com.airbnb.lottie;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;

class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {
  AnimatablePointValue(JsonReader reader, int frameRate, LottieComposition composition)
      throws IOException {
    super(reader, composition, true);
  }

  @Override public PointF valueFromObject(JsonReader reader, float scale) throws IOException {
    JsonToken token = reader.peek();
    if (token == JsonToken.BEGIN_ARRAY) {
      return JsonUtils.pointFromJsonArray(reader, scale);
    } else {
      return JsonUtils.pointValueFromJsonObject(reader, scale);
    }
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    KeyframeAnimation<PointF> animation =
        new PointKeyframeAnimation(getDuration(), composition, keyTimes, keyValues, interpolators);
    animation.setStartDelay(delay);
    return animation;
  }

  @Override public boolean hasAnimation() {
    return !keyValues.isEmpty();
  }
}
