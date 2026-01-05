package pl.maxim.blog.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    @Test
    void controllers_should_not_depend_on_entities() {
        JavaClasses classes = new ClassFileImporter().importPackages("pl.maxim.blog");

        noClasses()
                .that().areAnnotatedWith(RestController.class)
                .or().areAnnotatedWith(Controller.class)
                .should().dependOnClassesThat().areAnnotatedWith(Entity.class)
                .check(classes);
    }

    @Test
    void services_should_not_depend_on_web_layer() {
        JavaClasses classes = new ClassFileImporter().importPackages("pl.maxim.blog");

        noClasses()
                .that().areAnnotatedWith(org.springframework.stereotype.Service.class)
                .should().dependOnClassesThat().areAnnotatedWith(RestController.class)
                .check(classes);

        noClasses()
                .that().areAnnotatedWith(org.springframework.stereotype.Service.class)
                .should().dependOnClassesThat().areAnnotatedWith(Controller.class)
                .check(classes);
    }
}
