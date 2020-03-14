import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

LocalTime endTime = LocalTime.parse(payload.get("endTime").asText() + ":00")

if(endTime != null){
    LocalDateTime workDateTime = LocalDateTime.of(LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).toLocalDate(), endTime)
    LocalDateTime workDateTimeUTC = workDateTime.minusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
    Integer mins = 15
    if(LocalDateTime.now().isAfter(workDateTimeUTC.minusMinutes(mins))
            || LocalDateTime.now().isBefore(workDateTimeUTC)){
        Map<String, Object> params = Map.of("tenantId", user.get("tenantId").asLong(),
                "userId", user.get("tenantUserId").asLong(), "type", "OUT")
        JsonNode attendance = api.call("mapbots-core","/attendances/last",params)
        if(attendance == null || !in12Hour(attendance)){
            Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                    "tenantUserId",user.get("tenantUserId").asLong(),
                    "tenantId", user.get("tenantId").asLong(),
                    "frequency","day",
                    "ruleId", event.getId(),
                    "businessType", event.getScope(),
                    "message","Dear "+ user.get("firstName").asText()
                    +", you’re almost done for today"
                    +". You’re scheduled to finish the day at " +endTime+ " " +user.get("tzStr").asText()
                    +". Please check your pending tasks. If you have none and are ready to head out"
                    +", please type \"Out\" or \"EOD\", and MSPbots will do an end of day check for you and log you out."
                    +"<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
            return api.call("mspbots-teams", "/message/send", "post",param)
        }
    }
}

boolean in12Hour(JsonNode attendanceDTO) {
    return LocalDateTime.now().minusHours(12).isBefore(ZonedDateTime.parse(attendanceDTO.get("logTime").asText()).toLocalDateTime().minusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))))
}
