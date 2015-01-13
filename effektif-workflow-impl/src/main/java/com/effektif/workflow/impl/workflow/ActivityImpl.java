/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.workflow;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.plugin.ActivityType;
import com.effektif.workflow.impl.plugin.PluginService;


public class ActivityImpl extends ScopeImpl {
  
  public ActivityType activityType;
  /** the list of transitions for which this activity is the destination.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeImpl#transitionDefinitions} */
  public List<TransitionImpl> incomingTransitions;
  /** the list of transitions for which this activity is the source.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeImpl#transitionDefinitions} */
  public List<TransitionImpl> outgoingTransitions;
  public TransitionImpl defaultTransition;
  public MultiInstanceImpl multiInstance;
  
  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public void parse(Activity apiActivity, Scope apiScope, WorkflowParser workflowParser, ScopeImpl parent) {
    super.parse(apiActivity, workflowParser, parent);
    String id = apiActivity.getId();
    if (id!=null && !"".equals(id)) {
      this.id = id;
    } else {
      workflowParser.addError("Activity has no id");
    }

    PluginService pluginService = workflowParser.workflowEngine.getServiceRegistry().getService(PluginService.class);
    this.activityType = pluginService.instantiateActivityType(apiActivity);
    this.activityType.parse(this, apiActivity, workflowParser);
    // some activity types need to validate incoming and outgoing transitions, 
    // that's why they are NOT validated here, but after the transitions.
    if (this.activityType==null) {
      workflowParser.addError("Activity '%s' has no activityType configured", id);
    }

    if (apiActivity.getMultiInstance()!=null) {
      this.multiInstance = new MultiInstanceImpl();
      workflowParser.pushContext(multiInstance);
      this.multiInstance.parse(apiActivity.getMultiInstance(), this, workflowParser);
      workflowParser.popContext();
    }
    
    if (apiActivity.getOutgoingTransitions()!=null) {
      for (Transition transition: apiActivity.getOutgoingTransitions()) {
        transition.from(apiActivity.getId());
        apiScope.transition(transition);
      }
    }
  }

  public boolean isMultiInstance() {
    return multiInstance != null; 
  }

  /// other methods ////////////////////////////

  public void addOutgoingTransition(TransitionImpl transitionDefinition) {
    if (outgoingTransitions==null) {
      outgoingTransitions = new ArrayList<TransitionImpl>();
    }
    outgoingTransitions.add(transitionDefinition);
  }

  public boolean hasOutgoingTransitionDefinitions() {
    return outgoingTransitions!=null && !outgoingTransitions.isEmpty();
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Transition> getOutgoingTransitions() {
    return (List) outgoingTransitions;
  }

  public void setOutgoingTransitions(List<TransitionImpl> outgoingTransitionDefinitions) {
    this.outgoingTransitions = outgoingTransitionDefinitions;
  }


  public void addIncomingTransition(TransitionImpl transitionDefinition) {
    if (incomingTransitions==null) {
      incomingTransitions = new ArrayList<TransitionImpl>();
    }
    incomingTransitions.add(transitionDefinition);
  }

  public boolean hasIncomingTransitionDefinitions() {
    return incomingTransitions!=null && !incomingTransitions.isEmpty();
  }

  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Transition> getIncomingTransitions() {
    return (List) incomingTransitions;
  }

  public void setIncomingTransitions(List<TransitionImpl> incomingTransitionDefinitions) {
    this.incomingTransitions = incomingTransitionDefinitions;
  }
  
  public ActivityType getActivityType() {
    return activityType;
  }
  
  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  public TransitionImpl getDefaultTransition() {
    return defaultTransition;
  }
  
  public void setDefaultTransition(TransitionImpl defaultTransition) {
    this.defaultTransition = defaultTransition;
  }

  public String toString() {
    return id!=null ? "["+id.toString()+"]" : "["+Integer.toString(System.identityHashCode(this))+"]";
  }
}
