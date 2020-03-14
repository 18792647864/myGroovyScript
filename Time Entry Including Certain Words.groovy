import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.ZoneId


def wordList = ["I think", "I believe"]
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



      String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
      boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
      println "Time Entry Including Certain Words:::::::::::sendEscalation" + params.get("sendEscalation").asBoolean()
      String messageButton = escalationOnStr(escalationOn)

      String timeEntryStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>";
      String wordStr = "<span style = 'color:#FF0000;font-weight:bold'>‘"+word +"’</span>"
      String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                .replace("{timeEntry}",timeEntryStr)
                                .replace("{word}",wordStr)

      String messageName = "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"

      Map<String,String> param = Map.of(
              "teamsUserId", user.get("teamsUserId"),
              "tenantUserId",user.get("tenantUserId").asLong(),
              "tenantId", user.get("tenantId").asLong(),                       
              "frequency",params.get("frequency").asText(),
              "send",params.get("send").asBoolean(),
              "businessId",payload.get("id").asLong(),
              "businessType",event.getScope(),
              "ruleId", event.getId(),
              "escalation",escalationOn,
              "message",message + "<br> --" + messageButton + messageName)
      sendMessageAndCheck(param,escalationOn,"IncludingWords")

  }
}

String escalationOnStr(boolean escalationOn) {

      String messageButton = ""
      if(escalationOn){
          Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), 
                                                "userId", user.get("tenantUserId").asLong(), 
                                                "ruleId", event.getId(), 
                                                "escalation", escalationOn)
          println "IncludingWords:: countParam" + countParam
          JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)
          Integer escalationTime = Integer.parseInt(times.asText())
          println "IncludingWords::escalationTime" + escalationTime
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