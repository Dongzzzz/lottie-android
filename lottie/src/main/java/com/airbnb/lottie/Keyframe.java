package com.airbnb.lottie;

import android.graphics.PointF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.JsonReader;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.io.IOException;

public class Keyframe<T> {
  private static Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

  T startValue;
  T endValue;
  long frame;
  Interpolator timingFunction;

  public Keyframe(JsonReader reader, AnimatableValueDeserializer<T> valueDeserializer, float scale)
      throws IOException {
    PointF cp1 = null;
    PointF cp2 = null;
    boolean hold = false;
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "t":
          frame = reader.nextLong();
          break;
        case "s":
          startValue = valueDeserializer.valueFromObject(reader, scale);
          break;
        case "e":
          endValue = valueDeserializer.valueFromObject(reader, scale);
          break;
        case "o":
          cp1 = JsonUtils.pointValueFromJsonObject(reader);
          break;
        case "i":
          cp2 = JsonUtils.pointValueFromJsonObject(reader);
          break;
        case "h":
          if (reader.nextInt() == 1) {
            hold = true;
          }
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    if (hold) {
      endValue = startValue;
      // TODO: create a HoldInterpolator so progress changes don't invalidate.
      timingFunction = LINEAR_INTERPOLATOR;
    } else if (cp1 == null || cp2 == null) {
      timingFunction = LINEAR_INTERPOLATOR;
    } else {
      timingFunction = PathInterpolatorCompat.create(cp1.x, cp1.y, cp2.x, cp2.y);
    }
  }

  @Override public String toString() {
    return "Keyframe{" + "startValue=" + startValue +
        ", endValue=" + endValue +
        ", frame=" + frame +
        ", timingFunction=" + timingFunction +
        '}';
  }
}
