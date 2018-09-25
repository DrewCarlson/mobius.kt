import UIKit
import Todo

class ViewController: UIViewController {

    @IBOutlet weak var addTaskButton: UIBarButtonItem!
    @IBOutlet weak var taskTableView: UITableView!
    
    let loopFactory: MobiusLoopBuilder
    let loopController: MobiusLoopController
    let defaultModel = AppModel.init(tasks: [], isLoadingTasks: true, isAddingTask: false)
    
    required init?(coder aDecoder: NSCoder) {
        loopFactory = Mobius().loop(update: AppUpdate(), effectHandler: TodoEffectHandler())
            .doInit(init: AppInit())
            .logger(logger: SimpleLogger(tag: "Todo"))
        loopController = Mobius().controller(
            loopFactory: loopFactory,
            defaultModel: defaultModel
        )
        super.init(coder: aDecoder)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        loopController.connect(view: ViewConnectable(viewController: self))
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

class TodoEffectHandler: NSObject, Connectable {
    func connect(output: Consumer) -> Connection {
        return AppEffectHandler(output: output, store: TaskStore())
    }
}
