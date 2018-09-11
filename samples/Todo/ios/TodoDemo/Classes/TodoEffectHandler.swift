import Foundation
import Todo

class TodoEffectHandler: NSObject, TodoConnectable {
    func connect(output: TodoConsumer) -> TodoConnection {
        return TodoEffectConnection(output: output);
    }
}

class TodoEffectConnection: NSObject, TodoConnection {
    
    var tasks: [TodoTask] = []
    var consumer: TodoConsumer
    
    init(output: TodoConsumer) {
        consumer = output
    }
    
    func accept(value: Any?) {
        if value is TodoEffectLoadTasks {
            consumer.accept(value: TodoEventOnTasksLoaded(tasks: tasks))
        } else if let effect = value as? TodoEffectCreateTask {
            let task = TodoTask(id: Int32(tasks.count), description: effect.component1(), complete: false)
            tasks.append(task)
            consumer.accept(value: TodoEventOnTasksLoaded(tasks: tasks))
        } else if let effect = value as? TodoEffectDeleteTask {
            tasks.remove(at: Int(effect.taskId))
        } else if let effect = value as? TodoEffectUpdateTask {
            tasks[Int(effect.task.id)] = effect.task
        }
    }
    
    func dispose() {
    }
}
