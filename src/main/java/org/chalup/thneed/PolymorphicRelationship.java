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

package org.chalup.thneed;

import com.google.common.collect.ImmutableList;

public class PolymorphicRelationship<TModel> implements Relationship<TModel> {

  public final TModel mModel;
  public final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> mTypes;
  public final String mPolymorphicModelIdColumn;
  public final String mTypeColumnName;
  public final String mIdColumnName;

  PolymorphicRelationship(TModel model, ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> types, String polymorphicModelIdColumn, String typeColumnName, String idColumnName) {
    mModel = model;
    mTypes = types;
    mPolymorphicModelIdColumn = polymorphicModelIdColumn;
    mTypeColumnName = typeColumnName;
    mIdColumnName = idColumnName;
  }

  @Override
  public void accept(RelationshipVisitor<? super TModel> visitor) {
    visitor.visit(this);
  }

  public ImmutableList<TModel> getPolymorphicModels() {
    ImmutableList.Builder<TModel> builder = ImmutableList.builder();

    ImmutableList<? extends PolymorphicType<? extends TModel, ? extends TModel>> types = mTypes;
    for (PolymorphicType<? extends TModel, ? extends TModel> type : types) {
      builder.add(type.getModel());
    }

    return builder.build();
  }
}
