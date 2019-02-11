package cn.admin.core.annotation;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class MapAnnotationAttributeExtractor extends AbstractAliasAwareAnnotationAttributeExtractor<Map<String,Object>> {

    MapAnnotationAttributeExtractor(Map<String,Object> attribute, Class<? extends Annotation> annotationType,
                                    @Nullable AnnotatedElement annotatedElement) {
        super(annotationType, annotatedElement, enrichAndValidateAttributes(attribute,annotationType));
    }

    @Override
    @Nullable
    protected Object getRawAttributeValue(Method attributeMethod) {
        return getRawAttributeValue(attributeMethod.getName());
    }

    @Override
    @Nullable
    protected Object getRawAttributeValue(String attributeName) {
        return getSource().get(attributeName);
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> enrichAndValidateAttributes(Map<String,Object> originalAttributes,Class<?
            extends Annotation> annotationType) {
        Map<String, Object> attributes = new LinkedHashMap<>(originalAttributes);
        Map<String, List<String>> attributeAliasMap = AnnotationUtils.getAttributeAliasMap(annotationType);

        for (Method attributeMethod : AnnotationUtils.getAttributeMethods(annotationType)) {
            String attributeName = attributeMethod.getName();
            Object attributeValue = attributes.get(attributeName);

            // if attribute not present, check aliases
            if (attributeValue == null) {
                List<String> aliasNames = attributeAliasMap.get(attributeName);
                if (aliasNames != null) {
                    for (String aliasName : aliasNames) {
                        Object aliasValue = attributes.get(aliasName);
                        if (aliasValue != null) {
                            attributeValue = aliasValue;
                            attributes.put(attributeName, attributeValue);
                            break;
                        }
                    }
                }
            }

            // if aliases not present, check default
            if (attributeValue == null) {
                Object defaultValue = AnnotationUtils.getDefaultValue(annotationType, attributeName);
                if (defaultValue != null) {
                    attributeValue = defaultValue;
                    attributes.put(attributeName, attributeValue);
                }
            }

            // if still null
            Assert.notNull(attributeValue, () -> String.format(
                    "Attributes map %s returned null for required attribute '%s' defined by annotation type [%s].",
                    attributes, attributeName, annotationType.getName()));

            // finally, ensure correct type
            Class<?> requiredReturnType = attributeMethod.getReturnType();
            Class<?> actualReturnType = attributeValue.getClass();

            if (!ClassUtils.isAssignable(requiredReturnType, actualReturnType)) {
                boolean converted = false;

                // Single element overriding an array of the same type?
                if (requiredReturnType.isArray() && requiredReturnType.getComponentType() == actualReturnType) {
                    Object array = Array.newInstance(requiredReturnType.getComponentType(), 1);
                    Array.set(array, 0, attributeValue);
                    attributes.put(attributeName, array);
                    converted = true;
                }

                // Nested map representing a single annotation?
                else if (Annotation.class.isAssignableFrom(requiredReturnType) &&
                        Map.class.isAssignableFrom(actualReturnType)) {
                    Class<? extends Annotation> nestedAnnotationType =
                            (Class<? extends Annotation>) requiredReturnType;
                    Map<String, Object> map = (Map<String, Object>) attributeValue;
                    attributes.put(attributeName, AnnotationUtils.synthesizeAnnotation(map, nestedAnnotationType, null));
                    converted = true;
                }

                // Nested array of maps representing an array of annotations?
                else if (requiredReturnType.isArray() && actualReturnType.isArray() &&
                        Annotation.class.isAssignableFrom(requiredReturnType.getComponentType()) &&
                        Map.class.isAssignableFrom(actualReturnType.getComponentType())) {
                    Class<? extends Annotation> nestedAnnotationType =
                            (Class<? extends Annotation>) requiredReturnType.getComponentType();
                    Map<String, Object>[] maps = (Map<String, Object>[]) attributeValue;
                    attributes.put(attributeName, AnnotationUtils.synthesizeAnnotationArray(maps, nestedAnnotationType));
                    converted = true;
                }

                Assert.isTrue(converted, () -> String.format(
                        "Attributes map %s returned a value of type [%s] for attribute '%s', " +
                                "but a value of type [%s] is required as defined by annotation type [%s].",
                        attributes, actualReturnType.getName(), attributeName, requiredReturnType.getName(),
                        annotationType.getName()));
            }
        }

        return attributes;
    }
}
