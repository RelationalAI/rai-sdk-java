# Changelog

## v0.4.1-alpha
* Adding support to new java versions.
* Upgrading `apache arrow` version to `9.0.0`.

## v0.4.0-alpha
* Renamed:
  - `execute` to `executeV1`.
  - `executeAsyncWait` to `execute`.

## v0.3.0-alpha
* Added `CancelTransaction` feature.

## v0.2.2-alpha
* Fixed `getTransaction` missing required properties: [finished_at].
* Added `deleteTransaction`.

## v0.2.1-alpha
* Fixed `executeAsync` inputs issue.

## v0.2.0-alpha
* Added v2 predefined results formats:

  - `getTransactions` returns `TransactionsAsyncMultipleResponses`.
  - `getTransaction` returns `TransactionAsyncSingleResponse`.
  - `getTransactionResults` returns `List<ArrowRelation>`.
  - `getTransactionMetadata` returns `List<TransactionAsyncMetadataResponse>`.
  - `getTransactionProblems` returns `List<ClientProblem|IntegrityConstraintViolation>`.
  - `executeAsync` returns `TransactionAsyncResult`.

## v0.1.0-alpha
* Added support to the asynchronous protocol including:
    - `executeAsync`: runs an asynchronous request.
    - `executeAsyncWait`: runs an asynchronous request and wait of its completion.
    - `getTransaction`: gets information about transaction.
    - `getTransactions`: gets the list of transactions.
    - `getTransactionResults`: gets transaction execution results.
    - `getTransactionMetadata`: gets transaction metadata.
    - `getTransactionProblems`: gets transaction execution problems.
* Added `ExecuteAsyncTest` test class
