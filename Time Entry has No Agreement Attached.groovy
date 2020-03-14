import com.fasterxml.jackson.databind.JsonNode
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId



if (tenant.get("mspbots.sync.wise.site") == null || payload.get("chargeToId")==null || !payload.has("company")) {
  println "NoAgreementAttached=====null====="
  return false
}



Boolean companyFlag = true
if(params.get("ExcludeCompany") != null){
    String[] companys = params.get("ExcludeCompany").asText().split(",")
    for(com in companys){
         companyFlag = !payload.get("company").get("name").asText().toLowerCase().contains(com.toLowerCase())
         if(!companyFlag){
             println "NoAgreementAttached=====ExcludeCompany====="+payload.get("company").get("name").asText().toLowerCase()+"  "+com.toLowerCase()
             break
         }
    }
}

boolean workTypeFlag = true
if(params.get("ExcludeWorkType") != null){
    String[] workTypes = params.get("ExcludeWorkType").asText().split(",")
    for (worktype in workTypes) {
        workTypeFlag = !payload.get("workType").get("name").asText().toLowerCase().contains(worktype.toLowerCase())
        if(!workTypeFlag){
             println "NoAgreementAttached=====ExcludeWorkType====="+payload.get("workType").get("name").asText().toLowerCase()+"  "+worktype.toLowerCase()
             break
        }
    }

}

boolean employeeFlag = true
if(params.get("ExcludeEmployee") != null){
    String[] employees = params.get("ExcludeEmployee").asText().split(",")
    for (employ in employees) {
        employeeFlag = !user.get("firstName").asText().toLowerCase().contains(employ.toLowerCase())
        if(!employeeFlag){
            println "NoAgreementAttached=====ExcludeEmployee====="+user.get("firstName").asText().toLowerCase()+"  "+employ.toLowerCase()
             break
        }
    }
}


if (companyFlag && workTypeFlag && employeeFlag){
    Map<String, Object> companyParams = Map.of("tenantId", user.get("tenantId").asLong(), "companyId", payload.get("company").get("id").asText())
    try {
        println "NoAgreementAttached=====payloadId====="+payload
        JsonNode agreements = api.call("mspbots-core", "/agreements/queryareements", companyParams);
        println "NoAgreementAttached=====agreements====="+agreements

        if (agreements[0].get("id") != null) {
            if (!payload.has("agreement")) {

                String timeFormat = ZonedDateTime.now(ZoneId.of(user.get("tz").asText())).toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))+" "+user.get("tzStr").asText()

                boolean escalationOn = params.get("sendEscalation")==null ? false : params.get("sendEscalation").asBoolean()
                String messageButton = escalationOnStr(escalationOn)

                String timeEntryStr = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=TimeEntryFV&recid=" + payload.get("id") + "'>" + payload.get("id") + "</a>" 
              
                String ticketService = "<a href='https://"+tenant.get("mspbots.sync.wise.site").asText()+"/v4_6_release/ConnectWise.aspx?routeTo=ServiceFV&recid=" +                     				payload.get("chargeToId").asLong() + "'>" + payload.get("chargeToId").asLong() + "</a>"
                String summaryStr = ""
                if(payload.get("summary") != null){
                    summaryStr = "- Summary:"+payload.get("summary").asText()
                }
              
              String message = params.get("message").asText().replace("{user}",user.get("firstName").asText())
                                                        .replace("{timeentry}",timeEntryStr).replace("{Ticket}",ticketService).replace("{Client}",payload.get("company").get("name").asText()).replace("{Summary}",summaryStr)

                String messageName = "<span style='color:#999d9c;font-size:10px;'>["+user.get("userName").asText()+"]&nbsp;["+timeFormat+"]</span>"


                Map<String, String> param = Map.of("teamsUserId", user.get("teamsUserId"),
                        "tenantUserId", user.get("tenantUserId").asLong(),
                        "tenantId", user.get("tenantId").asLong(),
                        "frequency", "once",
                        "send", params.get("send").asText(),
                        "businessId", payload.get("id").asLong(),
                        "businessType", event.getScope(),
                        "ruleId", event.getId(),
                         "escalation",escalationOn,
                        "message", message + "<br> --" + messageButton + messageName)
                sendMessageAndCheck(param,escalationOn,"NoAgreementAttached")
            }
        }
    } catch (Exception e) {
        println "NoAgreementAttached=====paloadId====tenantId="+payload.get("id").asLong()+"    "+user.get("tenantId").asLong()
        println "NoAgreementAttached====companyId="+payload.get("company").get("id").asText()
      	println "NoAgreementAttached=====error====="
        e.printStackTrace();
    }
}



String escalationOnStr(boolean escalationOn) {

      String messageButton = ""
        if(escalationOn){
          Map<String, String> countParam = Map.of("tenantId", user.get("tenantId").asLong(), 
                                                "userId", user.get("tenantUserId").asLong(), 
                                                "ruleId", event.getId(), 
                                                "escalation", escalationOn)
          JsonNode times = api.call("mspbots-core", "/teams/messages/countEscalation", countParam)
          Integer escalationTime = Integer.parseInt(times.asText())
          String escalationTimeStr = (escalationTime+1) + (escalationTime > 1 ? " times" : " time")
          messageButton ="[ <span style='color:#999d9c;'>"+escalationTimeStr+"</span> this week. Threshold <span style='color:#999d9c;'>"+params.get("times").asText().replace(",","-")+"</span> ]"
      }

      return messageButton;
}


void sendMessageAndCheck(Map<String, String> param,boolean escalationOn,String triggerName) {

      JsonNode sendResult = api.call("mspbots-teams", "/message/send", "post", param)
      if(!sendResult.isEmpty() && escalationOn){
          Map<String, Object> escalationParams = Map.of(
              "tenantId", user.get("tenantId").asLong(), 
              "ruleId", event.getId(), 
              "triggerName",triggerName, 
              "times",  params.get("times").asText(), 
              "tenantUserId", user.get("tenantUserId").asLong())
          api.call("mspbots-teams", "/escalation/check", "post", escalationParams)
      }

}



