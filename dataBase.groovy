/**
 * @Date:Created by luzy on 2017/5/6.
 * @Description:
 */
import groovy.sql.Sql;
import java.sql.*;
/**
 * @author Leon
 *
 */
public class DataBase{
    public static void main(def args){
        //链接数据库
        def sql= Sql.newInstance("jdbc:postgresql://192.168.100.120:5432/mspbots","farmer","q1w2e3..",
                "org.postgresql.Driver")

        def db= new DataBase();

        //插入数据
        // db.insert(sql)

        //更新数据
       // db.update(sql)
        //如果没有返回结果则为false
       // println db.delete(sql);

        def word=db.netset(sql);//获得结果集
        //对结果集进行操作-注:同时影响表
        //word.add("username":"newname");
        //遍历结果集
        word.each{
            //打印username对应的列值
            println it.username;
            //如果用户名是hello则获取倒数第三个索引元素
            if(it.username=="hello"){
                println it.getAt(-3);
            }
        }
    }
    //查询
    def select(sql){
        //查询并遍历结果集
        sql.eachRow("select * from user"){
            println it.username;
        }
    }
    //插入
    def insert(sql){
        sql.execute("insert into user(username,password) values('hello','kitty')");
    }
    //删除
    def delete(sql){
        sql.execute("delete from user where id=1");
    }
    //更新
    def update(sql){
        sql.executeUpdate("update user set password='213456' where id=1");
    }
    //结果集
    def netset(sql){
        sql.dataSet("user");
    }
}
