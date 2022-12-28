package com.camunda.training;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {
  
  @Test
  @Deployment(resources = "model.bpmn")
  public void testHappyPath() {
    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    //variables.put("approved", true);
    variables.put("content", "Exercise 5 - Ryan Souza");
    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TwitterQAProcess", variables);

    // Make assertions on the process instance
    List<Task> taskList = taskService()
            .createTaskQuery()
            .taskCandidateGroup("management")
            .processInstanceId(processInstance.getId())
            .list();
    //assert that taskList is not null and has size equal 1
    assertThat(taskList)
            .isNotNull()
            .hasSize(1);

    Task task = taskList.get(0);

    String generatedActivityId = processInstance.getProcessDefinitionId();

    assertThat(processInstance).isStarted().isWaitingAt(task.getTaskDefinitionKey());

    Map<String, Object> approvedMap = new HashMap<String, Object>();
    approvedMap.put("approved", true);

    taskService().complete(task.getId(), approvedMap);

    assertThat(processInstance).isEnded();
  }

}
