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

  public static <TModel> DefaultIdColumnSelector<TModel> of(Class<TModel> klass) {
    return new DefaultIdColumnSelector<TModel>();
  }

  public interface ColumnSelector<TReturnType> {
    TReturnType by(String columnName);
  }

  public static class Builder<TModel> {
    private Builder(String defaultColumnName) {
      mDefaultIdColumn = defaultColumnName;
    }

    public interface PolymorphicColumnSelector<TReturnType> {
      TReturnType by(String typeColumnName, String typeColumnId);
    }

    protected final String mDefaultIdColumn;
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

        mModels.add(relationship.mLinkedModel);
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

    public RelationshipAdder<TModel> where() {
      return new RelationshipAdder<TModel>(this);
    }

    public static class RelationshipAdder<TModel> {
      private final Builder<TModel> mBuilder;

      private RelationshipAdder(Builder<TModel> builder) {
        mBuilder = builder;
      }

      public RelationshipBuilder<TModel> the(TModel model) {
        return new RelationshipBuilder<TModel>(this, model);
      }

      public ModelGraph<TModel> build() {
        return mBuilder.build();
      }

      public static class OneToOneAndRecursiveRelationshipsBuilder<TModel> {
        protected final RelationshipAdder<TModel> mRelationshipAdder;
        protected final TModel mModel;
        protected String mModelIdColumn;

        private OneToOneAndRecursiveRelationshipsBuilder(RelationshipAdder<TModel> relationshipAdder, TModel model) {
          mRelationshipAdder = relationshipAdder;
          mModel = model;
          mModelIdColumn = relationshipAdder.mBuilder.mDefaultIdColumn;
        }

        public OneToOneRelationshipBuilder mayHave(TModel linkedModel) {
          return new OneToOneRelationshipBuilder(linkedModel, mModelIdColumn);
        }

        public ColumnSelector<RelationshipAdder<TModel>> groupsOther() {
          return new ColumnSelector<RelationshipAdder<TModel>>() {
            @Override
            public RelationshipAdder<TModel> by(String columnName) {
              new RecursiveModelRelationship<TModel>(mModel, mModelIdColumn, columnName).accept(mRelationshipAdder.mBuilder.mRelationshipVisitor);

              return mRelationshipAdder;
            }
          };
        }

        public class OneToOneRelationshipBuilder {
          private final TModel mLinkedModel;
          private final String mParentModelIdColumn;

          private OneToOneRelationshipBuilder(TModel linkedModel, String parentModelIdColumn) {
            mLinkedModel = linkedModel;
            mParentModelIdColumn = parentModelIdColumn;
          }

          public ColumnSelector<RelationshipAdder<TModel>> linked() {
            return new ColumnSelector<RelationshipAdder<TModel>>() {
              @Override
              public RelationshipAdder<TModel> by(String columnName) {
                new OneToOneRelationship<TModel>(mModel, mLinkedModel, mParentModelIdColumn, columnName).accept(mRelationshipAdder.mBuilder.mRelationshipVisitor);

                return mRelationshipAdder;
              }
            };
          }
        }
      }

      public static class OneToOneAndRecursiveRelationshipsBuilderWithDefaultIdColumn<TModel> extends OneToOneAndRecursiveRelationshipsBuilder<TModel> {
        private OneToOneAndRecursiveRelationshipsBuilderWithDefaultIdColumn(RelationshipAdder<TModel> relationshipAdder, TModel model) {
          super(relationshipAdder, model);
        }

        public ColumnSelector<OneToOneAndRecursiveRelationshipsBuilder<TModel>> identified() {
          return new ColumnSelector<OneToOneAndRecursiveRelationshipsBuilder<TModel>>() {
            @Override
            public OneToOneAndRecursiveRelationshipsBuilder<TModel> by(String columnName) {
              mModelIdColumn = columnName;

              return OneToOneAndRecursiveRelationshipsBuilderWithDefaultIdColumn.this;
            }
          };
        }
      }

      public static class RelationshipBuilder<TModel> extends OneToOneAndRecursiveRelationshipsBuilderWithDefaultIdColumn<TModel> {
        private RelationshipBuilder(RelationshipAdder<TModel> relationshipAdder, TModel model) {
          super(relationshipAdder, model);
        }

        public ColumnSelector<RelationshipAdder<TModel>> references(final TModel model) {
          return new ColumnSelector<RelationshipAdder<TModel>>() {
            @Override
            public RelationshipAdder<TModel> by(String columnName) {
              new OneToManyRelationship<TModel>(mModel, model, mModelIdColumn, columnName).accept(mRelationshipAdder.mBuilder.mRelationshipVisitor);

              return mRelationshipAdder;
            }
          };
        }

        public OneToManyRelationshipWithCustomIdBuilder<TModel> references(String modelIdColumn) {
          mModelIdColumn = modelIdColumn;
          return new OneToManyRelationshipWithCustomIdBuilder<TModel>(this);
        }

        public static class OneToManyRelationshipWithCustomIdBuilder<TModel> {
          private final RelationshipBuilder<TModel> mRelationshipBuilder;

          private OneToManyRelationshipWithCustomIdBuilder(RelationshipBuilder<TModel> relationshipBuilder) {
            mRelationshipBuilder = relationshipBuilder;
          }

          public ColumnSelector<RelationshipAdder<TModel>> in(final TModel model) {
            return mRelationshipBuilder.references(model);
          }

          public PolymorphicColumnSelector<RelationshipAdder<TModel>> in(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
            return mRelationshipBuilder.references(models);
          }
        }

        public PolymorphicColumnSelector<RelationshipAdder<TModel>> references(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
          return new PolymorphicColumnSelector<RelationshipAdder<TModel>>() {
            @Override
            public RelationshipAdder<TModel> by(String typeColumnName, String idColumnName) {
              new PolymorphicRelationship<TModel>(mModel, models, mModelIdColumn, typeColumnName, idColumnName).accept(mRelationshipAdder.mBuilder.mRelationshipVisitor);

              return mRelationshipAdder;
            }
          };
        }

        public ManyToManyRelationshipWithCustomIdBuilder<TModel> links(String modelIdColumn) {
          mModelIdColumn = modelIdColumn;
          return new ManyToManyRelationshipWithCustomIdBuilder<TModel>(this);
        }

        public static class ManyToManyRelationshipWithCustomIdBuilder<TModel> {
          private final RelationshipBuilder<TModel> mRelationshipBuilder;

          private ManyToManyRelationshipWithCustomIdBuilder(RelationshipBuilder<TModel> relationshipBuilder) {
            mRelationshipBuilder = relationshipBuilder;
          }

          public ColumnSelector<RelationshipBuilder<TModel>.ManyToManyRelationshipBuilder> in(final TModel model) {
            return mRelationshipBuilder.links(model);
          }

          public PolymorphicColumnSelector<RelationshipBuilder<TModel>.ManyToManyRelationshipBuilder> in(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
            return mRelationshipBuilder.links(models);
          }
        }

        public ColumnSelector<ManyToManyRelationshipBuilder> links(final TModel model) {
          return new ColumnSelector<ManyToManyRelationshipBuilder>() {

            @Override
            public ManyToManyRelationshipBuilder by(String columnName) {
              return new ManyToManyRelationshipBuilder(new OneToManyRelationship<TModel>(mModel, model, mModelIdColumn, columnName));
            }
          };
        }

        public PolymorphicColumnSelector<ManyToManyRelationshipBuilder> links(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
          return new PolymorphicColumnSelector<ManyToManyRelationshipBuilder>() {

            @Override
            public ManyToManyRelationshipBuilder by(String typeColumnName, String idColumnName) {
              return new ManyToManyRelationshipBuilder(new PolymorphicRelationship<TModel>(mModel, models, mModelIdColumn, typeColumnName, idColumnName));
            }
          };
        }

        public class ManyToManyRelationshipBuilder {
          private final Relationship<TModel> mLeftRelationship;
          private String mRightRelationshipModelIdColumn;

          private ManyToManyRelationshipBuilder(Relationship<TModel> leftRelationship) {
            mLeftRelationship = leftRelationship;
            mRightRelationshipModelIdColumn = mRelationshipAdder.mBuilder.mDefaultIdColumn;
          }

          public WithCustomIdColumn with(String modelIdColumn) {
            mRightRelationshipModelIdColumn = modelIdColumn;
            return new WithCustomIdColumn(this);
          }

          public class WithCustomIdColumn {
            private final ManyToManyRelationshipBuilder mManyToManyRelationshipBuilder;

            private WithCustomIdColumn(ManyToManyRelationshipBuilder manyToManyRelationshipBuilder) {
              mManyToManyRelationshipBuilder = manyToManyRelationshipBuilder;
            }

            public ColumnSelector<RelationshipAdder<TModel>> in(final TModel model) {
              return mManyToManyRelationshipBuilder.with(model);
            }

            public PolymorphicColumnSelector<RelationshipAdder<TModel>> in(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
              return mManyToManyRelationshipBuilder.with(models);
            }
          }

          public ColumnSelector<RelationshipAdder<TModel>> with(final TModel model) {
            return new ColumnSelector<RelationshipAdder<TModel>>() {

              @Override
              public RelationshipAdder<TModel> by(String columnName) {
                Relationship<TModel> rightRelationship = new OneToManyRelationship<TModel>(mModel, model, mRightRelationshipModelIdColumn, columnName);

                new ManyToManyRelationship<TModel>(mModel, mLeftRelationship, rightRelationship).accept(mRelationshipAdder.mBuilder.mRelationshipVisitor);

                return mRelationshipAdder;
              }
            };
          }

          public PolymorphicColumnSelector<RelationshipAdder<TModel>> with(final ImmutableList<? extends PolymorphicType<TModel, ? extends TModel>> models) {
            return new PolymorphicColumnSelector<RelationshipAdder<TModel>>() {

              @Override
              public RelationshipAdder<TModel> by(String typeColumnName, String idColumnName) {
                Relationship<TModel> rightRelationship = new PolymorphicRelationship<TModel>(mModel, models, mRightRelationshipModelIdColumn, typeColumnName, idColumnName);

                new ManyToManyRelationship<TModel>(mModel, mLeftRelationship, rightRelationship).accept(mRelationshipAdder.mBuilder.mRelationshipVisitor);

                return mRelationshipAdder;
              }
            };
          }
        }
      }
    }
  }

  public static class DefaultIdColumnSelector<TModel> {
    public ColumnSelector<Builder<TModel>> identifiedByDefault() {
      return new ColumnSelector<Builder<TModel>>() {
        @Override
        public Builder<TModel> by(String columnName) {
          return new Builder<TModel>(columnName);
        }
      };
    }
  }
}