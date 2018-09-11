import UIKit
import Todo

class ViewController: UIViewController {

    @IBOutlet weak var addTaskButton: UIBarButtonItem!
    @IBOutlet weak var taskTableView: UITableView!
    
    let loopFactory: TodoMobiusLoopBuilder
    let loopController: TodoMobiusLoopControllerProtocol
    let defaultModel = TodoAppModel.init(tasks: [], loadingTasks: false, addingTask: false)
    
    required init?(coder aDecoder: NSCoder) {
        let syncRunnerProducer = ImmediateWorkRunnerProducer()
        loopFactory = TodoMobius().loop(update: TodoAppUpdate(), effectHandler: TodoEffectHandler())
            .effectRunner(effectRunner: syncRunnerProducer)
            .eventRunner(eventRunner: syncRunnerProducer)
            .logger(logger: TodoSimpleLogger(tag: "Todo"))
        loopController = TodoMobius().controller(
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

class ImmediateWorkRunnerProducer: NSObject, TodoProducer {
    func get() -> Any? {
        return TodoImmediateWorkRunner()
    }
}
