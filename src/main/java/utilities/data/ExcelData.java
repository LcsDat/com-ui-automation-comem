package utilities.data;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@LabelAnnotation(name = "Excel data driven", value = "hello world")
public @interface ExcelData {
    String file() default  "TestCaseReference.xlsx";
    String sheet() ;
    String scenario() default "";
    Class<?> dataClass() default Object.class;
    String[] ignoreColumns() default {};
}
