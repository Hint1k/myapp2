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
- (Optional step) Log in as an admin and create a new customer to generate a unique customer number <br> 
- On the login page, select the option to register a new user <br> 
- Complete the registration and link the new user to an existing customer using a valid customer number <br>
  - You can link one of the default customer numbers: 1, 2 or 3 <br>
  - Alternatively, link the customer number you created as an admin <br>
- After completing the registration process, log in with your newly created username and password <br>

**Installation info:** <br>
- Download and unzip the application <br>
- Navigate to the project folder where docker-compose.yml is located <br>
- Run the console command: docker-compose up --build <br>
- Wait for the process to complete (this may take a few minutes depending on your system and network speed) <br>
- Once completed, the following link will appear in the console: http://localhost:8080 <br>
- Open the link in your browser to access the application <br>