package com.erp.server.plm.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.common.business.constant.ThirdConstants;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BaseStatusEnum;
import com.common.business.service.impl.RedisService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.plm.dto.LarkPressMessageDTO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.dto.ProductShowDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.enums.LarkPressBusinessTypeEnum;
import com.erp.model.plm.enums.NoticeEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.sdk.fs.dto.LarkResultDTO;
import com.erp.sdk.fs.dto.SingleResultDTO;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.plm.constant.NoticeMessageConstant;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.hutool.core.util.StrUtil.format;
import static cn.hutool.core.util.StrUtil.isNotBlank;
import static com.alibaba.fastjson.JSON.parseObject;
import static com.erp.server.plm.service.impl.NoticeMessageServiceImpl.TASK_CHARGE;

/**
 * 飞书消息实现类
 *
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

    @Resource
    private RedisService redisService;

    @Resource
    private PilotApplicationService pilotApplicationService;

    @Override
    public Boolean press(LarkPressMessageDTO dto) {
        // 查询业务相关内容
        LarkPressBusinessTypeEnum businessType = LarkPressBusinessTypeEnum.getByCode(dto.getBusinessType());
        String titleContent = null;
        String textContent = null;
        NoticeEnum noticeFlag = null;
        String processId = null;
        String redisBaseKey = RedisKeyConstant.PRESS;
        String redisKey = dto.getBusinessType() + "_" + dto.getBusinessId() + redisBaseKey;
        //是否存在
        String redisValue = redisService.getCacheObject(redisKey);
        if (StringUtils.isNotBlank(redisValue)) {
            throw new ServiceException(ApiError.ERROR_95178);
        }
        List<LarkPressMessageDTO.SendUserInfo> pressUserList = new ArrayList<>(10);
        switch (businessType) {
            case PILOT_APPLICATION:
                PilotApplicationDTO.ApprovePilotNoticeDTO entity = pilotApplicationService.getPilotApplicationNoticeData(dto.getBusinessId());
                if(null != entity) {
                    if (!entity.getApproveStatus().getCode().equals(ApproveStatusEnum.APPROVE_ING.getCode())) {
                        throw new ServiceException(ApiError.ERROR_95273);
                    }
                    noticeFlag = NoticeEnum.AUDIT_PILOT_APPLICATION;
                    long timeInMillis = Calendar.getInstance().getTimeInMillis();
                    dto.setBusinessName(businessType.getName()+"【"+entity.getCode()+"】###"+timeInMillis);
                    //标题
                    titleContent = String.format(NoticeMessageConstant.AUDIT_PILOT_PRESS_TITLE,entity.getUserName(),entity.getCode());
                    //消息内容
                    String chargeName = Arrays.asList(entity.getChargeName().split(",")).stream().distinct().collect(Collectors.joining(";"));
                    String skuNo = Arrays.asList(entity.getSkuNo().split(",")).stream().distinct().collect(Collectors.joining(";"));
                    textContent = String.format(NoticeMessageConstant.AUDIT_PILOT_PRESS_CONTENT , "试产量产催办" , chargeName, skuNo);

                    //被通知人
                    //根据节点标示获取到通知消息实体
                    NoticeMessageEntity notice = noticeMessageService.getByNodeFlag(noticeFlag);
                    if (Objects.isNull(notice)) {
                        throw new ServiceException(ApiError.ERROR_MSG_IS_NOT_NULL);
                    }
                    //根据单据id查询审核流程
                    List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessId(Collections.singletonList(dto.getBusinessId()));
                    List<String> curApproveIds = processTaskManagementList.stream().filter(req -> req.getBusinessId().equals(dto.getBusinessId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveId).distinct().collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(curApproveIds)){
                        for (String userId : curApproveIds) {
                            LarkPressMessageDTO.SendUserInfo sendUserInfo = new LarkPressMessageDTO.SendUserInfo();
                            sendUserInfo.setUserId(userId);
                            sendUserInfo.setUserName("");
                            pressUserList.add(sendUserInfo);
                        }
                    }else {
                        List<String> handleUserIdList = noticeMessageService.getSetPilotNotice(notice, entity, Boolean.FALSE);
                        if(CollectionUtils.isNotEmpty(handleUserIdList)) {
                            for (String userId : handleUserIdList) {
                                LarkPressMessageDTO.SendUserInfo sendUserInfo = new LarkPressMessageDTO.SendUserInfo();
                                sendUserInfo.setUserId(userId);
                                sendUserInfo.setUserName("");
                                pressUserList.add(sendUserInfo);
                            }
                        }
                    }
                }
                break;
            case PRODUCT_TASK:
                List<Integer> statusList = new ArrayList<>(3);
                statusList.add(TaskStateEnum.APPROVAL_PASS.getCode());
                statusList.add(TaskStateEnum.APPROVAL_NO_PASS.getCode());
                statusList.add(TaskStateEnum.FINISH.getCode());
                noticeFlag = NoticeEnum.APPROVAL_TASK;
                // 根据业务id查询流程id
                ProjectTaskEntity task = projectTaskService.getById(dto.getBusinessId());
                if (null == task || statusList.contains(task.getStatus())) {
                    throw new ServiceException(ApiError.ERROR_TASK_AUDIT_STATUS);
                }
                ProductShowDTO productInfo = productInfoService.getProductInfo(task.getProductId());
                if (null == productInfo) {
                    throw new ServiceException(ApiError.ERROR_95010);
                }
                dto.setBusinessName(task.getName());
                processId = task.getProcessId();
                titleContent = String.format(NoticeMessageConstant.TASK_CHARGE_PRESS, "加急");
                textContent = String.format(NoticeMessageConstant.TASK_PROJECT_CONTENT, task.getName(), productInfo.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                //当没有流程就要给任务负责人发消息
                if (StringUtils.isEmpty(processId)) {
                    //任务负责人
                    String taskChargeId = task.getChargeId();
                    String taskChargeName = task.getChargeName();
                    String[] taskChargeIdList = taskChargeId.split(",");
                    String[] taskChargeNameList = taskChargeName.split(",");
                    for (int i = 0; i < taskChargeIdList.length; i++) {
                        LarkPressMessageDTO.SendUserInfo sendUserInfo = new LarkPressMessageDTO.SendUserInfo();
                        sendUserInfo.setUserId(taskChargeIdList[i]);
                        if (i < taskChargeNameList.length) {
                            sendUserInfo.setUserName(taskChargeNameList[i]);
                        } else {
                            sendUserInfo.setUserName("");
                        }
                        pressUserList.add(sendUserInfo);
                    }
                }else{
                    titleContent = String.format(NoticeMessageConstant.FINISH_WAIT_CONFIRM_PRESS, "加急");
                    // 根据流程id查询下级审核人
                    if (StringUtils.isNotBlank(processId)) {
                        List<AuditorHandleDTO> approveRecordShowList = workflowFeign.getHistoryTaskByProcessId(processId);
                        List<AuditorHandleDTO> auditorHandleList = approveRecordShowList.stream()
                                .filter(obj -> BaseStatusEnum.WAIT_AUDIT.getName().equals(obj.getHandContent()))
                                .collect(Collectors.toList());
                        for (AuditorHandleDTO item : auditorHandleList) {
                            LarkPressMessageDTO.SendUserInfo sendUserInfo = new LarkPressMessageDTO.SendUserInfo();
                            sendUserInfo.setUserId(item.getHandleUserId());
                            sendUserInfo.setUserName(item.getHandleUserName());
                            pressUserList.add(sendUserInfo);
                        }

                    }
                }
                break;
            default:
                throw new ServiceException(ApiError.ERROR_BUSINESS_NOT_EXIT);

        }
        // 发送飞书加急消息
        sendMessage(pressUserList, titleContent, textContent, noticeFlag, ThirdConstants.FS_MESSAGE_INTERACTIVE, Boolean.TRUE);
        redisService.setCacheObject(redisKey, dto.getBusinessName(), 30L, TimeUnit.MINUTES);
        return Boolean.TRUE;
    }


    @Override
    public List<String> pilotListPress(List<LarkPressMessageDTO> list) {
        List<String> result = new ArrayList<>();
        if(CollectionUtils.isEmpty(list)) {
            return result;
        }
        String redisBaseKey = RedisKeyConstant.PRESS;
        String msg = "试产量产";
        for (LarkPressMessageDTO dto : list) {
            String titleContent = null;
            String textContent = null;
            NoticeEnum noticeFlag = null;
            String processId = null;
            String redisKey = dto.getBusinessType() + "_" + dto.getBusinessId() + redisBaseKey;
            // 查询业务相关内容
            LarkPressBusinessTypeEnum businessType = LarkPressBusinessTypeEnum.getByCode(dto.getBusinessType());
            List<LarkPressMessageDTO.SendUserInfo> pressUserList = new ArrayList<>();
            PilotApplicationDTO.ApprovePilotNoticeDTO entity = pilotApplicationService.getPilotApplicationNoticeData(dto.getBusinessId());
            if(null != entity) {
                if (!entity.getApproveStatus().getCode().equals(ApproveStatusEnum.APPROVE_ING.getCode())) {
                    result.add(msg + "【"+entity.getCode()+"】"+ApiError.ERROR_95273.msg);
                    continue;
                }
                //是否存在
                String redisValue = redisService.getCacheObject(redisKey);
                if(StringUtils.isNotBlank(redisValue)){
                    String[] split = redisValue.split("###");
                    long lastTime = Long.parseLong(split[1]);
                    Calendar cal = Calendar.getInstance();
                    long nowTime = cal.getTimeInMillis();
                    long min = (nowTime - lastTime) / (60 * 1000);
                    redisValue = split[0] + " " + min + "分钟前";
                    result.add(String.format(ApiError.ERROR_95274.msg, redisValue));
                    continue;
                }

                noticeFlag = NoticeEnum.AUDIT_PILOT_APPLICATION;
                long timeInMillis = Calendar.getInstance().getTimeInMillis();
                dto.setBusinessName(businessType.getName()+"【"+entity.getCode()+"】###"+timeInMillis);
                //标题
                titleContent = String.format(NoticeMessageConstant.AUDIT_PILOT_PRESS_TITLE,entity.getUserName(),entity.getCode());
                //消息内容
                String chargeName = Arrays.asList(entity.getChargeName().split(",")).stream().distinct().collect(Collectors.joining(";"));
                String skuNo = Arrays.asList(entity.getSkuNo().split(",")).stream().distinct().collect(Collectors.joining(";"));
                textContent = String.format(NoticeMessageConstant.AUDIT_PILOT_PRESS_CONTENT , "试产量产催办" , chargeName, skuNo);

                //被通知人
                //根据节点标示获取到通知消息实体
                NoticeMessageEntity notice = noticeMessageService.getByNodeFlag(noticeFlag);
                if (Objects.isNull(notice)) {
                    result.add(msg + "【"+entity.getCode()+"】"+ApiError.ERROR_MSG_IS_NOT_NULL.msg);
                    continue;
                }
                //根据单据id查询审核流程
                List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessId(Collections.singletonList(dto.getBusinessId()));
                List<String> curApproveIds = processTaskManagementList.stream().filter(req -> req.getBusinessId().equals(dto.getBusinessId()) && req.getTaskStatus().equals(ApproveStatusEnum.APPROVE_ING)).map(ProcessTaskManagementEntity::getCurApproveId).distinct().collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(curApproveIds)){
                    for (String userId : curApproveIds) {
                        LarkPressMessageDTO.SendUserInfo sendUserInfo = new LarkPressMessageDTO.SendUserInfo();
                        sendUserInfo.setUserId(userId);
                        sendUserInfo.setUserName("");
                        pressUserList.add(sendUserInfo);
                    }
                }else {
                    List<String> handleUserIdList = noticeMessageService.getSetPilotNotice(notice, entity, Boolean.FALSE);
                    if(CollectionUtils.isNotEmpty(handleUserIdList)) {
                        for (String userId : handleUserIdList) {
                            LarkPressMessageDTO.SendUserInfo sendUserInfo = new LarkPressMessageDTO.SendUserInfo();
                            sendUserInfo.setUserId(userId);
                            sendUserInfo.setUserName("");
                            pressUserList.add(sendUserInfo);
                        }
                    }
                }
                // 发送飞书加急消息
                sendMessage(pressUserList, titleContent, textContent, noticeFlag, ThirdConstants.FS_MESSAGE_INTERACTIVE, Boolean.TRUE);
                redisService.setCacheObject(redisKey, dto.getBusinessName(), 30L, TimeUnit.MINUTES);
                result.add(msg +"【"+entity.getCode()+"】发送成功");
            }
        }
        return result;
    }

    @Override
    public Boolean sendMessage(List<LarkPressMessageDTO.SendUserInfo> pressUserList, String titleContent, String textContent, NoticeEnum noticeFlag, String msgType, Boolean isPress) {
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = noticeMessageService.getByNodeFlag(noticeFlag);
        if (Objects.isNull(notice)) {
            throw new ServiceException(ApiError.ERROR_MSG_IS_NOT_NULL);
        }

        //排除关闭通知的人员 并去重
        List<String> cancelNoticeUserIds = userCancelNoticeService.cancelNoticeUserIds(notice.getId());
        List<String> noticeUserIds = pressUserList.stream().map(LarkPressMessageDTO.SendUserInfo::getUserId).collect(Collectors.toList());
        Map<String, String> userIdNameMap = pressUserList.stream().collect(Collectors.toMap(LarkPressMessageDTO.SendUserInfo::getUserId, LarkPressMessageDTO.SendUserInfo::getUserName));
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
        //发送消息的结果
        for (String unionId : unionIds) {
            String userName = unionIdUserNameMap.get(unionId);
            if (isNotBlank(userName)) {
                titleContent = format(titleContent, userName);
            }
            LarkResultDTO<Object> larkResult = fsService.sendMessage(unionId, titleContent, textContent, msgType);
            // 催办
            if (isPress) {
                SingleResultDTO resultDTO = parseObject(larkResult.getData().toString(), SingleResultDTO.class);
                String messageId = resultDTO.getMessage_id();
                fsService.pressMessage(messageId, Collections.singletonList(unionId));
            }
        }

        return Boolean.TRUE;
    }

    /**
     * 批量发送催办信息
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-19 16:15
     */
    @Override
    public Boolean batchPress(LarkPressMessageDTO.BatchLarkPressMessageDTO dto) {
        List<String> businessIdList = dto.getBusinessIdList();
        String businessType = dto.getBusinessType();
        String redisBaseKey = RedisKeyConstant.PRESS;
        List<String> alreadyPress = new ArrayList<>();
        List<LarkPressMessageDTO> pilotList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(businessIdList)) {
            for (String businessId : businessIdList) {
                if(dto.getBusinessType().equals(LarkPressBusinessTypeEnum.PILOT_APPLICATION.getCode())) {//试产量产类型
                    LarkPressMessageDTO pressMessage = new LarkPressMessageDTO();
                    pressMessage.setBusinessId(businessId);
                    pressMessage.setBusinessType(businessType);
                    pilotList.add(pressMessage);
                }else {
                    String redisKey = dto.getBusinessType() + "_" + businessId + redisBaseKey;
                    //是否存在
                    String redisValue = redisService.getCacheObject(redisKey);
                    if (StringUtils.isNotBlank(redisValue)) {
                        alreadyPress.add(redisValue);
                    } else {
                        LarkPressMessageDTO pressMessage = new LarkPressMessageDTO();
                        pressMessage.setBusinessId(businessId);
                        pressMessage.setBusinessType(businessType);
                        this.press(pressMessage);
                    }
                }
            }
        }
        //针对试产量产类型做特殊处理
        if(CollectionUtils.isNotEmpty(pilotList)){
            List<String> pilotListPress = this.pilotListPress(pilotList);
            StringBuilder sb = new StringBuilder();
            boolean b = pilotListPress.stream().allMatch(s -> s.contains("成功"));
            if(!b){
                if (CollectionUtils.isNotEmpty(pilotListPress)) {
                    for (String er : pilotListPress) {
                        sb.append(er);
                        sb.append("<br>");
                    }
                }
                if (StringUtils.isNotBlank(sb.toString())) {
                    throw new ServiceException(sb.toString());
                }
            }
        }else if (CollectionUtils.isNotEmpty(alreadyPress)) {
            String name = alreadyPress.stream().collect(Collectors.joining(","));
            ApiError error = ApiError.ERROR_95191;
            String message = error.msg;
            throw new ServiceException(error.code, String.format(message, name));
        }
        return Boolean.TRUE;
    }
}
