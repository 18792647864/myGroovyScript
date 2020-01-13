import com.fasterxml.jackson.databind.JsonNode
import java.time.Duration
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.LocalTime

def beginDate = ignoreSeconds(ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime())
def endDate = ignoreSeconds(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime())
Duration times = Duration.between(beginDate,endDate)

println beginDate
println endDate
println times.toMinutes()

if(times.toMinutes() <  Integer.parseLong(params.get("times"))){

	Map<String, String> ticketParams = Map.of(
		"tenantId", user.get("tenantId").asLong(),
		"cwId", payload.get("chargeToId").asLong()
	)
	JsonNode ticket = api.call("mspbots-core","/tickets/detail",ticketParams)

	if(ticket != null){
		Map<String, String> param = Map.of(
			"teamsUserId", user.get("teamsUserId"),
			"tenantUserId",user.get("tenantUserId").asLong(),
			"tenantId", user.get("tenantId").asLong(),
			"frequency", params.get("frequency"),
			"ruleId", event.getId(),
			"message", "Dear " + user.get("firstName").asText() + ", Your Time Entry [" + payload.get("id") + "] (https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") 
				+ ") on [" + ticket.get("id") + "] (https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + ticket.get("id") 
				+ ") is less than 5 minutes. Please review this time to make sure it should be billed."
		)
		return api.call("mspbots-teams", "/message/send", "post",param)
	}
    
}

LocalDateTime ignoreSeconds(LocalDateTime dateTime) {
    return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(dateTime.getHour(), dateTime.getMinute(), 0));
}