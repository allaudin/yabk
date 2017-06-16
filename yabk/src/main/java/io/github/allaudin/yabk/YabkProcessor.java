package io.github.allaudin.yabk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import io.github.allaudin.yabk.model.ClassModel;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class YabkProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        // round completed
        if (roundEnvironment.processingOver()) {
            note("%s", "YABK round completed");
            return true;
        }


        for (Element e : roundEnvironment.getElementsAnnotatedWith(YabkProcess.class)) {

            if (!e.getModifiers().contains(Modifier.ABSTRACT)) {
                note("Skipping non-abstract class [%s]", e.getSimpleName());
                continue;
            }
            note("Processing %s", e.toString());

            List<? extends Element> enclosedElements = ((TypeElement) e).getEnclosedElements();

            ClassModel classModel = new ClassModel();

            for (Element ee : enclosedElements) {

                boolean isProtectedOrPrivate = ee.getModifiers().contains(Modifier.PROTECTED) || ee.getModifiers().contains(Modifier.PRIVATE);
                if (ee.getKind() == ElementKind.FIELD && isProtectedOrPrivate) {
                    note("%s", ee.getSimpleName());
                    String type = ee.asType().toString();
                    classModel.add(type, ee.getSimpleName().toString());
                } // end if
            }
            note("%s", classModel.toString());
        } // end for


        return true;
    } // process

    private void note(String format, Object... objects) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(format + " ...", objects));
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {
            {
                add("io.github.allaudin.yabk.YabkProcess");
            }
        };
    } // getSupportedAnnotationTypes

} // YabkProcessor
