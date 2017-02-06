package com.airbnb.lottie;

import android.graphics.PointF;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AnimatablePathValue implements IAnimatablePathValue, AnimatableValueDeserializer<Integer> {

  static IAnimatablePathValue createAnimatablePathOrSplitDimensionPath(
      JsonReader reader, LottieComposition composition) throws IOException {
    IAnimatablePathValue animatablePathValue = null;
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "k":
          animatablePathValue = new AnimatablePathValue(reader, composition);
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    if (animatablePathValue == null) {
      animatablePathValue = new AnimatableSplitDimensionPathValue(reader, composition);
    }

    return animatablePathValue;
  }

  private final List<Keyframe<Integer>> keyframes = new ArrayList<>();
  private final LottieComposition composition;

  private PointF initialPoint;
  private final SegmentedPath animationPath = new SegmentedPath();

  /**
   * Create a default static animatable path.
   */
  AnimatablePathValue(LottieComposition composition) {
    this.composition = composition;
    this.initialPoint = new PointF(0, 0);
  }

  AnimatablePathValue(JsonReader reader, LottieComposition composition) throws IOException {
    this(reader, composition, "k");
  }

  AnimatablePathValue(JsonReader reader, LottieComposition composition, String jsonKey)
      throws IOException {
    this.composition = composition;

    boolean foundValue = false;
    reader.beginObject();
    while (reader.hasNext()) {
      if (reader.nextName().equals(jsonKey) && !foundValue) {
        foundValue = true;
        setupAnimationForValue(reader);
      } else {
        reader.skipValue();
      }
    }
    reader.endObject();

    if (!foundValue) {
      throw new IllegalArgumentException("Point values have no keyframes.");
    }
  }

  private void setupAnimationForValue(JsonReader reader) throws IOException {
    JsonToken token = reader.peek();

    if (token == JsonToken.BEGIN_ARRAY) {
      buildAnimationForKeyframes(reader);
    } else {
      initialPoint = JsonUtils.pointFromJsonArray(reader, composition.getScale());
    }
  }

  @SuppressWarnings("Duplicates")
  private void buildAnimationForKeyframes(JsonReader reader) throws IOException {
    reader.beginArray();
    while (reader.hasNext()) {
      keyframes.add(new Keyframe<>(reader, this, composition.getScale()));
    }
    reader.endArray();

    if (!keyframes.isEmpty()) {
      initialValue = keyframes.get(0).startValue;
    }


    try {
      for (int i = 0; i < keyframes.length(); i++) {
        JSONObject kf = keyframes.getJSONObject(i);
        if (kf.has("t")) {
          startFrame = kf.getLong("t");
          break;
        }
      }

      for (int i = keyframes.length() - 1; i >= 0; i--) {
        JSONObject keyframe = keyframes.getJSONObject(i);
        if (keyframe.has("t")) {
          long endFrame = keyframe.getLong("t");
          if (endFrame <= startFrame) {
            throw new IllegalStateException(
                "Invalid frame compDuration " + startFrame + "->" + endFrame);
          }
          durationFrames = endFrame - startFrame;
          duration = (long) (durationFrames / (float) composition.getFrameRate() * 1000);
          delay = (long) (startFrame / (float) composition.getFrameRate() * 1000);
          break;
        }
      }

      boolean addStartValue = true;
      boolean addTimePadding = false;
      PointF outPoint = null;

      for (int i = 0; i < keyframes.length(); i++) {
        JSONObject keyframe = keyframes.getJSONObject(i);
        long frame = keyframe.getLong("t");
        float timePercentage = (float) (frame - startFrame) / (float) durationFrames;

        if (outPoint != null) {
          PointF vertex = outPoint;
          animationPath.lineTo(vertex.x, vertex.y);
          interpolators.add(new LinearInterpolator());
          outPoint = null;
        }

        float scale = composition.getScale();
        PointF startPoint =
            keyframe.has("s") ? JsonUtils.pointFromJsonArray(keyframe.getJSONArray("s"), scale) :
                new PointF();
        if (addStartValue) {
          if (i == 0) {
            animationPath.moveTo(startPoint.x, startPoint.y);
            initialPoint = startPoint;
          } else {
            animationPath.lineTo(startPoint.x, startPoint.y);
            interpolators.add(new LinearInterpolator());
          }
          addStartValue = false;
        }

        if (addTimePadding) {
          float holdPercentage = timePercentage - 0.00001f;
          keyTimes.add(holdPercentage);
          addTimePadding = false;
        }

        PointF cp1;
        PointF cp2;
        if (keyframe.has("e")) {
          cp1 = keyframe.has("to") ?
              JsonUtils.pointFromJsonArray(keyframe.getJSONArray("to"), scale) : null;
          cp2 = keyframe.has("ti") ?
              JsonUtils.pointFromJsonArray(keyframe.getJSONArray("ti"), scale) : null;
          PointF vertex = JsonUtils.pointFromJsonArray(keyframe.getJSONArray("e"), scale);
          if (cp1 != null && cp2 != null && cp1.length() != 0 && cp2.length() != 0) {
            animationPath.cubicTo(
                startPoint.x + cp1.x, startPoint.y + cp1.y,
                vertex.x + cp2.x, vertex.y + cp2.y,
                vertex.x, vertex.y);
          } else {
            animationPath.lineTo(vertex.x, vertex.y);
          }

          Interpolator interpolator;
          if (keyframe.has("o") && keyframe.has("i")) {
            cp1 = JsonUtils.pointValueFromJsonObject(keyframe.getJSONObject("o"), scale);
            cp2 = JsonUtils.pointValueFromJsonObject(keyframe.getJSONObject("i"), scale);
            interpolator = PathInterpolatorCompat
                .create(cp1.x / scale, cp1.y / scale, cp2.x / scale, cp2.y / scale);
          } else {
            interpolator = new LinearInterpolator();
          }
          interpolators.add(interpolator);
        }

        keyTimes.add(timePercentage);

        if (keyframe.has("h") && keyframe.getInt("h") == 1) {
          outPoint = startPoint;
          addStartValue = true;
          addTimePadding = true;
        }
      }
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse keyframes " + keyframes, e);
    }
  }

  @Override
  public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialPoint);
    }

    KeyframeAnimation<PointF> animation =
        new PathKeyframeAnimation(duration, composition, keyTimes, animationPath, interpolators);
    animation.setStartDelay(delay);
    return animation;
  }

  @Override
  public boolean hasAnimation() {
    return animationPath.hasSegments();
  }

  @Override
  public PointF getInitialPoint() {
    return initialPoint;
  }

  @Override public Integer valueFromObject(JsonReader reader, float scale) throws IOException {
    // This is the value in the keyframe
    return keyframes.size();
  }

  @Override
  public String toString() {
    return "initialPoint=" + initialPoint;
  }
}
