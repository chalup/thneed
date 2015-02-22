/*
 * Copyright (C) 2013 Jerzy Chalupski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chalup.thneed.tests;

import static org.chalup.thneed.tests.TestData.CONTACT;
import static org.chalup.thneed.tests.TestData.DEAL;
import static org.chalup.thneed.tests.TestData.Models.TASK;
import static org.chalup.thneed.tests.TestData._ID;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;

import org.chalup.thneed.ModelGraph;
import org.chalup.thneed.ModelVisitor;
import org.chalup.thneed.Thneeds;
import org.chalup.thneed.tests.TestData.ModelInterface;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SpecialModelCasesTest {

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  ModelVisitor<ModelInterface> defaultVisitor;

  @Mock
  ModelVisitor<ModelInterface> specialCaseVisitor;

  @Test
  public void shouldProcessSpecialCaseForModelVisitors() throws Exception {
    ModelGraph<ModelInterface> fullModelGraph = ModelGraph.of(ModelInterface.class)
        .identifiedByDefault().by(_ID)
        .with(TASK)
        .with(CONTACT)
        .build();

    Thneeds
        .with(fullModelGraph, defaultVisitor)
        .plus(Lists.newArrayList(TASK), specialCaseVisitor)
        .process();

    verify(defaultVisitor).visit(CONTACT);
    verify(defaultVisitor, never()).visit(TASK);
    verify(specialCaseVisitor).visit(TASK);
  }

  @Test
  public void shouldAllowExcludingModelsFromVisiting() throws Exception {
    ModelGraph<ModelInterface> fullModelGraph = ModelGraph.of(ModelInterface.class)
        .identifiedByDefault().by(_ID)
        .with(TASK)
        .with(CONTACT)
        .build();

    Thneeds
        .with(fullModelGraph, defaultVisitor)
        .exclude(Lists.newArrayList(TASK))
        .process();

    verify(defaultVisitor).visit(CONTACT);
    verify(defaultVisitor, never()).visit(TASK);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowSpecifyingSpecialCaseForOutsideOfMainGraph() throws Exception {
    ModelGraph<ModelInterface> fullModelGraph = ModelGraph.of(ModelInterface.class)
        .identifiedByDefault().by(_ID)
        .with(TASK)
        .with(CONTACT)
        .build();

    Thneeds
        .with(fullModelGraph, defaultVisitor)
        .plus(Lists.newArrayList(DEAL), specialCaseVisitor);
  }
}