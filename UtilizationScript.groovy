import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.HashMap;

Map<String, String> param = Map.of("tenantId", payload.get("tenantId").asLong(),
                                   "tenantUserId",payload.get("tenantUserId").asLong(),
                                   "enteredDateStart",payload.get("enteredDateStart").asText(),
                                   "enteredDateEnd",payload.get("enteredDateEnd").asText())


println "payload"
println payload

println "user"
println user

JsonNode timeEntryJson = api.call("mspbots-core", "/tenant/user-time-entry/tenant-user-list", "get",param)

Map<Long, String> ticketStatus = new HashMap<Long, String>()

println "timeEntryJson"
println timeEntryJson

double sumHour = 0
for(int i = 0;i < timeEntryJson.size();i++){

    println "i:"+i


    JsonNode chargeToId = timeEntryJson.get(i).get("chargeToId")
    println "chargeToId"
    println chargeToId

    if(chargeToId != null){
        String status
        if(!ticketStatus.containsKey(chargeToId.asLong())){
            println "chargeToId"
            println timeEntryJson.get(i).get("chargeToId").asLong()
            Map<String, String> ticketParam = Map.of("cwId", chargeToId.asLong())
            JsonNode ticketJson = api.call("mspbots-core", "/tickets/selectbycwId", "get",ticketParam)
            if(ticketJson != null){

                println "ticketJson"
                println ticketJson
                status = ticketJson.get("contactName").asText()
                println "111111"
                ticketStatus.put(chargeToId.asLong(),status)
                println "2222222"
            }
           
        }else{
            status = ticketStatus.get(chargeToId)

            println "3333333"
        }
        if(status != null){
             if(status.equalsIgnoreCase("Admin") || status.equalsIgnoreCase("Break")){
                continue
                println "44444"
            }
        }

       
    }

    println "sumHour"
    println sumHour
    println timeEntryJson.get(i)
    println timeEntryJson.get(i).get("actualHours").asLong()
    println timeEntryJson.get(i).get("actualHours").asText()
    sumHour  = sumHour + timeEntryJson.get(i).get("actualHours").asDouble()

    println "55555555"

}


Map<String, String> utilizationParam = Map.of("tenantId", payload.get("tenantId").asLong(),
                                   "userId",user.get("tenantUserId").asLong(),
                                   "percent",sumHour/8)

    println "666666"
api.call("mspbots-core", "/utilization", "post",utilizationParam)


    println "done"

