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
import static org.chalup.thneed.tests.TestData.ID;
import static org.chalup.thneed.tests.TestData.LEAD;
import static org.chalup.thneed.tests.TestData.Models.TASK;
import static org.chalup.thneed.tests.TestData.TASKABLE_ID;
import static org.chalup.thneed.tests.TestData.TASKABLE_TYPE;
import static org.chalup.thneed.tests.TestData._ID;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;

import org.chalup.thneed.ModelGraph;
import org.chalup.thneed.PolymorphicRelationship;
import org.chalup.thneed.RelationshipVisitor;
import org.chalup.thneed.tests.TestData.ModelInterface;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PolymorphicTest {

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  RelationshipVisitor<ModelInterface> mockVisitor;

  @Captor
  ArgumentCaptor<PolymorphicRelationship<ModelInterface>> captor;

  @Test
  public void shouldVisitEveryRelationship() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph.of(ModelInterface.class)
        .identifiedByDefault().by(_ID)
        .where()
        .the(TASK).references(ImmutableList.of(CONTACT, DEAL, LEAD)).by(TASKABLE_TYPE, TASKABLE_ID)
        .build();

    graph.accept(mockVisitor);

    verify(mockVisitor).visit(captor.capture());
    PolymorphicRelationship<ModelInterface> relationship = captor.getValue();
    assertThat(relationship.mTypes).contains(LEAD, DEAL, CONTACT);
    assertThat(relationship.mModel).isEqualTo(TASK);
    assertThat(relationship.mTypeColumnName).isEqualTo(TASKABLE_TYPE);
    assertThat(relationship.mIdColumnName).isEqualTo(TASKABLE_ID);
    assertThat(relationship.mPolymorphicModelIdColumn).isEqualTo(_ID);
  }

  @Test
  public void shouldUseRelationshipSpecificIdColumn() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph.of(ModelInterface.class)
        .identifiedByDefault().by(_ID)
        .where()
        .the(TASK).references(ID).in(ImmutableList.of(CONTACT, DEAL, LEAD)).by(TASKABLE_TYPE, TASKABLE_ID)
        .build();

    graph.accept(mockVisitor);

    verify(mockVisitor).visit(captor.capture());
    PolymorphicRelationship<ModelInterface> relationship = captor.getValue();
    assertThat(relationship.mPolymorphicModelIdColumn).isEqualTo(ID);
  }
}