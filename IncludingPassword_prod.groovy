import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

def wordList = ["password is","password:","password :","password -"]
if (payload.get("notes")==null) {
  return false
}

for(i in wordList){
    if(payload.get("notes").asText().toLowerCase().contains(i)) {
        Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                "tenantUserId", user.get("tenantUserId").asLong(),
                "tenantId", user.get("tenantId").asLong(),
                "frequency","once",
                "send","true",
                "businessId",payload.get("id").asLong(),
                "businessType",event.getScope(),
                "ruleId", event.getId(),
                "message", "Dear " + user.get("firstName").asText()
                + ", you included your password in your time entry <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>"
                + ". For your own security, please avoid putting your account credentials in your entries.Thanks!"
                + "<br> --<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>");
        return api.call("mspbots-teams", "/message/send", "post", param)
    }
}