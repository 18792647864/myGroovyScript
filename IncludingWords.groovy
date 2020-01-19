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

if (!"".equals(word) && payload.get("chargeToId")!=null) {
    Map<String, Object> ticketsParams = Map.of("tenantId",user.get("tenantId").asLong(),"cwId",payload.get("chargeToId").asLong());
    JsonNode ticket = api.call("mapbots-core","/tickets/detail", ticketsParams)
    
    if (ticket==null || ticket.get("extend").findPath("board")==null || ticket.get("extend").findPath("board").get("name")==null) {
    	return false
    }

  
  if (ticket  != null && ticket.get("extend").findPath("board")!=null &&  ticket.get("extend").findPath("board").get("name").asText().toLowerCase().contains("reactive")) {


      Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), "userId", user.get("tenantUserId").asLong(), "ruleId", event.getId(), "escalation", false)

      JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)

      String timeEntryStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>";
      String wordStr = "<span style = 'color:#FF0000;font-weight:bold'>‘"+word +"’</span>"
      String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                .replace("{timeEntry}",timeEntryStr)
                                .replace("{word}",wordStr)

      Map<String,String> param = Map.of(
              "teamsUserId", user.get("teamsUserId"),
              "tenantUserId",user.get("tenantUserId").asLong(),
              "tenantId", user.get("tenantId").asLong(),                       
              "frequency",params.get("frequency").asText(),
              "send",params.get("send").asBoolean(),
              "businessId",payload.get("id").asLong(),
              "businessType",event.getScope(),
              "ruleId", event.getId(),
              "message",message +
              "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>" );
      println "Includingwords========================3==================================="
      api.call("mspbots-teams", "/message/send", "post",param)

      //escalation
      if(params.get("sendEscalation").asBoolean()){
        Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "IncludingWords", "times",  params.get("times").asText(), "tenantUserId", user.get("tenantUserId").asLong())
        return api.call("mspbots-teams", "/escalation/check", "post", escalation)
      }
  }
}

println "IncludingWords_end"
                                                                                                                       