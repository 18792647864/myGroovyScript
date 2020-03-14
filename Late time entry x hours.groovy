import java.time.ZonedDateTime
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.fasterxml.jackson.databind.JsonNode

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
        || payload.get("workType").get("name").asText().toLowerCase().contains("delivery")
        || "travel".equalsIgnoreCase(payload.get("workType").get("name").asText())) {

  if(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(params.get("hours").asInt())
     .isBefore(ZonedDateTime.parse(payload.get("dateEntered").asText()).toLocalDateTime())){
    

      	String message = params.get("message").asText()
      	String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
    boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
    message = message.replace("{USER}",user.get("firstName").asText())
          message = message.replace("{WORKTYPE}",payload.get("workType").get("name").asText())
          message = message.replace("{CWSITE}", tenant.get("mspbots.sync.wise.site").asText())
          message = message.replace("{CWTID}",payload.get("id").asText())
          message = message.replace("{HOURS}",params.get("hours").asText())
    	  message = message.replace("{TIME}",timeStr)
    
    		message = message + "<br> --"
        if(params.get("sendEscalation").asBoolean()){
          Map<String, String> countParam = Map.of(
            "tenantId", user.get("tenantId").asLong(), 
            "userId", user.get("tenantUserId").asLong(), 
            "ruleId", event.getId(), 
            "escalation", escalationOn
          )
          
         String escalationTimeStr = "{countMessage}"+" times"
          
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
          "escalation", escalationOn,
          "send",params.get("send").asBoolean(),
          "message", message
        );
        JsonNode sendResult = api.call("mspbots-teams", "/message/send","post",param)
		if(!sendResult.isEmpty() && escalationOn){
          Map<String, Object> escalation = Map.of(
            "tenantId", user.get("tenantId").asLong(), 
            "ruleId", event.getId(), 
            "triggerName", "LateTimeEntry", 
            "times",  params.get("times").asText(), 
            "tenantUserId", user.get("tenantUserId").asLong())
          return api.call("mspbots-teams", "/escalation/check", "post", escalation)
        }
    }
}