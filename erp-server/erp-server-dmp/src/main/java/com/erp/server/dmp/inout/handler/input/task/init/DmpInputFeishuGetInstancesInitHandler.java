package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.model.workflow.entity.ThirdProcessInstanceEntity;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import com.lark.oapi.service.approval.v4.model.GetApprovalRespBody;
import com.lark.oapi.service.approval.v4.model.GetInstanceResp;
import com.lark.oapi.service.approval.v4.model.GetInstanceRespBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        //分页查询
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "feishu_instanceIds_data");
        //查询
        List<ThirdProcessInstanceEntity> thirdProcessInstanceEntityList = FeignQuery.create(ThirdProcessInstanceEntity.class)
                .isNotNull(ThirdProcessInstanceEntity::getApprovalCode)
                .ne(ThirdProcessInstanceEntity::getApprovalCode , "")
                .list();
        //收集done的id,
        thirdProcessInstanceEntityList.removeIf(entity -> FSApprovalStatusEnum.PENDING.getCode().equals(entity.getStatus()));
        List<String> ids = thirdProcessInstanceEntityList.stream().map(ThirdProcessInstanceEntity::getInstanceCode).collect(Collectors.toList());
        JSONArray result = new JSONArray();
        if (CollUtil.isNotEmpty(dmpInputMongoChildList)) {
            for (Map<String, Object> map : dmpInputMongoChildList) {
                String instanceid = (String) map.get("instance_id");
                //过滤done记录
                if (CollUtil.isNotEmpty(ids) && ids.contains(instanceid)) {
                    continue;
                }
                try {
                    GetInstanceResp instance = fsService.getInstance(instanceid);
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
        dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(result.toJSONString()));
        return dmpInputTaskInitDTOList;
    }
}
