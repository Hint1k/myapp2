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
- CRUD operations on accounts, transactions, and customers <br>
- Registering new users with an existing customer number <br>

**Additional info:** <br>
- Customer and Transaction CRUD operations update linked accounts <br>
- Account CRUD operations update linked customer and transactions <br>

**User rights:** <br>
- Admin can perform all operations on all users <br>
- Manager can only view customers, accounts, and transactions <br>
- User can only view their own accounts, transactions, and customer info <br>

**To log in as admin or manager:** <br>
- Usernames: admin or manager <br>
- Password: 123 <br>

**New user registration process:** <br>
- Log in as admin <br> 
- Create a new customer. Or find a customer number of an existing customer <br>
- Register a new user with the customer number <br>
- Log in using the new username and password <br>