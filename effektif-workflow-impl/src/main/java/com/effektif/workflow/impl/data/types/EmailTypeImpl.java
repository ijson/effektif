/* Copyright (c) 2014, Effektif GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.effektif.workflow.impl.data.types;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.JavaBeanType;
import com.effektif.workflow.impl.email.PersistentEmail;
import com.effektif.workflow.impl.file.FileService;


/**
 * @author Tom Baeyens
 */
public class EmailTypeImpl extends JavaBeanTypeImpl<JavaBeanType> {
  
  FileService fileService;

  public EmailTypeImpl() {
  }

  public EmailTypeImpl(Configuration configuration) {
    initialize(configuration);
    initializeFields(configuration);
  }

  public void initialize(Configuration configuration) {
    initialize(new JavaBeanType(PersistentEmail.class), PersistentEmail.class, configuration);
    initializeFields(configuration);
    this.fileService = configuration.get(FileService.class);
  }

  public EmailTypeImpl(JavaBeanType typeApi, Configuration configuration) {
    initialize(typeApi, PersistentEmail.class, configuration);
    initializeFields(configuration);
  }

  @Override
  protected void initializeFields(Configuration configuration) {
    addField(new JavaBeanFieldImpl(PersistentEmail.class, "subject", new TextTypeImpl(configuration)));
  }
}
