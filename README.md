# Effektif

Effektif is an easy, scalable workflow engine for designed for the cloud.  It's embeddable in JVM applications like Java and scala.  Effektif runs in memory or with a persistence engine like eg MongoDB.

A workflow is based on a diagram (eg BPMN or nodes and edges) and specify an execution flow to coordinate tasks, automatic activities and timers.  The workflow engine keeps track of each execution (aka workflow instance) and executes the activities as specified in the workflow.

```java
// Create the default (in-memory) workflow engine
WorkflowEngineConfiguration configuration = new MemoryWorkflowEngineConfiguration()
  .initialize();
WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
TaskService taskService = configuration.getTaskService();

// Create a workflow
Workflow workflow = new Workflow()
  .name("Release")
  .activity("Move open issues", new UserTask()
    .transitionToNext())
  .activity("Check continuous integration", new UserTask()
    .transitionToNext())
  .activity("Notify community", new EmailTask()
    .to("releases@example.com")
    .subject("New version released")
    .bodyText("Enjoy!"));

// Deploy the workflow to the engine
workflow = workflowEngine.deployWorkflow(workflow);

// Start a new workflow instance
WorkflowInstance workflowInstance = workflowEngine.startWorkflowInstance(workflow);

List<Task> tasks = taskService.findTasks(new TaskQuery());
assertEquals("Move open issues", tasks.get(0).getName());
assertEquals(1, tasks.size());
```

# User Documentation

* [Getting started](Getting-started)
* [Workflow engine types](Workflow-engine-types)
* [Building workflows](Building workflows)
* [Create your own activity](Create-your-own-activity)
* [Create your own datasource](Create-your-own-datasource)
* [Run the REST service](Run-the-REST-service)

# Developer Documentation

* Building the sources
* Working with MongoDB
* How to contribute
* Coding style
