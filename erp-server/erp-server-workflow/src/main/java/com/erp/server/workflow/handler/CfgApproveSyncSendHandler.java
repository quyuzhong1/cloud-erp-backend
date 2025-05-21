package com.erp.server.workflow.handler;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.dto.FsBotParamsDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.CfgApproveSyncSyncPlatformEnum;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.enmu.UserIdTypeEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.service.CfgApproveSyncService;
import com.erp.server.workflow.service.CfgSettingService;
import com.erp.server.workflow.service.ProcessTaskManagementExtService;
import com.lark.oapi.service.approval.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author jack
 * @date 2025-05-21
 */
@Slf4j
@Component
public class CfgApproveSyncSendHandler {

    @Resource
    private ProcessTaskManagementExtService processTaskManagementExtService;
    @Resource
    private FsService fsService;


    /**
     * 发送撤销通知
     * @author jack
     * @date 2025-05-21
     */
    public void sendRecallNotice(List<String> summaries,List<ProcessTaskCcEntity> processTaskCcEntities , Map<String, ThirdUnionDTO> thirdUnionMap, CfgApproveSyncEntity cfgApproveSyncEntity,String pcLinkByEnv){
        if(CollUtil.isNotEmpty(processTaskCcEntities)){
            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            for (ProcessTaskCcEntity e : processTaskCcEntities) {
                if(thirdUnionMap.containsKey(e.getCcUserId())){
                    FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                    params.setTemplateId(ThirdConstants.TEMPLATE_ID_1015);
                    params.setUserId(thirdUnionMap.get(e.getCcUserId()).getThirdUserId());
                    params.setUuid(e.getId());
                    params.setApprovalName(cfgApproveSyncEntity.getTitle());
                    params.setTitleUserId(thirdUnionMap.get(e.getCreateUserId()).getThirdUserId());
                    params.setTitleUserIdType(UserIdTypeEnum.USERID.getCode());
                    params.setActionDetailUrl(pcLinkByEnv);
                    params.setActionCallbackUrl("");
                    params.setActionCallbackToken("");
                    params.setActionCallbackKey("");
                    params.setActionContext("");
                    params.setSummaries(summaries);
                    sendParams.add(params);
                }
            }

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    //构建请求体
                    Map<String, Object> bodyMap = fsService.buildRecallBodyMap(sendParam);
                    //发送消息
                    String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                    if(StringUtils.isBlank(messageId)){//发送失败
                        //todo 记录错误信息
                    }else{
                        //保持messageId 用于后续更新接口
                        ProcessTaskManagementExtEntity entity = new ProcessTaskManagementExtEntity();
                        entity.setProcessTaskManagementId(sendParam.getUuid());
                        entity.setMessageId(messageId);
                        entity.setSoucePlatform(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                        processTaskManagementExtService.save(entity);
                    }
                }
            }
        }
    }


    /**
     * 发送抄送通知
     * @author jack
     * @date 2025-05-21
     */
    public void sendCcNotice(List<String> summaries,List<ProcessTaskCcEntity> processTaskCcEntities , Map<String, ThirdUnionDTO> thirdUnionMap, CfgApproveSyncEntity cfgApproveSyncEntity,String pcLinkByEnv){
        if(CollUtil.isNotEmpty(processTaskCcEntities)){
            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            for (ProcessTaskCcEntity e : processTaskCcEntities) {
                if(thirdUnionMap.containsKey(e.getCcUserId())){
                    FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                    params.setTemplateId(ThirdConstants.TEMPLATE_ID_1016);
                    params.setUserId(thirdUnionMap.get(e.getCcUserId()).getThirdUserId());
                    params.setUuid(e.getId());
                    params.setApprovalName(cfgApproveSyncEntity.getTitle());
                    params.setTitleUserId(thirdUnionMap.get(e.getCreateUserId()).getThirdUserId());
                    params.setTitleUserIdType(UserIdTypeEnum.USERID.getCode());
                    params.setActionDetailUrl(pcLinkByEnv);
                    params.setActionCallbackUrl("");
                    params.setActionCallbackToken("");
                    params.setActionCallbackKey("");
                    params.setActionContext("");
                    params.setSummaries(summaries);
                    sendParams.add(params);
                }
            }

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    //构建请求体
                    Map<String, Object> bodyMap = fsService.buildCcBodyMap(sendParam);
                    //发送消息
                    String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                    if(StringUtils.isBlank(messageId)){//发送失败
                        //todo 记录错误信息
                    }else{
                        //保持messageId 用于后续更新接口
                        ProcessTaskManagementExtEntity entity = new ProcessTaskManagementExtEntity();
                        entity.setProcessTaskManagementId(sendParam.getUuid());
                        entity.setMessageId(messageId);
                        entity.setSoucePlatform(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                        processTaskManagementExtService.save(entity);
                    }
                }
            }
        }
    }

    /**
     * 发送审核通知
     * @author jack
     * @date 2025-05-21
     */
    public void sendApproveNotice(List<String> summaries, List<ProcessTaskManagementEntity> processTaskManagementEntities, Map<String, ThirdUnionDTO> thirdUnionMap, CfgApproveSyncEntity cfgApproveSyncEntity,String pcLinkByEnv) {
        if(CollUtil.isEmpty(processTaskManagementEntities)){
            List<FsBotParamsDTO.SendParamsDTO> sendParams = new ArrayList<>();
            for (ProcessTaskManagementEntity e : processTaskManagementEntities) {
                if(thirdUnionMap.containsKey(e.getCreateUserId())){
                    FsBotParamsDTO.SendParamsDTO params = new FsBotParamsDTO.SendParamsDTO();
                    params.setTemplateId(ThirdConstants.TEMPLATE_ID_1008);
                    params.setUserId(thirdUnionMap.get(e.getCurApproveId()).getThirdUserId());
                    params.setUuid(e.getId());
                    params.setApprovalName(cfgApproveSyncEntity.getTitle());
                    params.setTitleUserId(thirdUnionMap.get(e.getCreateUserId()).getThirdUserId());
                    params.setTitleUserIdType(UserIdTypeEnum.USERID.getCode());
                    params.setActionDetailUrl(pcLinkByEnv);
                    params.setActionCallbackUrl("");
                    params.setActionCallbackToken("");
                    params.setActionCallbackKey("");
                    params.setActionContext("");
                    params.setSummaries(summaries);
                    sendParams.add(params);
                }
            }

            if(CollUtil.isNotEmpty(sendParams)){
                for (FsBotParamsDTO.SendParamsDTO sendParam : sendParams) {
                    //构建请求体
                    Map<String, Object> bodyMap = fsService.buildApproveBodyMap(sendParam);
                    //发送消息
                    String messageId = fsService.sendErpApproveSyncMessage(bodyMap);
                    if(StringUtils.isBlank(messageId)){//发送失败
                        //todo 记录错误信息
                    }else{
                        //保持messageId 用于后续更新接口
                        ProcessTaskManagementExtEntity entity = new ProcessTaskManagementExtEntity();
                        entity.setProcessTaskManagementId(sendParam.getUuid());
                        entity.setMessageId(messageId);
                        entity.setSoucePlatform(CfgApproveSyncSyncPlatformEnum.FEISHU.getCode());
                        processTaskManagementExtService.save(entity);
                    }
                }
            }
        }
    }

}
