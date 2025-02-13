//package com.ming.protocol;
//
//import com.google.gson.*;
//
//import java.io.*;
//import java.lang.reflect.Type;
//import java.nio.charset.StandardCharsets;
//
//public interface Serializer {
////    反序列化方法
//    <T> T deserialize(Class<T> clazz, byte[] bytes);
////    序列化方法
//    <T> byte[] serialize(T object);
//
//    enum Algorithm implements Serializer {
//        Java{
//        @Override
//        public <T> T deserialize(Class<T> clazz, byte[] bytes) {
//            try {
//                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
//                return  (T) ois.readObject();
//            } catch (IOException e) {
//                throw new RuntimeException("序列化失败",e);
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        @Override
//        public <T> byte[] serialize(T object) {
//            try {
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                ObjectOutputStream oos = new ObjectOutputStream(bos);
//                oos.writeObject(object);
//                return bos.toByteArray();
//            } catch (IOException e) {
//                throw new RuntimeException("序列化失败",e);
//            }
//        }
//
//        },
//        Json{
//            @Override
//            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
//                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Classcodec()).create();
//                String s = new String(bytes, StandardCharsets.UTF_8);
//                return gson.fromJson(s, clazz);
//            }
//
//            @Override
//            public <T> byte[] serialize(T object) {
//                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Classcodec()).create();
//                String json = gson.toJson(object);
//                return json.getBytes(StandardCharsets.UTF_8);
//            }
//        }
//
//    }
//    static class Classcodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {
//        @Override
//        public Class<?> deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
//            String str = json.getAsString();
//            try {
//                return Class.forName(str);
//            } catch (ClassNotFoundException e) {
//                throw new JsonParseException(e);
//            }
//        }
//
//        @Override
//        public JsonElement serialize(Class<?> src, Type type, JsonSerializationContext jsonSerializationContext) {
//            return new JsonPrimitive(src.getName());
//        }
//    }
//}
