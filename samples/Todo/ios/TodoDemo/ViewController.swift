//
//  ViewController.swift
//  TodoDemo
//
//  Created by Andrew Carlson on 9/10/18.
//  Copyright © 2018 Drew Carlson. All rights reserved.
//

import UIKit
import Todo

class ViewController: UIViewController {

    @IBOutlet weak var newTask: UIButton!
    var controller: TodoMobiusLoopControllerProtocol? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let update = TodoAppUpdate(); // Kotlin Event+State transition logic
        let effectHandler = TodoEffectHandler(); // Swift side-effect handlers
        
        // Create out loop
        let mobius = TodoMobius.init();
        let loopFactory = mobius.loop(update: update, effectHandler: effectHandler)
        
        let defaultModel = TodoAppModel.init(tasks: [], loadingTasks: false, addingTask: false)
        
        controller = mobius.controller(loopFactory: loopFactory, defaultModel: defaultModel)
        controller!.connect(view: ViewConnectable(viewController: self))
    }
    
    override func viewDidAppear(_ animated: Bool) {
        controller!.start()
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        controller!.stop()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

class ViewConnectable: NSObject, TodoConnectable {
    let viewController: ViewController
    var output: TodoConsumer? = nil
    init(viewController: ViewController) {
        self.viewController = viewController
    }
    func connect(output: TodoConsumer) -> TodoConnection {
        self.output = output
        return ViewConnection(output: output, viewController: viewController)
    }
}

class ViewConnection: NSObject, TodoConnection {
    let consumer: TodoConsumer
    let viewController: ViewController
    
    var alert: CustomAlertController? = nil
    
    init(output: TodoConsumer, viewController: ViewController) {
        consumer = output
        self.viewController = viewController
        super.init()
        viewController.newTask.addTarget(self, action: #selector(newTaskClicked), for: .touchUpInside)
    }
    
    @objc func newTaskClicked() {
        consumer.accept(value: TodoEventOnAddTask())
    }
    
    func accept(value: Any?) {
        let model = value as! TodoAppModel
        print(model)
        
        if (model.addingTask && alert == nil) {
            alert = CustomAlertController(title: "New Task", message: "Describe your Task.", preferredStyle: .alert)
            alert!.addTextField { (textField) in
                textField.text = ""
            }
            alert!.addAction(UIAlertAction(title: "OK", style: .default, handler: { [weak alert] (_) in
                let textField = alert!.textFields![0]
                self.consumer.accept(value: TodoEventOnSubmitNewTask(description: textField.text!))
            }))
            alert!.willDisappearBlock = { alert in
                self.consumer.accept(value: TodoEventOnDiscardNewTask())
                self.alert = nil
            }
            viewController.present(alert!, animated: true, completion: nil)
        } else if (!model.addingTask && alert != nil) {
            alert!.dismiss(animated: true, completion: nil)
            alert = nil
        }
    }
    
    func dispose() {
        alert = nil
    }
}
