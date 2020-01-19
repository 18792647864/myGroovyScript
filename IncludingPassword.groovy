import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.JsonNode

println "IncludingPassword_params:"
println params



def wordList = ["password is","password:","password :","password -"]
if (payload.get("notes")==null) {
  println "IncludingPassword_params:111"
  return false
}

println "IncludingPassword_notes:"+payload.get("notes").asText()

for(i in wordList){
    println "IncludingPassword_params:"+i
    if(payload.get("notes").asText().toLowerCase().contains(i)) {


        Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), "userId", user.get("tenantUserId").asLong(), "ruleId", event.getId(), "escalation", true)

        JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)

        println "IncludingPassword_params:1111111"

        String userStr = user.get("firstName").asText()
        println "IncludingPassword_params:2222222"

        String timeEntry = "<a href='https://"+ tenant.get("mspbots.sync.wise.site").asText() + "/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid="  + payload.get("id") + "'>" + payload.get("id") + "</a>"

        println "IncludingPassword_params:3333333333"
        String message = params.get("message").asText().replace("{user}",userStr).replace("{timeentry}",timeEntry)
        println "IncludingPassword_params:444444444"

        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId", user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
                "frequency", params.get("frequency").asText(),
                "send", params.get("send").asText(),
                "businessId",payload.get("id").asLong(),
                "businessType",event.getScope(),
                "ruleId", event.getId(),
                "escalation",params.get("sendEscalation").asBoolean(),
                "message", message + "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
        println "IncludingPassword_params:55555555::::"+param

        try{
           api.call("mspbots-teams", "/message/send", "post", param)
        } catch (Exception e) {
          println "IncludingPassword_Exception:"+e.printStackTrace();
        }
       

        println "IncludingPassword_params:sendEscalation"+params.get("sendEscalation").asBoolean()

        //escalation
        if(params.get("sendEscalation").asBoolean()){
        
          Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "IncludingPassword", "times",  params.get("times").asText(), "tenantUserId", user.get("tenantUserId").asLong())
        
          return api.call("mspbots-teams", "/escalation/check", "post", escalation)
        }
        println "IncludingPassword_params:6666666666"
        
    }
}

println "IncludingPassword-----end"