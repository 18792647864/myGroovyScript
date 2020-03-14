import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

if (tenant.get("mspbots.sync.wise.site")==null) {
 return false 
}

if (!params.get("onsite").asText().equalsIgnoreCase(payload.get("workType").get("name").asText())) {
    Map<String, String> timeEntryParams = Map.of("tenantId", user.get("tenantId").asLong(),
    	"cwUid", payload.get("member").get("id").asText(), 
        "enteredDate", payload.get("dateEntered").asText()
    )
    JsonNode tenantTimeEntryDTO = api.call("mapbots-core", "/tenant/user-time-entry/findLastOneById", timeEntryParams)

    if (tenantTimeEntryDTO != null && "travel".equalsIgnoreCase(tenantTimeEntryDTO.get("extend").findPath("workType").get("name").asText())) {
		String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
     	boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
      	
      	String timeEntry1 = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"
        String timeEntry2 = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + tenantTimeEntryDTO.get("cwTid") + "'>" + tenantTimeEntryDTO.get("cwTid") + "</a>"
        String message = params.get("message").asText()
      		.replace("{user}",user.get("firstName").asText())
            .replace("{timeEntry1}",timeEntry1)
            .replace("{workTypeName}",payload.get("workType").get("name").asText())
            .replace("{timeEntry2}",timeEntry2)
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
            "message",message
        )
        JsonNode sendResult = api.call("mspbots-teams", "/message/send", "post",param)
        //escalation
        if(!sendResult.isEmpty() && escalationOn){
			Map<String, Object> escalationParams = Map.of(
				"tenantId", user.get("tenantId").asLong(), 
				"ruleId", event.getId(), 
				"triggerName", "Remote Support Time entry After Travel Time Entry", 
				"times",  params.get("times").asText(), 
				"tenantUserId", user.get("tenantUserId").asLong())
			return api.call("mspbots-teams", "/escalation/check", "post", escalationParams)
		}
    }
}
