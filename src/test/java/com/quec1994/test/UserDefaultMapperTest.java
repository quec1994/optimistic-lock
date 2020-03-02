package com.quec1994.test;

import com.quec1994.entity.UserDefault;
import com.quec1994.mapper.user.UserDefaultMapper;
import org.apache.ibatis.binding.BindingException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDefaultMapperTest {

    @Autowired
    private UserDefaultMapper userDefaultMapper;
    private UserDefault user = new UserDefault();

    //初始化数据
    @Before
    public void initPojo() throws Exception {
        user.setId(1);
        user.setName("张三");
        user.setPassword("123456");
        user.setVersion(100L);
        userDefaultMapper.initData(user);
    }

    @After
    public void resetDatabaseTest() {
        user.setId(1);
        userDefaultMapper.resetData(user);
    }

    @Test
    public void updateUserPojoTest() {
        user.setName("张三三");
        user.setPassword("654321");
        Integer result = userDefaultMapper.updateUser(user);
        Assert.assertEquals(1L, Long.parseLong(result + ""));
    }

    @Test
    public void updateUserAtParamTest() {
        Integer result = userDefaultMapper.updateUser("张三三", "654321", 100L, 1);
        Assert.assertEquals(1L, Long.parseLong(result + ""));
    }

    @Test
    public void updateUserMapTest() {
        Map<Object, Object> param = new HashMap<>();
        param.put("name", "test");
        param.put("password", "test");
        param.put("version", 100L);
        param.put("id", 1);
        Integer result = userDefaultMapper.updateUser(param);
        Assert.assertEquals(1L, Long.parseLong(result + ""));
    }

    @Test(expected = BindingException.class)
    public void updateUserErrorTest() {
        Integer result = userDefaultMapper.updateUserError("test", "test", 100L, 1);
        Assert.assertEquals(1L, Long.parseLong(result + ""));
    }


}
