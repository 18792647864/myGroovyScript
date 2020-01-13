 
 import java.time.LocalDateTime
 


 LocalDateTime beginWork = LocalDateTime.now().minusHours(2)

LocalDateTime endWork = LocalDateTime.now()

LocalDateTime beginDate = LocalDateTime.now().minusHours(48)




if((beginWork.getDayOfYear() - beginDate.getDayOfYear()) != 0){
    beginWork.minusHours((beginWork.getDayOfYear() - beginDate.getDayOfYear())*24)
    endWork.minusHours((beginWork.getDayOfYear() - beginDate.getDayOfYear())*24) 
}

println beginWork.toString()
println endWork.toString()
