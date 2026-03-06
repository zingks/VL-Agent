package com.shang.vl.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by shangwei2009@hotmail.com on 2024/8/30 16:16
 */
@Slf4j
public class JacksonUtil {

    /**
     * 普通ObjectMapper
     */
    private static final ObjectMapper mapper;

    static {
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        mapper = new ObjectMapper();
        mapper.setLocale(Locale.CHINA).setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        mapper.registerModule(simpleModule);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            throw new RuntimeException("No ObjectMapper");
        }
        return mapper;
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        return parseObject(mapper, json, clazz);
    }

    public static <T> T parseObject(ObjectMapper objectMapper, String json, Class<T> clazz) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return objectMapper.readValue("{}", clazz);
            } else {
                return objectMapper.readValue(json, clazz);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON: " + json, e);
        }
    }

    /**
     * 无法将集合反序列化为具体元素集合，只能到LinkedHashMap
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        return parseArray(mapper, json, clazz);
    }

    /**
     * 无法将集合反序列化为具体元素集合，只能到LinkedHashMap
     *
     * @param objectMapper
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    @Deprecated
    public static <T> List<T> parseArray(ObjectMapper objectMapper, String json, Class<T> clazz) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return objectMapper.readValue("[]", new TypeReference<List<T>>() {
                });
            } else {
                return objectMapper.readValue(json, new TypeReference<List<T>>() {
                });
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSONArray: " + json, e);
        }
    }

    public static <T> T parse(String json, TypeReference<T> typeReference) {
        return parse(mapper, json, typeReference);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(ObjectMapper objectMapper, String json, TypeReference<T> typeReference) {
        try {
            if (json == null || json.trim().isEmpty()) {
                final Type parameterizedType = typeReference.getType();
                // List、Map
                if (parameterizedType instanceof ParameterizedType) {
                    final Type rawType = ((ParameterizedType) parameterizedType).getRawType();
                    if (rawType instanceof Class) {
                        return getInstance((Class<T>) rawType);
                    } else {
                        throw new RuntimeException("Type undefined: " + typeReference.getType());
                    }
                }
                // String、Integer
                else if (parameterizedType instanceof Class) {
                    return getInstance((Class<T>) parameterizedType);
                } else {
                    throw new RuntimeException("Type undefined: " + typeReference.getType());
                }
            } else {
                return objectMapper.readValue(json, typeReference);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON: " + json, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static <T> T getInstance(Class<T> clazz) {
        final int modifiers = clazz.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            throw new RuntimeException("Type must be concrete class");
        } else {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Error instantiating class: " + clazz, e);
            }
        }
    }

    public static String writeValueAsString(Object value) {
        return writeValueAsString(mapper, value);
    }

    public static String writeValueAsString(Object value, String defaultValue) {
        return writeValueAsString(mapper, value, defaultValue);
    }

    public static String writeValueAsString(ObjectMapper objectMapper, Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing JSONString: " + value, e);
        }
    }

    public static String writeValueAsString(ObjectMapper objectMapper, Object value, String defaultValue) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Error writing JSONString: " + value, e);
            return defaultValue;
        }
    }

    public static String writeValueAsStringPretty(Object value) {
        return writeValueAsStringPretty(mapper, value);
    }

    public static String writeValueAsStringPretty(Object value, String defaultValue) {
        return writeValueAsStringPretty(mapper, value, defaultValue);
    }

    public static String writeValueAsStringPretty(ObjectMapper objectMapper, Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing JSONString: " + value, e);
        }
    }

    public static String writeValueAsStringPretty(ObjectMapper objectMapper, Object value, String defaultValue) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Error writing JSONString: " + value, e);
            return defaultValue;
        }
    }

    public static <T> T convert(Object source, TypeReference<T> typeReference) {
        return convert(mapper, source, typeReference);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(ObjectMapper objectMapper, Object source, TypeReference<T> typeReference) {
        try {
            if (source == null) {
                final Type parameterizedType = typeReference.getType();
                // List、Map
                if (parameterizedType instanceof ParameterizedType) {
                    final Type rawType = ((ParameterizedType) parameterizedType).getRawType();
                    if (rawType instanceof Class) {
                        return getInstance((Class<T>) rawType);
                    } else {
                        throw new RuntimeException("Type undefined: " + typeReference.getType());
                    }
                }
                // String、Integer
                else if (parameterizedType instanceof Class) {
                    return getInstance((Class<T>) parameterizedType);
                } else {
                    throw new RuntimeException("Type undefined: " + typeReference.getType());
                }
            } else {
                return objectMapper.convertValue(source, typeReference);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error converting Object: " + source, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
