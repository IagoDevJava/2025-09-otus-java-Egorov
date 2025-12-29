package ru.otus.jdbc.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.otus.crm.model.Id;

@SuppressWarnings("java:S3011")
public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {

    private final String name;
    private final Constructor<T> constructor;
    private final Field idField;
    private final List<Field> allFields;
    private final List<Field> fieldsWithoutId;

    public EntityClassMetaDataImpl(Class<T> clazz) {
        this.name = clazz.getSimpleName();
        try {
            this.constructor = clazz.getDeclaredConstructor();
            this.constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class must have a no-arg constructor", e);
        }

        this.allFields = new ArrayList<>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            this.allFields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        this.allFields.forEach(f -> f.setAccessible(true));

        this.idField = findIdField();
        if (this.idField == null) {
            throw new IllegalArgumentException("No field annotated with @Id found in " + clazz.getName());
        }

        this.fieldsWithoutId = allFields.stream().filter(f -> f != idField).toList();
    }

    private Field findIdField() {
        return allFields.stream()
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    public Field getIdField() {
        return idField;
    }

    @Override
    public List<Field> getAllFields() {
        return allFields;
    }

    @Override
    public List<Field> getFieldsWithoutId() {
        return fieldsWithoutId;
    }
}
