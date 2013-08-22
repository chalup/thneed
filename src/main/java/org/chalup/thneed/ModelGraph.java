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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import android.provider.BaseColumns;

import java.util.Collection;
import java.util.Set;

public class ModelGraph<TModel> {
  private final Collection<Relationship<? extends TModel>> mRelationships;
  private final Collection<TModel> mModels;

  private ModelGraph(Collection<TModel> models, Collection<Relationship<? extends TModel>> relationships) {
    mModels = models;
    mRelationships = relationships;
  }

  public void accept(RelationshipVisitor<? super TModel> visitor) {
    for (Relationship<? extends TModel> relationship : mRelationships) {
      relationship.accept(visitor);
    }
  }

  public void accept(ModelVisitor<? super TModel> visitor) {
    for (TModel model : mModels) {
      visitor.visit(model);
    }
  }

  public static <TModel> BuilderWithDefaultColumns<TModel> of(Class<TModel> klass) {
    return new BuilderWithDefaultColumns<TModel>();
  }

  public static class Builder<TModel> {
    private Builder() {
    }

    public interface ColumnSelector<TReturnType> {
      TReturnType by(String columnName);
    }

    public interface PolymorphicColumnSelector<TReturnType> {
      TReturnType by(String typeColumnName, String typeColumnId);
    }

    protected String mDefaultIdColumn = BaseColumns._ID;
    private final Set<TModel> mModels = Sets.newHashSet();
    private final Collection<Relationship<? extends TModel>> mRelationships = Lists.newArrayList();

    private final RelationshipVisitor<TModel> mRelationshipVisitor = new RelationshipVisitor<TModel>() {
      @Override
      public void visit(OneToManyRelationship<? extends TModel> relationship) {
        mRelationships.add(relationship);
        mModels.add(relationship.mModel);

        mModels.add(relationship.mReferencedModel);
      }

      @Override
      public void visit(OneToOneRelationship<? extends TModel> relationship) {
        mRelationships.add(relationship);
        mModels.add(relationship.mModel);

        mModels.add(relationship.mParentModel);
      }

      @Override
      public void visit(RecursiveModelRelationship<? extends TModel> relationship) {
        mRelationships.add(relationship);
        mModels.add(relationship.mModel);
      }

      @Override
      public void visit(ManyToManyRelationship<? extends TModel> relationship) {
        mRelationships.add(relationship);
        mModels.add(relationship.mModel);

        relationship.mRightRelationship.accept(this);
        relationship.mLeftRelationship.accept(this);
      }

      @Override
      public void visit(PolymorphicRelationship<? extends TModel> relationship) {
        mRelationships.add(relationship);
        mModels.add(relationship.mModel);

        mModels.addAll(relationship.getPolymorphicModels());
      }
    };

    public ModelGraph<TModel> build() {
      return new ModelGraph<TModel>(mModels, mRelationships);
    }

    public Builder<TModel> with(TModel model) {
      mModels.add(model);

      return this;
    }

    public RelationshipAdder where() {
      return new RelationshipAdder();
    }

    public class RelationshipAdder {
      public RelationshipBuilder the(TModel model) {
        return new RelationshipBuilder(model);
      }

      public ModelGraph<TModel> build() {
        return Builder.this.build();
      }

      public class RelationshipBuilder {
        private final TModel mModel;

        private RelationshipBuilder(TModel model) {
          mModel = model;
        }

        public OneToOneRelationshipBuilder isPartOf(TModel parentModel) {
          return new OneToOneRelationshipBuilder(parentModel, mDefaultIdColumn);
        }

        public ColumnSelector<RelationshipAdder> groupsOther() {
          return new ColumnSelector<RelationshipAdder>() {
            @Override
            public RelationshipAdder by(String columnName) {
              new RecursiveModelRelationship<TModel>(mModel, mDefaultIdColumn, columnName).accept(mRelationshipVisitor);

              return RelationshipAdder.this;
            }
          };
        }

