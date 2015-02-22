package org.chalup.thneed;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class Thneeds {
  private Thneeds() {
  }

  public static <TModel> RelationshipsSpecialCaseBuilder<TModel> with(ModelGraph<? extends TModel> modelGraph, RelationshipVisitor<? super TModel> visitor) {
    return new RelationshipsSpecialCaseBuilder<TModel>(modelGraph, visitor);
  }

  public static class RelationshipsSpecialCaseBuilder<TModel> {
    private final Map<Relationship<? extends TModel>, RelationshipVisitor<? super TModel>> mCases = Maps.newHashMap();

    public RelationshipsSpecialCaseBuilder(ModelGraph<? extends TModel> modelGraph, final RelationshipVisitor<? super TModel> visitor) {
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

    public RelationshipsSpecialCaseBuilder<TModel> plus(ModelGraph<? extends TModel> subGraph, final RelationshipVisitor<? super TModel> visitor) {
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

    private static final RelationshipVisitor<Object> NO_OP_VISITOR = new RelationshipVisitor<Object>() {
      @Override
      public void visit(OneToManyRelationship<?> relationship) {
        // no op
      }

      @Override
      public void visit(OneToOneRelationship<?> relationship) {
        // no op
      }

      @Override
      public void visit(RecursiveModelRelationship<?> relationship) {
        // no op
      }

      @Override
      public void visit(ManyToManyRelationship<?> relationship) {
        // no op
      }

      @Override
      public void visit(PolymorphicRelationship<?> relationship) {
        // no op
      }
    };

    public RelationshipsSpecialCaseBuilder<TModel> exclude(ModelGraph<? extends TModel> subGraph) {
      return plus(subGraph, NO_OP_VISITOR);
    }

    public void process() {
      for (Entry<Relationship<? extends TModel>, RelationshipVisitor<? super TModel>> entry : mCases.entrySet()) {
        entry.getKey().accept(entry.getValue());
      }
    }
  }

  public static <TModel> ModelsSpecialCaseBuilder<TModel> with(ModelGraph<? extends TModel> modelGraph, ModelVisitor<? super TModel> visitor) {
    return new ModelsSpecialCaseBuilder<TModel>(modelGraph, visitor);
  }

  public static class ModelsSpecialCaseBuilder<TModel> {
    private final Map<TModel, ModelVisitor<? super TModel>> mCases = Maps.newHashMap();

    public ModelsSpecialCaseBuilder(ModelGraph<? extends TModel> modelGraph, final ModelVisitor<? super TModel> visitor) {
      modelGraph.accept(new ModelVisitor<TModel>() {
        @Override
        public void visit(TModel model) {
          mCases.put(model, visitor);
        }
      });
    }

    public ModelsSpecialCaseBuilder<TModel> plus(List<? extends TModel> models, final ModelVisitor<? super TModel> visitor) {
      for (TModel model : models) {
        ModelVisitor<? super TModel> oldVisitor = mCases.put(model, visitor);
        Preconditions.checkState(oldVisitor != null);
      }

      return this;
    }

    private static final ModelVisitor<Object> NO_OP_VISITOR = new ModelVisitor<Object>() {
      @Override
      public void visit(Object model) {
        // no op
      }
    };

    public ModelsSpecialCaseBuilder<TModel> exclude(List<? extends TModel> models) {
      return plus(models, NO_OP_VISITOR);
    }

    public void process() {
      for (Entry<TModel, ModelVisitor<? super TModel>> entry : mCases.entrySet()) {
        entry.getValue().visit(entry.getKey());
      }
    }
  }
}