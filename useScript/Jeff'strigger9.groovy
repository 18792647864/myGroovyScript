import com.fasterxml.jackson.databind.JsonNode

def word = "Travel".toLowerCase()
def noteword = payload.get("notes").asText().toLowerCase()

def workTypeWord = payload.get("workType").get("name").asText().toLowerCase()

println noteword
println workTypeWord

if (noteword.contains(word) && !word.equals(workTypeWord)){

	
	Map<String, String> params = Map.of("tenantId", user.get("tenantId").asLong(),
            "cwId", payload.get("chargeToId").asLong())

	
	JsonNode ticket = api.call("mapbots-core","/tickets/detail",params)
	if(ticket != null){

		Map<String, String> param = Map.of(
				"tenantUserId",user.get("tenantUserId").asLong(),
				"tenantId", user.get("tenantId").asLong(),
				"frequency","once",
				"ruleId", event.getId(),
				"message", "Your Time Entry " +"(https://cw.jexet.com/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + ")on" + "(https://cw.jexet.com/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid="
				+ ticket.get("id") + ")includes the word Travel, but the Work Type is not<br> --<span style='color:#999d9c;font-size:10px;'>[Travel]&nbsp,Please check to make sure the Work Type is correct.");
		
		return api.call("mspbots-teams", "/message/send", "post",param)
	}else{
		println "CheckTravel ticket is null!!"
	}
}



JsonNode ticket = api.call("mapbots-core","/tickets/detail",params)
https://cw.jexet.com/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=