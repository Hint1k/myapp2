**Application Overview::** <br>
- Banking web application with a microservice architecture <br>
- Primarily event-driven approach with synchronous communication for authorization & authentication <br>
- High microservices decoupling (no shared libraries, separate databases, but shared cache) <br>
- JWT Token-Based Authentication <br>

**Microservices:** <br>
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
- Create a new customer or use one of the default customers (customer numbers: 1, 2, or 3) <br>
- Register a new user and link them to the customer using the customer number <br>
- Log in with the new username and password <br>