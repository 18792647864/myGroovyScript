import java.time.format.DateTimeFormatter
import com.mspbots.common.TimeZoneUtils
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.JsonNode
import java.time.ZonedDateTime
import java.time.ZoneId

println "TicketOnSite_params:"
println params

status = payload.findPath("status").get("name").asText().toLowerCase()

if (status.contains("scheduled on site") || status.contains("scheduled onsite")) {

    String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()

    boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
    String messageButton = escalationOnStr(escalationOn)

    String ticketStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"

    String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                .replace("{ticket}",ticketStr)

    String messageName = "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"

    Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantId",user.get("tenantId").asLong(),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "ruleId",event.getId(),
            "frequency", params.get("frequency").asText(),
            "send", params.get("send").asText(),
            "businessId",payload.get("id").asLong(),
            "businessType",event.getScope(),
            "escalation",escalationOn,
            "message",message + "<br> --" + messageButton + messageName
    );
    sendMessageAndCheck(param,escalationOn,"Scheduled Onsite Reminder")
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
