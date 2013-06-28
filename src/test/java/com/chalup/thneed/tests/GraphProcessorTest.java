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
import static com.chalup.thneed.tests.TestData.CUSTOM_FIELD_ID;
import static com.chalup.thneed.tests.TestData.DEAL;
import static com.chalup.thneed.tests.TestData.LEAD;
import static com.chalup.thneed.tests.TestData.LEAD_ID;
import static com.chalup.thneed.tests.TestData.Models.CONTACT_DATA;
import static com.chalup.thneed.tests.TestData.Models.CUSTOM_FIELD;
import static com.chalup.thneed.tests.TestData.Models.CUSTOM_FIELD_VALUE;
import static com.chalup.thneed.tests.TestData.Models.TAG;
import static com.chalup.thneed.tests.TestData.Models.TAGGING;
import static com.chalup.thneed.tests.TestData.Models.TASK;
import static com.chalup.thneed.tests.TestData.SUBJECT_ID;
import static com.chalup.thneed.tests.TestData.TAGGABLE_ID;
import static com.chalup.thneed.tests.TestData.TAGGABLE_TYPE;
import static com.chalup.thneed.tests.TestData.TAG_ID;
import static com.chalup.thneed.tests.TestData.TASKABLE_ID;
import static com.chalup.thneed.tests.TestData.TASKABLE_TYPE;
import static org.mockito.Mockito.*;

import com.chalup.thneed.ModelGraph;
import com.chalup.thneed.ModelVisitor;
import com.chalup.thneed.tests.TestData.ModelInterface;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GraphProcessorTest {

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  ModelVisitor<ModelInterface> mockProcessor;

  @Test
  public void shouldVisitEveryExplicitlyAddedModel() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .with(CONTACT)
        .with(DEAL)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(CONTACT);
    verify(mockProcessor).visit(DEAL);
  }

  @Test
  public void shouldVisitEveryModelFromOneToOneRelationships() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .where()
        .the(CONTACT_DATA).isPartOf(LEAD).identified().by(LEAD_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(CONTACT_DATA);
    verify(mockProcessor).visit(LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromOneToManyRelationships() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .where()
        .the(DEAL).references(CONTACT).by(CONTACT_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(CONTACT);
    verify(mockProcessor).visit(DEAL);
  }

  @Test
  public void shouldVisitEveryModelFromRecursiveRelationships() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .where()
        .the(CONTACT).groupsOther().by(CONTACT_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(CONTACT);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationships() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .where()
        .the(CUSTOM_FIELD_VALUE)
        .links(CONTACT).by(SUBJECT_ID)
        .with(CUSTOM_FIELD).by(CUSTOM_FIELD_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(CONTACT);
    verify(mockProcessor).visit(CUSTOM_FIELD_VALUE);
    verify(mockProcessor).visit(CUSTOM_FIELD);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationshipsWithFirstSidePolymorphic() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .where()
        .the(TAGGING)
        .links(TAG).by(TAG_ID)
        .with(ImmutableList.of(CONTACT, DEAL, LEAD)).by(TAGGABLE_TYPE, TAGGABLE_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TAG);
    verify(mockProcessor).visit(TAGGING);
    verify(mockProcessor).visit(CONTACT);
    verify(mockProcessor).visit(DEAL);
    verify(mockProcessor).visit(LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationshipsWithSecondSidePolymorphic() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .where()
        .the(TAGGING)
        .links(ImmutableList.of(CONTACT, DEAL, LEAD)).by(TAGGABLE_TYPE, TAGGABLE_ID)
        .with(TAG).by(TAG_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TAG);
    verify(mockProcessor).visit(TAGGING);
    verify(mockProcessor).visit(CONTACT);
    verify(mockProcessor).visit(DEAL);
    verify(mockProcessor).visit(LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationshipsWithBothSidesPolymorphic() throws Exception {
    ModelGraph<ModelInterface> graph = ModelGraph
        .of(ModelInterface.class)
        .where()
        .the(TAGGING)
        .links(ImmutableList.of(CONTACT, DEAL)).by(TAGGABLE_TYPE, TAGGABLE_ID)
        .with(ImmutableList.of(CONTACT, LEAD)).by("LOL", "WUT?")
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TAGGING);
    verify(mockProcessor).visit(CONTACT);
    verify(mockProcessor).visit(DEAL);
    verify(mockProcessor).visit(LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromPolymorphicRelationships() throws Exception {

    ModelGraph<ModelInterface> graph = ModelGraph.of(ModelInterface.class)
        .where()
        .the(TASK)
        .references(ImmutableList.of(CONTACT, DEAL, LEAD))
        .by(TASKABLE_TYPE, TASKABLE_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TASK);
    verify(mockProcessor).visit(CONTACT);
    verify(mockProcessor).visit(DEAL);
    verify(mockProcessor).visit(LEAD);
  }
}
