import com.fasterxml.jackson.databind.JsonNode


println "Callback_params:"
println params

JsonNode callbackJson = api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "get")
bool ticketFlag = true
bool scheduleFlag = true
if(callbackJson != null ){
    for(callback in callbackJson){
        if(callback.get("url").asText().contains("app.mspbots.ai")){
           
            println "callbacks has contains app.mspbots.ai  tenantId:::"+payload.get("id").asLong()

            if(callback.get("type").asText().equalsIgnoreCase(params.get("ticket").asText())){
                 ticketFlag = false
            }
            if(callback.get("type").asText().equalsIgnoreCase(params.get("schedule").asText())){
                 scheduleFlag = false
            }
            
        }

    }
}

if(ticketFlag){
    Map<String, String> param = Map.of("objectId", Integer.parseInt(params.get("objectId").asText()),
                                "type",params.get("ticket").asText(),
                                "level",params.get("level").asText())
                    
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",param)

}
if(scheduleFlag){
     Map<String, String> param = Map.of("objectId", Integer.parseInt(params.get("objectId").asText()),
                                "type",params.get("schedule").asText(),
                                "level",params.get("level").asText())
                    
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",param)

}


println "Callback_end:"

   
    



    
