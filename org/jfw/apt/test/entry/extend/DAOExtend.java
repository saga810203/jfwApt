package org.jfw.apt.test.entry.extend;
public class DAOExtend extends org.jfw.apt.test.entry.DAO {DAOExtend dAOExtend = new DAOExtend();

@Override
 public int insert(java.sql.Connection con,org.jfw.apt.test.entry.TTable table) throws java.sql.SQLException {
String sql="INSERT INTO T_TABLE (CREATE_TIME,MODIFY_TIME,ENABLE,NAME,OLD,SEX,COMMENT) values ( TO_DATA('YYYYMMDDHH24MISS',SYSDATE),TO_DATA('YYYYMMDDHH24MISS',SYSDATE),?,?,?,?,?)";
java.sql.PreparedStatement ps = con.prepareStatement(sql);
try{int paramIndex = 1; 
if(table.isEnable()){
ps.setString(paramIndex++,"1");
}else{
ps.setString(paramIndex++,"0");
}
ps.setString(paramIndex++,table.getName());
ps.setInt(paramIndex++,table.getOld());
ps.setByte(paramIndex++,table.getSex());
ps.setString(paramIndex++,table.getComment());
return ps.executeUpdate();}finally{
try{ps.close();}catch(Exception e){}
}
}

@Override
 public int update(java.sql.Connection con,org.jfw.apt.test.entry.TTable table) throws java.sql.SQLException {
String sql="UPDATE T_TABLE SET MODIFY_TIME=TO_DATA('YYYYMMDDHH24MISS',SYSDATE),ENABLE=?,OLD=?,SEX=?,COMMENT=? WHERE NAME=?";
java.sql.PreparedStatement ps = con.prepareStatement(sql);
try{int paramIndex = 1; 
if(table.isEnable()){
ps.setString(paramIndex++,"1");
}else{
ps.setString(paramIndex++,"0");
}
ps.setInt(paramIndex++,table.getOld());
ps.setByte(paramIndex++,table.getSex());
ps.setString(paramIndex++,table.getComment());
ps.setString(paramIndex++,table.getName());
return ps.executeUpdate();}finally{
try{ps.close();}catch(Exception e){}
}
}

}