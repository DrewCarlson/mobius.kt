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

class ViewConnection: NSObject, TodoConnection, UITableViewDataSource {
    
    let consumer: TodoConsumer
    let view: ViewController
    var alert: UIAlertController? = nil
    
    var tasks: [TodoTask] = []
    
    init(output: TodoConsumer, viewController: ViewController) {
        consumer = output
        view = viewController

        super.init()

        view.addTaskButton.action = #selector(self.addTaskClicked)
        view.addTaskButton.target = self
        view.taskTableView.dataSource = self
    }
    
    func accept(value: Any?) {
        let model = value as! TodoAppModel
        tasks = model.tasks
        
        if (model.isAddingTask && alert == nil) {
            showNewTaskAlert()
        } else if (!model.isAddingTask && alert != nil) {
            hideNewTaskAlert()
        }
        
        if (tasks.count != view.taskTableView.numberOfRows(inSection: 1)) {
            view.taskTableView.reloadData()
        }
    }
    
    func showNewTaskAlert() {
        alert = UIAlertController(title: "New Task", message: "Describe your Task.", preferredStyle: .alert)
        alert!.addTextField { (textField) in
            textField.text = ""
        }
        alert!.addAction(UIAlertAction(title: "Save", style: .default, handler: { [weak alert] (_) in
            let textField = alert!.textFields![0]
            self.consumer.accept(value: TodoEventOnSubmitNewTask(todo: textField.text!))
            self.alert = nil
        }))
        alert!.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: { (_) in
            self.consumer.accept(value: TodoEventOnDiscardNewTask())
            self.alert = nil
        }))
        view.present(alert!, animated: true, completion: nil)
    }
    
    func hideNewTaskAlert() {
        alert!.dismiss(animated: true, completion: nil)
        alert = nil
    }
    
    @objc func addTaskClicked() {
        consumer.accept(value: TodoEventOnAddTask())
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return tasks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "identifier")!
        let task = tasks[indexPath.row]
        cell.textLabel!.text = task.todo
        return cell
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func dispose() {
        alert?.dismiss(animated: false, completion: nil)
        alert = nil
        view.addTaskButton.action = nil
        view.addTaskButton.target = nil
        view.taskTableView.dataSource = nil
    }
}
