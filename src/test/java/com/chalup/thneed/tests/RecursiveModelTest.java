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

package com.chalup.thneed.tests;

import static com.chalup.thneed.tests.TestData.CONTACT;
import static com.chalup.thneed.tests.TestData.CONTACT_ID;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.chalup.thneed.ModelGraph;
import com.chalup.thneed.RecursiveModelRelationship;
import com.chalup.thneed.RelationshipVisitor;
import com.chalup.thneed.tests.TestData.ModelInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RecursiveModelTest {

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  RelationshipVisitor<ModelInterface> mockVisitor;

  @Captor
  ArgumentCaptor<RecursiveModelRelationship<ModelInterface>> captor;

  @Test
  public void shouldVisitEveryRelationship() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph.of(ModelInterface.class)
        .where()
        .the(CONTACT).groupsOther().by(CONTACT_ID)
        .build();

    graph.accept(mockVisitor);

    verify(mockVisitor).visit(captor.capture());
    RecursiveModelRelationship<ModelInterface> relationship = captor.getValue();
    assertThat(relationship.mModel).isEqualTo(CONTACT);
    assertThat(relationship.mGroupByColumn).isEqualTo(CONTACT_ID);
  }
}