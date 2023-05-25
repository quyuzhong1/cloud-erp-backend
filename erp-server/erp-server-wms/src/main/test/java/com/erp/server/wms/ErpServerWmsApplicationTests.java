import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.wms.entity.CfgOperateLogFieldEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.CfgOperateLogFieldService;
import com.erp.server.wms.service.QcResultService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lambda
 * @Classname ErpServerScmApplicationTests
 * @Description TODO
 * @Date 2023-04-03 9:21
 * @Created by yl
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerWmsApplicationTests {

    @Resource
    private CfgOperateLogFieldService logFieldService;

    @Resource
    private QcResultService qcResultService;


    @Test
    public void addLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(SoOutstockEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("soCode").setFieldName("销售订单号").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("deliveryOrgName").setFieldName("发货组织").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("packDate").setFieldName("打包日期").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("actualDeliveryDate").setFieldName("实际发货日期").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("trackNo").setFieldName("运输单号").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("warehouseKeeperName").setFieldName("仓管员").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("orderType").setFieldName("订单类型").setClassPath(classPath).setType(2).setEnumClass(BillTypeEnum.class.getName()),
                new CfgOperateLogFieldEntity().setField("warehouseName").setFieldName("仓库").setClassPath(classPath).setType(0).setEnumClass("")

        );
        logFieldService.saveBatch(logFields);
        System.out.println("sss");
    }


    @Test
    public void testSendMsg() {

        qcResultService.sendQcResultMsg(Arrays.asList("1653969938324520962"));
    }


}
