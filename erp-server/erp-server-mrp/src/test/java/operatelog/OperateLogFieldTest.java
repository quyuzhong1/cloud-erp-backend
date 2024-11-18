package operatelog;

import com.erp.model.mrp.entity.CfgOperateLogFieldEntity;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.server.mrp.ErpServerMrpApplication;
import com.erp.server.mrp.service.CfgOperateLogFieldService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerMrpApplication.class})
public class OperateLogFieldTest {

    @Resource
    private CfgOperateLogFieldService cfgOperateLogFieldService;


    @Test
    public void testOperateLogAdd() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String  classPath = String.valueOf(CfgRuleWarehouseEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgOperateLogFieldEntity().setField("isEnableVirtual").setFieldName("是否启禁用虚拟仓").setClassPath(classPath).setType(1) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("isEnableOverseas").setFieldName("是否启禁用海外仓").setClassPath(classPath).setType(1) .setEnumClass("")
                );
        cfgOperateLogFieldService.saveBatch(logFields);
    }
}
