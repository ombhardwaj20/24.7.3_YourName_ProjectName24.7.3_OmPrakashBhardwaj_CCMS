package com.card.Credit_card.annotaion;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "int", paramType = "query", defaultValue = "0", value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "int", paramType = "query", defaultValue = "10", value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). "
                + "Default sort order is ascending. " + "Multiple sort criteria are supported.")})
public @interface ApiPageable {
}
