import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

Map<String, Object> paramBOTOUT = Map.of("tenantId", user.get("tenantId").asLong(),
        "userId", user.get("tenantUserId").asLong(), "type", "BOTOUT")
JsonNode attendanceDTOBOTOUT = api.call("mapbots-core","/attendances/last",paramBOTOUT)


Map<String, Object> paramOUT = Map.of("tenantId", user.get("tenantId").asLong(),
        "userId", user.get("tenantUserId").asLong(), "type", "OUT")
JsonNode attendanceDTOUT = api.call("mapbots-core","/attendances/last",paramOUT)

Map<String, Object> paramIN = Map.of("tenantId", user.get("tenantId").asLong(),
        "userId", user.get("tenantUserId").asLong(), "type", "IN")
JsonNode attendanceDTOIN = api.call("mapbots-core","/attendances/last",paramIN)

Integer Hours = 15


LocalDateTime LocalDateTimeNow = LocalDateTime.now().minusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))

if(attendanceDTOIN != null){
    if(attendanceDTOBOTOUT == null && attendanceDTOUT == null){
        if(logTimeUTC(attendanceDTOIN).isBefore(LocalDateTimeNow.minusHours(Hours))){
            Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                    "tenantUserId",user.get("tenantUserId").asLong(),
                    "tenantId", user.get("tenantId").asLong(),
		   "frequency","day",
           "bussinessId", payload.get("configId").asLong(),
           "bussinessType", event.getScope(),
                    "ruleId", event.getId(),
                    "message","Dear " + user.get("firstName").asText()
                    +", you started the day 15 hours ago, but you haven't logged out yet"
                    +". I assume that you just forgot, so I have done it for you. You can type \"EOD at [time]\" to correct the time entry."
                    +"<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
            return api.call("mspbots-teams", "/message/send", "post",param)
        }
    }
}

if(logTimeUTC(attendanceDTOIN).isBefore(LocalDateTimeNow.minusHours(Hours))){
    Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
	        "frequency","day",
             "bussinessId", payload.get("configId").asLong(),
             "bussinessType", event.getScope(),
            "ruleId", event.getId(),
            "message","Dear " + user.get("firstName").asText()
            +", you started the day 15 hours ago, but you haven't logged out yet"
            +". I assume that you just forgot, so I have done it for you. You can type \"EOD at [time]\" to correct the time entry."
            +"<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
    return api.call("mspbots-teams", "/message/send", "post",param)
}

LocalDateTime logTimeUTC(JsonNode attendanceDTO) {
    return ZonedDateTime.parse(attendanceDTO.get("logTime").asText()).toLocalDateTime().minusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
}