package com.erp.server.oms.b2c;

import cn.hutool.json.JSONUtil;
import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.entity.CfgOperateLogFieldEntity;
import com.erp.model.oms.entity.KolB2bApplicationEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.CfgOperateLogFieldService;
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
    @Resource
    private CfgOperateLogFieldService logFieldService;

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


    @Test
    public void addLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(KolB2bApplicationEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("date").setFieldName("申请日期").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("type").setFieldName("寄样类型").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("customerId").setFieldName("客户").setClassPath(classPath).setType(8).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("applyRemark").setFieldName("申请说明").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("applyUserId").setFieldName("申请人").setClassPath(classPath).setType(4).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("applyDeptId").setFieldName("申请部门").setClassPath(classPath).setType(5).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("receiverName").setFieldName("收货人").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("telNumber").setFieldName("联系电话").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("receiveAddress").setFieldName("收货地址").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("addressType").setFieldName("地址类型").setClassPath(classPath).setType(2).setEnumClass("com.erp.model.oms.enums.CustomerAddressType")
                ,new CfgOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0).setEnumClass("")

        );
        logFieldService.saveBatch(logFields);
        System.out.println("sss");
    }
}
