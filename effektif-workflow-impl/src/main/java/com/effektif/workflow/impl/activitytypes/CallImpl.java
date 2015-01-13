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
package com.effektif.workflow.impl.activitytypes;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.CallMapping;
import com.effektif.workflow.api.command.StartCommand;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowStore;
import com.effektif.workflow.impl.plugin.AbstractActivityType;
import com.effektif.workflow.impl.tooling.ConfigurationField;
import com.effektif.workflow.impl.tooling.ConfigurationPanel;
import com.effektif.workflow.impl.tooling.FieldTypeBinding;
import com.effektif.workflow.impl.tooling.FieldTypeList;
import com.effektif.workflow.impl.tooling.FieldTypeObject;
import com.effektif.workflow.impl.tooling.FieldTypeVariableId;
import com.effektif.workflow.impl.tooling.FieldTypeWorkflowId;
import com.effektif.workflow.impl.tooling.FieldTypeWorkflowName;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.WorkflowParser;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

public class CallImpl extends AbstractActivityType<Call> {

  // TODO Boolean waitTillSubWorkflowEnds; add a configuration property to specify if this is fire-and-forget or wait-till-subworkflow-ends
  BindingImpl<String> subProcessName;
  BindingImpl<String> subProcessId;
  List<CallMappingImpl> inputMappings;
  List<CallMappingImpl> outputMappings;

  public CallImpl() {
    super(Call.class);
  }

  @Override
  public ConfigurationPanel getConfigurationPanel() {
    return new ConfigurationPanel("Call")
      .description("Invoke another workflow")
      .field(new ConfigurationField("Sub worfklow id")
        .key(Call.SUB_WORKFLOW_ID)
        .type(new FieldTypeWorkflowId()))
      .field(new ConfigurationField("Sub workflow name")
        .key(Call.SUB_WORKFLOW_NAME)
        .type(new FieldTypeBinding(new FieldTypeWorkflowName())))
      .field(new ConfigurationField("Input mappings")
        .key(Call.INPUT_MAPPINGS)
        .type(new FieldTypeList(new FieldTypeObject()
          .field(new ConfigurationField("Item in this workflow")
            .key("sourceBinding")
            .type(new FieldTypeBinding()))
          .field(new ConfigurationField("Variable in the sub workflow")
            .key("destinationVariableId")
            .type(new FieldTypeVariableId())))))
      .field(new ConfigurationField("Output mappings")
        .key(Call.OUTPUT_MAPPINGS)
        .type(new FieldTypeList(new FieldTypeObject()
          .field(new ConfigurationField("Item in the sub workflow")
            .key("sourceBinding")
            .type(new FieldTypeBinding()))
          .field(new ConfigurationField("Variable in this workflow")
            .key("destinationVariableId")
            .type(new FieldTypeVariableId())))));
  }
  
  @Override
  public void parse(ActivityImpl activityImpl, Activity activityApi, WorkflowParser workflowParser) {
    subProcessId = workflowParser.parseBinding(activityApi, Call.SUB_WORKFLOW_ID, String.class);
    subProcessName = workflowParser.parseBinding(activityApi, Call.SUB_WORKFLOW_NAME, String.class);
    inputMappings = validateCallMappings(activityApi, Call.INPUT_MAPPINGS, workflowParser);
    outputMappings = validateCallMappings(activityApi, Call.OUTPUT_MAPPINGS, workflowParser);
  }

  private List<CallMappingImpl> validateCallMappings(Activity activityApi, String key, WorkflowParser workflowParser) {
    List<Object> callMappings = (List<Object>) activityApi.getConfiguration(key); 
    if (callMappings!=null) {
      String activityId = activityApi.getId();
      List<CallMappingImpl> callMappingImpls = new ArrayList<>(callMappings.size());
      int i=0;
      for (Object callMappingObject: callMappings) {
        CallMapping callMapping = workflowParser.parseObject(callMappingObject, CallMapping.class, false, activityId, key);
        CallMappingImpl callMappingImpl = new CallMappingImpl();
        callMappingImpl.sourceBinding = workflowParser.parseBinding(callMapping.getSourceBinding(), Object.class, activityId, key+"["+i+"]");
        callMappingImpl.destinationVariableId = callMapping.getDestinationVariableId();
        i++;
      }
      return callMappingImpls;
    }
    return null;
  }

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    WorkflowEngineImpl workflowEngine = activityInstanceImpl.workflowEngine;

    String subProcessIdValue = null;
    if (subProcessId!=null) {
      subProcessIdValue = activityInstance.getValue(subProcessId);
    } else if (subProcessName!=null) {
      String subProcessNameValue = activityInstance.getValue(subProcessName);
      WorkflowStore workflowStore = workflowEngine.getWorkflowStore();
      subProcessIdValue = workflowStore.findLatestWorkflowIdByName(subProcessNameValue, activityInstance.requestContext);
      if (subProcessIdValue==null) {
        throw new RuntimeException("Couldn't find subprocess by name: "+subProcessNameValue);
      }
    }

    StartCommand start = new StartCommand()
      .workflowId(subProcessIdValue);
    
    if (inputMappings!=null) {
      for (CallMappingImpl inputMapping: inputMappings) {
        Object value = activityInstance.getValue(inputMapping.sourceBinding);
        start.variableValue(inputMapping.destinationVariableId, value);
      }
    }
    
    CallerReference callerReference = new CallerReference(activityInstance.workflowInstance.id, activityInstance.id);

    WorkflowInstance calledProcessInstance = workflowEngine.startWorkflowInstance(start, callerReference, activityInstance.requestContext);
    activityInstanceImpl.setCalledWorkflowInstanceId(calledProcessInstance.getId()); 
  }
  
  public void calledProcessInstanceEnded(ActivityInstanceImpl activityInstance, WorkflowInstanceImpl calledProcessInstance) {
    if (outputMappings!=null) {
      for (CallMappingImpl outputMapping: outputMappings) {
        Object value = calledProcessInstance.getValue(outputMapping.sourceBinding);
        activityInstance.setVariableValue(outputMapping.destinationVariableId, value);
      }
    }
  }
}
