import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), "userId", user.get("tenantUserId").asLong(), "ruleId", event.getId(), "escalation", false)


JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)


if (times==null || user.get("firstName")==null || user.get("userName")==null || tenant.get("mspbots.sync.wise.site")==null) {
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

    if (!"travel".equalsIgnoreCase(payload.get("workType").get("name").asText())) {
        boolean contains = false
      
        for (i in wordList) {
            contains = contains || payload.get("notes").asText().toLowerCase().contains(i.toLowerCase());
        }
        if (!contains && payload.get("chargeToId") != null) {
          
          println "NoNextStep===================1==============================="
            Map<String, String> ticketsParams = Map.of("tenantId", user.get("tenantId").asLong(),
                    "cwId", payload.get("chargeToId").asText())
            JsonNode tenantTicketDTO = api.call("mapbots-core", "/tickets/detail", ticketsParams)
            if (tenantTicketDTO != null && tenantTicketDTO.get("status") != null) {
              if (tenantTicketDTO.get("status")==null || tenantTicketDTO.get("extend").get("type").get("name")==null || user.get("tzStr")==null || user.get("tz")==null) {
                   return false 
                  }
                if(tenantTicketDTO.get("extend").get("type") == null || !tenantTicketDTO.get("extend").get("type").get("name").asText().toLowerCase().contains("admin")) {
                    Long count = 0
                  
                    for (i in TICKET_STATUS) {
                      
                        if (tenantTicketDTO.get("status").asText().toLowerCase().contains(i.toLowerCase())) {
                            count++
                        }
                    }
                  println "NoNextStep===================2==============================="
                    if (count == 0 && !tenantTicketDTO.get("status").asText().toLowerCase().contains("completed")) {
                        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                                "tenantUserId", user.get("tenantUserId").asLong(),
                                "tenantId", user.get("tenantId").asLong(),
                                "frequency", "once",
                                "send", "true",
                                "ruleId", event.getId(),
                                "businessId",payload.get("id").asLong(),
                                "businessType",event.getScope(),
                                "message", "Dear " + user.get("firstName").asText() +
                                ", please add <span style = 'color:#FF0000;font-weight:bold'> Next Step </span> to your time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + ". This will save time for the next tech by listing the outstanding items needed to complete the ticket. If there aren't any," +
                                " please add <span style='color:#FF0000;font-weight:bold'>No Next step</span> to avoid this message. Thanks!"
                                + "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]"
                                + "<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
                      println "NoNextStep===================3==============================="
                      api.call("mspbots-teams", "/message/send", "post", param)
                      Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "NoNextStep", "times", "3,6,9", "tenantUserId", user.get("tenantUserId").asLong())
                      return api.call("mspbots-teams", "/escalation/check", "post", escalation)
                    }
                }
            }
        }
    }
}

