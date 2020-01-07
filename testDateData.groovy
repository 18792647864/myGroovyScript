import com.fasterxml.jackson.databind.JsonNode;
import java.text.SimpleDateFormat;
import java.util.Date;


ScriptInfo sinfo = new ScriptInfo();
JsonNode datedata =  sinfo.getDatedata();

for (int i = 0; i < datedata.size(); i++) {   
    JsonNode dataI=datedata.get(i);
    for (int j = i+1; j < datedata.size(); j++) {
        JsonNode dataJ=datedata.get(j);
        if (StrToDate(dataI.getEnd().asText()).compareTo(StrToDate(dataJ.getStart().asText())) >= 0 
                && dataI.getEnd().compareTo(dataJ.getEnd()) <= 0){
            dataI.setEnd(dataJ.getEnd());
            datedata.remove(j);
            j--;
        }else if(StrToDate(dataI.getEnd().asText()).compareTo(StrToDate(dataJ.getEnd().asText())) >= 0){
            datedata.remove(j);
            j--;
        }else{
            break;
        }
        i=j;
    }
}


def StrToDate(String str) {
  
   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
   Date date = null; 
  
   date = format.parse(str); 
   return date;
}
