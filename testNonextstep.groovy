List<String> TICKET_STATUS = Arrays.asList(
        "Completed (Emails Contact & CC)",
        "Canceled No Response (Emails Contact & CC)",
        "Client Canceled (Emails Contact & CC)",
        "Canceled (No Email)",
        "Closed (Canceled)",
        "Closed (MGR)",
        "Closed (FINAL)"
)

String status = "Completed (Emails Contact & CC)"

boolean contains = false
for (i in TICKET_STATUS) {
    contains = contains || status.toLowerCase().contains(i.toLowerCase())
}

println contains