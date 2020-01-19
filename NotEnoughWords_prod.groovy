import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

if (tenant.get("mspbots.sync.wise.site")==null) {
  return false
}


if (!"travel".equalsIgnoreCase(payload.get("workType").get("name").asText())) {
    if (Double.parseDouble(payload.get("actualHours").asText()) > 2 && payload.get("notes").asText().length() < 100) {
        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId",user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
                "frequency","once",
                "send","true",
                "businessId",payload.get("id").asLong(),
                "businessType",event.getScope(),
                "ruleId", event.getId(),
                "message", "Dear " + user.get("firstName").asText() + ", you just logged a time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + " that has more than <span style = 'color:#FF0000;font-weight:bold'>2</span> hours. The time log note is shorter than <span style = 'color:#FF0000;font-weight:bold'>100</span>, so please add more details. Thanks!"
                + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
        return api.call("mspbots-teams", "/message/send", "post",param)
    }
}