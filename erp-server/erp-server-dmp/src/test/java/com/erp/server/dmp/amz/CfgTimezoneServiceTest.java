package com.erp.server.dmp.amz;

import com.common.business.constant.RedisCacheConstants;
import com.erp.model.dmp.entity.CfgTimezoneEntity;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.service.CfgTimezoneService;
import com.erp.server.dmp.service.impl.TbTaskTypeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class CfgTimezoneServiceTest {

    @Resource
    private CfgTimezoneService cfgTimezoneService;
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void getTask() {
        List<CfgTimezoneEntity> list = cfgTimezoneService.listAndCache();
    }

    @Test
    public void checkCache() {
    }
}


