package org.cinos.core.users.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class DTOConverter {

    // Convierte una entidad en un DTO (record)
    public static <T, U> U toDTO(T entity, Class<U> dtoClass) {
        try {
            Constructor<?>[] constructors = dtoClass.getConstructors();
            Constructor<U> constructor = (Constructor<U>) constructors[0];
            Field[] dtoFields = dtoClass.getDeclaredFields();
            Object[] args = new Object[dtoFields.length];

            for (int i = 0; i < dtoFields.length; i++) {
                Field dtoField = dtoFields[i];
                dtoField.setAccessible(true);

                try {
                    Field entityField = entity.getClass().getDeclaredField(dtoField.getName());
                    entityField.setAccessible(true);
                    args[i] = entityField.get(entity);
                } catch (NoSuchFieldException e) {
                    args[i] = null; // Si no hay un campo correspondiente, establece null
                }
            }

            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to DTO", e);
        }
    }

    // Convierte un DTO (record) en una entidad
    public static <T, U> T toEntity(U dto, Class<T> entityClass) {
        try {
            T entityInstance = entityClass.getDeclaredConstructor().newInstance();
            Field[] dtoFields = dto.getClass().getDeclaredFields();

            for (Field dtoField : dtoFields) {
                dtoField.setAccessible(true);

                try {
                    Field entityField = entityClass.getDeclaredField(dtoField.getName());
                    entityField.setAccessible(true);

                    // Solo copia el valor si el tipo coincide entre el DTO y la entidad
                    if (entityField.getType().isAssignableFrom(dtoField.getType())) {
                        entityField.set(entityInstance, dtoField.get(dto));
                    }
                } catch (NoSuchFieldException e) {
                    // Si no hay un campo correspondiente en la entidad, continúa
                }
            }

            return entityInstance;
        } catch (Exception e) {
            throw new RuntimeException("Error converting to Entity", e);
        }
    }
}
