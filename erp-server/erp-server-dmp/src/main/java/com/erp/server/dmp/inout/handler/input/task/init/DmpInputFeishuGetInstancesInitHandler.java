package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.model.workflow.entity.ThirdProcessInstanceEntity;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.lark.oapi.service.approval.v4.model.GetInstanceResp;
import com.lark.oapi.service.approval.v4.model.GetInstanceRespBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputFeishuGetInstancesInitHandler extends DmpInputInitHandler {

    @Resource
    private FsService fsService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        //从manggodb获取数据
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        List<ParamData> paramDataList = new ArrayList<>();
        //获取第三方审批定义
        List<CfgThirdProcessEntity> cfgThirdProcessList = listCfgThirdProcess();

        //分页查询
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "feishu_instanceIds_data");

        //已启用的审批定义编码
        List<String> approvalCodeList = cfgThirdProcessList.stream().map(CfgThirdProcessEntity::getThirdProcessDefinitionCode).distinct().collect(Collectors.toList());
        //查询审批实例
        List<ThirdProcessInstanceEntity> instanceList = FeignQuery.create(ThirdProcessInstanceEntity.class)
                .in(ThirdProcessInstanceEntity::getApprovalCode,approvalCodeList)
                .list();

        //收集done的id,
        JSONArray result = new JSONArray();
        if (CollUtil.isNotEmpty(dmpInputMongoChildList)) {
            for (Map<String, Object> map : dmpInputMongoChildList) {
                String instanceId = (String) map.get("instance_id");
                String approvalCode = (String) map.get("ulanzi_approval_code");

                //第三方审核生成配置
                CfgThirdProcessEntity cfgThirdProcessEntity = cfgThirdProcessList.stream().filter(obj -> CharSequenceUtil.equals(obj.getThirdProcessDefinitionCode(), approvalCode)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(cfgThirdProcessEntity)) {
                    //创建并更新只拉取审核完成的数据
                    ThirdProcessInstanceEntity thirdProcessInstanceEntity = instanceList.stream().filter(obj -> CharSequenceUtil.equals(obj.getInstanceCode(), instanceId)).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(thirdProcessInstanceEntity)
                            && CharSequenceUtil.equals(FSApprovalStatusEnum.APPROVED.getCode(),thirdProcessInstanceEntity.getStatus())) {
                        log.warn("审批实例已审核通过无需拉取，instanceId: {}", instanceId);
                        continue;
                    }
                }
                try {
                    GetInstanceResp instance = fsService.getInstance(instanceId);
                    GetInstanceRespBody data = instance.getData();
					String jsonString = JSON.toJSONString(data);
					JSONObject parseObject = JSON.parseObject(jsonString);
					result.add(parseObject);
                } catch (Exception e) {
                    throw new ServiceException("调用飞书失败");
                }
            }
        }
        //更新原表中的数据
        DmpInputTaskInitDTO dmpInputTaskInitDTO = DmpInputTaskInitDTO.initMsg(result.toJSONString());
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        return dmpInputTaskInitDTOList;
    }


    /**
     * 获取第三方审批配置
     * @author will
     * @date 2025/7/14 16:30
     * @return List<ThirdProcessDefinitionEntity>
     */
    private List<CfgThirdProcessEntity> listCfgThirdProcess () {

        List<ThirdProcessDefinitionEntity> thirdProcessDefinitionList = FeignQuery.create(ThirdProcessDefinitionEntity.class)
                .eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode())
                .eq(ThirdProcessDefinitionEntity::getEnableStatus, Boolean.TRUE)
                .list();
        if (CollUtil.isEmpty(thirdProcessDefinitionList)) {
            log.warn("无可用的飞书审批定义，handler: {}", this.getClass().getSimpleName());
            return Collections.emptyList();
        }

        //查询第三方审批
        return FeignQuery.create(CfgThirdProcessEntity.class)
                .eq(CfgThirdProcessEntity::getEnableStatus , Boolean.TRUE)
                .list();
    }
}
