### 🧮 **Service B**

**Service B** is a Kafka consumer microservice responsible for maintaining a **running total** of all addition results produced by Service A.

#### 🔁 How it works:

1. **Consumes messages** from a Kafka topic containing addition results.
2. **Parses each result** and adds it to an internal or persisted **running total**.
3. Optionally **logs, persists, or exposes metrics** for observability and monitoring.

#### 🧠 Key Responsibilities:

* Ensures **no result is missed** by reliably processing all Kafka messages.
* Keeps a **real-time aggregate** of results for analytics, monitoring, or further processing.
* Works asynchronously and decoupled from Service A, enabling **scalability** and **fault isolation**.

