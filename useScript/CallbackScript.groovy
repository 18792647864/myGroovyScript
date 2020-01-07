import com.fasterxml.jackson.databind.JsonNode


JsonNode callbackJson = api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",param)
bool flag = true
if(callbackJson != null ){
    for(callback in callbackJson){
        if(callback.get("url").asText().contains("app.mspbots.ai")){
            flag = false
            println "callbacks has contains app.mspbots.ai  tenantId:::"+payload.get("id").asLong()
            break
        }

    }
}

if(flag){
    Map<String, String> param = Map.of("objectId", 1,
                                "type","ticket",
                                "level","owner")
                    
    api.call("mspbots-wise", "/callbacks/" + payload.get("id").asLong(), "post",param)

}
    



    
