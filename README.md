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
(simple validation & error handling, not error proof yet):<br> 

- Show all accounts / transactions in database <br>
- Add new account / transaction to database <br>
- Delete an account / transaction from database <br>
- Update an account / transaction in database <br>
- Show account / transaction details (by default only one account / transaction with id = 1 exists) <br>
- Show all transactions belong to an accountNumber (by default only 1 transaction belong to 1 account) <br>