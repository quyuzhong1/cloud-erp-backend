package com.erp.sdk.oms.yunting.cem.client;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * JSON序列化/反序列化工具类
 *
 * @author ERP System
 */
public class JSON {
    
    private Gson gson;

    public JSON() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter())
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    /**
     * 获取Gson实例
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * 设置Gson实例
     */
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    /**
     * 序列化对象为JSON字符串
     */
    public String serialize(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 反序列化JSON字符串为对象
     */
    public <T> T deserialize(String body, Type returnType) {
        try {
            return gson.fromJson(body, returnType);
        } catch (JsonParseException e) {
            return null;
        }
    }

    /**
     * Date类型适配器
     */
    public static class DateTypeAdapter extends TypeAdapter<Date> {
        private DateFormat dateFormat;

        public DateTypeAdapter() {
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        }

        @Override
        public void write(JsonWriter out, Date date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(dateFormat.format(date));
            }
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    String dateStr = in.nextString();
                    try {
                        return dateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        throw new JsonParseException(e);
                    }
            }
        }
    }

    /**
     * LocalDate类型适配器
     */
    public static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public void write(JsonWriter out, LocalDate date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(date));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    String dateStr = in.nextString();
                    return LocalDate.parse(dateStr, formatter);
            }
        }
    }

    /**
     * OffsetDateTime类型适配器
     */
    public static class OffsetDateTimeTypeAdapter extends TypeAdapter<OffsetDateTime> {
        private DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        @Override
        public void write(JsonWriter out, OffsetDateTime date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(date));
            }
        }

        @Override
        public OffsetDateTime read(JsonReader in) throws IOException {
            switch (in.peek()) {
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    String dateStr = in.nextString();
                    return OffsetDateTime.parse(dateStr, formatter);
            }
        }
    }
}

