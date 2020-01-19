import com.fasterxml.jackson.databind.JsonNode



JsonNode callbackJson = api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong())


Boolean ticketFlag = true
Boolean scheduleFlag = true
if(callbackJson != null ){
    for(callback in callbackJson){
        if(callback.get("url").asText().contains("app.mspbots.ai")){

         
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
    Map<String, String> ticketParam = Map.of("objectId", Integer.parseInt(params.get("objectId").asText()),
                                "type",params.get("ticket").asText(),
                                "level",params.get("level").asText())
   
                    
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",ticketParam)

}
if(scheduleFlag){
     Map<String, String> scheduleParam = Map.of("objectId", Integer.parseInt(params.get("objectId").asText()),
                                "type",params.get("schedule").asText(),
                                "level",params.get("level").asText())


   
                    
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",scheduleParam)

}

