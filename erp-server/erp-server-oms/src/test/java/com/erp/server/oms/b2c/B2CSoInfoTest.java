package com.erp.server.oms.b2c;

import cn.hutool.json.JSONUtil;

import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.dto.TransferDeclareProductDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.enums.WarehouseDeliveryTypeEnum;
import com.erp.model.wms.enums.WarehouseManageTypeEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    @Resource
    private SkuMappingService skuMappingService;


    //@Test
    public void splitSoInfo(){
        String soId = "1751895670669832193";
        List<SplitSkuDTO> skusBySoInfo = soB2cService.getTransferDeclareProductBySoInfo(soId);
        System.out.println(JSONUtil.parse(skusBySoInfo));
    }

    @Test
    public void test(){
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_RECCONDITION.getCode());
        //查询子单据id
        String fieldKeys = "FID,FNumber,FName,FFORBIDSTATUS,FDOCUMENTSTATUS";
        List<Map<String, Object>> list = apiUtils.queryList("", fieldKeys, 1000, 1, 0);
        System.out.println("-----------"+JSONUtil.toJsonStr(list));



    }
}
