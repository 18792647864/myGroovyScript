import com.fasterxml.jackson.databind.JsonNode
import java.time.Duration
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
if (payload.get("timeStart")==null || payload.get("timeEnd")==null || payload.get("workType").get("name")==null || user.get("firstName")==null || 							user.get("userName")==null || tenant.get("mspbots.sync.wise.site")==null) {
	return false
}
def beginDate = ignoreSeconds(ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime())
def endDate = ignoreSeconds(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime())
def minutes = Duration.between(beginDate,endDate).toMinutes()
if (!payload.get("workType").get("name").asText().toLowerCase().contains("inc") && minutes < 5 &&payload.get("chargeToId")!=null) {
  	JsonNode ticket = api.call("mspbots-core","/tickets/detail",Map.of("tenantId", user.get("tenantId").asLong(),"cwId", payload.get("chargeToId").asLong()))
    if (ticket != null && (ticket.get("extend").get("type") == null || !ticket.get("extend").get("type").get("name").asText().toLowerCase().contains("admin"))) {
      	String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
     	boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
       	
        String timeEntry = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + 			       				payload.get("id") + "'>" + payload.get("id") + "</a>"
        String ticketService = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" +                     				payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>"
        String message = params.get("message").asText()
      		.replace("{firstName}",user.get("firstName").asText())
      		.replace("{timeEntry}",timeEntry)
      		.replace("{ticketService}",ticketService)
      		.replace("{minute}","<span style = 'color:#FF0000;font-weight:bold'>5</span>")
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

        Map<String, String> param = Map.of(
        	"teamsUserId", user.get("teamsUserId"),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "frequency", "once",
            "send", params.get("send").asText(),
            "businessId", payload.get("id").asLong(),
            "businessType", event.getScope(),
            "ruleId", event.getId(),
          	"escalation",escalationOn,
            "message", message
        )
        JsonNode sendResult = api.call("mspbots-teams", "/message/send", "post", param)
        //escalation
        if(!sendResult.isEmpty() && escalationOn){
			Map<String, Object> escalationParams = Map.of(
				"tenantId", user.get("tenantId").asLong(), 
				"ruleId", event.getId(), 
				"triggerName", "Time entry less than 5 minutes", 
				"times",  params.get("times").asText(), 
				"tenantUserId", user.get("tenantUserId").asLong())
			return api.call("mspbots-teams", "/escalation/check", "post", escalationParams)
		}
    }
}

LocalDateTime ignoreSeconds(LocalDateTime dateTime) {
    return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(dateTime.getHour(), dateTime.getMinute(), 0));
}
