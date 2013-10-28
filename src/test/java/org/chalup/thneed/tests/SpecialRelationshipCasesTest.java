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
import static org.chalup.thneed.tests.TestData.CONTACT_ID;
import static org.chalup.thneed.tests.TestData.DEAL;
import static org.chalup.thneed.tests.TestData.Models.TAG;
import static org.chalup.thneed.tests.TestData.Models.TAGGING;
import static org.chalup.thneed.tests.TestData.TAG_ID;
import static org.chalup.thneed.tests.TestData._ID;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.chalup.thneed.ModelGraph;
import org.chalup.thneed.OneToManyRelationship;
import org.chalup.thneed.RelationshipVisitor;
import org.chalup.thneed.Thneeds;
import org.chalup.thneed.tests.TestData.ModelInterface;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SpecialRelationshipCasesTest {

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  RelationshipVisitor<ModelInterface> defaultVisitor;

  @Mock
  RelationshipVisitor<ModelInterface> specialCaseVisitor;

  @Captor
  ArgumentCaptor<OneToManyRelationship<ModelInterface>> defaultCasesCaptor;

  @Captor
  ArgumentCaptor<OneToManyRelationship<ModelInterface>> specialCasesCaptor;

  @Test
  public void shouldProcessSpecialCaseForModelVisitors() throws Exception {
    ModelGraph<ModelInterface> fullModelGraph = ModelGraph.of(ModelInterface.class)
        .identifiedByDefault().by(_ID)
        .where()
        .the(DEAL).references(CONTACT).by(CONTACT_ID)
        .the(TAGGING).references(TAG).by(TAG_ID)
        .build();

    ModelGraph<ModelInterface> specialCasesGraph = ModelGraph.of(ModelInterface.class)
        .identifiedByDefault().by(_ID)
        .where()
        .the(DEAL).references(CONTACT).by(CONTACT_ID)
        .build();

    Thneeds
        .with(fullModelGraph, defaultVisitor)
        .plus(specialCasesGraph, specialCaseVisitor)
        .process();

    verify(defaultVisitor).visit(defaultCasesCaptor.capture());
    assertThat(defaultCasesCaptor.getAllValues()).hasSize(1);
    OneToManyRelationship<ModelInterface> relationshipHandledInDefaultWay = defaultCasesCaptor.getValue();
    assertThat(relationshipHandledInDefaultWay.mModel).isEqualTo(TAGGING);
    assertThat(relationshipHandledInDefaultWay.mReferencedModel).isEqualTo(TAG);

    verify(specialCaseVisitor).visit(specialCasesCaptor.capture());
    assertThat(specialCasesCaptor.getAllValues()).hasSize(1);
    OneToManyRelationship<ModelInterface> relationshipHandledInSpecialWay = specialCasesCaptor.getValue();
    assertThat(relationshipHandledInSpecialWay.mModel).isEqualTo(DEAL);
    assertThat(relationshipHandledInSpecialWay.mReferencedModel).isEqualTo(CONTACT);
  }
}