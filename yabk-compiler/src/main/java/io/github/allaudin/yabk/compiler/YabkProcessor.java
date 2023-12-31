package io.github.allaudin.yabk.compiler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import io.github.allaudin.yabk.YabkProcess;
import io.github.allaudin.yabk.YabkSkip;
import io.github.allaudin.yabk.compiler.generator.ClassGenerator;
import io.github.allaudin.yabk.compiler.generator.FieldGenerator;
import io.github.allaudin.yabk.compiler.model.ClassModel;
import io.github.allaudin.yabk.compiler.model.FieldModel;
import io.github.allaudin.yabk.compiler.processor.ClassProcessor;
import io.github.allaudin.yabk.compiler.processor.FieldProcessor;

public class YabkProcessor extends AbstractProcessor {


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        YabkLogger.init(processingEnv.getMessager());

        // round completed
        if (roundEnvironment.processingOver()) {
            YabkLogger.note("%s", "YABK round completed [Everything is OK]");
            return true;
        }


        for (Element e : roundEnvironment.getElementsAnnotatedWith(YabkProcess.class)) {


            boolean shouldSkip = !e.getModifiers().contains(Modifier.ABSTRACT) || !e.getKind().isClass();

            if (shouldSkip) {
                YabkLogger.note("Skipping %s  [%s]", e.getKind(), e.getSimpleName());
                continue;
            }

            TypeElement type = (TypeElement) e;

            YabkLogger.note("Processing %s", e.toString());

            ClassModel classModel = ClassProcessor.newInstance(type).process();
            FieldGenerator fieldGenerator = FieldGenerator.getInstance();

            ClassGenerator classGenerator = new ClassGenerator(classModel, fieldGenerator);

            List<? extends Element> enclosedElements = type.getEnclosedElements();


            for (Element ee : enclosedElements) {

                if (shouldBeAdded(ee)) {
                    FieldModel fieldModel = FieldProcessor.newInstance(ee, processingEnv).process();
                    classGenerator.add(fieldModel);
                }

            } // end for enclosed elements

            try {
                classGenerator.getFile().writeTo(processingEnv.getFiler());
            } catch (Exception ioe) {
                ioe.printStackTrace();
                YabkLogger.error(e, "%s", "Error while writing file");
            }

        } // end for


        return true;
    } // process

    /**
     * Either this element should be added to generated class or not
     *
     * @param element this element
     * @return true - if should be added, false otherwise
     */
    private boolean shouldBeAdded(Element element) {
        boolean isNotSkipped = element.getAnnotation(YabkSkip.class) == null;
        boolean isProtected = element.getModifiers().contains(Modifier.PROTECTED) || element.getModifiers().isEmpty();
        boolean isField = element.getKind() == ElementKind.FIELD;
        return isNotSkipped && isField && isProtected;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    } // getSupportedSourceVersion


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(YabkProcess.class.getCanonicalName());
        return annotations;
    } // getSupportedAnnotationTypes

} // YabkProcessor
