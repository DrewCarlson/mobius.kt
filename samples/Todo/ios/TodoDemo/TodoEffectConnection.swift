//
//  TodoEffectConnection.swift
//  TodoDemo
//
//  Created by Andrew Carlson on 9/10/18.
//  Copyright © 2018 Drew Carlson. All rights reserved.
//

import Foundation
import Todo

class TodoEffectConnection: NSObject, TodoConnection {
    var tasks: [TodoTask] = []
    var consumer: TodoConsumer
    init(output: TodoConsumer) {
        consumer = output
    }
    func accept(value: Any?) {
        let effect = value as! TodoEffect
        if (effect is TodoEffectLoadTasks) {
            consumer.accept(value: TodoEventOnTasksLoaded(tasks: tasks))
        } else if (effect is TodoEffectCreateTask) {
            let e = effect as! TodoEffectCreateTask
            let task = TodoTask(id: Int32(tasks.count), description: e.component1(), complete: false)
            tasks.append(task)
            self.consumer.accept(value: TodoEventOnTasksLoaded(tasks: tasks))
        } else if (effect is TodoEffectDeleteTask) {
            let e = effect as! TodoEffectDeleteTask
            tasks.remove(at: Int(e.taskId))
        } else if (effect is TodoEffectUpdateTask) {
            let e = effect as! TodoEffectUpdateTask
            tasks[Int(e.task.id)] = e.task
        }
    }
    
    func dispose() {
    }
}
