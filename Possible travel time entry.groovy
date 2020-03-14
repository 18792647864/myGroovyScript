import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime


def noteword = payload.get("notes").asText().toLowerCase()
def workTypeWord = payload.get("workType").get("name").asText().toLowerCase()

if (noteword.contains("travel") && !"travel".equals(workTypeWord) && payload.get("chargeToId")!=null){

	Map<String, String> ticketParams = Map.of("tenantId", user.get("tenantId").asLong(),
            "cwId", payload.get("chargeToId").asLong())
	JsonNode ticket = api.call("mapbots-core","/tickets/detail",ticketParams)
	if(ticket != null){

      	String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
      	boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
      
      	String firstName = user.get("firstName").asText()
        String timeEntry = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"
        String ticketService = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>"
        String message = params.get("message").asText().replace("{USER}",firstName).replace("{TIME_ENTRY}",timeEntry).replace("{TICKET}",ticketService)
      message = message+"<br> --"

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

		Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId").asText(),
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
        	Map<String, Object> escalation = Map.of(
            	"tenantId", user.get("tenantId").asLong(), 
              	"ruleId", event.getId(), 
              	"triggerName", "Possible travel time entry", 
              	"times", params.get("times").asText(), 
              	"tenantUserId", user.get("tenantUserId").asLong()
            )
          	return api.call("mspbots-teams", "/escalation/check", "post", escalation)
        } 
	}
}
