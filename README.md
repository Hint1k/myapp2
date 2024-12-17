**Project plan:** <br>

- Banking app <br>
- Microservice architecture <br>
- Mostly event-driven approach <br>
- Synchronous communication for authorization & authentication <br>
- High microservices decoupling (no shared libraries, separate databases) <br>
- Secure API Gateway <br>
- JWT Token-Based Authentication <br>

**The current state of the app:** <br>

- Web-service - serves web pages <br>
- Account-service - handles account CRUD operations and account-database <br>
- Transaction-service - handles transaction CRUD operations and transaction-database <br>
- Customer-service - handles customer CRUD operations and customer-database <br>
- Gateway-service - handles user authentication / authorization process and user-database <br>

**User actions available:** <br>

- Performing CRUD operations on accounts, transactions and customers. <br>

**Additional info:** <br>

- Customer and Transaction CRUD operations update linked accounts. <br>
- Account CRUD operations update linked customer and transactions. <br>

**To log in, use the following credentials:** <br>

- Usernames: admin or manager <br>
- Password: 123 <br>