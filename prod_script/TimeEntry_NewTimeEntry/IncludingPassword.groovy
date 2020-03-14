import java.time.format.DateTimeFormatter
import java.time.LocalDateTime


println "IncludingPassword_params:"
println params

def wordList = ["password is","password:","password :","password -"]
if (payload.get("notes")==null) {
  return false
}

for(i in wordList){
    if(payload.get("notes").asText().toLowerCase().contains(i)) {
        String user = user.get("firstName").asText()

        String timeEntry = "<a href='https://"
                             + tenant.get("mspbots.sync.wise.site").asText() 
                             + "/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" 
                             + payload.get("id") + "'>" + payload.get("id") + "</a>"

        String message = params.get("message").asText().replace("{user}",user).replace("{timeentry}",timeEntry)
        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId", user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
                "frequency", params.get("frequency").asText(),
                "send", params.get("send").asText(),
                "businessId",payload.get("id").asLong(),
                "businessType",event.getScope(),
                "ruleId", event.getId(),
                "message", message + 
                "<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");

        api.call("mspbots-teams", "/message/send", "post", param)

        //escalation
        Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "NoNextStep", "times",  params.get("times").asText(), "tenantUserId", user.get("tenantUserId").asLong())
        return  api.call("mspbots-teams", "/escalation/check", "post", escalation)
    }
}

println "IncludingPassword-----end"