package com.erp.server.oms.b2c;

import cn.hutool.json.JSONUtil;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.TransferDeclareProductDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.enums.WarehouseDeliveryTypeEnum;
import com.erp.model.wms.enums.WarehouseManageTypeEnum;
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
        List<TransferDeclareProductDTO> skusBySoInfo = soB2cService.getTransferDeclareProductBySoInfo(soId);
        System.out.println(JSONUtil.parse(skusBySoInfo));
    }

    @Test
    public void test(){
        ListingInfoParamDTO param = new ListingInfoParamDTO();
        String platform="AliExpress";
        String shopId="1744978322774822913";
        List<String> skuIdList = Arrays.asList("1619184264417382401");
        List<String> shopIdList = Arrays.asList(shopId);
        param.setPlatform(platform);
        param.setSkuIdList(skuIdList);
        param.setShopIdList(shopIdList);
        String warehouseManageType = "selfBuild";
        //子件发货
        String singleDelivery = WarehouseDeliveryTypeEnum.SINGLE.getCode();
        //自建
        String selfBuild = WarehouseManageTypeEnum.SELF_BUILD.getCode();
        //是否自建 如果是就是要拆分
        Boolean isSelfBuild = selfBuild.equals(warehouseManageType);
//        List<ListingInfoWithSkuMappingDTO> skuMappingList = skuMappingService.findListDto(param);
//        System.out.println(JSONUtil.parse(skuMappingList));



    }
}
