package com.airbnb.lottie;

import android.graphics.PointF;
import android.util.JsonReader;

import org.json.JSONException;

import java.io.IOException;

class AnimatableSplitDimensionPathValue implements IAnimatablePathValue {
  private final PointF point = new PointF();

  private AnimatableFloatValue animatableXDimension;
  private AnimatableFloatValue animatableYDimension;

  AnimatableSplitDimensionPathValue(JsonReader reader, LottieComposition composition)
      throws IOException {
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "x":
          animatableXDimension = new AnimatableFloatValue(reader, composition);
          break;
        case "y":
          animatableYDimension = new AnimatableFloatValue(reader, composition);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    if (animatableXDimension == null || animatableYDimension == null) {
      throw new IllegalArgumentException("Unable to parse split dimension values.");
    }
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    return new SplitDimensionPathKeyframeAnimation(
        animatableXDimension.createAnimation(), animatableYDimension.createAnimation());
  }

  @Override public boolean hasAnimation() {
    return animatableXDimension.hasAnimation() || animatableYDimension.hasAnimation();
  }

  @Override public PointF getInitialPoint() {
    point.set(animatableXDimension.getInitialValue(), animatableYDimension.getInitialValue());
    return point;
  }
}
