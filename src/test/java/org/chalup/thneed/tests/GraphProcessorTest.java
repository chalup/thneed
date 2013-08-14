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

import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;

import org.chalup.thneed.ModelGraph;
import org.chalup.thneed.ModelVisitor;
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
  ModelVisitor<TestData.ModelInterface> mockProcessor;

  @Test
  public void shouldVisitEveryExplicitlyAddedModel() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .with(TestData.CONTACT)
        .with(TestData.DEAL)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.CONTACT);
    verify(mockProcessor).visit(TestData.DEAL);
  }

  @Test
  public void shouldVisitEveryModelFromOneToOneRelationships() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .where()
        .the(TestData.Models.CONTACT_DATA).isPartOf(TestData.LEAD).identified().by(TestData.LEAD_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.Models.CONTACT_DATA);
    verify(mockProcessor).visit(TestData.LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromOneToManyRelationships() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .where()
        .the(TestData.DEAL).references(TestData.CONTACT).by(TestData.CONTACT_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.CONTACT);
    verify(mockProcessor).visit(TestData.DEAL);
  }

  @Test
  public void shouldVisitEveryModelFromRecursiveRelationships() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .where()
        .the(TestData.CONTACT).groupsOther().by(TestData.CONTACT_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.CONTACT);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationships() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .where()
        .the(TestData.Models.CUSTOM_FIELD_VALUE)
        .links(TestData.CONTACT).by(TestData.SUBJECT_ID)
        .with(TestData.Models.CUSTOM_FIELD).by(TestData.CUSTOM_FIELD_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.CONTACT);
    verify(mockProcessor).visit(TestData.Models.CUSTOM_FIELD_VALUE);
    verify(mockProcessor).visit(TestData.Models.CUSTOM_FIELD);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationshipsWithFirstSidePolymorphic() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .where()
        .the(TestData.Models.TAGGING)
        .links(TestData.Models.TAG).by(TestData.TAG_ID)
        .with(ImmutableList.of(TestData.CONTACT, TestData.DEAL, TestData.LEAD)).by(TestData.TAGGABLE_TYPE, TestData.TAGGABLE_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.Models.TAG);
    verify(mockProcessor).visit(TestData.Models.TAGGING);
    verify(mockProcessor).visit(TestData.CONTACT);
    verify(mockProcessor).visit(TestData.DEAL);
    verify(mockProcessor).visit(TestData.LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationshipsWithSecondSidePolymorphic() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .where()
        .the(TestData.Models.TAGGING)
        .links(ImmutableList.of(TestData.CONTACT, TestData.DEAL, TestData.LEAD)).by(TestData.TAGGABLE_TYPE, TestData.TAGGABLE_ID)
        .with(TestData.Models.TAG).by(TestData.TAG_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.Models.TAG);
    verify(mockProcessor).visit(TestData.Models.TAGGING);
    verify(mockProcessor).visit(TestData.CONTACT);
    verify(mockProcessor).visit(TestData.DEAL);
    verify(mockProcessor).visit(TestData.LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromManyToManyRelationshipsWithBothSidesPolymorphic() throws Exception {
    ModelGraph<TestData.ModelInterface> graph = ModelGraph
        .of(TestData.ModelInterface.class)
        .where()
        .the(TestData.Models.TAGGING)
        .links(ImmutableList.of(TestData.CONTACT, TestData.DEAL)).by(TestData.TAGGABLE_TYPE, TestData.TAGGABLE_ID)
        .with(ImmutableList.of(TestData.CONTACT, TestData.LEAD)).by("LOL", "WUT?")
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.Models.TAGGING);
    verify(mockProcessor).visit(TestData.CONTACT);
    verify(mockProcessor).visit(TestData.DEAL);
    verify(mockProcessor).visit(TestData.LEAD);
  }

  @Test
  public void shouldVisitEveryModelFromPolymorphicRelationships() throws Exception {

    ModelGraph<TestData.ModelInterface> graph = ModelGraph.of(TestData.ModelInterface.class)
        .where()
        .the(TestData.Models.TASK)
        .references(ImmutableList.of(TestData.CONTACT, TestData.DEAL, TestData.LEAD))
        .by(TestData.TASKABLE_TYPE, TestData.TASKABLE_ID)
        .build();

    graph.accept(mockProcessor);

    verify(mockProcessor).visit(TestData.Models.TASK);
    verify(mockProcessor).visit(TestData.CONTACT);
    verify(mockProcessor).visit(TestData.DEAL);
    verify(mockProcessor).visit(TestData.LEAD);
  }
}
