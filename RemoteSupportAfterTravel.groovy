import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime


if (tenant.get("mspbots.sync.wise.site")==null) {
 return false 
}

if (!params.get("onsite").asText().equalsIgnoreCase(payload.get("workType").get("name").asText())) {
    Map<String, String> timeEntryParams = Map.of("tenantId", user.get("tenantId").asLong(),
            "cwUid", payload.get("member").get("id").asText(), params.get("enteredDate").asText(), payload.get("dateEntered").asText())
    JsonNode tenantTimeEntryDTO = api.call("mapbots-core", "/tenant/user-time-entry/findLastOneById", timeEntryParams)
    println tenantTimeEntryDTO.get("extend").findPath("workType").get("name").asText()
    if (tenantTimeEntryDTO != null && "travel".equalsIgnoreCase(tenantTimeEntryDTO.get("extend").findPath("workType").get("name").asText())) {

        Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), "userId", user.get("tenantUserId").asLong(), "ruleId", event.getId(), "escalation", false)

        JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)


        String timeentry1 = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"

        String timeEntry2 = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + tenantTimeEntryDTO.get("cwTid") + "'>" + tenantTimeEntryDTO.get("cwTid") + "</a>"
        String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                        .replace("{timeentry1}",timeentry1)
                                                        .replace("{workTypeName}",payload.get("workType").get("name").asText())
                                                        .replace("{timeentry2}",timeEntry2)

        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId",user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
		"frequency", params.get("frequency").asText(),
                "send", params.get("send").asText(),
                "businessId",payload.get("id").asLong(),
				"businessType",event.getScope(),
                "ruleId", event.getId(),
                "message",message
                + "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
        api.call("mspbots-teams", "/message/send", "post",param)
         //escalation
        if(params.get("sendEscalation").asBoolean()){
            Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "RemoteSupportAfterTravel", "times",  params.get("times").asText(), "tenantUserId", user.get("tenantUserId").asLong())
            return api.call("mspbots-teams", "/escalation/check", "post", escalation)
        }
    }
}

println "RemoteSupportAfterTravel------end"
