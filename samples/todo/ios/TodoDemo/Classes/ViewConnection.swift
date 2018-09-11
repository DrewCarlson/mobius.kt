import Foundation
import UIKit
import Todo

class ViewConnectable: NSObject, TodoConnectable {
    let viewController: ViewController
    init(viewController: ViewController) {
        self.viewController = viewController
    }
    func connect(output: TodoConsumer) -> TodoConnection {
        return ViewConnection(output: output, viewController: viewController)
    }
}

class ViewConnection: NSObject, TodoConnection {
    let consumer: TodoConsumer
    let view: ViewController
    
    var alert: SimpleAlertController? = nil
    
    init(output: TodoConsumer, viewController: ViewController) {
        consumer = output
        view = viewController
        super.init()
        
        view.addTaskButton.action = #selector(self.addTaskClicked)
        view.addTaskButton.target = self
    }
    
    func accept(value: Any?) {
        let model = value as! TodoAppModel
        print(model)
        
        if (model.addingTask && alert == nil) {
            showNewTaskAlert()
        } else if (!model.addingTask && alert != nil) {
            clearNewTaskAlert()
        }
    }
    
    func showNewTaskAlert() {
        alert = SimpleAlertController(title: "New Task", message: "Describe your Task.", preferredStyle: .alert)
        alert!.addTextField { (textField) in
            textField.text = ""
        }
        alert!.addAction(UIAlertAction(title: "OK", style: .default, handler: { [weak alert] (_) in
            let textField = alert!.textFields![0]
            self.consumer.accept(value: TodoEventOnSubmitNewTask(description: textField.text!))
        }))
        alert!.willDisappear = { alert in
            self.consumer.accept(value: TodoEventOnDiscardNewTask())
            self.alert = nil
        }
        view.present(alert!, animated: false, completion: nil)
    }
    
    func clearNewTaskAlert() {
        alert!.dismiss(animated: true, completion: nil)
        alert = nil
    }
    
    @objc func addTaskClicked() {
        print("clicked")
        consumer.accept(value: TodoEventOnAddTask())
    }
    
    func dispose() {
        alert?.dismiss(animated: false, completion: nil)
        alert?.willDisappear = nil
        alert = nil
        view.addTaskButton.action = nil
        view.addTaskButton.target = nil
    }
}
