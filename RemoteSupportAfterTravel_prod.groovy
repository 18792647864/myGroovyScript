import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

if (tenant.get("mspbots.sync.wise.site")==null) {
 return false 
}

if (!"onsite".equalsIgnoreCase(payload.get("workType").get("name").asText())) {
    Map<String, String> params = Map.of("tenantId", user.get("tenantId").asLong(),
            "cwUid", payload.get("member").get("id").asText(), "enteredDate", payload.get("dateEntered").asText())
    JsonNode tenantTimeEntryDTO = api.call("mapbots-core", "/tenant/user-time-entry/findLastOneById", params)
    println tenantTimeEntryDTO.get("extend").findPath("workType").get("name").asText()
    if (tenantTimeEntryDTO != null && "travel".equalsIgnoreCase(tenantTimeEntryDTO.get("extend").findPath("workType").get("name").asText())) {
        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId",user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
                "frequency","once",
                "send","true",
                "businessId",payload.get("id").asLong(),
                "businessType",event.getScope(),
                "ruleId", event.getId(),
                "message","Dear " + user.get("firstName").asText() +
                ", you created a time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" +" typed as " + payload.get("workType").get("name").asText() +
                ". It’s followed with the time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + tenantTimeEntryDTO.get("cwTid") + "'>" + tenantTimeEntryDTO.get("cwTid") + "</a>" +" which is classified as \"Travel\""+
                ". Please check if the type should instead be \"onsite\". Thanks!"
                + "<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
        return api.call("mspbots-teams", "/message/send", "post",param)
    }
}
