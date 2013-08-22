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

import org.chalup.thneed.PolymorphicType;

class TestData {

  static final String ID = "id";

  static final String LEAD_ID = "lead_id";
  static final String CONTACT_ID = "contact_id";
  static final String TASKABLE_TYPE = "taskable_type";
  static final String TASKABLE_ID = "taskable_id";
  static final String SUBJECT_ID = "subject_id";
  static final String CUSTOM_FIELD_ID = "custom_field_id";
  static final String TAG_ID = "tag_id";
  static final String TAGGABLE_ID = "taggable_id";
  static final String TAGGABLE_TYPE = "taggable_type";

  public interface ModelInterface {
  }

  public static abstract class PolyModel implements ModelInterface, PolymorphicType<ModelInterface, PolyModel> {
    @Override
    public PolyModel getModel() {
      return this;
    }
  }

  public enum Models implements ModelInterface {
    TAGGING,
    TAG,
    TASK,
    CONTACT_DATA,
    CUSTOM_FIELD,
    CUSTOM_FIELD_VALUE
  }

  public static final PolyModel CONTACT = new PolyModel() {
    @Override
    public String getModelName() {
      return "Contact";
    }
  };

  public static final PolyModel DEAL = new PolyModel() {
    @Override
    public String getModelName() {
      return "Deal";
    }
  };

  public static final PolyModel LEAD = new PolyModel() {
    @Override
    public String getModelName() {
      return "Lead";
    }
  };
}
