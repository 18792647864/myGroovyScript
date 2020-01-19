import com.fasterxml.jackson.databind.JsonNode
import java.time.Duration
import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

if (payload.get("timeStart")==null || payload.get("timeEnd")==null || payload.get("workType").get("name")==null || user.get("firstName")==null || user.get("userName")==null || tenant.get("mspbots.sync.wise.site")==null) {
  return false
}

def beginDate = ignoreSeconds(ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime())
def endDate = ignoreSeconds(ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime())
Duration times = Duration.between(beginDate, endDate)

if (!payload.get("workType").get("name").asText().toLowerCase().contains("inc")) {
    if (times.toMinutes() < 5) {

        Map<String, String> ticketParams = Map.of(
                "tenantId", user.get("tenantId").asLong(),
                "cwId", payload.get("chargeToId").asLong()
        )
        JsonNode ticket = api.call("mspbots-core", "/tickets/detail", ticketParams)

        if (ticket != null) {
            if (ticket.get("extend").get("type") == null || !ticket.get("extend").get("type").get("name").asText().toLowerCase().contains("admin")) {
                Map<String, String> param = Map.of(
                        "teamsUserId", user.get("teamsUserId"),
                        "tenantUserId", user.get("tenantUserId").asLong(),
                        "tenantId", user.get("tenantId").asLong(),
                        "frequency", "once",
                        "businessId", payload.get("id").asLong(),
                        "businessType", event.getScope(),
                        "send", "true",
                        "ruleId", event.getId(),
                        "message", "Dear " + user.get("firstName").asText() + ", your time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"
                        + " on <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>"
                        + " is less than <span style = 'color:#FF0000;font-weight:bold'>5</span> minutes. Please review this time to make sure it should be billed. Thanks!"
                        + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;[" + LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0, 3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm")) + " " + user.get("tzStr").asText() + "]</span>");
                return api.call("mspbots-teams", "/message/send", "post", param)
            }
        }

    }
}

LocalDateTime ignoreSeconds(LocalDateTime dateTime) {
    return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(dateTime.getHour(), dateTime.getMinute(), 0));
}