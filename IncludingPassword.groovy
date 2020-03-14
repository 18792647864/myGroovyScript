import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter



if (payload.get("workType").get("name")==null || payload.get("timeStart")==null || payload.get("timeEnd")==null) {

  return false
}
println payload.get("workType").get("name").asText().toLowerCase()
if(payload.get("workType").get("name").asText().toLowerCase().contains("bus")){

    Map<String,Object> params = Map.of("tenantId",user.get("tenantId").asLong(),"cwId",payload.get("chargeToId").asLong())
    JsonNode ticket = api.call("mspbots-core","/tickets/detail",params)
    if(ticket != null){

        LocalDateTime workTime = LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        LocalDateTime beginWork = LocalDateTime.of(workTime.toLocalDate(), LocalTime.MIN).plusHours(8)
        LocalDateTime endWork = LocalDateTime.of(workTime.toLocalDate(), LocalTime.MAX).minusHours(5)
        LocalDateTime beginDate = ZonedDateTime.parse(payload.get("timeStart").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        LocalDateTime endDate = ZonedDateTime.parse(payload.get("timeEnd").asText()).toLocalDateTime().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3)))
        int days = beginWork.getDayOfYear() - beginDate.getDayOfYear()

        if(days != 0){
            beginWork.minusHours(days*24)
            endWork.minusHours(days*24)
        }


        if (beginDate.isBefore(beginWork) || beginDate.isAfter(endWork)) {


            Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), "userId", user.get("tenantUserId").asLong(), "ruleId", event.getId(), "escalation", false)

            JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)

            String timeEntryStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"
            String ticketStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" + payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>"
            String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                .replace("{timeentry}",timeEntryStr)
                                                .replace("{ticket}",ticketStr)



            Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                    "tenantUserId",user.get("tenantUserId").asLong(),
                    "tenantId", user.get("tenantId").asLong(),
                    "frequency","once",
                    "businessId",payload.get("id").asLong(),
				  	"businessType",event.getScope(),       
                    "send","true", 
                    "ruleId", event.getId(),
                    "message",message
                    +"<br> --[ <span style='color:#999d9c;'>" + (Integer.parseInt(times.asText()) + 1) + (Integer.parseInt(times.asText()) > 1 ? " times" : " time") + "</span> this week. Threshold <span style='color:#999d9c;'>" + "3-6-9" + "</span> ]<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>")

            api.call("mspbots-teams", "/message/send", "post",param)


            //escalation
            if(params.get("sendEscalation").asBoolean()){
                Map<String, Object> escalation = Map.of("tenantId", user.get("tenantId").asLong(), "ruleId", event.getId(), "triggerName", "IncludingPassword", "times",  params.get("times").asText(), "tenantUserId", user.get("tenantUserId").asLong())
                return api.call("mspbots-teams", "/escalation/check", "post", escalation)
            }
        }
    }
}