import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.mspbots.common.StringKit

if (payload.get("timeStart")==null || payload.get("timeEnd")==null || "null".equals(payload.get("timeStart")) || "null".equals(payload.get("timeEnd"))) {
  return false
}



Map<String, Object> entryParams = Map.of(
  "tenantId", user.get("tenantId").asLong(),
  "tenantUserId", user.get("tenantUserId").asLong(),
  "enteredDateStart", LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
)

JsonNode userTimeEntryList = api.call("mapbots-core","/tenant/user-time-entry/tenant-user-list",entryParams)
if(userTimeEntryList != null){
  boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
  for(i in userTimeEntryList){
    if ("null".equals(i.get("endDate").asText()) || "null".equals(i.get("beginDate").asText())) {
      continue
    }
    if(i.get("cwTid").equals(payload.get("id")) ||
       ignoreSeconds(ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime()).isAfter(ignoreSeconds(ZonedDateTime.parse(i.get("endDate").asText()).toLocalDateTime())) ||
       ignoreSeconds(ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime()).equals(ignoreSeconds(ZonedDateTime.parse(i.get("endDate").asText()).toLocalDateTime())) ||
       ignoreSeconds(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime()).isBefore(ignoreSeconds(ZonedDateTime.parse(i.get("beginDate").asText()).toLocalDateTime()))||
       ignoreSeconds(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime()).equals(ignoreSeconds(ZonedDateTime.parse(i.get("beginDate").asText()).toLocalDateTime()))){
      continue
    }else {
      overlappedEntry = i
      Map<String,Object> ruleParams = Map.of("tenantId",user.get("tenantUserId").asLong(),"timeEntryId",i.get("cwTid"),"ruleId",event.getId())
      if(Integer.parseInt(api.call("mapbots-core","/teams/messages/countMessagesByForRule",ruleParams).asText()) == 0){
        Integer escalationTime = 0;
        String message = params.get("message").asText()
        String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
        message = message.replace("{USER}",user.get("firstName").asText())     
        message = message.replace("{CWTID}","<a href='https://{CWSITE}/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid="+payload.get("id").asText()+"'>"+payload.get("id").asText()+"</a>")
        message = message.replace("{TIME_START}",StringKit.color(ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()))
        message = message.replace("{TIME_END}",StringKit.color(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()))
        message = message.replace("{CWTID2}","<a href='https://{CWSITE}/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid="+i.get("cwTid").asText()+"'>"+i.get("cwTid").asText()+"</a>")
        message = message.replace("{CalendarLink}","<a href='https://{CWSITE}/v4_6_release/connectwise.aspx?fullscreen=false&locale=en_US#startscreen=my_calendar'>[CalendarLink]</a>")
        message = message.replace("{TrainingURL}","<a href='https://wiki.jexet.com/pages/viewpage.action?pageId=146277979'>[TrainingURL]</a>")
        message = message.replace("{CWSITE}", tenant.get("mspbots.sync.wise.site").asText())
        message = message.replace("{TIME_START_2}",StringKit.color(ZonedDateTime.parse(i.get("beginDate").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()))
        message = message.replace("{TIME_END_2}",StringKit.color(ZonedDateTime.parse(i.get("endDate").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()))
        message = message + "<br> --"
              
        if(escalationOn){
          Map<String, String> countParam = Map.of(
            "tenantId", user.get("tenantId").asLong(), 
            "userId", user.get("tenantUserId").asLong(), 
            "ruleId", event.getId(), 
            "escalation", escalationOn
          )
          JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)
          escalationTime = Integer.parseInt(times.asText())
          String escalationTimeStr = (escalationTime+1) + (escalationTime > 1 ? " times" : " time")
          message =message+"[ <span style='color:#999d9c;'>"+escalationTimeStr+"</span> this week. Threshold <span style='color:#999d9c;'>"+params.get("times").asText().replace(",","-")+"</span> ]"
        }
        message = message + "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"
              
        Map<String, String> param = Map.of(
          "teamsUserId", user.get("teamsUserId"),
          "tenantUserId",user.get("tenantUserId").asLong(),
          "tenantId", user.get("tenantId").asLong(),
          "ruleId", event.getId(),
          "businessId",payload.get("id").asLong(),
          "businessType",event.getScope(),
          "send", params.get("send").asText(),
          "escalation", escalationOn,
          "message",message
        )
        JsonNode sendResult = api.call("mspbots-teams", "/message/send","post",param)
        if(!sendResult.isEmpty() && escalationOn){
          Map<String, Object> escalation = Map.of(
            "tenantId",user.get("tenantId").asLong(),
            "ruleId",event.getId(),
            "triggerName", "Overlapped",
            "times",params.get("times").asText(),
            "tenantUserId",user.get("tenantUserId").asLong()
          )
          api.call("mspbots-teams","/escalation/check","post",escalation)
        }
        break       
      }
    }
  }
}

LocalDateTime ignoreSeconds(LocalDateTime dateTime) {
    return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(dateTime.getHour(), dateTime.getMinute(), 0));
}
