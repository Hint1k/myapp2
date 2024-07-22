document.addEventListener("DOMContentLoaded", () => {
    if (window.location.pathname.startsWith('/api/transactions/all-transactions/') &&
        window.location.pathname !== '/api/transactions/all-transactions') {

        // Debugging: Ensure the script is running
        console.log("Withdrawal Sign Script running on account-specific transactions page");

        // Select all rows in the table
        const transactionRows = document.querySelectorAll('tr[th\\:each]');

        transactionRows.forEach(row => {
            // Find the amount and transaction type cells
            const amountCell = row.querySelector('td:nth-child(2)');
            const transactionTypeCell = row.querySelector('td:nth-child(3)');

            if (amountCell && transactionTypeCell) {
                // Get text content from cells
                const amountValue = amountCell.textContent.trim();
                const transactionType = transactionTypeCell.textContent.trim();

                // Debugging: Print the values obtained
                console.log(`Amount Cell: ${amountValue}, Transaction Type Cell: ${transactionType}`);

                // Check if the transaction is a withdrawal and format accordingly
                if (transactionType === 'Withdrawal') {
                    amountCell.textContent = '-' + amountValue;
                }

                // Debugging: Confirm the update
                console.log(`Updated Amount Cell: ${amountCell.textContent}`);
            } else {
                console.warn("Amount cell or Transaction Type cell not found");
            }
        });
    }
});