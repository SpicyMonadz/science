package org.decaf.science.metrics
import org.decaf.science._
import org.specs2.specification.Scope
import org.specs2.mutable.Specification
import io.dropwizard.metrics.{Meter, MetricRegistry}

object MetricsExperimentSpec extends Specification {

  "record in metrics when experiments pass through" in new context {
    (1 to 10) foreach { _ =>
      always(control(), experiment())
    }

    storage.getAllControlResults() must have size(10)

    val meter1: Meter = registry.meter("test-metrics-experiment.control")
    val meter2: Meter = registry.meter("test-metrics-experiment.experiment")
    val meter3: Meter = registry.meter("test-metrics-experiment.exceptions-during-experiments")

    meter1.getCount() must be_==(10)
    meter2.getCount() must be_==(10)
    meter3.getCount() must be_==(0)
  }

  "mark a failure during experiments" in new context {
    always(control(), failing())
    storage.getAllControlResults() must beEmpty
    val meter: Meter = registry.meter("test-metrics-experiment.exceptions-during-experiments")
    meter.getCount() must be_==(1)
  }

  def control(): String = "control"
  def experiment(): String = "experiment"
  def failing(): String = throw new Exception("nope")

  trait context extends Scope {
    val registry = new MetricRegistry()
    val storage = new InMemoryStorageStrategy[String]
    val exp = Experiment(storage)(ExperimentStrategy.always[String])
    val always = MetricsExperiment(registry)("test-metrics-experiment", exp)
  }
}