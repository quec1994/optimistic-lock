package com.quec1994.test;

import com.quec1994.entity.UserVersion;
import com.quec1994.mapper.user.UserVersionMapper;
import org.apache.ibatis.binding.BindingException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserVersionMapperTest {

    private UserVersion user = new UserVersion();
    @Autowired
    private UserVersionMapper userVersionMapper = null;

    // 每次测试前都将数据库中的id为100的User的version设置成100
    @Before
    public void initPojo() throws Exception {
        user.setId(1);
        user.setName("李四");
        user.setPassword("123456");
        user.setMyVersion(1L);
        userVersionMapper.initData(user);
    }

    @After
    public void resetDatabaseTest() {
        user.setId(1);
        userVersionMapper.resetData(user);
    }

    @Test
    public void updateUserPojoTest() {
        user.setName("李四四");
        Integer result = userVersionMapper.updateUser(user);
        Assert.assertEquals(1L, Long.parseLong(result + ""));
    }

    @Test
    public void updateUserAtParamTest() {
        Integer result = userVersionMapper.updateUser("李四四", "pass", 1024L, 1);
        Assert.assertEquals(1L, Long.parseLong(result + ""));
    }


    @Test
    public void updateUserMapTest() {
        Map<Object, Object> param = new HashMap<>();
        param.put("name", "test");
        param.put("password", "test");
        param.put("myVersion", 1024L);
        param.put("id", 1);
        Integer result = userVersionMapper.updateUser(param);
        Assert.assertEquals(1L, Long.parseLong(result + ""));

    }

    @Test(expected = BindingException.class)
    public void updateUserErrorTest() {
        Integer result = userVersionMapper.updateUserError("test", "test", 100L, 1);
        Assert.assertEquals(1L, Long.parseLong(result + ""));
    }

    @Test
    public void updateUserListTest() {
        List<UserVersion> userlist = new ArrayList<UserVersion>();

        UserVersion user2 = new UserVersion();
        user2.setId(2);
        user2.setName("批量更新");
        user2.setPassword("test");
        user2.setMyVersion(1L);
        userVersionMapper.initData(user2);

        user.setName("第一条批量更新");
        user2.setName("第二条批量更新");
        userlist.add(user);
        userlist.add(user2);

        Integer result = userVersionMapper.updateUserList(userlist);
        Assert.assertEquals(2L, Long.parseLong(result + ""));

    }


}
