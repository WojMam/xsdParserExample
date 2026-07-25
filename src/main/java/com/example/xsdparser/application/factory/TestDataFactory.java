package com.example.xsdparser.application.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Factory for creating test data dynamically from JAXB-generated classes.
 * Uses reflection to populate objects without requiring static test files.
 * 
 * This factory supports:
 * - Automatic field population with sensible defaults
 * - Custom value providers for specific fields
 * - Builder pattern for fluent configuration
 * - Random data generation for testing
 */
public class TestDataFactory {

    private static final Logger logger = LoggerFactory.getLogger(TestDataFactory.class);
    
    private static final Random random = new Random();
    private static DatatypeFactory datatypeFactory;
    
    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            logger.error("Failed to initialize DatatypeFactory", e);
        }
    }

    private final Map<Class<?>, Supplier<?>> typeProviders = new HashMap<>();
    private final Map<String, Object> fieldValues = new HashMap<>();
    private int maxDepth = 3;
    private boolean populateOptionalFields = false;
    private boolean useRandomValues = true;

    public TestDataFactory() {
        registerDefaultProviders();
    }

    private void registerDefaultProviders() {
        typeProviders.put(String.class, () -> useRandomValues ? generateRandomString() : "TestValue");
        typeProviders.put(BigDecimal.class, () -> BigDecimal.valueOf(useRandomValues ? random.nextDouble() * 1000 : 100.00));
        typeProviders.put(Integer.class, () -> useRandomValues ? random.nextInt(10000) : 1);
        typeProviders.put(int.class, () -> useRandomValues ? random.nextInt(10000) : 1);
        typeProviders.put(Long.class, () -> useRandomValues ? random.nextLong() : 1L);
        typeProviders.put(long.class, () -> useRandomValues ? random.nextLong() : 1L);
        typeProviders.put(Boolean.class, () -> useRandomValues ? random.nextBoolean() : true);
        typeProviders.put(boolean.class, () -> useRandomValues ? random.nextBoolean() : true);
        typeProviders.put(Double.class, () -> useRandomValues ? random.nextDouble() * 1000 : 100.0);
        typeProviders.put(double.class, () -> useRandomValues ? random.nextDouble() * 1000 : 100.0);
        typeProviders.put(XMLGregorianCalendar.class, this::createXmlCalendar);
        typeProviders.put(byte[].class, () -> "TestData".getBytes());
    }

    /**
     * Creates an instance of the specified class with populated fields.
     *
     * @param clazz the class to instantiate
     * @param <T>   the type
     * @return a populated instance
     */
    public <T> T create(Class<T> clazz) {
        return create(clazz, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T create(Class<T> clazz, int depth) {
        if (depth > maxDepth) {
            return null;
        }

        try {
            logger.debug("Creating instance of {} at depth {}", clazz.getSimpleName(), depth);
            T instance = clazz.getDeclaredConstructor().newInstance();
            populateFields(instance, depth);
            return instance;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error("Failed to create instance of {}", clazz.getSimpleName(), e);
            return null;
        }
    }

    private <T> void populateFields(T instance, int depth) {
        Class<?> clazz = instance.getClass();
        
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            
            String fieldKey = clazz.getSimpleName() + "." + field.getName();
            
            if (fieldValues.containsKey(fieldKey)) {
                setFieldValue(instance, field, fieldValues.get(fieldKey));
                continue;
            }
            
            if (fieldValues.containsKey(field.getName())) {
                setFieldValue(instance, field, fieldValues.get(field.getName()));
                continue;
            }

            if (shouldSkipField(field)) {
                continue;
            }

            Object value = createValueForField(field, depth);
            if (value != null) {
                setFieldValue(instance, field, value);
            }
        }
    }

    private boolean shouldSkipField(Field field) {
        return !populateOptionalFields && isOptionalField(field);
    }

    private boolean isOptionalField(Field field) {
        return field.getName().startsWith("_") || 
               field.getType().getSimpleName().contains("Optional");
    }

    private Object createValueForField(Field field, int depth) {
        Class<?> fieldType = field.getType();

        if (typeProviders.containsKey(fieldType)) {
            return typeProviders.get(fieldType).get();
        }

        if (fieldType.isEnum()) {
            return createEnumValue(fieldType);
        }

        if (List.class.isAssignableFrom(fieldType)) {
            return createListValue(field, depth);
        }

        if (fieldType.getPackageName().startsWith("com.example.xsdparser.generated")) {
            return create(fieldType, depth + 1);
        }

        return null;
    }

    private Object createEnumValue(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        if (constants != null && constants.length > 0) {
            return useRandomValues ? constants[random.nextInt(constants.length)] : constants[0];
        }
        return null;
    }

    private Object createListValue(Field field, int depth) {
        if (depth >= maxDepth) {
            return List.of();
        }

        try {
            ParameterizedType paramType = (ParameterizedType) field.getGenericType();
            Class<?> elementType = (Class<?>) paramType.getActualTypeArguments()[0];
            
            if (elementType.getPackageName().startsWith("com.example.xsdparser.generated")) {
                Object element = create(elementType, depth + 1);
                return element != null ? List.of(element) : List.of();
            }
            
            if (typeProviders.containsKey(elementType)) {
                return List.of(typeProviders.get(elementType).get());
            }
        } catch (Exception e) {
            logger.debug("Could not determine list element type for field: {}", field.getName());
        }
        
        return List.of();
    }

    private void setFieldValue(Object instance, Field field, Object value) {
        try {
            String setterName = "set" + capitalize(field.getName());
            Method setter = findSetter(instance.getClass(), setterName, field.getType());
            
            if (setter != null) {
                setter.invoke(instance, value);
            } else {
                field.setAccessible(true);
                field.set(instance, value);
            }
        } catch (Exception e) {
            logger.debug("Failed to set field {}: {}", field.getName(), e.getMessage());
        }
    }

    private Method findSetter(Class<?> clazz, String name, Class<?> paramType) {
        try {
            return clazz.getMethod(name, paramType);
        } catch (NoSuchMethodException e) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == 1) {
                    return method;
                }
            }
            return null;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String generateRandomString() {
        String[] prefixes = {"TEST", "SAMPLE", "DATA", "MSG", "REF"};
        return prefixes[random.nextInt(prefixes.length)] + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private XMLGregorianCalendar createXmlCalendar() {
        if (datatypeFactory == null) {
            return null;
        }
        LocalDate date = useRandomValues 
                ? LocalDate.now().minusDays(random.nextInt(365))
                : LocalDate.now();
        return datatypeFactory.newXMLGregorianCalendar(date.toString());
    }

    // Builder methods

    /**
     * Sets the maximum depth for recursive object creation.
     *
     * @param maxDepth the maximum depth
     * @return this factory for chaining
     */
    public TestDataFactory withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    /**
     * Enables population of optional fields.
     *
     * @return this factory for chaining
     */
    public TestDataFactory withOptionalFields() {
        this.populateOptionalFields = true;
        return this;
    }

    /**
     * Disables random value generation, using fixed defaults instead.
     *
     * @return this factory for chaining
     */
    public TestDataFactory withFixedValues() {
        this.useRandomValues = false;
        return this;
    }

    /**
     * Registers a custom value provider for a specific type.
     *
     * @param type     the type
     * @param provider the value provider
     * @param <T>      the type
     * @return this factory for chaining
     */
    public <T> TestDataFactory withTypeProvider(Class<T> type, Supplier<T> provider) {
        this.typeProviders.put(type, provider);
        return this;
    }

    /**
     * Sets a specific value for a field by name.
     *
     * @param fieldName the field name (can be "ClassName.fieldName" or just "fieldName")
     * @param value     the value to set
     * @return this factory for chaining
     */
    public TestDataFactory withFieldValue(String fieldName, Object value) {
        this.fieldValues.put(fieldName, value);
        return this;
    }

    /**
     * Creates a new builder for creating instances with specific configurations.
     *
     * @param clazz the class to build
     * @param <T>   the type
     * @return a new builder
     */
    public static <T> Builder<T> builder(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    /**
     * Builder for creating customized test data instances.
     *
     * @param <T> the type to build
     */
    public static class Builder<T> {
        private final Class<T> clazz;
        private final TestDataFactory factory;

        private Builder(Class<T> clazz) {
            this.clazz = clazz;
            this.factory = new TestDataFactory();
        }

        public Builder<T> maxDepth(int depth) {
            factory.withMaxDepth(depth);
            return this;
        }

        public Builder<T> withOptionalFields() {
            factory.withOptionalFields();
            return this;
        }

        public Builder<T> withFixedValues() {
            factory.withFixedValues();
            return this;
        }

        public Builder<T> with(String fieldName, Object value) {
            factory.withFieldValue(fieldName, value);
            return this;
        }

        public <V> Builder<T> withTypeProvider(Class<V> type, Supplier<V> provider) {
            factory.withTypeProvider(type, provider);
            return this;
        }

        public T build() {
            return factory.create(clazz);
        }
    }
}
