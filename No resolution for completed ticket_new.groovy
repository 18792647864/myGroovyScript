import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

TimeUnit.SECONDS.sleep(30)

if (payload.findPath("status").get("name").asText().toLowerCase().contains("completed")) {
        
        Map<String, Object> timeEntry = Map.of("tenantId", user.get("tenantId").asLong(), "chargeToId", payload.get("id").asLong())
        JsonNode count = api.call("mspbots-core", "/tenant/user-time-entry/findByChargeToId", timeEntry)
        println count
     
        boolean resolutionFlg = false
        if (Integer.parseInt(count.asText()) == 0) {
          
                try {
                    Map<String, Object> params = Map.of("tenantId", user.get("tenantId").asLong(), "id", payload.get("id").asLong())
                    JsonNode notes = api.call("mspbots-wise", "/tickets/notes", params)
                    println "aff No resolution for completed tickets----------notes"
                    println notes

                    for(JsonNode note : notes) {
                      if (note.get("resolutionFlag").asBoolean()) {
                        println note.get("resolutionFlag").asBoolean()
                        resolutionFlg = true
                        break
                      }
                    }           
                } catch (Exception e) {
                  println "aff No resolution for completed tickets----------Exception"
              
                }
        }else{
           resolutionFlg = true
        }

        if (!resolutionFlg) {
              Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId").asText(),
                      "tenantUserId", user.get("tenantUserId").asLong(),
                      "tenantId", user.get("tenantId").asLong(),
                      "frequency", "once",
                      "businessId", payload.get("id"),
                      "businessType",event.getScope(),
                      "send", "true",
                      "ruleId", event.getId(),
                      "message", "Dear " + user.get("firstName").asText()
                      + ", the ticket <a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" + " status is <span style = 'color:#FF0000;font-weight:bold'>\"Completed by Technician\"</span>"
                      + ", but a Resolution is missing. Please click the ticket link to fix the issue.Thanks!"
                      + "<br> --<span style='color:#999d9c;font-size:10px;'>[" + user.get("userName").asText() + "]&nbsp;["+LocalDateTime.now().plusHours(Integer.parseInt(user.get("tz").asText().substring(0,3))).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()+"]</span>")
              return api.call("mspbots-teams", "/message/send", "post", param)
        }
    
}