document.addEventListener('DOMContentLoaded', function () {
    var rows = document.querySelectorAll('tr[data-status]');

    rows.forEach(function (row) {
        var status = row.getAttribute('data-status');

        if (status !== 'ACTIVE') {
            row.classList.add('frozen-account');
        }
    });
});