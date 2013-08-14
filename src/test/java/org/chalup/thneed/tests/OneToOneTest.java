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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.chalup.thneed.ModelGraph;
import org.chalup.thneed.OneToOneRelationship;
import org.chalup.thneed.RelationshipVisitor;
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
public class OneToOneTest {

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  RelationshipVisitor<TestData.ModelInterface> mockVisitor;

  @Captor
  ArgumentCaptor<OneToOneRelationship<TestData.ModelInterface>> captor;

  @Test
  public void shouldVisitEveryRelationship() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph.of(TestData.ModelInterface.class)
        .where()
        .the(TestData.Models.CONTACT_DATA).isPartOf(TestData.LEAD).identified().by(TestData.LEAD_ID)
        .build();

    graph.accept(mockVisitor);

    verify(mockVisitor).visit(captor.capture());
    OneToOneRelationship<TestData.ModelInterface> relationship = captor.getValue();
    assertThat(relationship.mModel).isEqualTo(TestData.Models.CONTACT_DATA);
    assertThat(relationship.mParentModel).isEqualTo(TestData.LEAD);
    assertThat(relationship.mLinkedByColumn).isEqualTo(TestData.LEAD_ID);
  }
}