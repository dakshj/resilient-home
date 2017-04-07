package com.resilienthome.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.resilienthome.enums.SensorType;

import java.lang.reflect.Type;

public class SensorTypeDeserializer implements JsonDeserializer {

    @Override
    public SensorType deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        int typeInt = json.getAsInt();
        return SensorType.from(typeInt);
    }
}
