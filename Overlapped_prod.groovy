import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

if (payload.get("timeStart")==null || payload.get("timeEnd")==null || "null".equals(payload.get("timeStart")) || "null".equals(payload.get("timeEnd"))) {
  return false
}


Map<String, String> countParam = Map.of("tenantId",user.get("tenantId").asLong(),"userId",user.get("tenantUserId").asLong(),"ruleId",event.getId(),"escalation",false)


JsonNode times = api.call("mspbots-core","/teams/messages/countEscalation",countParam)


Map<String, Object> entryParams = Map.of("tenantId", user.get("tenantId").asLong(),
        "tenantUserId", user.get("tenantUserId").asLong(),
        "enteredDateStart", LocalDateTime.of(LocalDate.now(), LocalTime.MIN))

JsonNode userTimeEntryList = api.call("mapbots-core","/tenant/user-time-entry/tenant-user-list",entryParams)
if(userTimeEntryList != null){
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
            println ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
            println ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
            println ZonedDateTime.parse(i.get("beginDate").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
            println ZonedDateTime.parse(i.get("endDate").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
            overlappedEntry = i
            Map<String,Object> params = Map.of("tenantId",user.get("tenantUserId").asLong(),"timeEntryId",i.get("cwTid"),"ruleId",event.getId())
            if(Integer.parseInt(api.call("mapbots-core","/teams/messages/countMessagesByForRule",params).asText()) == 0){
                Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                        "tenantUserId",user.get("tenantUserId").asLong(),
                        "tenantId", user.get("tenantId").asLong(),
                        "ruleId", event.getId(),
                        "businessId",payload.get("id").asLong(),
                        "businessType",event.getScope(),
                        "send","false",
                        "message","Dear "+ user.get("firstName").asText() +
                        ", we just noticed that you have logged a time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" +" from <span style = 'color:#FF0000;font-weight:bold'>" +ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+ "</span> to <span style = 'color:#FF0000;font-weight:bold'>" +ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+ "</span> that overlaps with other time logs <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + i.get("cwTid") +"'>" + i.get("cwTid") + "</a>" +" from <span style = 'color:#FF0000;font-weight:bold'>" +ZonedDateTime.parse(i.get("beginDate").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+ "</span> to <span style = 'color:#FF0000;font-weight:bold'>"+ ZonedDateTime.parse(i.get("endDate").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
                        +"</span>. Please go to <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/connectwise.aspx?fullscreen=false&locale=en_US#startscreen=my_calendar'>[CalendarLink]</a> to fix it. If you need more information, please click <a href='https://wiki.jexet.com/pages/viewpage.action?pageId=146277979'>[TrainingURL]</a> to understand why we avoid overlapping entries.Thanks!"
                        + "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText())+1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>"+ "3-6-9" +"</span> ]"
                        + "<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
                api.call("mspbots-teams", "/message/send", "post",param)
                Map<String, Object> escalation = Map.of("tenantId",user.get("tenantId").asLong(),"ruleId",event.getId(),"triggerName","Overlapped","times","3,6,9","tenantUserId",user.get("tenantUserId").asLong())
                return api.call("mspbots-teams","/escalation/check","post",escalation)
            }
        }
    }
}

LocalDateTime ignoreSeconds(LocalDateTime dateTime) {
    return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(dateTime.getHour(), dateTime.getMinute(), 0));
}

