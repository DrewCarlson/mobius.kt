import Foundation
import Todo

class TodoEffectHandler: NSObject, TodoConnectable {
    func connect(output: TodoConsumer) -> TodoConnection {
        return TodoEffectConnection(output: output)
    }
}

class TodoEffectConnection: NSObject, TodoConnection {
    
    let store = NSUbiquitousKeyValueStore.default
    var tasks: [TodoTask] = []
    
    var consumer: TodoConsumer
    
    init(output: TodoConsumer) {
        consumer = output
        super.init()
        reloadTasks()
    }
    
    func accept(value: Any?) {
        if value is TodoEffectLoadTasks {
            consumer.accept(value: TodoEventOnTasksLoaded(tasks: tasks))
        } else if let effect = value as? TodoEffectCreateTask {
            let task = TodoTask(id: Int32(tasks.count + 1), todo: effect.todo, complete: false)
            addTask(task: task)
            consumer.accept(value: TodoEventOnTasksLoaded(tasks: tasks))
        } else if let effect = value as? TodoEffectDeleteTask {
            removeTask(taskId: effect.taskId)
        } else if let effect = value as? TodoEffectUpdateTask {
            updateTask(task: effect.task)
        }
    }
    
    func addTask(task: TodoTask) {
        tasks.append(task)
        store.set(task.todo, forKey: "\(task.id)-todo")
        store.set(task.complete, forKey: "\(task.id)-complete")
    }
    
    func removeTask(taskId: Int32) {
        tasks.remove(at: Int(taskId))
        store.removeObject(forKey: "\(taskId)-todo")
        store.removeObject(forKey: "\(taskId)-complete")
    }
    
    func updateTask(task: TodoTask) {
        tasks[Int(task.id)] = task
        store.set(task.todo, forKey: "\(task.id)-todo")
        store.set(task.complete, forKey: "\(task.id)-complete")
    }
    
    func reloadTasks() {
        var i = 1
        var taskTodo = store.string(forKey: "\(i)-todo")
        while taskTodo != nil {
            let taskComplete = store.bool(forKey: "\(i)-complete")
            tasks.append(TodoTask(id: Int32(i), todo: taskTodo!, complete: taskComplete))
            i += 1
            taskTodo = store.string(forKey: "\(i)-todo")
        }
    }
    
    func dispose() {
        store.synchronize()
    }
}
