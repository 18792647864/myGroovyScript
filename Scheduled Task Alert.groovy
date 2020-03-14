import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.fasterxml.jackson.databind.JsonNode

println "Scheduled Task Alert:"
println params

println user.get("tenantId").asLong() + "---------" +event.getId() + "-------" +payload
if (!payload.get("doneFlag").asBoolean()) {

  if (payload.get("startTime")!=null && comp_time()) {
       String ticketStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("cwId") + "'>" + payload.get("cwId") + "</a>"

       String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                .replace("{ticket}",ticketStr)
          Map<String, String> param = Map.of(
            "teamsUserId", user.get("teamsUserId"),
            "tenantUserId",user.get("tenantUserId").asLong(),
            "tenantId", user.get("tenantId").asLong(),
            "frequency","once",
            "send", params.get("send"),
            "businessId",payload.get("cwId"),
            "businessType",event.getScope(),
            "ruleId", event.getId(),
            "message", message
                  + "<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
             api.call("mspbots-teams", "/message/send", "post",param)      
    }
}

boolean comp_time(){

  return LocalDateTime.now().plusMinutes(15) = LocalDateTime.parse(payload.get("startTime").asText()) 

}

