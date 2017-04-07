package com.resilienthome.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.resilienthome.enums.DeviceType;

import java.lang.reflect.Type;

public class DeviceTypeDeserializer implements JsonDeserializer {

    @Override
    public DeviceType deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        int typeInt = json.getAsInt();
        return DeviceType.from(typeInt);
    }
}
