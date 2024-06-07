**Project plan:**<br>
- Banking app <br>
- Microservice architecture <br>
- Event-driven approach <br>
- Maximum microservices decoupling (no shared libraries)
- Multi-threading and asynchronous programming <br>
- Secure API Gateway <br>

**The current state of the app:**<br>
- Web-service - serves web pages <br> 
- Account-service - handles account operations and account-database <br>
- Transaction-service - handles transactions and transaction-database <br>

**User actions available:**<br> 

- Show all accounts in database <br>
- Add new account to database <br>
- Delete an account from database <br>
- Update an account in database <br>
- Show account details (by default only account with id = 1 exists) <br>
- (Automatic action) Transaction-service adds 1000 to any newly created account <br>