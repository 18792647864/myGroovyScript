import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId


if (tenant.get("mspbots.sync.wise.site") == null || payload.get("chargeToId")==null) {
  return false
}

Boolean flag = true
if(params.get("company") != null){
    String[] companys = params.get("company").asText().split(",")
    for(com in companys){
         flag = !payload.get("company").get("name").asText().toLowerCase().contains(com.toLowerCase())
         if(!flag){
             break
         }
    }
}

if (payload.has("company") && flag){
    Map<String, Object> companyParams = Map.of("tenantId", user.get("tenantId").asLong(), "companyId", payload.get("company").get("id").asText())
    try {
        JsonNode agreements = api.call("mspbots-wise", "/companies/agreement", companyParams);
        
        if (agreements[0].get("id") != null) {
            if (!payload.has("agreement")) {

                String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()

                boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
                String messageButton = escalationOnStr(escalationOn)

                String timeEntryStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" 
              
              String ticketService = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" +                     				payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>"
                String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                        .replace("{timeentry}",timeEntryStr).replace("{Ticket}",ticketService).replace("{Client}",payload.get("company").get("name").asText()).replace("{Summary}",payload.get("summary").asText())

                String messageName = "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"


                Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                        "tenantUserId", user.get("tenantUserId").asLong(),
                        "tenantId", user.get("tenantId").asLong(),
                        "frequency", "once",
                        "send", params.get("send").asText(),
                        "businessId", payload.get("id").asLong(),
                        "businessType", event.getScope(),
                        "ruleId", event.getId(),
                         "escalation",escalationOn,
                        "message", message + "<br> --" + messageButton + messageName)
                sendMessageAndCheck(param,escalationOn,"NoAgreementAttached")
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}



String escalationOnStr(boolean escalationOn) {

      String messageButton = ""
        if(escalationOn){
          Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), 
                                                "userId", user.get("tenantUserId").asLong(), 
                                                "ruleId", event.getId(), 
                                                "escalation", escalationOn)
          JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)
          Integer escalationTime = Integer.parseInt(times.asText())
          String escalationTimeStr = (escalationTime+1) + (escalationTime > 1 ? " times" : " time")
          messageButton ="[ <span style='color:#999d9c;'>"+escalationTimeStr+"</span> this week. Threshold <span style='color:#999d9c;'>"+params.get("times").asText().replace(",","-")+"</span> ]"
      }

      return messageButton;
}


void sendMessageAndCheck(Map<String, String> param,boolean escalationOn,String triggerName) {

      JsonNode sendResult = api.call("mspbots-teams", "/message/send", "post", param)
      if(!sendResult.isEmpty() && escalationOn){
          Map<String, Object> escalationParams = Map.of(
              "tenantId", user.get("tenantId").asLong(), 
              "ruleId", event.getId(), 
              "triggerName",triggerName, 
              "times",  params.get("times").asText(), 
              "tenantUserId", user.get("tenantUserId").asLong())
          api.call("mspbots-teams", "/escalation/check", "post", escalationParams)
      }

}



