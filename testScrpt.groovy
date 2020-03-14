import com.fasterxml.jackson.databind.JsonNode


ScriptInfo sinfo = new ScriptInfo();
JsonNode payload =  sinfo.getPayload();
JsonNode user =  sinfo.getUser();
JsonNode boards =  sinfo.getBoards();
JsonNode priorities =  sinfo.getPriorities();



println payload
println user
println boards
println priorities
