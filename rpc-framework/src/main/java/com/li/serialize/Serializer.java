package com.li.serialize;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.li.rpc.AppRpcResult;
import com.li.transport.DefaultRequest;
import com.li.transport.DefaultResponse;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public interface Serializer {

    <T> T deserialize(Class<T> clazz, byte[] bytes);

    <T> byte[] serialize(T object);

    enum Algorithm implements Serializer{
        Json{
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Class.class, new Serializer.ClassCodeC())
                        .registerTypeAdapter(Throwable.class, new ThrowableDecoder())
                        .create();
                String json = new String(bytes,StandardCharsets.UTF_8);
                return gson.fromJson(json,clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Class.class, new Serializer.ClassCodeC())
                        .create();
                String json = gson.toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }


    }

     class ClassCodeC implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String str = json.getAsString();
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

    class ThrowableDecoder implements JsonDeserializer<Throwable>{

        @Override
        public Throwable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            StringBuilder message = new StringBuilder();
            JsonElement target = json.getAsJsonObject().get("target");
            String detailMessage = target.getAsJsonObject().get("detailMessage").getAsString();
            message.append(detailMessage);
            JsonArray stackTrace = target.getAsJsonObject().get("stackTrace").getAsJsonArray();
            StackTraceElement[] stackTraceElements = new StackTraceElement[stackTrace.size()];
            for(int i=0; i<stackTrace.size(); i++){
                JsonObject asJsonObject = stackTrace.get(i).getAsJsonObject();
                StackTraceElement deserialize = context.deserialize(asJsonObject, StackTraceElement.class);
                stackTraceElements[i] = deserialize;

            }
            Throwable throwable = new Throwable(message.toString());
            throwable.setStackTrace(stackTraceElements);
            return throwable;
        }
    }


}
