package itk.student.task.manager.aqa.hw.consts;

import itk.student.task.manager.aqa.config.ApiPaths;

public final class TasksApi {

  public static final String TASK_BY_TASK_ID = ApiPaths.TASKS + "/{taskId}";
  public static final String TASKS_STATUS = TASK_BY_TASK_ID + "/status";
  public static final String TASKS_COMMENTS = TASK_BY_TASK_ID + "/comments";

}
