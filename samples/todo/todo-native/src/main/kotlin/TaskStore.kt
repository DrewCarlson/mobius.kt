package todo

import platform.Foundation.NSUserDefaults


actual class TaskStore() {

  private val cache = NSUserDefaults.standardUserDefaults
  private var tasks = listOf<Task>()

  init {
    tasks = cache.dictionaryRepresentation()
        .filter { (key: Any?, value: Any?) ->
          (key as String).endsWith("todo")
        }
        .map { (key: Any?, value: Any?) ->
          val taskStr = value as String
          val id = "$key".substring(0, 1).toInt()
          val todo = taskStr.substring(1, taskStr.lastIndex)
          val isComplete = taskStr.substring(0, 1).toInt().toBoolean()
          Task(id, todo, isComplete)
        }
  }

  actual fun listTasks(): List<Task> {
    return tasks
  }

  actual fun saveTask(task: Task) {
    val (id, todo, isComplete) = task
    cache.setObject("${isComplete.toInt()}$todo", forKey = "$id-todo")
    tasks = tasks + task
  }

  actual fun updateTask(task: Task) {
    val (id, todo, isComplete) = task
    cache.setObject("${isComplete.toInt()}$todo", forKey = "$id-todo")
  }

  actual fun deleteTask(taskId: Int) {
    //cache.removeObject(taskId.toString())
  }

  actual fun dispose() {
    cache.synchronize()
  }

  fun Boolean.toInt() = if (this) 1 else 0
  fun Int.toBoolean() = this == 1
}
