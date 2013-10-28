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

public class OneToOneRelationship<TModel> implements Relationship<TModel> {

  public final TModel mModel;
  public final TModel mLinkedModel;
  public final String mParentModelIdColumn;
  public final String mLinkedByColumn;

  OneToOneRelationship(TModel model, TModel linkedModel, String parentModelIdColumn, String linkedByColumn) {
    mModel = model;
    mLinkedModel = linkedModel;
    mParentModelIdColumn = parentModelIdColumn;
    mLinkedByColumn = linkedByColumn;
  }

  @Override
  public void accept(RelationshipVisitor<? super TModel> visitor) {
    visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mModel, mLinkedModel, mParentModelIdColumn, mLinkedByColumn);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final OneToOneRelationship other = (OneToOneRelationship) o;
    return Objects.equal(this.mModel, other.mModel) &&
        Objects.equal(this.mLinkedModel, other.mLinkedModel) &&
        Objects.equal(this.mParentModelIdColumn, other.mParentModelIdColumn) &&
        Objects.equal(this.mLinkedByColumn, other.mLinkedByColumn);
  }
}
