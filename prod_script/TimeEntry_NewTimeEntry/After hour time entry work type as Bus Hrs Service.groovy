import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

println 11111111

if(payload.get("workType").get("name").asText().toLowerCase().contains("bus")){
    Map<String,Object> params = Map.of("tenantId",user.get("tenantId").asLong(),"cwId",payload.get("chargeToId").asLong())
    JsonNode ticket = api.call("mspbots-core","/tickets/detail",params)
    if(ticket != null){
        LocalDateTime workTime = LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        LocalDateTime beginWork = LocalDateTime.of(workTime.toLocalDate(), LocalTime.MIN).plusHours(8)
        LocalDateTime endWork = LocalDateTime.of(workTime.toLocalDate(), LocalTime.MAX).minusHours(5)
        LocalDateTime beginDate = ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        LocalDateTime endDate = ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        if (beginDate.isBefore(beginWork) || endDate.isAfter(endWork)) {
            Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                    "tenantUserId",user.get("tenantUserId").asLong(),
                    "tenantId", user.get("tenantId").asLong(),
                    "frequency","once",
                    "businessId",payload.get("id").asLong(),
                    "businessType",event.getScope(),
                    "send","true",
                    "ruleId", event.getId(),
                    "message","Dear " + user.get("firstName").asText()
                    +", your Time Entry <a href='https://cw.jexet.com/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"
                    + " on <a href='https://cw.jexet.com/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>" + " is set to Work Type"
                    +": \"Bus Hrs Service\", but happened during After Hours"
                    +". If you meant to bill at our Business Hours rate, please add an internal note that says: \"Bus Hrs Service that was done After Hours.\" Thanks!"
                    +"<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>")
            return api.call("mspbots-teams", "/message/send", "post",param)
        }
    }
}