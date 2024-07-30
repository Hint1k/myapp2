document.addEventListener('DOMContentLoaded', function() {
    const transactionRows = document.querySelectorAll('tbody tr');

    transactionRows.forEach(row => {
        const amountCell = row.querySelector('td:nth-child(2)');
        const typeCell = row.dataset.transactionType;

        if (amountCell && window.location.pathname.endsWith('/all-transactions')) {
            amountCell.textContent = amountCell.textContent.trim();
        } else {
            let prefix = '';
            const isTransfer = typeCell === 'TRANSFER';
            const sourceCell = row.querySelector('td:nth-child(4)').textContent.trim();
            const destinationCell = row.querySelector('td:nth-child(5)').textContent.trim();

            if (isTransfer) {
                prefix = sourceCell === destinationCell ? '' :
                    (sourceCell === window.location.pathname.split('/').pop() ? '-' : '+');
            } else if (typeCell === 'DEPOSIT') {
                prefix = '+';
            } else if (typeCell === 'WITHDRAWAL') {
                prefix = '-';
            } else {
                console.warn("Unknown transaction type:", typeCell);
            }

            amountCell.textContent = prefix + amountCell.textContent.trim();
        }
    });
});