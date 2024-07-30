function toggleDestinationField() {
    var transactionType = document.getElementById("transactionType").value;
    var destinationField = document.getElementById("destinationField");
    var accountDestinationNumber = document.getElementById("accountDestinationNumber");

    if (transactionType === "TRANSFER") {
        destinationField.style.display = "";
        accountDestinationNumber.disabled = false;
    } else {
        destinationField.style.display = "none";
        accountDestinationNumber.disabled = true;
        accountDestinationNumber.value = ""; // Reset value for a non-transfer type of transaction
    }
}

document.addEventListener('change', function(event) {
    if (event.target && event.target.id === "transactionType") {
        toggleDestinationField();
    }
});

window.onload = function() {
    toggleDestinationField();
};