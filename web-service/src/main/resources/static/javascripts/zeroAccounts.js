// document.addEventListener('DOMContentLoaded', () => {
//     const customerRows = document.querySelectorAll('tbody tr');
//     for (const row of customerRows) {
//         const accountNumbersCell = row.querySelector('td:nth-child(4)');
//         const accountNumbersText = accountNumbersCell.textContent.trim();
//         if (accountNumbersText === "" || accountNumbersText === "0" || accountNumbersText === "[]") {
//             row.classList.add('zero-accounts');
//         }
//     }
// });

document.addEventListener('DOMContentLoaded', () => {
    const customerRows = document.querySelectorAll('tbody tr');
    for (const row of customerRows) {
        const accountNumbersCell = row.querySelector('td:nth-child(4)');  // Select the 4th cell (accountNumbers)
        const accountNumbersText = accountNumbersCell.textContent.trim();
        if (accountNumbersText === "" || accountNumbersText === "0" || accountNumbersText === "[]") {
            accountNumbersCell.classList.add('zero-accounts'); // Add class only to the cell
        }
    }
});