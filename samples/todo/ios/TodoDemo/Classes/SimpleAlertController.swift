import Foundation
import UIKit

class SimpleAlertController: UIAlertController {
    
    var willDisappear: ((UIAlertController) -> Void)?
    
    override func viewWillDisappear(_ animated: Bool) {
        willDisappear?(self)
        super.viewWillDisappear(animated)
    }
}
