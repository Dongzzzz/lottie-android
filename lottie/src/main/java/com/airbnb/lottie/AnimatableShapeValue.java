package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AnimatableShapeValue extends BaseAnimatableValue<ShapeData, Path> {
  private final Path convertTypePath = new Path();
  private boolean closed;

  AnimatableShapeValue(JsonReader reader, int frameRate, LottieComposition composition,
      boolean closed) throws IOException {
    super(null, composition, true);
    this.closed = closed;
    init(reader);
  }

  @Override public ShapeData valueFromObject(JsonReader reader, float scale) throws IOException {
    JsonToken token = reader.peek();

    ShapeData shapeData = null;
    if (token == JsonToken.BEGIN_ARRAY) {
      int i = 0;
      reader.beginArray();
      while (reader.hasNext()) {
        if (i > 0) {
          reader.skipValue();
          continue;
        }
        shapeData = parsePointsData(reader, scale);
        i++;
      }
      reader.endArray();
    } else if (token == JsonToken.BEGIN_OBJECT) {
      shapeData = parsePointsData(reader, scale);
    }

    if (shapeData == null) {
      throw new IllegalStateException("Unable to get shape with token " + token);
    }
    return shapeData;
  }

  private ShapeData parsePointsData(JsonReader reader, float scale) throws IOException {
    List<PointF> vertices = null;
    List<PointF> inTangents = null;
    List<PointF> outTangents = null;
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "v":
          vertices = parsePointsArray(reader);
          break;
        case "i":
          inTangents = parsePointsArray(reader);
          break;
        case "o":
          outTangents = parsePointsArray(reader);
          break;
        case "c":
          // Bodymovin < 4.4 uses "closed" one level up in the json so it is passed in to the
          // constructor.
          // Bodymovin 4.4+ has closed here.
          closed = true;
          break;
        default:
          reader.skipValue();
      }
    }

    if (vertices == null) {
      throw new IllegalArgumentException("Unable to find vertices.");
    }
    if (inTangents == null) {
      throw new IllegalArgumentException("Unable to find inTangents.");
    }
    if (outTangents == null) {
      throw new IllegalArgumentException("Unable to find outTangents.");
    }

    ShapeData shape = new ShapeData();

    PointF vertex = vertices.get(0);
    vertex.x *= scale;
    vertex.y *= scale;
    shape.setInitialPoint(vertex);

    for (int i = 1; i < vertices.size(); i++) {
      vertex = vertices.get(i);
      PointF previousVertex = vertices.get(i - 1);
      PointF cp1 = outTangents.get(i - 1);
      PointF cp2 = inTangents.get(i);

      PointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
      PointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);

      shapeCp1.x *= scale;
      shapeCp1.y *= scale;
      shapeCp2.x *= scale;
      shapeCp2.y *= scale;
      vertex.x *= scale;
      vertex.y *= scale;

      shape.addCurve(new CubicCurveData(shapeCp1, shapeCp2, vertex));
    }

    if (closed) {
      vertex = vertices.get(0);
      PointF previousVertex = vertices.get(vertices.size() - 1);
      PointF cp1 = outTangents.get(outTangents.size() - 1);
      PointF cp2 = inTangents.get(0);

      PointF shapeCp1 = MiscUtils.addPoints(previousVertex, cp1);
      PointF shapeCp2 = MiscUtils.addPoints(vertex, cp2);

      if (scale != 1f) {
        shapeCp1.x *= scale;
        shapeCp1.y *= scale;
        shapeCp2.x *= scale;
        shapeCp2.y *= scale;
        vertex.x *= scale;
        vertex.y *= scale;
      }

      shape.addCurve(new CubicCurveData(shapeCp1, shapeCp2, vertex));
    }
    return shape;
  }

  private List<PointF> parsePointsArray(JsonReader reader) throws IOException {
    reader.beginArray();
    List<PointF> points = new ArrayList<>();
    while (reader.hasNext()) {
      points.add(parsePoint(reader));
    }
    reader.endArray();
    return points;
  }

  private PointF parsePoint(JsonReader reader) throws IOException {
    reader.beginArray();
    int i = 0;
    float x = 0;
    float y = 0;
    while (reader.hasNext()) {
      if (i == 0) {
        x = (float) reader.nextDouble();
      } else if (i == 1) {
        y = (float) reader.nextDouble();
      } else {
        reader.skipValue();
      }
      i++;
    }
    reader.endArray();
    return new PointF(x, y);
  }

  @Override public KeyframeAnimation<Path> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(convertType(initialValue));
    }

    ShapeKeyframeAnimation animation =
        new ShapeKeyframeAnimation(getDuration(), composition, keyTimes, keyValues, interpolators);
    animation.setStartDelay(delay);
    return animation;
  }

  @Override Path convertType(ShapeData shapeData) {
    convertTypePath.reset();
    MiscUtils.getPathFromData(shapeData, convertTypePath);
    return convertTypePath;
  }
}
