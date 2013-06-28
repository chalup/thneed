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

package com.chalup.thneed;

public class ManyToManyRelationship<TModel> implements Relationship<TModel> {

  public final TModel mModel;
  public final Relationship<TModel> mLeftRelationship;
  public final Relationship<TModel> mRightRelationship;

  ManyToManyRelationship(TModel model, Relationship<TModel> leftRelationship, Relationship<TModel> rightRelationship) {
    mModel = model;
    mLeftRelationship = leftRelationship;
    mRightRelationship = rightRelationship;
  }

  @Override
  public void accept(RelationshipVisitor<? super TModel> visitor) {
    visitor.visit(this);
  }
}
