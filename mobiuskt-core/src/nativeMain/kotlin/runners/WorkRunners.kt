package kt.mobius.runners

public actual object WorkRunners {

    public actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }
}
