package com.airbnb.lottie;

import android.util.JsonReader;

import java.io.IOException;

public interface AnimatableValueDeserializer<T> {

  T valueFromObject(JsonReader reader, float scale) throws IOException;
}
