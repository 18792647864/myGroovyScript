
import com.fasterxml.jackson.databind.JsonNode

import com.fasterxml.jackson.databind.ObjectMapper

class  ScriptInfo{

    def readFile(String filename){
        def filePath = filename+".json";

        File textfile = new File(filePath);

        return textfile.text;
    }

    def getPayload(){
        ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(readFile("payloadData"));
        return rootNode;
    }

    def getUser(){
        ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(readFile("userData"));
        return rootNode;
    }


    def getDatedata(){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(readFile("datedata"));
        return rootNode;
    }


    def getBoards(){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(readFile("boards"));
        return rootNode;
    }

    def getPriorities(){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(readFile("priorities"));
        return rootNode;
    }
}
