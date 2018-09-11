import UIKit
import Todo

class ViewController: UIViewController {

    @IBOutlet weak var addTaskButton: UIBarButtonItem!
    
    let loopFactory: TodoMobiusLoopBuilder
    let loopController: TodoMobiusLoopControllerProtocol
    
    required init?(coder aDecoder: NSCoder) {
        loopFactory = TodoMobius().loop(update: TodoAppUpdate(), effectHandler: TodoEffectHandler())
        loopController = TodoMobius().controller(
            loopFactory: loopFactory,
            defaultModel: TodoAppModel.init(tasks: [], loadingTasks: false, addingTask: false)
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
