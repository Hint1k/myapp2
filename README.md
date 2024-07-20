**Project plan:**<br>

- Banking app <br>
- Microservice architecture <br>
- Event-driven approach <br>
- Maximum microservices decoupling (no shared libraries, separate databases)
- Multi-threading and asynchronous programming <br>
- Secure API Gateway <br>

**The current state of the app:**<br>

- Web-service - serves web pages <br>
- Account-service - handles account operations and account-database <br>
- Transaction-service - handles transaction operations and transaction-database <br>

**User actions available (1):** <br>

- Show all accounts / transactions in database <br>
- Add new account / transaction to database <br>
- Delete an account (2) / transaction from database (3) <br>
- Update an account / transaction in database (4) <br>
- Show account / transaction details <br>
- Show all transactions belong to an account <br>

(1) Handled most common users errors. <br>
(2) Deleting an account freezes all linked transactions. Frozen transactions can't be updated or deleted. <br>
(3) Deleting a transaction refunds the money to the linked accounts. <br>
(4) Changing a transaction amount affects the balances of the linked accounts. <br>