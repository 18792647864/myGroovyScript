import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import com.fasterxml.jackson.databind.JsonNode

println "NotEnoughWords----start"


println "NotEnoughWords----params"
println params

if (!params.get("travel").asText().equalsIgnoreCase(payload.get("workType").get("name").asText())) {
    if (Double.parseDouble(payload.get("actualHours").asText()) > Double.parseDouble(params.get("hourstime").asText()) 
            && payload.get("notes").asText().length() < Integer.parseInt(params.get("notesnum").asText()) ) {

        Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), "userId", user.get("tenantUserId").asLong(), "ruleId", event.getId(), "escalation", false)

        JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)


        String timeEntryStr =  "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"

        String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                     .replace("{timeentry}",timeEntryStr)

        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId",user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
				"frequency", params.get("frequency").asText(),
                "send", params.get("send").asText(),
                "businessId",payload.get("id").asLong(),
				"businessType",event.getScope(),
                "ruleId", event.getId(),
                "message", message
                + "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
        api.call("mspbots-teams", "/message/send", "post",param)
        //escalation
        if(params.get("sendEscalation").asBoolean()){
            Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "NotEnoughWords", "times",  params.get("times").asText(), "tenantUserId", user.get("tenantUserId").asLong())
            return api.call("mspbots-teams", "/escalation/check", "post", escalation)
        }
    }
}

println "NotEnoughWords----end"