        public ColumnSelector<RelationshipAdder> references(final TModel model) {
          return new ColumnSelector<RelationshipAdder>() {
            @Override
            public RelationshipAdder by(String columnName) {
              new OneToManyRelationship<TModel>(mModel, model, mDefaultIdColumn, columnName).accept(mRelationshipVisitor);

              return RelationshipAdder.this;
            }
          };
        }

        public PolymorphicColumnSelector<RelationshipAdder> references(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
          return new PolymorphicColumnSelector<RelationshipAdder>() {
            @Override
            public RelationshipAdder by(String typeColumnName, String idColumnName) {
              new PolymorphicRelationship<TModel>(mModel, models, mDefaultIdColumn, typeColumnName, idColumnName).accept(mRelationshipVisitor);

              return RelationshipAdder.this;
            }
          };
        }

        public ColumnSelector<ManyToManyRelationshipBuilder> links(final TModel model) {
          return new ColumnSelector<ManyToManyRelationshipBuilder>() {

            @Override
            public ManyToManyRelationshipBuilder by(String columnName) {
              return new ManyToManyRelationshipBuilder(new OneToManyRelationship<TModel>(mModel, model, mDefaultIdColumn, columnName));
            }
          };
        }

        public PolymorphicColumnSelector<ManyToManyRelationshipBuilder> links(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
          return new PolymorphicColumnSelector<ManyToManyRelationshipBuilder>() {

            @Override
            public ManyToManyRelationshipBuilder by(String typeColumnName, String idColumnName) {
              return new ManyToManyRelationshipBuilder(new PolymorphicRelationship<TModel>(mModel, models, mDefaultIdColumn, typeColumnName, idColumnName));
            }
          };
        }

        public class ManyToManyRelationshipBuilder {
          private final Relationship<TModel> mLeftRelationship;

          private ManyToManyRelationshipBuilder(Relationship<TModel> leftRelationship) {
            mLeftRelationship = leftRelationship;
          }

          public ColumnSelector<RelationshipAdder> with(final TModel model) {
            return new ColumnSelector<RelationshipAdder>() {

              @Override
              public RelationshipAdder by(String columnName) {
                Relationship<TModel> rightRelationship = new OneToManyRelationship<TModel>(mModel, model, mDefaultIdColumn, columnName);

                new ManyToManyRelationship<TModel>(mModel, mLeftRelationship, rightRelationship).accept(mRelationshipVisitor);

                return RelationshipAdder.this;
              }
            };
          }

          public PolymorphicColumnSelector<RelationshipAdder> with(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
            return new PolymorphicColumnSelector<RelationshipAdder>() {

              @Override
              public RelationshipAdder by(String typeColumnName, String idColumnName) {
                Relationship<TModel> rightRelationship = new PolymorphicRelationship<TModel>(mModel, models, mDefaultIdColumn, typeColumnName, idColumnName);

                new ManyToManyRelationship<TModel>(mModel, mLeftRelationship, rightRelationship).accept(mRelationshipVisitor);

                return RelationshipAdder.this;
              }
            };
          }
        }

        public class OneToOneRelationshipBuilder {
          private final TModel mParentModel;
          private final String mParentModelIdColumn;

          private OneToOneRelationshipBuilder(TModel parentModel, String parentModelIdColumn) {
            mParentModel = parentModel;
            mParentModelIdColumn = parentModelIdColumn;
          }

          public ColumnSelector<RelationshipAdder> identified() {
            return new ColumnSelector<RelationshipAdder>() {
              @Override
              public RelationshipAdder by(String columnName) {
                new OneToOneRelationship<TModel>(mModel, mParentModel, mParentModelIdColumn, columnName).accept(mRelationshipVisitor);

                return RelationshipAdder.this;
              }
            };
          }
        }
      }
    }
  }

  public static class BuilderWithDefaultColumns<TModel> extends Builder<TModel> {
    public ColumnSelector<Builder<TModel>> identifiedByDefault() {
      return new ColumnSelector<Builder<TModel>>() {
        @Override
        public Builder<TModel> by(String columnName) {
          mDefaultIdColumn = columnName;

          return BuilderWithDefaultColumns.this;
        }
      };
    }
  }
}