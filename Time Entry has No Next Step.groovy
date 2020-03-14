import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.mspbots.common.TimeZoneUtils

if (user.get("firstName")==null || user.get("userName")==null || tenant.get("mspbots.sync.wise.site")==null) {
        return false
}

List<String> TICKET_STATUS = Arrays.asList(
        "Completed (Emails Contact & CC)",
        "Canceled No Response (Emails Contact & CC)",
        "Client Canceled (Emails Contact & CC)",
        "Canceled (No Email)",
        "Closed (Canceled)",
        "Closed (MGR)",
        "Closed (FINAL)"
)

def wordList = ["Next Step", "Confirmed with client", "Next Steps"]
if (payload.get("notes") != null) {
    if (!"travel".equalsIgnoreCase(payload.get("workType").get("name").asText()) && payload.get("notes")!=null) {
        boolean contains = false
        for (i in wordList) {
            contains = contains || payload.get("notes").asText().toLowerCase().contains(i.toLowerCase());
        }
        if (!contains && payload.get("chargeToId") != null) {
            Map<String, String> ticketsParams = Map.of(
              "tenantId", user.get("tenantId").asLong(),
              "cwId", payload.get("chargeToId").asText()
            )
            JsonNode tenantTicketDTO = api.call("mapbots-core", "/tickets/detail", ticketsParams)
            println "*****No Next Step******" + tenantTicketDTO
            if (tenantTicketDTO != null && tenantTicketDTO.get("status") != null && tenantTicketDTO.get("extend").get("type")!=null) {
                String ticketTypeStr = tenantTicketDTO.get("extend").get("type").get("name").asText().toLowerCase()
                boolean tickeTypeFalg = !ticketTypeStr.contains("meeting") && !ticketTypeStr.contains("admin") && !ticketTypeStr.contains("break") && !ticketTypeStr.contains("complete") 
                println "*****No Next Step**tickeTypeFalg****:" + tickeTypeFalg
                if(tickeTypeFalg) {
                   println "*****No Next Step**status****:" + tenantTicketDTO.get("status").asText()
                    for (i in TICKET_STATUS) {
                      contains = contains || tenantTicketDTO.get("status").asText().toLowerCase().contains(i.toLowerCase())
                    }
                    String statusStr = tenantTicketDTO.get("status").asText().toLowerCase()
                    if (!contains && !statusStr.contains("completed")) {
                      Integer escalationTime = 0;
                      String message = params.get("message").asText()
                      String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()
                      boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
                      message = message.replace("{USER}",user.get("firstName").asText())
                      message = message.replace("{CWSITE}", tenant.get("mspbots.sync.wise.site").asText())
                      message = message.replace("{CWTID}",payload.get("id").asText())
                      message = message + "<br> --"
                      if(params.get("sendEscalation").asBoolean()){
                        Map<String, String> countParam = Map.of(
                          "tenantId", user.get("tenantId").asLong(), 
                          "userId", user.get("tenantUserId").asLong(), 
                          "ruleId", event.getId(), 
                          "escalation", escalationOn
                        )
                        JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)
                        escalationTime = Integer.parseInt(times.asText())
                        String escalationTimeStr = (escalationTime+1) + (escalationTime > 1 ? " times" : " time")
                        
                        message =message+"[ <span style='color:#999d9c;'>"+escalationTimeStr+"</span> this week. Threshold <span style='color:#999d9c;'>"+params.get("times").asText().replace(",","-")+"</span> ]"
                      }
                      message = message + "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"
                      Map<String, String> param = Map.of(
                        "teamsUserId", user.get("teamsUserId"),
                        "tenantUserId", user.get("tenantUserId").asLong(),
                        "tenantId", user.get("tenantId").asLong(),
                        "frequency", "once",
                        "send", params.get("send").asText(),
                        "ruleId", event.getId(),
                        "businessId",payload.get("id").asLong(),
                        "businessType",event.getScope(),
                        "escalation", escalationOn,
                        "message", message
                      )
                      JsonNode sendResult = api.call("mspbots-teams", "/message/send","post",param)
                      if(!sendResult.isEmpty() && escalationOn){
                        Map<String, Object> escalation = Map.of(
                          "tenantId", user.get("tenantId").asLong(), 
                          "ruleId", event.getId(), 
                          "triggerName", "NoNextStep", 
                          "times",  params.get("times").asText(), 
                          "tenantUserId", user.get("tenantUserId").asLong())
                      	return api.call("mspbots-teams", "/escalation/check", "post", escalation)
                      }
        			}
            }
            }
        }
    }
}