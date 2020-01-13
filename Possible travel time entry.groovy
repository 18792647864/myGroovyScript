import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

def word = params.get("travel").toLowerCase()
def noteword = payload.get("notes").asText().toLowerCase()

def workTypeWord = payload.get("workType").get("name").asText().toLowerCase()

println "PossibleTravelTimeEntry----user"
println user
println "PossibleTravelTimeEntry----user---End"

if (noteword.contains(word) && !word.equals(workTypeWord)){

	
	Map<String, String> params = Map.of("tenantId", user.get("tenantId").asLong(),
            "cwId", payload.get("chargeToId").asLong())

	
	JsonNode ticket = api.call("mapbots-core","/tickets/detail",params)
	if(ticket != null){

		Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId").asText(),
				"tenantUserId",user.get("tenantUserId").asLong(),
				"tenantId", user.get("tenantId").asLong(),
				"frequency", params.get("frequency"),
                "send", params.get("send"),
                "businessId",payload.get("id").asLong(),
				"businessType",event.getScope(),
				"ruleId", event.getId(),
				"message", "Dear " + user.get("firstName").asText()
                 + ", your time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + 
				" on <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>" +
				 " includes the word \"Travel\", but the Work Type is not. Please check to make sure the \"Work Type\" is correct.Thanks!"
                  +  "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
		
		return api.call("mspbots-teams", "/message/send", "post",param)
	}else{
		println "CheckTravel ticket is null!!"
	}
}


