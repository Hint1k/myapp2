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

**User actions available:** <br>

- Performing CRUD operations on accounts and transactions

**Additional info:**

- Account status (active / non-active) dictates linked transactions status (approved / suspended). <br>
- Deleted account freezes linked transactions. <br>
- Changed transaction amount updates linked account balances. <br>
- Deleted transactions refunds linked accounts. <br>
- Frozen & suspended transactions are read-only. <br>