# Shipment Profit Calculator API

Java/Spring Boot backend implementing the **Calculate Profit** use case: records a
shipment's income and service-provision costs, then calculates and persists the
resulting profit or loss (`Profit = Income - Total Costs`).

## Tools & versions

- Java 17
- Spring Boot 3.1.5
- Maven 3.9.3
- H2 (embedded, in-memory)

## Run

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. The H2 console is available at
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:shipmentprofitdb`, user `sa`, no password).

Schema and seed data (`src/main/resources/schema.sql` and `data.sql`) are loaded automatically on startup.

## Test

```bash
mvn test
```

## API

### `POST /api/profit-calculations`

Records income and cost(s) for a shipment (creating the shipment if the code
doesn't exist yet) and returns the calculated profit or loss.

Recalculation is repeat-safe: repeated submissions do not append duplicate
income or cost records and therefore do not inflate the financial totals.
A shipment has at most one income record, one BASE cost and one ADDITIONAL
cost, and recalculating updates those in place. Submitting `additionalCost: 0`
after a previous non-zero value deletes the stored additional cost.

Request:

```json
{
  "shipmentCode": "0001",
  "income": 230.50,
  "cost": 200,
  "additionalCost": 0
}
```

Response:

```json
{
  "shipmentCode": "0001",
  "income": 230.50,
  "totalCosts": 200.00,
  "profitOrLoss": 30.50,
  "calculatedAt": "2026-07-29T17:21:07.397716"
}
```

A Postman collection with sample requests is at `postman_collection.json`.

## Architecture

Layered per SOLID, one endpoint exposed:

```
controller -> service -> repository -> entity
                 |
                dto
```

- **entity**: `Shipment`, `Income`, `Cost`, `ProfitLoss` — JPA-mapped to the schema in `schema.sql`
- **repository**: Spring Data JPA interfaces (CRUD + shipment-scoped lookups)
- **dto**: `CalculateProfitRequest` / `ProfitLossResponse` records — the API's wire format, decoupled from persistence
- **service**: `ProfitCalculationService` (interface) / `ProfitCalculationServiceImpl` — orchestrates the use case's main flow and builds `ProfitLossResponse` directly from the persisted result
- **controller**: `ProfitController` — the single exposed endpoint
- **exception**: `GlobalExceptionHandler` — validation errors return 400 with field-level detail; unexpected errors are logged and return a generic 500 (the use case's "Data Retrieval Error" alternative flow)

## Schema

```
shipment(id, shipment_code UNIQUE, created_at)
income(id, shipment_id FK UNIQUE, amount, description, created_at)
cost(id, shipment_id FK, cost_type [BASE|ADDITIONAL], amount, description, created_at, UNIQUE(shipment_id, cost_type))
profit_loss(id, shipment_id FK UNIQUE, total_income, total_costs, profit_or_loss, calculated_at)
```

`shipment.shipment_code`, `income.shipment_id` and `profit_loss.shipment_id` are
unique, and `cost` is unique per `(shipment_id, cost_type)` — these constraints
enforce the "one income, one BASE cost, one ADDITIONAL cost per shipment"
invariant at the database level, not just in application code. No separate
indexes are declared: a unique constraint is backed by a unique index
automatically, and the composite constraint on `cost` already indexes
`shipment_id`-only lookups as its leading column, so a standalone index would
be redundant.
