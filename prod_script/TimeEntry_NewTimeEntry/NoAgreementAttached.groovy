import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

println "NoAgreementAttached-----start"

println params

if (tenant.get("mspbots.sync.wise.site") == null) {
  return false
}

if (payload.has(params.get("company").asText()) && !payload.get(params.get("company").get("name").asText()).asText().toLowerCase().contains("sagiss") ){
    Map<String, Object> params = Map.of("tenantId", user.get("tenantId").asLong(), "companyId", payload.get("company").get("id").asText())
    try {
        JsonNode agreements = api.call("mspbots-wise", "/companies/agreement", params);
        for(JsonNode agreement: agreements){
            println agreement.get("id")
        }
        println agreements[0].get("id") + " " + agreements[0].get("name")
        if (agreements[0].get("id") == null) {

        } else {
            if (!payload.has("agreement")) {
                Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                        "tenantUserId", user.get("tenantUserId").asLong(),
                        "tenantId", user.get("tenantId").asLong(),
                        "frequency", params.get("frequency").asText(),
                        "send", params.get("send").asText(),
                        "businessId", payload.get("id").asLong(),
                        "businessType", event.getScope(),
                        "ruleId", event.getId(),
                        "message", "Dear " + user.get("firstName").asText() + ", please review your time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + " since it <span style='color:#FF0000;font-weight:bold'>has no</span> agreement attached.Thanks!"
                        + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
                return api.call("mspbots-teams", "/message/send", "post", param)
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

println "NoAgreementAttached-----end"
