package com.erp.server.workflow.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.DmpPullConstant;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.workflow.dto.FsCallbackEventDTO;
import com.erp.model.workflow.dto.FsCallbackUserEventDTO;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.sdk.fs.config.FsProperties;
import com.erp.server.workflow.constant.FsEventConstant;
import com.lark.oapi.core.request.EventReq;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.CustomEventHandler;
import com.lark.oapi.event.EventDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

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

    private EventDispatcher eventDispatcher;


    @PostConstruct
    public void init() {
        /**
         * 注册事件 Register event
         */
        this.eventDispatcher = EventDispatcher.newBuilder(fsProperties.getVerificationToken(), null)
                .onCustomizedEvent(FsEventConstant.APPROVAL_INSTANCE_EVENT, new CustomEventHandler() {
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书审批实例状态变更事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                        // 处理审批实例事件的逻辑
                        approvalInstanceHandle(event);
                    }
                }).onCustomizedEvent(FsEventConstant.APPROVAL_TASK_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书审批任务状态变更事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.APPROVAL_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书审批通过事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.WORK_APPROVAL_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书加班事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.LEAVE_APPROVAL_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书请假事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.SHIFT_APPROVAL_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书换班事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.APPROVAL_CC_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书审批抄送事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.TRIP_APPROVAL_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书出差事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.OUT_APPROVAL_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书外出事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.REMEDY_APPROVAL_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到飞书补卡事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                    }
                }).onCustomizedEvent(FsEventConstant.USER_CREATED_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到员工入职事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                        // 处理员工入职事件的逻辑
                        getUserHandle(event,FsEventConstant.USER_CREATED_EVENT);
                    }
                }).onCustomizedEvent(FsEventConstant.USER_DELETED_EVENT, new CustomEventHandler() {  // 新增审批任务事件
                    @Override
                    public void handle(EventReq event) throws Exception {
                        log.warn("收到员工离职事件:event= {},fsProperties = {}", Jsons.DEFAULT.toJson(event),fsProperties);
                        // 处理员工离职事件的逻辑
                        getUserHandle(event,FsEventConstant.USER_DELETED_EVENT);
                    }
                })
                .build();
    }


    /**
     * dmp生成即时推送任务
     * 处理员工入职、离职状态
     * @author jack
     * @date 2026-01-09
     * @return void
     */
    public void getUserHandle(EventReq event, String type) {
        String plain = event.getPlain();
        JSONObject jsonObject = JSON.parseObject(plain);
        JSONObject thisEvent = jsonObject.getJSONObject("event");
        if (ObjUtil.isEmpty(thisEvent)) {
            return;
        }
        FsCallbackUserEventDTO.UserDeletedDTO bean = BeanUtil.toBean(thisEvent, FsCallbackUserEventDTO.UserDeletedDTO.class);
        if (!CharSequenceUtil.equals(fsProperties.getClientId(), bean.getHeader().getAppId())) {
            log.warn("收到员工事件，应用ID未匹配，跳过处理,eventType={},clientId={}，appId={}",bean.getHeader().getEventType(),fsProperties.getClientId(), bean.getHeader().getAppId());
            return;
        }
        if (!CharSequenceUtil.equals(type,bean.getHeader().getEventType())) {
            log.warn("收到员工事件，事件类型未匹配，跳过处理,eventType={}", bean.getHeader().getEventType());
            return;
        }
        //根据审批定义和审批实例id生成中台即时拉取任务
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setSystemCode(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
        dto.setBillType(DmpPullConstant.USER_DELETED);
        dto.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());

        Map<String, Object> map = new HashMap<>();
        map.put("userId",bean.getEvent().getObject().getUserId());
        map.put("eventId",bean.getHeader().getEventId());
        map.put("eventType",bean.getHeader().getEventType());
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

    /**
     * dmp生成即时推送任务
     * @author will
     * @date 2025/12/11 11:47
     * @return void
     */
    public void approvalInstanceHandle (EventReq event) {
        String plain = event.getPlain();
        JSONObject jsonObject = JSON.parseObject(plain);
        JSONObject thisEvent = jsonObject.getJSONObject("event");
        if (ObjUtil.isEmpty(thisEvent)) {
            return;
        }
        FsCallbackEventDTO.ApprovalInstanceEventDTO bean = BeanUtil.toBean(thisEvent, FsCallbackEventDTO.ApprovalInstanceEventDTO.class);
        if (!CharSequenceUtil.equals(fsProperties.getClientId(), bean.getAppId())) {
            log.warn("飞书审批实例自定义事件，应用ID未匹配，跳过处理,clientId={}，appId={}",fsProperties.getClientId(), bean.getAppId());
            return;
        }
        if (!CharSequenceUtil.equals(FsEventConstant.APPROVAL_INSTANCE_EVENT,bean.getType())) {
            log.warn("飞书审批实例自定义事件，事件类型未匹配，跳过处理，type={}", bean.getType());
            return;
        }
        //根据审批定义和审批实例id生成中台即时拉取任务
        DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
        dto.setSystemCode(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
        dto.setBillType(DmpPullConstant.INSTANCE_IDS);
        dto.setNextLevelId(bean.getApprovalCode());
        dto.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
        // 手动指定创建审批实例id
        Map<String, Object> map = Collections.singletonMap("instanceId",bean.getInstanceCode());
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



    public EventDispatcher getEventHandler() {
        return eventDispatcher;
    }
}
