package com.airbnb.lottie;

import android.graphics.PointF;
import android.util.JsonReader;

import java.io.IOException;

class JsonUtils {
  private JsonUtils() {
  }

  static PointF pointValueFromJsonObject(JsonReader reader) throws IOException {
    return pointValueFromJsonObject(reader, 1f);
  }

  static PointF pointValueFromJsonObject(JsonReader reader, float scale) throws IOException {
    PointF point = new PointF();
    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "x":
          point.x = (float) reader.nextDouble();
          break;
        case "y":
          point.y = (float) reader.nextDouble();
        default:
          reader.peek();
      }
    }

    reader.endObject();

    point.x *= scale;
    point.y *= scale;
    return point;
  }

  static PointF pointFromJsonArray(JsonReader reader) throws IOException {
    return pointFromJsonArray(reader, 1f);
  }

  static PointF pointFromJsonArray(JsonReader reader, float scale) throws IOException {
    PointF point = new PointF();
    int i = 0;
    reader.beginArray();
    while (reader.hasNext()) {
      if (i == 0) {
        point.x = (float) (reader.nextDouble() * scale);
      } else if (i == 1) {
        point.y = (float) (reader.nextDouble() * scale);
      } else {
        reader.skipValue();
        continue;
      }
      i++;
    }
    reader.endArray();
    return point;
  }
}
