import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


if (payload.get("status").asText().toLowerCase().contains("in progress")
        || payload.get("status").asText().toLowerCase().contains("in-progress")) {
  if (payload.get("updatedTime")!=null && ZonedDateTime.parse(payload.get("updatedTime").asText()).toLocalDateTime()
      .plusMinutes(60).isBefore(LocalDateTime.now())) {
          Map<String, String> param = Map.of(
            "teamsUserId", user.get("teamsUserId"),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "frequency","schedule",
            "send","false",
            "businessId",payload.get("cwId"),
            "businessType",event.getScope(),
                "ruleId", event.getId(),
        "message", "Dear " + user.get("firstName").asText() + ", you have been processing the ticket <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("cwId") + "'>" + payload.get("cwId") + "</a>" + " for 60 mins. Please check if it needs to be escalated." +
        "<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
                return api.call("mspbots-teams", "/message/send", "post",param)
            }

}