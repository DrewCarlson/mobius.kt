import UIKit
import Todo

class ViewController: UIViewController {

    @IBOutlet weak var addTaskButton: UIBarButtonItem!
    @IBOutlet weak var taskTableView: UITableView!
    
    let loopFactory: MobiusLoop.Builder
    let loopController: MobiusLoop.Controller
    let defaultModel = AppModel.init(tasks: [], isLoadingTasks: true, isAddingTask: false)
    
    required init?(coder aDecoder: NSCoder) {
        let syncRunnerProducer = ImmediateWorkRunnerProducer()
        loopFactory = Mobius().loop(update: AppUpdate(), effectHandler: TodoEffectHandler())
            .doInit(init: AppInit())
            .effectRunner(effectRunner: syncRunnerProducer)
            .eventRunner(eventRunner: syncRunnerProducer)
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

class ImmediateWorkRunnerProducer: NSObject, Producer {
    func get() -> Any? {
        return ImmediateWorkRunner()
    }
}
