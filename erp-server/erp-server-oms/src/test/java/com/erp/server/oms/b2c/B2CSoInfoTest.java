package com.erp.server.oms.b2c;

import cn.hutool.json.JSONUtil;
import com.erp.model.oms.dto.TransferDeclareProductDTO;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName B2CSoInfoTest
 * @description: TODO
 * @date 2024年01月30日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class B2CSoInfoTest {

    @Resource
    private SoB2cService soB2cService;

    @Test
    public void splitSoInfo(){
        String soId = "1751895670669832193";
        List<TransferDeclareProductDTO> skusBySoInfo = soB2cService.getSkusBySoInfo(soId);
        System.out.println(JSONUtil.parse(skusBySoInfo));
    }
}
