//Scope   :  NextTicket
//trigger_event   :  NextTicket

// If Ticket_service_board= "xxxx" and user = "a" or "b" then add "Y" points


println payload
println user

//获取Ticket_service_board
Map<String, String> boardParam = Map.of()
JsonNode board = api.call("mspbots-core","/icket_service_board",boardParam)
if(board.get("name").asText().equalsIgnoreCase("xxxx")){
    if(user.get("tenantUserId").asLong() == a.id || user.get("tenantUserId").asLong() == b.id ){
        Map<String, String> pointsParam = Map.of()
        api.call("mspbots-core","/addPoints",pointsParam)

    }

}