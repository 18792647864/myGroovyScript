import com.fasterxml.jackson.databind.JsonNode

println "IncludingWords_params"

println params

def wordList = ["think", "believe","good"]
def word = ""
for (i in wordList) {
  if (payload.get("notes").asText().toLowerCase().contains(i)) {
    word = i;
    break;
  }
}
println payload.get("notes").asText()
println "".equals(wrod)
if (!"".equals(word) && payload.get("chargeToId")!=null) {
    Map<String, Object> ticketsParams = Map.of("tenantId",user.get("tenantId").asLong(),"cwId",payload.get("chargeToId").asLong());
    JsonNode ticket = api.call("mapbots-core","/tickets/detail", ticketsParams)
    
    if (ticket==null || ticket.get("extend").findPath("board")==null || ticket.get("extend").findPath("board").get("name")==null) {
    	return false
    }

  
  if (ticket  != null && ticket.get("extend").findPath("board")!=null &&  ticket.get("extend").findPath("board").get("name").asText().toLowerCase().contains("reactive")) {
    Map<String,String> param = Map.of(
      "teamsUserId", user.get("teamsUserId"),
      "tenantUserId",user.get("tenantUserId").asLong(),
      "tenantId", user.get("tenantId").asLong(),                       
      "frequency",params.get("frequency").asText(),
      "send","true",
	"businessId",payload.get("id").asLong(),
	"businessType",event.getScope(),
      "ruleId", event.getId(),
      "message","Dear "+user.get("firstName").asText()+
      ", you just created a time entry ["+payload.get("id")+"](http://example.com/ "+ payload.get("id")+") that has <font color=Red>‘"+wrod +"’</font> in it.<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+payload.get("dateEntered").asText()+"]</span>" );
    return api.call("mspbots-teams", "/message/send", "post",param)
  }
}

println "IncludingWords_end"
                                                                                                                       