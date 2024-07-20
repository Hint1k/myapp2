document.addEventListener('DOMContentLoaded', function () {
    var rows = document.querySelectorAll('tr[data-status]');

    rows.forEach(function (row) {
        var status = row.getAttribute('data-status');

        if (status === 'FROZEN') {
            var updateButton = row.querySelector('.update-button');
            var deleteButton = row.querySelector('.delete-button');

            if (updateButton) updateButton.style.display = 'none';
            if (deleteButton) deleteButton.style.display = 'none';

            row.classList.add('frozen-transaction');
        }
    });
});