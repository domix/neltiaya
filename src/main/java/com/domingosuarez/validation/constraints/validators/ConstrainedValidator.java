/**
 *
 * Copyright (C) 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.domingosuarez.validation.constraints.validators;

import com.domingosuarez.validation.constraints.Constrained;
import com.domingosuarez.validation.constraints.Constraint;
import lombok.AllArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;
import static org.springframework.util.ReflectionUtils.*;

/**
 * Created by domix on 25/07/15.
 */
public class ConstrainedValidator implements ConstraintValidator<Constrained, Object> {

  @Override
  public void initialize(Constrained constraint) {

  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    List<Field> fields = new ArrayList<>();

    doWithFields(value.getClass(), field -> {
      makeAccessible(field);
      fields.add(field);
    }, field -> isPredicate.and(hasConstraintAnnotation).test(field));

    @SuppressWarnings("unchecked")
    List<ContraintViolation> violations = fields.stream()
      .map(field -> constraintInformation.apply(field, value))
      .filter(constraintInformation -> !constraintInformation.predicate.test(value))
      .map(constraintInformation -> constraintViolation.apply(constraintInformation))
      .collect(toList());

    boolean isValid = violations.isEmpty();

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      violations.forEach(constraintViolation ->
        context.buildConstraintViolationWithTemplate(constraintViolation.message)
          .addPropertyNode(constraintViolation.property).addConstraintViolation());
    }

    return isValid;
  }

  Function<ConstraintInformation, ContraintViolation> constraintViolation = constraintInformation ->
    new ContraintViolation(constraintInformation.constraint.message(), constraintInformation.constraint.property());

  BiFunction<Field, Object, Predicate> extractPredicate = (field, value) -> (Predicate) getField(field, value);

  Predicate<Field> isPredicate = field -> field.getType().isAssignableFrom(Predicate.class);

  Predicate<Field> hasConstraintAnnotation = field -> getAnnotation(field, Constraint.class) != null;

  BiFunction<Field, Object, ConstraintInformation> constraintInformation = (field, value) ->
    new ConstraintInformation(getAnnotation(field, Constraint.class), extractPredicate.apply(field, value), value);

  @AllArgsConstructor
  static class ConstraintInformation {
    Constraint constraint;
    Predicate predicate;
    Object entity;
  }

  @AllArgsConstructor
  static class ContraintViolation {
    String message;
    String property;
  }

}
