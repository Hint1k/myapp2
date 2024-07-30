document.addEventListener('DOMContentLoaded', function () {
    var rows = document.querySelectorAll('tr[data-status]');

    rows.forEach(function (row) {
        var status = row.getAttribute('data-status');

        switch (status) {
            case 'FROZEN':
                row.classList.add('frozen-transaction');
                break;
            case 'SUSPENDED':
                row.classList.add('suspended-transaction');
                break;
            default:
        }

        // Check status and disable buttons only for frozen/suspended
        var updateButton = row.querySelector('.update-button');
        var deleteButton = row.querySelector('.delete-button');

        if (status === 'FROZEN' || status === 'SUSPENDED') {
            if (updateButton) updateButton.disabled = true;
            if (deleteButton) deleteButton.disabled = true;
        }
    });
});