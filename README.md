**Project plan:**<br>

- Banking app <br>
- Microservice architecture <br>
- Event-driven approach <br>
- Maximum microservices decoupling (no shared libraries, separate databases)
- Multi-threading and asynchronous programming <br>
- Secure API Gateway <br>

**The current state of the app:**<br>

- Web-service - serves web pages <br>
- Account-service - handles account CRUD operations and account-database <br>
- Transaction-service - handles transaction CRUD operations and transaction-database <br>
- Customer-service - handles customer CRUD operations and customer-database <br>

**User actions available:** <br>

- Performing CRUD operations on accounts, transactions and customers.

**Additional info:**
- Customer and Transaction CRUD operations update linked accounts. <br>
- Account CRUD operations update linked customer and transactions. <br>
