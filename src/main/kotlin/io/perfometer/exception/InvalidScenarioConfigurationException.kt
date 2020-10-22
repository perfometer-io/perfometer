package io.perfometer.exception

class InvalidScenarioConfigurationException : RuntimeException {

    constructor() : super("Invalid scenario configuration")

    constructor(cause: Exception) : this("Invalid scenario configuration", cause)

    constructor(message: String, cause: Exception) : super(message, cause)
}
