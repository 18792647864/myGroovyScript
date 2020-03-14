import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId




if (payload.get("workType").get("name")==null || payload.get("timeStart")==null || payload.get("timeEnd")==null) {
  return false
}

if(payload.get("workType").get("name").asText().toLowerCase().contains("bus")){
    Map<String,Object> ticketParams = Map.of("tenantId",user.get("tenantId").asLong(),"cwId",payload.get("chargeToId").asLong())
    JsonNode ticket = api.call("mspbots-core","/tickets/detail",ticketParams)
    if(ticket != null){
        LocalDateTime workTime = LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        LocalDateTime beginWork = LocalDateTime.of(workTime.toLocalDate(), LocalTime.MIN).plusHours(8)
        LocalDateTime endWork = LocalDateTime.of(workTime.toLocalDate(), LocalTime.MAX).minusHours(5)
        LocalDateTime beginDate = ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        LocalDateTime endDate = ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        int days = beginWork.getDayOfYear() - beginDate.getDayOfYear()

        if(days != 0){
            beginWork.minusHours(days*24)
            endWork.minusHours(days*24)
        }


        if (beginDate.getHour() < 8 || beginDate.getHour() > 19) {

            String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()

            boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
            String messageButton = escalationOnStr(escalationOn)
           


            String timeEntryStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"
            String ticketStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>"
            String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                .replace("{timeentry}",timeEntryStr)
                                                .replace("{ticket}",ticketStr)
            

            String messageName = "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"
         

            Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                    "tenantUserId",user.get("tenantUserId").asLong(),
                    "tenantId", user.get("tenantId").asLong(),
                    "frequency","once",
                    "businessId",payload.get("id").asLong(),
				  	"businessType",event.getScope(),       
                    "send", params.get("send").asText(),
                    "ruleId", event.getId(),
                    "escalation",escalationOn,
                    "message",message + "<br> --" + messageButton + messageName)
            

            sendMessageAndCheck(param,escalationOn,"After hour time entry work type as Bus Hrs Service")
        }
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



