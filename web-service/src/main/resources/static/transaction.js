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
        accountDestinationNumber.value = 0; // Reset value to 0 for a non-transfer type of transaction
    }
}