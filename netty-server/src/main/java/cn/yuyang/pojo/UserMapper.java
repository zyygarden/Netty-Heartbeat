package cn.yuyang.pojo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select count(*) from user where username = #{username} and password = #{password}")
    Integer login(@Param("username") String username, @Param("password") String password);
}