import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.ZonedDateTime
if (tenant.get("mspbots.sync.wise.site")==null || user.get("tenantUserId").asLong() == null || user.get("teamsUserId") == null) {
	return false
}
TimeUnit.SECONDS.sleep(30)
String statusStr = payload.get("status").get("name").asText().toLowerCase()
println "no resolution for completed:::payloadId::::tenantId::::::"+payload.get("id").asLong()+"  -------  "+user.get("tenantId").asLong()
println "no resolution for completed:::tenantUserId::::::"+user.get("tenantUserId").asLong()
if (statusStr.contains("completed") || statusStr.contains("complete")) {

    Map<String, Object> timeEntry = Map.of("tenantId", user.get("tenantId").asLong(), "ticketId", payload.get("id").asLong())
    JsonNode entries = api.call("mspbots-wise", "/times/ticket/entries", timeEntry)

    boolean timeresolutionFlag = true
    if(entries != null){
      for(JsonNode entry : entries) {
          println "no resolution for completed-----------3"
          if ( entry.get("addToResolutionFlag").asBoolean()) {
            	timeresolutionFlag = false
            	break
          }
      }   
    }
    

    boolean resolutionFlg = false
    if (timeresolutionFlag) {

    	try {
        	JsonNode notes = api.call("mspbots-wise", "/tickets/notes", Map.of("tenantId", user.get("tenantId").asLong(), "id", payload.get("id").asLong()))
        	for(JsonNode note : notes) {
                println "no resolution for completed-----------3"
          		if (note == null || note.get("resolutionFlag") == null || note.get("resolutionFlag").asBoolean()) {
            		resolutionFlg = true
            		break
          		}
        	}           
      	} catch (Exception e) {
		
        }  
    }else{
      	resolutionFlg = true
    }

    if (!resolutionFlg) {
 
		    String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()

        boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
        String messageButton = escalationOnStr(escalationOn)
        

      	String firstName = user.get("firstName").asText()
        String ticketService = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + 						payload.get("id") + "'>" + payload.get("id") + "</a>"
        String completedByTechnician = "<span style = 'color:#FF0000;font-weight:bold'>\"Completed by Technician\"</span>"
        
      	String message = params.get("message").asText()
                      .replace("{firstName}",firstName)
                      .replace("{ticketService}",ticketService)
                      .replace("{completedByTechnician}",completedByTechnician)

        String messageName = "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"   
      
      
        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId").asText(),
        	 "tenantUserId", user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "frequency", params.get("frequency").asText(),
            "send", params.get("send").asText(),
            "businessId", payload.get("id"),
            "businessType",event.getScope(),
            "ruleId", event.getId(),
             "escalation",escalationOn,
            "message", message + "<br> --" + messageButton + messageName)
        
  		println "no resolution for completed-----------sendMessageAndCheck:::"+payload.get("id").asLong()
        sendMessageAndCheck(param,escalationOn,"No resolution for completed tickets")
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



