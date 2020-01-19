import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

Map<String,Object> params = Map.of(
  "tenantId", user.get("tenantId").asLong(),
  "userId", user.get("tenantUserId").asLong()
)

JsonNode attendanceDTO = api.call("mapbots-core","/attendances/last",params)
Integer time = 15
if (attendance == null && "LUNCH".equalsIgnoreCase(attendanceDTO.get("type").asText())) {
  ZonedDateTime logTime = ZonedDateTime.parse(attendance.get("logTime").asText())
  long durationTime = Duration.between(TimeZoneUtils.utcToZone(logTime.toLocalDateTime(), user.get("tz").asText()), zonedTimeNow).toMinutes() 
  println durationTime
  if(durationHours > time && durationHours<120){
    String timeFormat = zonedTimeNow.toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
    Map<String, String> param = Map.of(
      "teamsUserId", user.get("teamsUserId"),
      "tenantUserId",user.get("tenantUserId").asLong(),
      "tenantId", user.get("tenantId").asLong(),
      "frequency","hours",
      "hours",12,
      "bussinessId", payload.get("configId").asLong(),
      "bussinessType", event.getScope(),
      "ruleId", event.getId(),
      "message","Dear " + user.get("firstName").asText()
      +", your lunch time is now more than "+StringKit.color(String.valueOf(time))+" minutes"
      +". We just want to make sure that you didn't forget to log back in"
      +". If you already finished your lunch, please type \"Back\" to change your status."
      +"<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"
    )
    return api.call("mspbots-teams", "/message/send", "post",param)
  }
}