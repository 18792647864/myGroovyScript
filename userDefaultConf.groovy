
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


Map<String, String> param = Map.of("tenantId", payload.get("id").asLong())

println "start call rule api!!!mspbots-wise:::all"
JsonNode boardlistjson = api.call("mspbots-wise", "/boards/page", "get",param)

println "start call rule api!!!mspbots-wise:::priorities"
JsonNode prioritlistjson = api.call("mspbots-wise", "/boards/priorities", "get",param)
println "start call rule api!!!mspbots-wise:::end"

println "tenantId:"+payload.get("id").asLong()
JsonNode ruleCount = api.call("mspbots-core", "/ticket/rules/rulecount", "get",param)
println ruleCount
println "11111111111111"
if(ruleCount.get("result").asLong() > 0){
    println "tenantId:"+payload.get("id").asLong()+" already has rules"
    return
}





ObjectMapper mapper = new ObjectMapper();

ArrayNode rulesJsonArr = mapper.createArrayNode();
for (json in boardlistjson) {
    ObjectNode ruleObj = mapper.createObjectNode();
	

    //AGREEMENT_POINT_RULE	Agreement MRR Points
    ObjectNode argreementObj = mapper.createObjectNode();
    argreementObj.put("tenantId",payload.get("id").asLong())
    argreementObj.put("board",json.get("cwId").asText())
    argreementObj.put("enable",true)
    argreementObj.put("name","AGREEMENT_POINT_RULE")
    argreementObj.put("displayName","Agreement MRR Points")

    ArrayNode argreementParamJson = mapper.createArrayNode();


    for (int j = 0; j < 5 ; j++) {
        ObjectNode argreementParamsObj = mapper.createObjectNode();
        argreementParamsObj.put("index",1);
        argreementParamsObj.put("amount",j*1000);
        argreementParamsObj.put("points",(j+1)*100);
        argreementParamsObj.put("selected",true);
        argreementParamJson.add(argreementParamsObj)
    }

    argreementObj.put("params",argreementParamJson)

    rulesJsonArr.add(argreementObj)



    //ASSIGN_USER_GRADE_RULE	Assigned Points
    ObjectNode assignObj = mapper.createObjectNode();
    assignObj.put("tenantId",payload.get("id").asLong())
    assignObj.put("board",json.get("cwId").asText())
    assignObj.put("enable",true)
    assignObj.put("name","ASSIGN_USER_GRADE_RULE")
    assignObj.put("displayName","Assigned Points")

    ArrayNode assignParamJson = mapper.createArrayNode();

    ObjectNode assignParamsObj = mapper.createObjectNode();

    assignParamsObj.put("index",0)
    assignParamsObj.put("points","1000")
    assignParamsObj.put("selected",true)
    assignParamsObj.put("condition","If this ticket is assigned to current user")
    assignParamJson.add(assignParamsObj)

    assignObj.put("params",assignParamJson)

    rulesJsonArr.add(assignObj)


    //CONTACT_TITLE_POINT_RULE	Contact Title Points


    ObjectNode contactObj = mapper.createObjectNode();
    contactObj.put("tenantId",payload.get("id").asLong())
    contactObj.put("board",json.get("cwId").asText())
    contactObj.put("enable",true)
    contactObj.put("name","CONTACT_TITLE_POINT_RULE")
    contactObj.put("displayName","Contact Title Points")

    ArrayNode contactParamJson = mapper.createArrayNode();
    // If title=CEO, president, manager, partner, points = 100
    ObjectNode contactCeoParamsObj = mapper.createObjectNode();
    contactCeoParamsObj.put("index",0)
    contactCeoParamsObj.put("title","CEO")
    contactCeoParamsObj.put("points",100)
    contactCeoParamsObj.put("selected",true)
    contactParamJson.add(contactCeoParamsObj)


    ObjectNode contactPresidentParamsObj = mapper.createObjectNode();
    contactPresidentParamsObj.put("index",0)
    contactPresidentParamsObj.put("title","president")
    contactPresidentParamsObj.put("points",100)
    contactPresidentParamsObj.put("selected",true)
    contactParamJson.add(contactPresidentParamsObj)

    ObjectNode contactManagerParamsObj = mapper.createObjectNode();
    contactManagerParamsObj.put("index",0)
    contactManagerParamsObj.put("title","manager")
    contactManagerParamsObj.put("points",100)
    contactManagerParamsObj.put("selected",true)
    contactParamJson.add(contactManagerParamsObj)

    ObjectNode contactPartnerParamsObj = mapper.createObjectNode();
    contactPartnerParamsObj.put("index",0)
    contactPartnerParamsObj.put("title","partner")
    contactPartnerParamsObj.put("points",100)
    contactPartnerParamsObj.put("selected",true)
    contactParamJson.add(contactPartnerParamsObj) 



    contactObj.put("params",contactParamJson)

    rulesJsonArr.add(contactObj)


    //PRIORITY_POINT_RULE	Priority Points


    ObjectNode priorityObj = mapper.createObjectNode();
    priorityObj.put("tenantId",payload.get("id").asLong())
    priorityObj.put("board",json.get("cwId").asText())
    priorityObj.put("enable",true)
    priorityObj.put("name","PRIORITY_POINT_RULE")
    priorityObj.put("displayName","Priority Points")

    ArrayNode priorityParamJson = mapper.createArrayNode();


    for (int i = 0; i < prioritlistjson.size(); i++) {

        ObjectNode priority1ParamsObj = mapper.createObjectNode();

        def prioritName = prioritlistjson.get(i).get("name").asText()
        priority1ParamsObj.put("name",prioritName);
        priority1ParamsObj.put("index",i);

        if(prioritName.contains("Priority 1")){
            priority1ParamsObj.put("points",500);
        }else if (prioritName.contains("Priority 2")){
            priority1ParamsObj.put("points",400);
        }else if (prioritName.contains("Priority 3")){
            priority1ParamsObj.put("points",300);
        }else if (prioritName.contains("Priority 4")){
            priority1ParamsObj.put("points",200);
        }else{
            priority1ParamsObj.put("points",100);
        }

        priority1ParamsObj.put("selected",true);
        priority1ParamsObj.put("priorityId",prioritlistjson.get(i).get("cwId").asLong());
        priorityParamJson.add(priority1ParamsObj)
    }



    priorityObj.put("params",priorityParamJson)

    rulesJsonArr.add(priorityObj)


    //STATUS_POINTS_GRADE_RULE	Status   Points

    ObjectNode statusObj = mapper.createObjectNode();
    statusObj.put("tenantId",payload.get("id").asLong())
    statusObj.put("board",json.get("cwId").asText())
    statusObj.put("enable",true)
    statusObj.put("name","STATUS_POINTS_GRADE_RULE")
    statusObj.put("displayName","Status Points")

    ArrayNode statusParamJson = mapper.createArrayNode();

    JsonNode statusArr = json.get("extend").get("status")

    def i = 0;
    for (statusJson in statusArr) {

        i++;

        ObjectNode statusParamsObj = mapper.createObjectNode();

        statusParamsObj.put("index",i);
        statusParamsObj.put("status",statusJson.get("id").asLong());

        //selected
        //"closed","completed",“In progress”, "waiting", "scheduled",“cancelled”
        def statusName = statusJson.get("name").asText().toLowerCase()
        if(statusName.contains("close") || statusName.contains("waiting")
                 || statusName.contains("cancel")||statusName.contains("complete")){
            statusParamsObj.put("selected",false);
			
			
        }else {
            statusParamsObj.put("selected",true);
			
        } 
		statusParamsObj.put("name",statusName);
		
		





        //points
        ArrayNode pointsJsonArr = mapper.createArrayNode();

        ObjectNode time10Obj = mapper.createObjectNode();
        time10Obj.put("time",10)
        if(statusName.contains("new")) {
            time10Obj.put("point",500)
        }else if(statusName.contains("escalated")){
            time10Obj.put("point",600)
        }else{
            time10Obj.put("point",10)
        }

        pointsJsonArr.add(time10Obj);




        ObjectNode time20Obj = mapper.createObjectNode();
        time20Obj.put("time",20)
        time20Obj.put("point",10)
        pointsJsonArr.add(time20Obj);

        ObjectNode time30Obj = mapper.createObjectNode();
        time30Obj.put("time",30)
        time30Obj.put("point",10)
        pointsJsonArr.add(time30Obj);

        ObjectNode time60Obj = mapper.createObjectNode();
        time60Obj.put("time",60)
        time60Obj.put("point",10)
        pointsJsonArr.add(time60Obj);


        ObjectNode time120Obj = mapper.createObjectNode();
        time120Obj.put("time",120)
        time120Obj.put("point",10)
        pointsJsonArr.add(time120Obj);

        ObjectNode time240Obj = mapper.createObjectNode();
        time240Obj.put("time",240)
        time240Obj.put("point",10)
        pointsJsonArr.add(time240Obj);


        ObjectNode time480Obj = mapper.createObjectNode();
        time480Obj.put("time",480)
        time480Obj.put("point",10)
        pointsJsonArr.add(time480Obj);

        ObjectNode time720Obj = mapper.createObjectNode();
        time720Obj.put("time",720)
        time720Obj.put("point",10)
        pointsJsonArr.add(time720Obj);

        ObjectNode time1440Obj = mapper.createObjectNode();
        time1440Obj.put("time",1440)
        time1440Obj.put("point",10)
        pointsJsonArr.add(time1440Obj);

        ObjectNode time2880Obj = mapper.createObjectNode();
        time2880Obj.put("time",2880)
        time2880Obj.put("point",10)
        pointsJsonArr.add(time2880Obj);

        ObjectNode time4320Obj = mapper.createObjectNode();
        time4320Obj.put("time",4320)
        time4320Obj.put("point",10)
        pointsJsonArr.add(time4320Obj);
      
        ObjectNode time7200Obj = mapper.createObjectNode();
        time7200Obj.put("time",7200)
        time7200Obj.put("point",10)
        pointsJsonArr.add(time7200Obj);
            
        ObjectNode time11520Obj = mapper.createObjectNode();
        time11520Obj.put("time",11520)
        time11520Obj.put("point",10)
        pointsJsonArr.add(time11520Obj);
                  
        ObjectNode time20106Obj = mapper.createObjectNode();
        time20106Obj.put("time",20106)
        time20106Obj.put("point",10)
        pointsJsonArr.add(time20106Obj);
                        
        ObjectNode time43200Obj = mapper.createObjectNode();
        time43200Obj.put("time",43200)
        time43200Obj.put("point",10)
        pointsJsonArr.add(time43200Obj);

        statusParamsObj.put("points",pointsJsonArr);
        statusParamJson.add(statusParamsObj)

    }

    statusObj.put("params",statusParamJson)


    rulesJsonArr.add(statusObj)


    //UPDATE_TIME_GRADE_RULE	Updated Time  Points

    ObjectNode updateObj = mapper.createObjectNode();
    updateObj.put("tenantId",payload.get("id").asLong())
    updateObj.put("board",json.get("cwId").asText())
    updateObj.put("enable",true)
    updateObj.put("name","UPDATE_TIME_GRADE_RULE")
    updateObj.put("displayName","Updated Time Points")

    ArrayNode updateParamJson = mapper.createArrayNode();

    for (j in [4,3,2,1]) {
        ObjectNode updateParamsObj = mapper.createObjectNode();
        updateParamsObj.put("hours",j*24);
        updateParamsObj.put("index",j-1);
        updateParamsObj.put("title","Last update time > "+j*24+"H");
        updateParamsObj.put("points",j*100);
        updateParamsObj.put("selected",true);
        updateParamJson.add(updateParamsObj)
    }

    updateObj.put("params",updateParamJson)

    rulesJsonArr.add(updateObj)



    
    //TEAM_MATCHING_POINT_RULE	Team Matching Points

    ObjectNode teamMarchingObj = mapper.createObjectNode();
    teamMarchingObj.put("tenantId",payload.get("id").asLong())
    teamMarchingObj.put("board",json.get("cwId").asText())
    teamMarchingObj.put("enable",true)
    teamMarchingObj.put("name","TEAM_MATCHING_POINT_RULE")
    teamMarchingObj.put("displayName","Team Matching Points")

    ArrayNode teamMarchingParamJson = mapper.createArrayNode();

   
    ObjectNode teamMarchingParamsObj = mapper.createObjectNode();
    teamMarchingParamsObj.put("index",0);
    teamMarchingParamsObj.put("condition","If ticket team is in user's teams, then add points");
    teamMarchingParamsObj.put("points",10000);
    teamMarchingParamsObj.put("selected",true);
    teamMarchingParamJson.add(teamMarchingParamsObj)
    

    teamMarchingObj.put("params",teamMarchingParamJson)

    rulesJsonArr.add(teamMarchingObj)



    //SELECTED_ASSIGNED_POINT_RULE	Selected Assigned Points

    ObjectNode selectAssignedObj = mapper.createObjectNode();
    selectAssignedObj.put("tenantId",payload.get("id").asLong())
    selectAssignedObj.put("board",json.get("cwId").asText())
    selectAssignedObj.put("enable",true)
    selectAssignedObj.put("name","SELECTED_ASSIGNED_POINT_RULE")
    selectAssignedObj.put("displayName","Selected Assigned Points")

    ArrayNode selectAssignedParamsJson = mapper.createArrayNode();

   
    ObjectNode selectAssignedParamsObj = mapper.createObjectNode();

    selectAssignedParamsObj.put("statusIds",mapper.createArrayNode());
    selectAssignedParamsObj.put("identifiers",mapper.createArrayNode());
    selectAssignedParamsObj.put("points",0);
    selectAssignedParamsObj.put("selected",true);
    selectAssignedParamsJson.add(selectAssignedParamsObj)
    

    selectAssignedObj.put("params",selectAssignedParamsJson)

    rulesJsonArr.add(selectAssignedObj)


    //SCHEDULED_TIME_POINT_RULE	Scheduled Time Points

    ObjectNode scheduledTimeObj = mapper.createObjectNode();
    scheduledTimeObj.put("tenantId",payload.get("id").asLong())
    scheduledTimeObj.put("board",json.get("cwId").asText())
    scheduledTimeObj.put("enable",true)
    scheduledTimeObj.put("name","SCHEDULED_TIME_POINT_RULE")
    scheduledTimeObj.put("displayName","Scheduled Task Points")

    ArrayNode scheduledTimeParamJson = mapper.createArrayNode();

   
    ObjectNode scheduledTimeParamsObj = mapper.createObjectNode();
    scheduledTimeParamsObj.put("index",0);
    scheduledTimeParamsObj.put("condition","A scheduled task past the 15 minutes of start time.");
    scheduledTimeParamsObj.put("points",1000);
    scheduledTimeParamsObj.put("selected",true);
    scheduledTimeParamJson.add(scheduledTimeParamsObj)
    

    scheduledTimeObj.put("params",scheduledTimeParamJson)

    rulesJsonArr.add(scheduledTimeObj)



    //DUE_DATE_PASSED_POINT_RULE    Due Date Passed Points


    ObjectNode duedateObj = mapper.createObjectNode();
    duedateObj.put("tenantId",payload.get("id").asLong())
    duedateObj.put("board",json.get("cwId").asText())
    duedateObj.put("enable",true)
    duedateObj.put("name","DUE_DATE_PASSED_POINT_RULE")
    duedateObj.put("displayName","Due Date Passed Points")

    ArrayNode duedateParamJson = mapper.createArrayNode();

   
    ObjectNode duedateParamsObj = mapper.createObjectNode();
    duedateParamsObj.put("index",0);
    duedateParamsObj.put("condition","If ticket due date passed, then add points.");
    duedateParamsObj.put("points",1000);
    duedateParamsObj.put("selected",true);
    duedateParamJson.add(duedateParamsObj)
    

    duedateObj.put("params",duedateParamJson)

    rulesJsonArr.add(duedateObj)


    //CUSTOMER_RESPONDED_POINT_RULE  Customer Responded Points

    ObjectNode customerrepondedObj = mapper.createObjectNode();
    customerrepondedObj.put("tenantId",payload.get("id").asLong())
    customerrepondedObj.put("board",json.get("cwId").asText())
    customerrepondedObj.put("enable",true)
    customerrepondedObj.put("name","CUSTOMER_RESPONDED_POINT_RULE")
    customerrepondedObj.put("displayName","Customer Responded Points")

    ArrayNode customerrepondedParamJson = mapper.createArrayNode();

   
    ObjectNode customerrepondedParamsObj = mapper.createObjectNode();
    customerrepondedParamsObj.put("index",0);
    customerrepondedParamsObj.put("condition","If ticket has customer responded box checked,then add points.");
    customerrepondedParamsObj.put("points",1000);
    customerrepondedParamsObj.put("selected",true);
    customerrepondedParamJson.add(customerrepondedParamsObj)
    
    customerrepondedObj.put("params",customerrepondedParamJson)

    rulesJsonArr.add(customerrepondedObj)

    
    //MEMBER_SKILLS_POINT_RULE  Member Skills Points
    

    ObjectNode memberskillObj = mapper.createObjectNode();
    memberskillObj.put("tenantId",payload.get("id").asLong())
    memberskillObj.put("board",json.get("cwId").asText())
    memberskillObj.put("enable",true)
    memberskillObj.put("name","MEMBER_SKILLS_POINT_RULE")
    memberskillObj.put("displayName","Member Skills Points")

    ArrayNode memberskillParamJson = mapper.createArrayNode();

   
    ObjectNode memberskillParamsObj = mapper.createObjectNode();
    memberskillParamsObj.put("index",0);
    memberskillParamsObj.put("points",1000);
    memberskillParamsObj.put("selected",true);
    memberskillParamsObj.put("type","");
    memberskillParamsObj.put("subtypes",mapper.createArrayNode());
    memberskillParamsObj.put("memberSkills",mapper.createArrayNode());
    memberskillParamJson.add(memberskillParamsObj)
    
    memberskillObj.put("params", memberskillParamJson)

    rulesJsonArr.add(memberskillObj)


    //IN_WATCH_LIST_POINT_RULE  In Watch List Points
    

    ObjectNode inwatchObj = mapper.createObjectNode();
    inwatchObj.put("tenantId",payload.get("id").asLong())
    inwatchObj.put("board",json.get("cwId").asText())
    inwatchObj.put("enable",true)
    inwatchObj.put("name","IN_WATCH_LIST_POINT_RULE")
    inwatchObj.put("displayName","In Watch List Points")

    ArrayNode inwatchParamJson = mapper.createArrayNode();

   
    ObjectNode inwatchParamsObj = mapper.createObjectNode();
    inwatchParamsObj.put("index",0);
    inwatchParamsObj.put("condition","If the company is in the client watch list,then add points.");
    inwatchParamsObj.put("points",1000);
    inwatchParamsObj.put("selected",true);
    inwatchParamsObj.put("type","");
    inwatchParamJson.add(inwatchParamsObj)
    
    inwatchObj.put("params", inwatchParamJson)

    rulesJsonArr.add(inwatchObj)

    //SCHEDULED_STATUS_POINT_RULE,Scheduled Status points

    ObjectNode scheduledStatusObj = mapper.createObjectNode();
    scheduledStatusObj.put("tenantId",payload.get("id").asLong())
    scheduledStatusObj.put("board",json.get("cwId").asText())
    scheduledStatusObj.put("enable",true)
    scheduledStatusObj.put("name","SCHEDULED_STATUS_POINT_RULE")
    scheduledStatusObj.put("displayName","Scheduled Status points")

    ArrayNode scheduledStatusJson = mapper.createArrayNode();

   
    ObjectNode scheduledStatusParamsObj = mapper.createObjectNode();
    scheduledStatusParamsObj.put("index",0);
    scheduledStatusParamsObj.put("points","");
    scheduledStatusParamsObj.put("selected",true);
    scheduledStatusParamsObj.put("statusIds",mapper.createArrayNode());
    scheduledStatusParamsObj.put("minutes","");
    scheduledStatusParamsObj.put("compareType","");
    scheduledStatusJson.add(scheduledStatusParamsObj)
    
    scheduledStatusObj.put("params", scheduledStatusJson)

    rulesJsonArr.add(scheduledStatusObj)

    //IN_PROCESS_STATUS_POINT_RULE, In Progress Status Points

    ObjectNode inProcessStatusObj = mapper.createObjectNode();
    inProcessStatusObj.put("tenantId",payload.get("id").asLong())
    inProcessStatusObj.put("board",json.get("cwId").asText())
    inProcessStatusObj.put("enable",true)
    inProcessStatusObj.put("name","IN_PROCESS_STATUS_POINT_RULE")
    inProcessStatusObj.put("displayName","In Progress Status Points")

    ArrayNode inProcessStatusJson = mapper.createArrayNode();

    ObjectNode inProcessStatusParamsObj = mapper.createObjectNode();
    inProcessStatusParamsObj.put("index",0);
    inProcessStatusParamsObj.put("selected",true);
    inProcessStatusParamsObj.put("statusIds",mapper.createArrayNode());
    inProcessStatusParamsObj.put("activeUserpoints","");
    inProcessStatusParamsObj.put("otherUserPoints","");
    inProcessStatusJson.add(inProcessStatusParamsObj)
    
    inProcessStatusObj.put("params", inProcessStatusJson)

    rulesJsonArr.add(inProcessStatusObj)

    //USER_IN_CAN_NOT_SERVICE_RULE    User In "can not service" Role

    ObjectNode userInCanNotServiceObj = mapper.createObjectNode();
    userInCanNotServiceObj.put("tenantId",payload.get("id").asLong())
    userInCanNotServiceObj.put("board",json.get("cwId").asText())
    userInCanNotServiceObj.put("enable",true)
    userInCanNotServiceObj.put("name","USER_IN_CAN_NOT_SERVICE_RULE")
    userInCanNotServiceObj.put("displayName","User In can not service Role")

    ArrayNode userInCanNotServiceJson = mapper.createArrayNode();

    ObjectNode userInCanNotServiceParamsObj = mapper.createObjectNode();
    userInCanNotServiceParamsObj.put("index",0);
    userInCanNotServiceParamsObj.put("selected",true);
    userInCanNotServiceParamsObj.put("teamRoleIds",mapper.createArrayNode());
    userInCanNotServiceJson.add(userInCanNotServiceParamsObj)
    
    userInCanNotServiceObj.put("params", userInCanNotServiceJson)

    rulesJsonArr.add(userInCanNotServiceObj)

    //COMPANY_TO_TECH_MACTHING_POINT_RULE    Company To Tech Matching Points

    ObjectNode companyToTechMachingObj = mapper.createObjectNode();
    companyToTechMachingObj.put("tenantId",payload.get("id").asLong())
    companyToTechMachingObj.put("board",json.get("cwId").asText())
    companyToTechMachingObj.put("enable",true)
    companyToTechMachingObj.put("name","COMPANY_TO_TECH_MACTHING_POINT_RULE")
    companyToTechMachingObj.put("displayName","Company To Tech Matching Points")

    ArrayNode companyToTechMachingJson = mapper.createArrayNode();

    ObjectNode companyToTechMachingParamsObj = mapper.createObjectNode();
    companyToTechMachingParamsObj.put("index",0);
    companyToTechMachingParamsObj.put("points","1000");
    companyToTechMachingParamsObj.put("selected",true);
    companyToTechMachingParamsObj.put("users",mapper.createArrayNode());
    companyToTechMachingParamsObj.put("companyId","");
    companyToTechMachingJson.add(companyToTechMachingParamsObj)
    
    companyToTechMachingObj.put("params", companyToTechMachingJson)

    rulesJsonArr.add(companyToTechMachingObj)

}


api.call("mspbots-core", "ticket/rules", "post",rulesJsonArr)

return api.call("mspbots-wise", "manual/tickets", "get",Map.of("tenantId",payload.get("id").asLong()))

