//
//  TodoEffectHandler.swift
//  TodoDemo
//
//  Created by Andrew Carlson on 9/10/18.
//  Copyright © 2018 Drew Carlson. All rights reserved.
//

import Foundation
import Todo

class TodoEffectHandler: NSObject, TodoConnectable {
    func connect(output: TodoConsumer) -> TodoConnection {
        return TodoEffectConnection(output: output);
    }
}
