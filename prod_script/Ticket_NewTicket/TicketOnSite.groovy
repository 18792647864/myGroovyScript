import java.time.format.DateTimeFormatter
import com.mspbots.common.TimeZoneUtils
import java.time.LocalDateTime

println "TicketOnSite_params:"
println params

status = payload.findPath("status").get("name").asText().toLowerCase()

if (status.contains(params.get("scheduledonsiteblank").asText()) || status.contains(params.get("scheduledonsite").asText())) {
    Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
            "tenantId",user.get("tenantId").asLong(),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "ruleId",event.getId(),
            "frequency", params.get("frequency").asText(),
            "send", params.get("send").asText(),
            "businessId",payload.get("id").asLong(),
            "businessType",event.getScope(),
            "message", "Dear "+user.get("firstName").asText()+", you have a ticket <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + " in Scheduled On Site coming up. Do you need to leave the office now?<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>"
    );
    return api.call("mspbots-teams", "/message/send","post", param)
}


println "TicketOnSite_end:"
