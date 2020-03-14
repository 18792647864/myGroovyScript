import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.JsonNode
import java.time.ZoneId
import java.time.ZonedDateTime


if (payload.get("notes")==null) {
  return false
}
boolean flag = Double.parseDouble(payload.get("actualHours").asText()) > 2
boolean notesflag =  payload.get("notes").asText().length() < Integer.parseInt(params.get("notesnum").asText())


if (!"travel".equalsIgnoreCase(payload.get("workType").get("name").asText())) {
    if (flag && notesflag) {

     	String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()

     	boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()

        String timeEntryStr =  "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"

        String message = params.get("message").asText()
      		.replace("{user}",user.get("firstName").asText())
      		.replace("{hours}","<span style = 'color:#FF0000;font-weight:bold'>2</span>")
      		.replace("{timeLogNote}","<span style = 'color:#FF0000;font-weight:bold'>100</span>")
            .replace("{timeEntry}",timeEntryStr)
      	message = message + "<br> --" 


      	if(escalationOn){
			Map<String, String> countParam = Map.of(
				"tenantId", user.get("tenantId").asLong(), 
				"userId", user.get("tenantUserId").asLong(), 
				"ruleId", event.getId(), 
				"escalation", escalationOn
			)
			JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)
			Integer escalationTime = Integer.parseInt(times.asText())
			String escalationTimeStr = (escalationTime+1) + (escalationTime > 1 ? " times" : " time")
			message =message+"[ <span style='color:#999d9c;'>"+escalationTimeStr+"</span> this week. Threshold <span style='color:#999d9c;'>"+params.get("times").asText().replace(",","-")+"</span> ]"
        } 
        message = message + "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"

        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
			"tenantUserId",user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
			"frequency", "once",
            "send", params.get("send").asText(),
            "businessId",payload.get("id").asLong(),
			"businessType",event.getScope(),
            "ruleId", event.getId(),
            "escalation",escalationOn,
            "message", message
       	)
        JsonNode sendResult = api.call("mspbots-teams", "/message/send", "post",param)
        //escalation
        if(!sendResult.isEmpty() && escalationOn){
			Map<String, Object> escalationParams = Map.of(
				"tenantId", user.get("tenantId").asLong(), 
				"ruleId", event.getId(), 
				"triggerName", "Time Entry has No Enough Words", 
				"times",  params.get("times").asText(), 
				"tenantUserId", user.get("tenantUserId").asLong())
			return api.call("mspbots-teams", "/escalation/check", "post", escalationParams)
		}
    }
}
