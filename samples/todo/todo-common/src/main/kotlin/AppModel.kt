package todo

data class AppModel(
    val tasks: List<Task> = emptyList(),
    val isLoadingTasks: Boolean = false,
    val isAddingTask: Boolean = false
)
