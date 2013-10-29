package org.chalup.thneed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class Thneeds {
  private Thneeds() {
  }

  public static <TModel> RelationshipsSpecialCaseBuilder<TModel> with(ModelGraph<? extends TModel> modelGraph, RelationshipVisitor<TModel> visitor) {
    return new RelationshipsSpecialCaseBuilder<TModel>(modelGraph, visitor);
  }

  public static class RelationshipsSpecialCaseBuilder<TModel> {
    private final Map<Relationship<? extends TModel>, RelationshipVisitor<TModel>> mCases = Maps.newHashMap();

    public RelationshipsSpecialCaseBuilder(ModelGraph<? extends TModel> modelGraph, final RelationshipVisitor<TModel> visitor) {
      modelGraph.accept(new RelationshipVisitor<TModel>() {
        @Override
        public void visit(OneToManyRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(OneToOneRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(RecursiveModelRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(ManyToManyRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(PolymorphicRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }
      });
    }

    public RelationshipsSpecialCaseBuilder<TModel> plus(ModelGraph<? extends TModel> subGraph, final RelationshipVisitor<TModel> visitor) {
      subGraph.accept(new RelationshipVisitor<TModel>() {
        @Override
        public void visit(OneToManyRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(OneToOneRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(RecursiveModelRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(ManyToManyRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }

        @Override
        public void visit(PolymorphicRelationship<? extends TModel> relationship) {
          mCases.put(relationship, visitor);
        }
      });

      return this;
    }

    public void process() {
      for (Entry<Relationship<? extends TModel>, RelationshipVisitor<TModel>> entry : mCases.entrySet()) {
        entry.getKey().accept(entry.getValue());
      }
    }
  }

  public static <TModel> ModelsSpecialCaseBuilder<TModel> with(ModelGraph<? extends TModel> modelGraph, ModelVisitor<TModel> visitor) {
    return new ModelsSpecialCaseBuilder<TModel>(modelGraph, visitor);
  }

  public static class ModelsSpecialCaseBuilder<TModel> {
    private final Map<TModel, ModelVisitor<TModel>> mCases = Maps.newHashMap();

    public ModelsSpecialCaseBuilder(ModelGraph<? extends TModel> modelGraph, final ModelVisitor<TModel> visitor) {
      modelGraph.accept(new ModelVisitor<TModel>() {
        @Override
        public void visit(TModel model) {
          mCases.put(model, visitor);
        }
      });
    }

    public ModelsSpecialCaseBuilder<TModel> plus(List<? extends TModel> models, final ModelVisitor<TModel> visitor) {
      for (TModel model : models) {
        ModelVisitor<TModel> oldVisitor = mCases.put(model, visitor);
        Preconditions.checkState(oldVisitor != null);
      }

      return this;
    }

    public void process() {
      for (Entry<TModel, ModelVisitor<TModel>> entry : mCases.entrySet()) {
        entry.getValue().visit(entry.getKey());
      }
    }
  }
}