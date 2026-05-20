package com.mypay.collection.config;

import com.mypay.common.constant.CollectionRole;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireCollectionRole {
    CollectionRole[] value() default {CollectionRole.MEMBER};
}
