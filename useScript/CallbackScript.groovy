import com.fasterxml.jackson.databind.JsonNode


JsonNode callbackJson = api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "get")
bool ticketFlag = true
bool scheduleFlag = true
if(callbackJson != null ){
    for(callback in callbackJson){
        if(callback.get("url").asText().contains("app.mspbots.ai")){
           
            println "callbacks has contains app.mspbots.ai  tenantId:::"+payload.get("id").asLong()

            if(callback.get("type").asText().equalsIgnoreCase("ticket")){
                 ticketFlag = false
            }
            if(callback.get("type").asText().equalsIgnoreCase("Schedule")){
                 scheduleFlag = false
            }
            
        }

    }
}

if(ticketFlag){
    Map<String, String> param = Map.of("objectId", 1,
                                "type","ticket",
                                "level","owner")
                    
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",param)

}
if(scheduleFlag){
     Map<String, String> param = Map.of("objectId", 1,
                                "type","Schedule",
                                "level","owner")
                    
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",param)

}

   
    



    
