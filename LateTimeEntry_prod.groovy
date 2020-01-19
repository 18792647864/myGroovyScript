import java.time.ZonedDateTime
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.fasterxml.jackson.databind.JsonNode

Map<String, String> countParam = Map.of("tenantId",user.get("tenantId").asLong(),"userId",user.get("tenantUserId").asLong(),"ruleId",event.getId(),"escalation",false)


JsonNode times = api.call("mspbots-core","/teams/messages/countEscalation",countParam)

if (payload.get("timeEnd")==null || payload.get("dateEntered")==null || payload.get("workType").get("name")==null || user.get("firstName")==null || user.get("userName")==null || payload.get("id")==null || user.get("tz")==null || user.get("tzStr")==null || tenant.get("mspbots.sync.wise.site")==null) {
  return false
}
  


Duration between = Duration.between(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime(),ZonedDateTime.parse(payload.get("dateEntered").asText()).toLocalDateTime())

String timeStr = ""

if (between.toDays() > 0) {
    if (between.toDays() == 1) {
        timeStr = between.toDays() + " day "
    } else {
        timeStr = between.toDays() + " days "
    }

}

if (between.toHoursPart() > 0) {
    if (between.toHoursPart() == 1) {
        timeStr += between.toHoursPart() + " hour "
    } else {
        timeStr += between.toHoursPart() + " hours "
    }

}

if (between.toMinutesPart() > 0) {
    if (between.toMinutesPart() == 1) {
        timeStr += between.toMinutesPart() + " min "
    } else {
        timeStr += between.toMinutesPart() + " mins "
    }
}


if ("onsite".equalsIgnoreCase(payload.get("workType").get("name").asText())
        || payload.get("workType").get("name").asText().contains("Delivery")
        || "travel".equalsIgnoreCase(payload.get("workType").get("name").asText())) {
  println "LateTimeEntry======================2========================"
  
    if (ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(2).isBefore(ZonedDateTime.parse(payload.get("dateEntered").asText()).toLocalDateTime())) {
        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId",user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
                "ruleId", event.getId(),
                "businessId",payload.get("id").asLong(),
                "businessType",event.getScope(),
                "message", "Dear " + user.get("firstName").asText() + ", you just created the time entry " + payload.get("workType").get("name").asText() + " <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + " <span style = 'color:#FF0000;font-weight:bold'>" + timeStr + "</span> after the time entry happened." +
                " To help you comply with company policy, please record it in real time, meaning no later than <span style = 'color:#FF0000;font-weight:bold'>2</span> hours after your end time. Thanks!" + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
      println "LateTimeEntry======================3========================"
        return api.call("mspbots-teams", "/message/send","post",param)
    }
}else if (ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusMinutes(6).isBefore(ZonedDateTime.parse(payload.get("dateEntered").asText()).toLocalDateTime())) {
    println "LateTimeEntry======================3========================"
    Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "frequency","once",
            "send","true",
            "ruleId", event.getId(),
            "businessId",payload.get("id").asLong(),
            "businessType",event.getScope(),
            "message", "Dear " + user.get("firstName").asText() + ", you just created the time entry "  + " <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + " <span style = 'color:#FF0000;font-weight:bold'>" + timeStr + "</span> after the time entry happened." +
            " To help you comply with company policy, please record it in real time, meaning no later than <span style = 'color:#FF0000;font-weight:bold'>5</span> minutes after your end time. Thanks!" +
            "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]"
            + "<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
    api.call("mspbots-teams", "/message/send", "post", param)
    Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "NoNextStep", "times", "3,6,9", "tenantUserId", user.get("tenantUserId").asLong())
  println "LateTimeEntry======================4========================"
    return api.call("mspbots-teams", "/escalation/check", "post", escalation)
}