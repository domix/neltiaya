/**
 *
 * Copyright (C) 2014-2020 the original author or authors.
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
package com.domingosuarez.validation.constraints;

import lombok.Getter;

import javax.validation.constraints.NotNull;

/**
 * Created by domix on 27/07/15.
 */
@Constrained
public class Dog {
  @Getter
  @NotNull
  private String name;

  @Constraint(property = "name")
  Boolean nameIsNotFirulais() {
    return !"firulais".equalsIgnoreCase(name);
  }
}
