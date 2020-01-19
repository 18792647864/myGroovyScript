import java.time.format.DateTimeFormatter
import com.fasterxml.jackson.databind.JsonNode
import com.mspbots.common.TimeZoneUtils
import java.time.LocalDateTime


if (payload.get("notes")==null || tenant.get("mspbots.sync.wise.site")==null) {
 return false 
}

def wordList = ["think", "believe"]
def word = ""
for (i in wordList) {
    if (payload.get("notes").asText().toLowerCase().contains(i)) {
        word = i;
        break;
    }
}

if (!"".equals(word) && payload.get("chargeToId")!=null) {
    Map<String, Object> ticketsParams = Map.of("tenantId",user.get("tenantId").asLong(),"cwId",payload.get("chargeToId").asLong());
    JsonNode ticket = api.call("mapbots-core","/tickets/detail", ticketsParams)
  
  if (ticket==null || ticket.get("extend").findPath("board")==null || ticket.get("extend").findPath("board").get("name")==null) {
    return false
  }

    if (ticket  != null && ticket.get("extend").findPath("board")!=null &&  ticket.get("extend").findPath("board").get("name").asText().toLowerCase().contains("reactive")) {

        Map<String,String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId",user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
                "frequency","once",
                "send","true",
                "businessId",payload.get("id").asLong(),
                "businessType",event.getScope(),
                "ruleId", event.getId(),
                "message","Dear "+user.get("firstName").asText()+", you just created a time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + " that has <span style = 'color:#FF0000;font-weight:bold'>‘"+word +"’</span> in it.<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>" );
      println "Includingwords========================3==================================="
        return api.call("mspbots-teams", "/message/send", "post",param)
    }
}
