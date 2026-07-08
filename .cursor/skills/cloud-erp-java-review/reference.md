# High-Signal Review Checklist — Cloud ERP Backend

Apply only to lines/files in the current diff. Skip dimensions with no applicable changes.

## Module And Layering

Flag issues when the diff:

- Adds business logic to a Controller instead of Service.
- Places domain-private logic in `erp-common`.
- Adds Spring logic or dependencies to `erp-model-*`.
- Bypasses `erp-rpc-*` Feign contracts with direct HTTP calls.
- Changes API paths, return structures, field meaning, or error codes in a breaking way.

## Database And SQL

Flag as **严重** or **高** when applicable:

- Mapper XML uses `${...}` for fields, sorting, table names, or SQL fragments without a whitelist.
- Dynamic sorting lacks legal column and ASC/DESC enum validation.
- Paging relies only on frontend advanced query for mandatory business type filters; Service must inject the filter.
- Loop performs per-row `getById`, Mapper, Feign, or count queries where a batch query or `GROUP BY` should be used.
- Update lacks `version` optimistic-lock validation where existing patterns require it.
- Physical delete is introduced instead of `is_deleted` soft delete.
- Large table query introduces `NOT IN` where `NOT EXISTS` is safer.
- Large batch write is not chunked (normally batches of at most 500).
- PostgreSQL JSONB values are written without explicit `::jsonb` conversion where needed.

## Required Annotations

Flag missing or incorrect annotations when changed Controller/Service behavior requires them:

| Annotation | When |
|------------|------|
| `@LogAction` | Write operations where local pattern requires operation logs |
| `@DataPermission` | List, view, modify, delete, export, batch — correct `operationType`, `menuCode`, `tableField` |
| `@WebAdvanceQuery(handler = XxxQueryHandler.class)` | Advanced query endpoints |
| `@Idempotent` / `@DataIdempotent` | Duplicate-submit or MQ idempotency |
| `@DistributeLocker` | Concurrent writes on shared business keys |

## Transactions

Flag issues when:

- Single-service multi-table writes lack `@Transactional(rollbackFor = Exception.class)`.
- Cross-service write orchestration lacks `@GlobalTransactional` or explicit compensation design.
- A transactional method calls another method in the same class expecting transaction proxy behavior.
- A transaction contains slow Feign calls, external HTTP, MQ send, large loops, or file IO.

## Exceptions

Flag issues when:

- Business failures use generic `RuntimeException` or a new exception hierarchy instead of `ServiceException`.
- Feign failures do not use `FeignServiceException` or existing fallback conventions.
- Batch operations throw on first item instead of collecting `BatchResultDTO` where required.
- `catch` only logs and continues when the caller must know the operation failed.
- External error messages leak stack traces, SQL details, secrets, tokens, signatures, or internal URLs.

## RocketMQ

Flag issues when:

- Topic, tag, or consumer group strings are hardcoded instead of constants such as `RocketMqTopic`, `RocketMqNewTag`, or `RocketMqConsumerGroup`.
- Consumer lacks idempotency for repeat delivery.
- Retry/failure paths are missing for business-critical messages.
- Message fields lack null handling before use.

## Feign

Flag issues when:

- Diff bypasses `erp-rpc-*` Feign contracts.
- `ApiResult.getData()` is used without status/null checks where null is possible.
- Feign failure paths lack fallback, retry, or explicit error handling.

## Performance

Flag issues when:

- Pagination enrichment loads dictionaries, users, departments, shops, regions, or Feign data inside a row loop instead of batching.
- Low-frequency dictionaries are fetched every request without cache where local patterns already cache them.
- EasyExcel import/export lacks batch processing or maximum row/file limits.
- New code uses `new Thread()`, ad hoc executors, or unbounded queues instead of managed thread pools.

## Code Quality

Flag issues when:

- Enum values or business codes are hardcoded instead of existing enums/constants.
- Environment addresses, passwords, tokens, signatures, app keys, or third-party credentials appear in code or resources.
- Duplicate query handlers or service logic should clearly reuse a local helper/base class.
- Split composite fields without array bounds checks.
- Null business times silently default to current time when that changes semantics.

## Security

Flag issues when:

- Dynamic SQL, path matching, signing, token handling, file upload/download, XML parsing, SpEL, serialization, or webhook changes weaken boundaries.
- Sensitive values are logged.
- Interfaces can bypass `@DataPermission` and expose view/export/batch modification data.
- Config migration leaves secrets in repository resources or logs.
