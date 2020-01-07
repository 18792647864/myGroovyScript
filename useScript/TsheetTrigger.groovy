
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.ZonedDateTime

println "payload"
println payload

println "userInfo"
println user

println "eventName"
println event


def startTime = payload.get("startTime").asText().substring(0,19);
println "startTime"
println startTime

Map<String, String> param = Map.of("tenantId", payload.get("tenantId").asLong(),
                                   "startTime",startTime)

JsonNode attendancesjson = api.call("mspbots-core", "/attendances/last", "get",param)


Map<String, String> saveParam

if(attendancesjson == null ){ 

    println "attendancesjson == null "

    // save()
    saveParam = Map.of("tenantId", payload.get("tenantId").asLong(),
                                   "startTime",startTime,
                                   "logTime",startTime,
                                    "tz",user.get("tz"),
                                   "tzStr",user.get("tzStr"),
                                   "fromType",1,
                                    "teamsId",user.get("teamsUserId"),
                                   "userId",payload.get("sheetUserId").asLong(),
                                   "id",payload.get("id").asLong(),
                                   "type",1)

}else{

    if(!BODin12Hour(attendancesjson)){
        println "!BODin12Hour "
        //save()
        saveParam = Map.of("tenantId", payload.get("tenantId").asLong(),
       
                                   "startTime",startTime,
                                   "logTime",startTime,
                                   "tz",user.get("tz"),
                                   "tzStr",user.get("tzStr"),
                                   "fromType",1,
                                   "teamsId",user.get("teamsUserId"),
                                   "userId",payload.get("sheetUserId").asLong(),
                                   "id",payload.get("id").asLong(),
                                   "type",1)
    }else{

        if(payload.get("fromtype").asLong!=1){
            println "payload.get("fromtype").asLong!=1 "
             // save()
            saveParam = Map.of("tenantId", payload.get("tenantId").asLong(),
                                "startTime",startTime,
                                "logTime",startTime,
                                "tz",user.get("tz"),
                                "tzStr",user.get("tzStr"),
                                "fromType",1,
                                "teamsId",user.get("teamsUserId"),
                                "userId",payload.get("sheetUserId").asLong(),
                                "id",payload.get("id").asLong(),
                                "type",1)
        
        }else{
            //update
            println "update"
             saveParam = Map.of(
                                "startTime",startTime,
                                "logTime",startTime,
                                "teamsId",user.get("teamsUserId"),
                                "type",1
                                ) 

        }
    }
}


println "saveParam"
println saveParam

api.call("mspbots-core", "/attendances/save", "post",saveParam)



Map<String, String> messageParam = Map.of("teamsUserId", user.get("teamsUserId").asText(),
				"tenantUserId",user.get("tenantUserId").asLong(),
				"tenantId", user.get("tenantId").asLong(),
				"frequency","once",
				"ruleId", event.getId(),
				"message", "Dear "+user.get("firstName").asText()
                     +"you have already logged in on MSPBots but we just noticed that you also logged in on T-sheet at[ " + startTime + 
				 ".] We have updated your clock in time as [ " + startTime + "] for you. In the future, you don’t need to log in from MSPbots anymore. Have a nice day. Thanks! ");
		
return api.call("mspbots-teams", "/message/send", "post",messageParam)


boolean BODin12Hour(JsonNode attendanceDTO) {
    return LocalDateTime.now().minusHours(12).isBefore(ZonedDateTime.parse(attendanceDTO.get("logTime").asText()).toLocalDateTime().minusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))))
}


println "Tsheet Script has done"
