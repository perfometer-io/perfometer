package io.perfometer.runner;

class CoroutinesScenarioRunnerSpecification : ScenarioRunnerSpecification() {

    override val runner: ScenarioRunner = CoroutinesScenarioRunner(httpClient)
}
