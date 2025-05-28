package com.erp.server.scm;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.server.workflow.service.ProcessTaskService;
import com.lark.oapi.Client;
import com.lark.oapi.service.approval.v4.model.GetApprovalReq;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname ErpServerScmApplicationTests

 * @Date 2023-04-03 9:21
 * @Created by yl
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWorkApplicationTests.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerWorkApplicationTests {

//    @Resource
//    private ProcessTaskService processTaskService;

//    @Test
//    public void testAuditorHandleDTO() {
//        List<AuditorHandleDTO> resultList =  processTaskService.getHistoryTaskByProcessId("fd226ba4-d76a-11ed-901e-2ed55b4d95a2");
//        log.warn(JSON.toJSONString(resultList));
//    }

    @Test
    public void testCreateInstance() throws Exception {
//        ThirdProcessDefinitionEntity body = thirdProcessDefinitionService.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode()).eq(ThirdProcessDefinitionEntity::getApprovalCode, "7DCF7A99-6E25-4A24-8386-5E2639712983").eq(ThirdProcessDefinitionEntity::getIsDeleted, false));
        // 构建client
        Client client = Client.newBuilder("cli_a8858e6f51b95013", "dMU3PHMMoC172dOxFdn8agJeQvYpKYd3").build();

        // 创建请求对象
        GetApprovalReq req = GetApprovalReq.newBuilder()
                .approvalCode("7DCF7A99-6E25-4A24-8386-5E2639712983")
                .locale("zh-CN")
                .withAdminId(false)
                .userIdType("open_id")
                .build();

        // 发起请求
        GetApprovalResp resp = client.approval().v4().approval().get(req);
        String form = resp.getData().getForm();
        //解析body
        JSONArray formArray = JSONUtil.parseArray(form);

    }
        public JSONArray mapForm(String formJson,
                List<CfgProcessFieldMapEntity> fieldMapList,
                List<CfgProcessValueMapEntity> valueMapList) {

            JSONArray formArray = JSONUtil.parseArray(formJson);
            JSONArray resultArray = new JSONArray();

            // 构建字段映射
            Map<String, CfgProcessFieldMapEntity> fieldMapByThirdFieldId = fieldMapList.stream()
                    .collect(Collectors.toMap(CfgProcessFieldMapEntity::getThirdFieldId, f -> f));

            // 构建选项映射
            Map<Long, List<CfgProcessValueMapEntity>> valueMapByFieldMapId = valueMapList.stream()
                    .collect(Collectors.groupingBy(CfgProcessValueMapEntity::getFieldMapId));

        for (Object obj : formArray) {
            JSONObject item = (JSONObject) obj;
            String type = item.getStr("type");
            String id = item.getStr("id");

            if ("fieldList".equals(type)) {
                JSONArray detailList = item.getJSONArray("value");
                JSONArray mappedDetailList = new JSONArray();

                for (Object rowObj : detailList) {
                    JSONArray rowArray = (JSONArray) rowObj;
                    JSONArray mappedRowArray = new JSONArray();

                    for (Object colObj : rowArray) {
                        JSONObject field = (JSONObject) colObj;
                        String fieldId = field.getStr("id");

                        CfgProcessFieldMapEntity fieldMap = fieldMapByThirdFieldId.get(fieldId);
                        Object value = field.get("value");
                        Object mappedValue = mapValue(value, fieldMap, valueMapByFieldMapId.get(fieldMap.getId()));

                        JSONObject mappedField = new JSONObject();
                        mappedField.set("id", fieldId);
                        mappedField.set("type", field.getStr("type"));
                        mappedField.set("value", mappedValue);
                        mappedRowArray.add(mappedField);
                    }
                    mappedDetailList.add(mappedRowArray);
                }

                JSONObject mappedItem = new JSONObject();
                mappedItem.set("id", id);
                mappedItem.set("type", type);
                mappedItem.set("value", mappedDetailList);
                resultArray.add(mappedItem);
            } else {
                CfgProcessFieldMapEntity fieldMap = fieldMapByThirdFieldId.get(id);
                Object value = item.get("value");

                Object mappedValue = mapValue(value, fieldMap, valueMapByFieldMapId.get(
                        fieldMap != null ? fieldMap.getId() : null));

                JSONObject mappedItem = new JSONObject();
                mappedItem.set("id", id);
                mappedItem.set("type", type);
                mappedItem.set("value", mappedValue);
                resultArray.add(mappedItem);
            }
        }

        return resultArray;
    }


    private Object mapValue(Object originalValue,
                            CfgProcessFieldMapEntity fieldMap,
                            List<CfgProcessValueMapEntity> valueMaps) {
        if (originalValue == null || fieldMap == null || valueMaps == null) return originalValue;

        String type = fieldMap.getThirdFieldType();
        if ("radioV2".equals(type)) {
            for (CfgProcessValueMapEntity vm : valueMaps) {
                if (vm.getThirdValue().equals(originalValue)) {
                    return vm.getSysValue();
                }
            }
        } else if ("checkboxV2".equals(type) && originalValue instanceof JSONArray) {
            JSONArray arr = (JSONArray) originalValue;
            JSONArray result = new JSONArray();
            for (Object val : arr) {
                for (CfgProcessValueMapEntity vm : valueMaps) {
                    if (vm.getThirdValue().equals(val)) {
                        result.add(vm.getSysValue());
                        break;
                    }
                }
            }
            return result;
        }
        return originalValue;
    }

    @Data
    public static class CfgProcessFieldMapEntity {
        private Long id;
        private String thirdFieldId;
        private String thirdFieldType;
        private String sysField;
        private Boolean isDetailField;
        private String thirdFieldFieldListId; // 明细控件 ID
    }

    @Data
    public static class CfgProcessValueMapEntity {
        private Long fieldMapId;
        private String thirdValue;
        private String sysValue;
    }
}
