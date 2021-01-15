package kt.mobius.runners

actual object WorkRunners {

    actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }
}
