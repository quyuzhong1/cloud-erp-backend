package com.erp.server.workflow.handler;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.constant.DmpPullConstant;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.sdk.fs.config.FsProperties;
import com.lark.oapi.core.request.EventReq;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.CustomEventHandler;
import com.lark.oapi.event.EventDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 飞书回调事件处理
 * @author will
 * @date 2025/12/11 11:47
 */
@Slf4j
@Component
public class FsCallbackEventHandler {

    @Resource
    private FsProperties fsProperties;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    /**
     * 注册事件 Register event
     */
    public EventDispatcher EVENT_HANDLER = EventDispatcher.newBuilder(fsProperties.getVerificationToken(), null)
            .onCustomizedEvent("approval_instance", new CustomEventHandler() {
                @Override
                public void handle(EventReq event) throws Exception {
                    log.warn("收到飞书审批实例自定义事件: {}", Jsons.DEFAULT.toJson(event));
                    // 处理审批实例事件的逻辑

                }
            })
            .build();

    /**
     * dmp生成即时推送任务
     * @author will
     * @date 2025/12/11 11:47
     * @return void
     */
    private void ss () {
        //根据审批定义和审批实例id生成中台即时拉取任务
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setSystemCode(ApprovePlatformEnum.FEI_SHU.getCode());
        dto.setBillType(DmpPullConstant.INSTANCE_IDS);
        dto.setNextLevelId("");
        dto.setTaskType(DmpInputTaskTaskTypeEnum.HOTFIX.getCode());
        // 手动指定创建审批实例id
        Map<String, Object> map = Collections.singletonMap("instanceId","");
        dto.setDetailExtendJson(JSON.toJSONString(map));

        List<String> requestList = new ArrayList<>();
        try {
            requestList = dmpInoutTaskFeign.doHotfixReturnInputTask(Collections.singletonList(dto));
        }catch (Exception e){
            throw new ServiceException(e.getMessage());
        }
        if (CollUtil.isEmpty(requestList)) {
            throw new ServiceException("所选数据未找到同步信息");
        }
    }
}
