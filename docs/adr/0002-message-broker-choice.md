# ADR-0002: Use RabbitMQ instead of Kafka for order events

## Status
Accepted

## Context
Order creation needs to trigger asynchronous payment processing without
blocking the API response. We considered RabbitMQ and Kafka.

## Decision
We chose **RabbitMQ** for this project.

## Rationale
- **Workload shape**: this is a task-queue problem (process each order's
  payment exactly-once-ish, retry on failure), not a high-throughput event
  log that multiple independent consumers replay. RabbitMQ's queue + ack
  model fits task distribution better than Kafka's log-based model.
- **Built-in DLQ semantics**: RabbitMQ's dead-letter-exchange mechanism
  gives us retry + DLQ almost for free via configuration. Kafka would
  require us to hand-roll retry topics and offset management.
- **Operational simplicity**: for a single-consumer-group workload like
  this, RabbitMQ is simpler to run and reason about locally (one
  container, a management UI) than a Kafka + Zookeeper/KRaft cluster.

## Trade-offs
- Kafka would be the better choice if we needed to replay order history,
  support multiple independent consumer groups (e.g., analytics,
  fraud-detection, notifications all reading the same event stream), or
  needed to scale to very high sustained throughput with partitioned
  ordering guarantees. If this system grew into a true event-driven
  platform with many downstream consumers, we would revisit this decision.

## Interview talking point
Be ready to explain: "I chose the broker based on the *access pattern*
(single consumer processing a task queue) rather than defaulting to
whichever technology is more popular. If asked to redesign for multiple
downstream consumers, I'd migrate to Kafka topics keyed by order ID to
preserve per-order ordering."
