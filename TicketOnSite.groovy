import java.time.format.DateTimeFormatter
import com.mspbots.common.TimeZoneUtils
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.JsonNode

println "TicketOnSite_params:"
println params

status = payload.findPath("status").get("name").asText().toLowerCase()

if (status.contains(params.get("scheduledonsiteblank").asText()) || status.contains(params.get("scheduledonsite").asText())) {

    
    Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), "userId", user.get("tenantUserId").asLong(), "ruleId", event.getId(), "escalation", false)

    JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)

    String ticketStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"

    String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                .replace("{ticket}",ticketStr)

    Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantId",user.get("tenantId").asLong(),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "ruleId",event.getId(),
            "frequency", params.get("frequency").asText(),
            "send", params.get("send").asText(),
            "businessId",payload.get("id").asLong(),
            "businessType",event.getScope(),
            "message",message
            +"<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>"
    );
    api.call("mspbots-teams", "/message/send","post", param)

     //escalation
    if(params.get("sendEscalation").asBoolean()){
    
        Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "TicketOnSite", "times",  params.get("times").asText(), "tenantUserId", user.get("tenantUserId").asLong())
    
        return api.call("mspbots-teams", "/escalation/check", "post", escalation)
    }
}


println "TicketOnSite_end:"
