package cn.admin.core.annotation;

import cn.admin.lang.Nullable;
import cn.admin.util.LinkedMultiValueMap;
import cn.admin.util.MultiValueMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

public abstract class AnnotatedElementUtils {

    @Nullable
    private static final Boolean CONTINUE = null;

    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    private static final Processor<Boolean> alwaysTrueAnnotationProcessor = new AlwaysTrueBooleanAnnotationProcessor();

    public static AnnotatedElement forAnnotations(final Annotation... annotations) {
        return new AnnotatedElement() {
            @Override
            @Nullable
            @SuppressWarnings("unchecked")
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
                for (Annotation ann : annotations) {
                    if (ann.annotationType() == annotationClass) {
                        return (T) ann;
                    }
                }
                return null;
            }

            @Override
            public Annotation[] getAnnotations() {
                return annotations;
            }

            @Override
            public Annotation[] getDeclaredAnnotations() {
                return annotations;
            }
        };
    }

    public static boolean hasMetaAnnotationTypes(AnnotatedElement element, String annotationName) {
        return hasMetaAnnotationTypes(element, null, annotationName);
    }

    private static boolean hasMetaAnnotationTypes(
            AnnotatedElement element, @Nullable Class<? extends Annotation> annotationType, @Nullable String annotationName) {

        return Boolean.TRUE.equals(
                searchWithGetSemantics(element, annotationType, annotationName, new SimpleAnnotationProcessor<Boolean>() {
                    @Override
                    @Nullable
                    public Boolean process(@Nullable AnnotatedElement annotatedElement, Annotation annotation, int metaDepth) {
                        return (metaDepth > 0 ? Boolean.TRUE : CONTINUE);
                    }
                }));
    }

    public static Set<String> getMetaAnnotationTypes(AnnotatedElement element, Class<? extends Annotation> annotationType) {
        return getMetaAnnotationTypes(element, element.getAnnotation(annotationType));
    }

    public static Set<String> getMetaAnnotationTypes(AnnotatedElement element, String annotationName) {
        return getMetaAnnotationTypes(element, AnnotationUtils.getAnnotation(element, annotationName));
    }

    private static Set<String> getMetaAnnotationTypes(AnnotatedElement element, @Nullable Annotation composed) {
        if (composed == null) {
            return Collections.emptySet();
        }

        try {
            final Set<String> types = new LinkedHashSet<>();
            searchWithGetSemantics(composed.annotationType(), Collections.emptySet(), null, null, new SimpleAnnotationProcessor<Object>(true) {
                @Override
                @Nullable
                public Object process(@Nullable AnnotatedElement annotatedElement, Annotation annotation, int metaDepth) {
                    types.add(annotation.annotationType().getName());
                    return CONTINUE;
                }
            }, new HashSet<>(), 1);
            return types;
        }
        catch (Throwable ex) {
            AnnotationUtils.rethrowAnnotationConfigurationException(ex);
            throw new IllegalStateException("Failed to introspect annotations on " + element, ex);
        }
    }

    public static boolean isAnnotated(AnnotatedElement element,Class<? extends Annotation> annotationType) {
        if (element.isAnnotationPresent(annotationType)) {
            return true;
        }
        return Boolean.TRUE.equals(searchWithGetSemantics(element,annotationType,null,alwaysTrueAnnotationProcessor));
    }

    public static boolean isAnnotated(AnnotatedElement element, String annotationName) {
        return Boolean.TRUE.equals(searchWithGetSemantics(element, null, annotationName, alwaysTrueAnnotationProcessor));
    }

    @Nullable
    public static AnnotationAttributes getMergedAnnotationAttributes(
            AnnotatedElement element, Class<? extends Annotation> annotationType) {

        AnnotationAttributes attributes = searchWithGetSemantics(element, annotationType, null,
                new MergedAnnotationAttributesProcessor());
        AnnotationUtils.postProcessAnnotationAttributes(element, attributes, false, false);
        return attributes;
    }

    @Nullable
    public static AnnotationAttributes getMergedAnnotationAttributes(AnnotatedElement element, String annotationName) {
        return getMergedAnnotationAttributes(element, annotationName, false, false);
    }

    @Nullable
    public static AnnotationAttributes getMergedAnnotationAttributes(AnnotatedElement element,
                                                                     String annotationName, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {

        AnnotationAttributes attributes = searchWithGetSemantics(element, null, annotationName,
                new MergedAnnotationAttributesProcessor(classValuesAsString, nestedAnnotationsAsMap));
        AnnotationUtils.postProcessAnnotationAttributes(element, attributes, classValuesAsString, nestedAnnotationsAsMap);
        return attributes;
    }

    @Nullable
    public static <A extends Annotation> A getMergedAnnotation(AnnotatedElement element, Class<A> annotationType) {
        // Shortcut: directly present on the element, with no merging needed?
        A annotation = element.getDeclaredAnnotation(annotationType);
        if (annotation != null) {
            return AnnotationUtils.synthesizeAnnotation(annotation, element);
        }

        // Shortcut: no searchable annotations to be found on plain Java classes and org.springframework.lang types...
        if (AnnotationUtils.hasPlainJavaAnnotationsOnly(element)) {
            return null;
        }

        // Exhaustive retrieval of merged annotation attributes...
        AnnotationAttributes attributes = getMergedAnnotationAttributes(element, annotationType);
        return (attributes != null ? AnnotationUtils.synthesizeAnnotation(attributes, annotationType, element) : null);
    }

    @Nullable
    public static MultiValueMap<String,Object> getAllAnnotationAttributes(AnnotatedElement element,
                                                                          String annotationName,
                                                                          final boolean classValuesAsString,
                                                                          final boolean nestedAnnotationsAsMap) {
        final MultiValueMap<String, Object> attributesMap = new LinkedMultiValueMap<>();

        searchWithGetSemantics(element, null, annotationName, new SimpleAnnotationProcessor<Object>() {
            @Override
            @Nullable
            public Object process(@Nullable AnnotatedElement annotatedElement, Annotation annotation, int metaDepth) {
                AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(
                        annotation, classValuesAsString, nestedAnnotationsAsMap);
                annotationAttributes.forEach(attributesMap::add);
                return CONTINUE;
            }
        });

        return (!attributesMap.isEmpty() ? attributesMap : null);
    }

    @Nullable
    private static <T> T searchWithGetSemantics(AnnotatedElement element,
                                                @Nullable Class<? extends Annotation> annotationType,
                                                @Nullable String annotationName, Processor<T> processor) {
        return searchWithGetSemantics(element,(annotationType != null ? Collections.singleton(annotationType) :
                Collections.emptySet()),annotationName,null,processor);
    }

    @Nullable
    private static <T> T searchWithGetSemantics(AnnotatedElement element,
                                                Set<Class<? extends Annotation>> annotationTypes,
                                                @Nullable String annotationName,
                                                @Nullable Class<? extends Annotation> containerType,
                                                Processor<T> processor) {
        try {
            return searchWithGetSemantics(element,annotationTypes,annotationName,containerType,processor,
                    new HashSet<>(),0);
        } catch (Throwable ex) {
            AnnotationUtils.rethrowAnnotationConfigurationException(ex);
            throw new IllegalStateException("Failed to introspect annotations on " + element, ex);
        }
    }

    @Nullable
    private static <T> T searchWithGetSemantics(AnnotatedElement element,
                                                Set<Class<? extends Annotation>> annotationTypes,
                                                @Nullable String annotationName,
                                                @Nullable Class<? extends Annotation> containerType,
                                                Processor<T> processor, Set<AnnotatedElement> visited, int metaDepth) {
        if (visited.add(element)) {
            try {
                List<Annotation> declaredAnnotations = Arrays.asList(AnnotationUtils.getDeclaredAnnotations(element));
                T result = searchWithGetSemanticsInAnnotations(element, declaredAnnotations,
                        annotationTypes, annotationName, containerType, processor, visited, metaDepth);
                if (result != null) {
                    return result;
                }

                if (element instanceof Class) {
                    Class<?> superclass = ((Class<?>) element).getSuperclass();
                    if (superclass != null && superclass != Object.class) {
                        List<Annotation> inheritedAnnotations = new LinkedList<>();
                        for (Annotation annotation : element.getAnnotations()) {
                            if (!declaredAnnotations.contains(annotation)) {
                                inheritedAnnotations.add(annotation);
                            }
                        }
                        // Continue searching within inherited annotations
                        result = searchWithGetSemanticsInAnnotations(element, inheritedAnnotations,
                                annotationTypes, annotationName, containerType, processor, visited, metaDepth);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            } catch (Throwable ex) {
                AnnotationUtils.handleIntrospectionFailure(element,ex);
            }
        }
        return null;
    }

    private static <T> T searchWithGetSemanticsInAnnotations(@Nullable AnnotatedElement element,
                                                             List<Annotation> annotations,
                                                             Set<Class<? extends Annotation>> annotationTypes,
                                                             @Nullable String annotationName,@Nullable Class<?
            extends Annotation> containerType,Processor<T> processor,Set<AnnotatedElement> visited,int metaDepth) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> currentAnnotationType = annotation.annotationType();
            if (!AnnotationUtils.isInJavaLangAnnotationPackage(currentAnnotationType)) {
                if (annotationTypes.contains(currentAnnotationType) ||
                        currentAnnotationType.getName().equals(annotationName) ||
                        processor.alwaysProcesses()) {
                    T result = processor.process(element,annotation,metaDepth);
                    if (result != null) {
                        if (processor.aggregates() && metaDepth == 0) {
                            processor.getAggregatedResults().add(result);
                        } else {
                            return result;
                        }
                    }
                } else if (currentAnnotationType == containerType) {
                    for (Annotation contained : getRawAnnotationsFromContainer(element, annotation)) {
                        T result = processor.process(element, contained, metaDepth);
                        if (result != null) {
                            processor.getAggregatedResults().add(result);
                        }
                    }
                }
            }
        }

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> currentAnnotationType = annotation.annotationType();
            if (!AnnotationUtils.hasPlainJavaAnnotationsOnly(currentAnnotationType)) {
                T result = searchWithGetSemantics(currentAnnotationType, annotationTypes,
                        annotationName, containerType, processor, visited, metaDepth + 1);
                if (result != null) {
                    processor.postProcess(element, annotation, result);
                    if (processor.aggregates() && metaDepth == 0) {
                        processor.getAggregatedResults().add(result);
                    }
                    else {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A[] getRawAnnotationsFromContainer(@Nullable AnnotatedElement element,
                                                                             Annotation container) {
        try {
            A[] value = (A[]) AnnotationUtils.getValue(container);
            if (value != null) {
                return value;
            }
        } catch (Throwable ex) {
            AnnotationUtils.handleIntrospectionFailure(element,ex);
        }
        return (A[]) EMPTY_ANNOTATION_ARRAY;
    }

    private interface Processor<T> {

        @Nullable
        T process(@Nullable AnnotatedElement annotatedElement, Annotation annotation, int metaDepth);

        void postProcess(@Nullable AnnotatedElement annotatedElement, Annotation annotation, T result);

        boolean alwaysProcesses();

        boolean aggregates();

        List<T> getAggregatedResults();

    }

    private abstract static class SimpleAnnotationProcessor<T> implements Processor<T> {

        private final boolean alwaysProcesses;

        public SimpleAnnotationProcessor() {
            this(false);
        }

        public SimpleAnnotationProcessor(boolean alwaysProcesses) {
            this.alwaysProcesses = alwaysProcesses;
        }

        @Override
        public boolean alwaysProcesses() {
            return this.alwaysProcesses;
        }

        @Override
        public boolean aggregates() {
            return false;
        }

        @Override
        public void postProcess(AnnotatedElement annotatedElement, Annotation annotation, T result) {
            // no-op
        }

        @Override
        public List<T> getAggregatedResults() {
            throw new UnsupportedOperationException("SimpleAnnotationProcessor does not support aggregated results");
        }
    }

    static class AlwaysTrueBooleanAnnotationProcessor extends SimpleAnnotationProcessor<Boolean> {

        @Override
        public Boolean process(AnnotatedElement annotatedElement, Annotation annotation, int metaDepth) {
            return Boolean.TRUE;
        }
    }

    private static class MergedAnnotationAttributesProcessor implements Processor<AnnotationAttributes> {

        private final boolean classValuesAsString;

        private final boolean nestedAnnotationsAsMap;

        private final boolean aggregates;

        private final List<AnnotationAttributes> aggregatedResults;

        MergedAnnotationAttributesProcessor() {
            this(false, false, false);
        }

        MergedAnnotationAttributesProcessor(boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
            this(classValuesAsString, nestedAnnotationsAsMap, false);
        }

        MergedAnnotationAttributesProcessor(boolean classValuesAsString, boolean nestedAnnotationsAsMap,
                                            boolean aggregates) {

            this.classValuesAsString = classValuesAsString;
            this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
            this.aggregates = aggregates;
            this.aggregatedResults = (aggregates ? new ArrayList<>() : Collections.emptyList());
        }

        @Override
        @Nullable
        public AnnotationAttributes process(AnnotatedElement annotatedElement, Annotation annotation, int metaDepth) {
            return null;
        }

        @Override
        public void postProcess(AnnotatedElement annotatedElement, Annotation annotation, AnnotationAttributes result) {

        }

        @Override
        public boolean alwaysProcesses() {
            return false;
        }

        @Override
        public boolean aggregates() {
            return this.aggregates;
        }

        @Override
        public List<AnnotationAttributes> getAggregatedResults() {
            return null;
        }
    }

    //TODO

}
