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
package com.effektif.workflow.impl.plugin;

import org.joda.time.LocalDateTime;


public class AdapterConnection {

  // configuration fields
  protected String url;
  protected String authorization;
  protected String organizationId;
  protected Long updateIntervalMillis;
  
  // runtime status fields
  protected AdapterConnectionStatus status;
  protected String errorMessage;
  protected LocalDateTime lastContact;
  
  public String getUrl() {
    return this.url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public AdapterConnection url(String url) {
    this.url = url;
    return this;
  }
  
  public String getAuthorization() {
    return this.authorization;
  }
  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }
  public AdapterConnection authorization(String authorization) {
    this.authorization = authorization;
    return this;
  }

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public AdapterConnection organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  public Long getUpdateIntervalMillis() {
    return this.updateIntervalMillis;
  }
  public void setUpdateIntervalMillis(Long updateIntervalMillis) {
    this.updateIntervalMillis = updateIntervalMillis;
  }
  public AdapterConnection updateIntervalMillis(Long updateIntervalMillis) {
    this.updateIntervalMillis = updateIntervalMillis;
    return this;
  }

  public AdapterConnectionStatus getStatus() {
    return this.status;
  }
  public void setStatus(AdapterConnectionStatus status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return this.errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public LocalDateTime getLastContact() {
    return this.lastContact;
  }
  public void setLastContact(LocalDateTime lastContact) {
    this.lastContact = lastContact;
  }
}
