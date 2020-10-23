package io.perfometer.runner;

class ThreadPoolScenarioRunnerSpecification : ScenarioRunnerSpecification() {

    override val runner: ScenarioRunner = ThreadPoolScenarioRunner(httpClient)
}
