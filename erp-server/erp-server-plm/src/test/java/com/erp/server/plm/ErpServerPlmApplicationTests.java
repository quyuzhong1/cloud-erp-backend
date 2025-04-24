package com.erp.server.plm;

import cn.hutool.json.JSONUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.server.plm.service.BomSkuService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerPlmApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
class ErpServerPlmApplicationTests {

    @Resource
    private BomSkuService bomSkuService;


}
