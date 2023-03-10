package com.erp.server.plm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.BaseStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.LarkPressMessageDTO;
import com.erp.model.plm.dto.ProductShowDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.enums.LarkPressBusinessTypeEnum;
import com.erp.model.plm.enums.NoticeEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.sdk.fs.dto.SingleResultDTO;
import com.erp.sdk.fs.dto.LarkResultDTO;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.plm.constant.NoticeMessageConstant;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.server.plm.service.impl.NoticeMessageServiceImpl.taskCharge;

/**
 * 飞书消息实现类
 * @Author Cloud
 */
@Service
public class LarkMessageServiceImpl implements LarkMessageService {

    @Resource
    private NoticeMessageService noticeMessageService;
    @Resource
    private UserCancelNoticeService userCancelNoticeService;
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private FsService fsService;
    @Resource
    private ProjectTaskService projectTaskService;
    @Resource
    private WorkflowFeign workflowFeign;
    @Resource
    private ProductInfoService productInfoService;


    @Override
    public Boolean press(LarkPressMessageDTO dto) {
        // 查询业务相关内容
        LarkPressBusinessTypeEnum businessType = LarkPressBusinessTypeEnum.getByCode(dto.getBusinessType());
        String titleContent = null;
        String textContent = null;
        NoticeEnum noticeFlag = null;
        String processId = null;
        switch (businessType){
            case PRODUCT_TASK :
                noticeFlag = NoticeEnum.APPROVAL_TASK;
                // 根据业务id查询流程id
                ProjectTaskEntity task = projectTaskService.getById(dto.getBusinessId());
                if(null == task || !(TaskStateEnum.WAIT_CONFIRM.getCode().equals(task.getStatus())|| TaskStateEnum.APPROVAL_ING.getCode().equals(task.getStatus()))){
                    throw new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS);
                }
                ProductShowDTO productInfo = productInfoService.getProductInfo(task.getProductId());
                if(null == productInfo){
                    throw new ServiceException(ApiError.ERROR_95010);
                }
                processId = task.getProcessId();
                titleContent= String.format(NoticeMessageConstant.FINISH_WAIT_CONFIRM_PRESS, "加急");
                textContent = String.format(NoticeMessageConstant.TASK_PROJECT_CONTENT, task.getName(), productInfo.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());;
                break;
            default:
                throw new ServiceException(ApiError.ERROR_BUSINESS_NOT_EXIT);

        }
        // 根据流程id查询下级审核人
        List<AuditorHandleDTO> approveRecordShowList = workflowFeign.getHistoryTaskByProcessId(processId);
        if (CollectionUtils.isEmpty(approveRecordShowList)) {
            throw new ServiceException(ApiError.ERROR_95045);
        }
        List<AuditorHandleDTO> auditorHandleDTO = approveRecordShowList.stream()
                .filter(obj -> BaseStatusEnum.WAIT_AUDIT.getName().equals(obj.getHandContent()))
                .collect(Collectors.toList());
        // 发送飞书加急消息
        sendMessage(auditorHandleDTO, titleContent, textContent, noticeFlag, ThirdConstants.FS_MESSAGE_INTERACTIVE, Boolean.TRUE);
        return Boolean.TRUE;
    }

    @Override
    public Boolean sendMessage(List<AuditorHandleDTO> auditorHandleDTOList,String titleContent, String textContent,NoticeEnum noticeFlag,String msgType,Boolean isPress) {
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = noticeMessageService.getByNodeFlag(noticeFlag);
        if (Objects.isNull(notice)) {
            throw new ServiceException(ApiError.ERROR_MSG_IS_NOT_NULL);
        }

        //排除关闭通知的人员 并去重
        List<String> cancelNoticeUserIds = userCancelNoticeService.cancelNoticeUserIds(notice.getId());
        List<String> noticeUserIds = auditorHandleDTOList.stream().map(AuditorHandleDTO::getHandleUserId).collect(Collectors.toList());
        Map<String, String> userIdNameMap = auditorHandleDTOList.stream().collect(Collectors.toMap(AuditorHandleDTO::getHandleUserId, AuditorHandleDTO::getHandleUserName));
        List<String> noticeUserList = noticeUserIds.stream().filter(n -> !cancelNoticeUserIds.contains(n)).distinct().collect(Collectors.toList());
        //获取飞书的 unionId 与用户关系
        List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
        List<ThirdUnionDTO> noticeUnionList = unionIdList
                .stream()
                .filter(u -> noticeUserList.contains(u.getUserId()))
                .collect(Collectors.toList());
        Map<String, String> unionIdUserNameMap = noticeUnionList.stream()
                .collect(Collectors.toMap(ThirdUnionDTO::getThirdUnionId,
                        unionDto -> userIdNameMap.getOrDefault(unionDto.getUserId(), ""))
                );
        List<String> unionIds = noticeUnionList
                .stream()
                .map(ThirdUnionDTO::getThirdUnionId)
                .distinct()
                .collect(Collectors.toList());
//        Boolean isLog = false;
        //发送消息的结果
        for (String unionId : unionIds) {
            String userName = unionIdUserNameMap.get(unionId);
            if (StrUtil.isNotBlank(userName)) {
                titleContent = StrUtil.format(titleContent, userName);
            }
            LarkResultDTO larkResult = fsService.sendMessage(unionId, titleContent, textContent, msgType);
            // 催办
            if (isPress) {
                SingleResultDTO resultDTO = JSONObject.parseObject(larkResult.getData().toString(), SingleResultDTO.class);
                String messageId = resultDTO.getMessage_id();
                LarkResultDTO larkResultDTO = fsService.pressMessage(messageId, Collections.singletonList(unionId));
            }
        }

        // 写入日志
//        if (isLog) {
//            //消息通知记录
//            List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
//            List<NoticeMessageRecordEntity> messageRecordList = acceptUserIds
//                    .stream()
//                    .map(userId ->
//                            new NoticeMessageRecordEntity(titleContent,notice.getId(),NoticeEnum.DOC_CHANGES.getFlag(),userId,taskName,productId,name,chargeName));
//            //保存发送消息通知记录
//            noticeMessageRecordService.saveBatch(messageRecordList);
//        }

        return Boolean.TRUE;
    }


}
