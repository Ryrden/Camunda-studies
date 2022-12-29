package com.camunda.training;

import org.camunda.bpm.engine.runtime.Job;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.findId;

@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessJUnitTest {
  
  @Test
  @Deployment(resources = "model.bpmn")
  public void testHappyPath() {
    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    //variables.put("approved", true);
    variables.put("content", "Exercise 8 - Ryan S. Junit");
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

    assertThat(processInstance).isStarted().isWaitingAt(task.getTaskDefinitionKey());

    Map<String, Object> approvedMap = new HashMap<String, Object>();
    approvedMap.put("approved", true);

    taskService().complete(task.getId(), approvedMap);

    // This will query for jobs that are waiting to be executed and execute them.
    List<Job> jobList = jobQuery()
            .processInstanceId(processInstance.getId())
            .list();
    assertThat(jobList).hasSize(1);
    Job job = jobList.get(0);
    execute(job);

    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "model.bpmn")
  public void testTweetRejected(){
    Map<String, Object> variables = new HashMap<String, Object>();
    //variables.put("approved", true);
    variables.put("content", "Exercise 8 - Ryan S. Junit");
    variables.put("approved", false);
    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService()
            .createProcessInstanceByKey("TwitterQAProcess")
            .setVariables(variables)
            .startAfterActivity(findId("Review Tweet"))
            .execute();

    assertThat(processInstance).isEnded().hasPassed(findId("Tweet Rejected"));
  }
}
