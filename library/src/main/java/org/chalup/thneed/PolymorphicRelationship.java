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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class PolymorphicRelationship<TModel> implements Relationship<TModel> {

  public final TModel mModel;
  public final ImmutableMap<String, TModel> mPolymorphicModels;
  public final String mPolymorphicModelIdColumn;
  public final String mTypeColumnName;
  public final String mIdColumnName;

  PolymorphicRelationship(TModel model, ImmutableList<? extends PolymorphicType<? extends TModel>> types, String polymorphicModelIdColumn, String typeColumnName, String idColumnName) {
    mModel = model;
    mPolymorphicModelIdColumn = polymorphicModelIdColumn;
    mTypeColumnName = typeColumnName;
    mIdColumnName = idColumnName;

    ImmutableMap.Builder<String, TModel> builder = ImmutableMap.builder();
    for (PolymorphicType<? extends TModel> type : types) {
      builder.put(type.getModelName(), (TModel) type.self());
    }
    mPolymorphicModels = builder.build();
  }

  @Override
  public void accept(RelationshipVisitor<? super TModel> visitor) {
    visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mModel, mPolymorphicModels, mPolymorphicModelIdColumn, mTypeColumnName, mIdColumnName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PolymorphicRelationship other = (PolymorphicRelationship) o;
    return Objects.equal(this.mModel, other.mModel) &&
        Objects.equal(this.mPolymorphicModels, other.mPolymorphicModels) &&
        Objects.equal(this.mPolymorphicModelIdColumn, other.mPolymorphicModelIdColumn) &&
        Objects.equal(this.mTypeColumnName, other.mTypeColumnName) &&
        Objects.equal(this.mIdColumnName, other.mIdColumnName);
  }
}
