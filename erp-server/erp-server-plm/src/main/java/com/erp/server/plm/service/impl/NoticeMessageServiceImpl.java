package com.erp.server.plm.service.impl;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.IsConstant;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BaseStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.NoticeEnum;
import com.erp.model.plm.enums.NoticeItemPeopleEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.model.workflow.dto.ProcessTaskManagementDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.ProcessTaskManagementFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.plm.constant.NoticeMessageConstant;
import com.erp.server.plm.mapper.NoticeMessageMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class NoticeMessageServiceImpl extends ServiceImpl<NoticeMessageMapper, NoticeMessageEntity>
        implements NoticeMessageService {

    @Autowired
    @Lazy
    private CommonService commonService;

    @Autowired
    private NoticeNodeService noticeNodeService;

    @Autowired
    private UserCancelNoticeService userCancelNoticeService;

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private FsService fsService;

    @Autowired
    private NoticeMessageRecordService noticeMessageRecordService;

    @Autowired
    private PreTaskService preTaskService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private TaskCommentRefService taskCommentRefService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private TaskFollowerService taskFollowerService;

    @Resource
    private ProcessTaskManagementFeign processTaskManagementFeign;

    @Autowired
    private CfgSettingService cfgSettingService;

    @Value("${third.fs.appUrl}")
    private String fsAppUrl;

    public static final String TASK_CHARGE = "任务负责人";
    public static final String PRODUCT_CHARGE = "产品经理";

    @Override
    public PagingVO<NoticeMessageDTO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page<BaseSearchDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage<NoticeMessageDTO> pageData = baseMapper.paging(query, params, IsConstant.YES);
        List<NoticeMessageDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            List<FindUserDTO> userList = commonService.getAllUser();
            for (NoticeMessageDTO item : list) {
                String createUserId = item.getCreateUserId();
                if (StringUtils.isNotBlank(createUserId)) {
                    FindUserDTO createUser = userList.stream().filter(u -> createUserId.equals(u.getUserId())).findFirst().orElse(null);
                    if (createUser != null) {
                        item.setCreateUserName(createUser.getUserName());
                    }
                }
                String updateUserId = item.getUpdateUserId();
                if (StringUtils.isNotBlank(updateUserId)) {
                    FindUserDTO updateUser = userList.stream().filter(u -> updateUserId.equals(u.getUserId())).findFirst().orElse(null);
                    if (updateUser != null) {
                        item.setUpdateUserName(updateUser.getUserName());
                    }
                }
                //其它人
                String otherPeople = item.getOtherPeople();
                List<String> otherPeopleList = new ArrayList<>();
                if (StringUtils.isNotBlank(otherPeople)) {
                    String[] other = otherPeople.split(",");
                    List<String> names = new ArrayList<>(other.length);
                    for (String otherUserId : other) {
                        FindUserDTO findUser = userList.stream().filter(u -> StringUtils.isNotBlank(otherUserId) && otherUserId.equals(u.getUserId())).findFirst().orElse(null);
                        if (findUser != null) {
                            names.add(findUser.getUserName());
                        } else {
                            names.add("");
                        }
                        otherPeopleList.add(otherUserId);
                    }

                    item.setOtherPeople(StringUtils.join(names, ","));
                }
                item.setOtherPeopleList(otherPeopleList);
                String itemPeople = item.getItemPeople();
                item.setItemPeopleList(Arrays.asList(itemPeople.split(",")));
                item.setItemPeopleName(NoticeItemPeopleEnum.getNameByFlags(itemPeople, ","));
            }
        }
        return new PagingVO<>(pageData);
    }

    /**
     * 保存通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-07 14:26
     */
    @Override
    @Transactional
    public Boolean add(NoticeMessageDTO dto) {
        String nodeId = dto.getNodeId();
        NoticeNodeEntity nodeEntity = noticeNodeService.getById(nodeId);
        if (Objects.isNull(nodeEntity)) {
            throw new ServiceException(ApiError.ERROR_95054);
        }
        //检查节点是否存在
        checkIfExist(nodeId, null);
        NoticeMessageEntity messageEntity = new NoticeMessageEntity();
        messageEntity.setNodeId(nodeId);
        List<String> itemPeopleList = dto.getItemPeopleList();
        List<String> otherPeopleList = dto.getOtherPeopleList();
        if (CollectionUtils.isNotEmpty(itemPeopleList)) {
            messageEntity.setItemPeople(String.join(",", itemPeopleList));
        }
        if (CollectionUtils.isNotEmpty(otherPeopleList)) {
            messageEntity.setOtherPeople(String.join(",", otherPeopleList));
        }
        if (CollectionUtils.isEmpty(otherPeopleList) && CollectionUtils.isEmpty(itemPeopleList)) {
            throw new ServiceException(ApiError.ERROR_95055);
        }
        boolean flag = this.save(messageEntity);
        if (flag) {
            //更改节点
            nodeEntity.setExistAdd(IsConstant.YES);
            noticeNodeService.updateById(nodeEntity);
        }
        return flag;
    }


    /**
     * 更改通知
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-07 14:51
     */
    @Override
    public Boolean updateNotice(NoticeMessageDTO dto) {
        String nodeId = dto.getNodeId();
        NoticeNodeEntity nodeEntity = noticeNodeService.getById(nodeId);
        if (Objects.isNull(nodeEntity)) {
            throw new ServiceException(ApiError.ERROR_95054);
        }
        //检查节点是否存在
        checkIfExist(nodeId, dto.getId());
        NoticeMessageEntity messageEntity = new NoticeMessageEntity();
        messageEntity.setNodeId(nodeId);
        List<String> itemPeopleList = dto.getItemPeopleList();
        List<String> otherPeopleList = dto.getOtherPeopleList();
        if (CollectionUtils.isNotEmpty(itemPeopleList)) {
            messageEntity.setItemPeople(String.join(",", itemPeopleList));
        } else {
            messageEntity.setItemPeople("");
        }
        if (CollectionUtils.isNotEmpty(otherPeopleList)) {
            messageEntity.setOtherPeople(String.join(",", otherPeopleList));
        } else {
            messageEntity.setOtherPeople("");
        }
        if (CollectionUtils.isEmpty(otherPeopleList) && CollectionUtils.isEmpty(itemPeopleList)) {
            throw new ServiceException(ApiError.ERROR_95055);
        }
        messageEntity.setId(dto.getId());
        return this.updateById(messageEntity);
    }


    /**
     * 更改状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-07 15:01
     */
    @Override
    public Boolean updateState(UpdateStateDTO dto) {
        NoticeMessageEntity entity = new NoticeMessageEntity();
        entity.setId(dto.getId());
        Boolean stateFlag = dto.getState();
        if (stateFlag) {
            entity.setState(IsConstant.YES);
        } else {
            entity.setState(IsConstant.NO);
        }
        return this.updateById(entity);
    }

    /**
     * 获取用户的通知节点列表
     *
     * @param userId
     * @return java.util.List<com.erp.model.plm.dto.UserNoticeNodeDTO>
     * @author yl
     * @date 2022-11-10 15:23
     */
    @Override
    public List<UserNoticeNodeDTO> getUserNoticeNode(String userId) {
        //获取用户取消的通知表id
        List<String> cancelNoticeIds = userCancelNoticeService.getUserCancelNoticeIds(userId);
        List<UserNoticeNodeDTO> resultList = baseMapper.getUserNoticeNode(IsConstant.YES);
        for (UserNoticeNodeDTO item : resultList) {
            if (cancelNoticeIds.contains(item.getNoticeMessageId())) {
                item.setState(false);
            }
        }
        return resultList;
    }


    /**
     * 新建任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean newTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.NEW_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        //所有的通知用户人
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());

            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();

            for (ProjectTaskEntity task : taskList) {
                String messageContent = String.format(NoticeMessageConstant.NEW_TASK, userName);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                List<String> allNoticeUserIds = new ArrayList<>();
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds = noticeUserIds;
                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                //获取飞书的unionid 与用户关系
                List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 获取项目内容
     *
     * @param
     * @return java.lang.String
     * @author yl
     * @date 2022-11-18 15:12
     */
    private String getProjectContent(String productName, String chargeFlag, String chargeName) {

        String projectContent = String.format(NoticeMessageConstant.PROJECT_CONTENT, productName, chargeFlag, chargeName);
        return projectContent;
    }

    /**
     * 获取项目内容
     *
     * @param
     * @return java.lang.String
     * @author yl
     * @date 2022-11-18 15:12
     */
    private String getTaskProjectContent(String taskName, String productName, String date, String chargeFlag, String chargeName) {
        if (StringUtils.isBlank(date)) {
            date = "-";
        }
        String projectContent = String.format(NoticeMessageConstant.TASK_PROJECT_CONTENT, taskName, productName, date, chargeFlag, chargeName);
        return projectContent;
    }


    /**
     * 发布任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean releaseTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.RELEASE_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            String productChargeId = product.getProductChargeId();
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeUserIds);
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(taskList)) {
                String messageContent = String.format(NoticeMessageConstant.RELEASE_TASK_OTHER, userName, taskList.size());
                String taskName = taskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.joining(","));
                String projectContent = getTaskProjectContent(taskName, product.getName(), LocalDateTimeUtil.format(product.getEndTime(), DateUtil.fmt_day), PRODUCT_CHARGE, product.getProductChargeName());
                Boolean result = batchSendFsMessage(unionIds, messageContent, projectContent);
                if (result) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(productChargeId);
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(product.getEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId("");
                        recordEntity.setTaskName("");
                        recordEntity.setChargeName("");
                        messageRecordList.add(recordEntity);
                    }
                }
            }


            //通知的任务负责人
            List<String> noticeTaskChargeIdList = new ArrayList<>();
            //消息通知记录
            for (ProjectTaskEntity task : taskList) {
                //所有的通知用户人
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    noticeTaskChargeIdList.addAll(chargeIdList);
                }
            }

            //获取取消通知的用户id
            List<String> cancelNoticeUserIds = userCancelNoticeService.cancelNoticeUserIds(notice.getId());
            List<String> resultList = noticeTaskChargeIdList.stream().filter(n -> !cancelNoticeUserIds.contains(n)).collect(Collectors.toList());
            List<ThirdUnionDTO> noticeTaskChargeUnionList = getNoticeUnionIds(unionIdList, resultList);
            Map<String, List<ThirdUnionDTO>> groupMap = noticeTaskChargeUnionList.stream().collect(Collectors.groupingBy(ThirdUnionDTO::getUserId));
            for (Map.Entry<String, List<ThirdUnionDTO>> item : groupMap.entrySet()) {
                String userId = item.getKey();
                List<ThirdUnionDTO> list = item.getValue();
                List<String> taskChargeUnionIds = list.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                Long count = taskList.stream().filter(t -> t.getChargeId().contains(userId)).count();
                String taskName = taskList.stream().filter(t -> t.getChargeId().contains(userId)).map(ProjectTaskEntity::getName).collect(Collectors.joining(","));
                String taskChargeMessageContent = String.format(NoticeMessageConstant.RELEASE_TASK, userName, count);
                String taskChargeProjectContent = getTaskProjectContent(taskName, product.getName(), LocalDateTimeUtil.format(product.getEndTime(), DateUtil.fmt_day), PRODUCT_CHARGE, product.getProductChargeName());
                //发送消息的结果
                Boolean sendResult = batchSendFsMessage(taskChargeUnionIds, taskChargeMessageContent, taskChargeProjectContent);
                //当发送成功后
                if (sendResult) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(productChargeId);
                    recordEntity.setMessageContent(taskChargeMessageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setPlanEndTime(product.getEndTime());
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setChargeName("");
                    messageRecordList.add(recordEntity);
                }

            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 批量发送信息
     *
     * @param
     * @return boolean
     * @author yl
     * @date 2022-11-23 12:26
     */
    public boolean batchSendFsMessage(List<String> noticeUnionList, String messageContent, String projectContent) {
        FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
        sendMessage.setUnionIds(noticeUnionList);
        Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
        sendMessage.setContentMap(contentMap);
        //发送消息的结果
        Boolean sendResult = fsService.sendMessage(sendMessage);
        return sendResult;
    }

    /**
     * 取消发布任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean cancelReleaseTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.CANCEL_RELEASE.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                //所有的通知用户人
                List<String> allNoticeUserIds = new ArrayList<>();
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds = noticeUserIds;
                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                String messageContent = String.format(NoticeMessageConstant.CANCEL_RELEASE, userName);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 开始任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean startTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.START_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                //所有的通知用户人
                List<String> allNoticeUserIds = new ArrayList<>();
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds = noticeUserIds;
                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                String messageContent = String.format(NoticeMessageConstant.START_TASK, userName);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    /**
     * 完成任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean finishTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        String flag = NoticeEnum.FINISH_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListByPreTaskIds(taskIdList);
            List<ProjectTaskEntity> projectTaskList = projectTaskService.getByProductId(productId);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                //所有的通知用户人
                List<String> allNoticeUserIds = new ArrayList<>();
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds = noticeUserIds;
                }
                //任务完成通知消息
                String messageContent = String.format(NoticeMessageConstant.FINISH_TASK, userName);
                //获取对应的前置关系
                PreTaskEntity preTask = preTaskList.stream().filter(p -> p.getPreTaskId().equals(task.getId())).findFirst().orElse(null);
                if (!Objects.isNull(preTask)) {
                    String taskId = preTask.getTaskId();
                    ProjectTaskEntity taskEntity = projectTaskList.stream().filter(p -> p.getId().equals(taskId)).findFirst().orElse(null);
                    if (!Objects.isNull(taskEntity)) {
                        if (StringUtils.isNotBlank(taskEntity.getChargeId())) {
                            List<String> preTaskChargeIdList = Arrays.asList(taskEntity.getChargeId().split(","));
                            allNoticeUserIds.addAll(preTaskChargeIdList);
                        }
                        //完成任务的通知信息
                        messageContent = String.format(NoticeMessageConstant.EXIST_PRE_FINISH_TASK, task.getName(), taskEntity.getName());
                    }
                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 发送完成待审核的通知
     *
     * @param userName
     * @param taskList
     * @param productId
     * @return void
     * @author yl
     * @date 2022-12-01 11:46
     */
    @Override
    public void finishWaitConfirmNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return;
        }
        String flag = NoticeEnum.FINISH_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                //查询当前需要审核的人员
                List<AuditorHandleDTO> approveRecordShowList = workflowFeign.getHistoryTaskByProcessId(task.getProcessId());
                if (CollectionUtils.isEmpty(approveRecordShowList)) {
                    throw new ServiceException(ApiError.ERROR_95045);
                }
                List<String> allNoticeUserIds = approveRecordShowList.stream().filter(obj -> BaseStatusEnum.WAIT_AUDIT.getName().equals(obj.getHandContent())).map(AuditorHandleDTO::getHandleUserId).collect(Collectors.toList());
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);

                String messageContent = String.format(NoticeMessageConstant.FINISH_WAIT_CONFIRM, task.getChargeName());
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }


    }

    @Override
    @Async("customExecutor")
    @Transactional
    public Boolean flyingBookReminder(FlyingBookReminderDTO dto) {
        //需要发生通知的人员
        List<String> sendIds = new ArrayList<>();
        //抄送人id
        List<String> userIds = dto.getUserIds();
        if (CollectionUtils.isNotEmpty(userIds)) {
            sendIds.addAll(userIds);
        }
        //提醒内容
        String content = dto.getContent();
        //任务id
        List<String> taskIds = dto.getTaskIds();
        //任务信息
        List<ProjectTaskEntity> projectTaskList = projectTaskService.listByIds(taskIds);
        if (CollectionUtils.isEmpty(projectTaskList)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        //消息记录
        List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
        for (ProjectTaskEntity task : projectTaskList) {
            //产品id
            String productId = task.getProductId();
            String chargeIds = task.getChargeId();
            if (StringUtils.isNotBlank(chargeIds)) {
                List<String> chargeIdList = Arrays.stream(chargeIds.split(",")).collect(Collectors.toList());
                sendIds.addAll(chargeIdList);
                //去重
                sendIds = sendIds.stream().distinct().collect(Collectors.toList());
            }
            //产品信息
            ProductInfoEntity productInfoEntity = productInfoService.getById(productId);
            if (ObjectUtils.isEmpty(productInfoEntity)) {
                throw new ServiceException(ApiError.ERROR_95010);
            }
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, sendIds);
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String projectContent = getTaskProjectContent(task.getName(), productInfoEntity.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
            Map<String,Object> contentMap = getCardMessageMap(content, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(task.getChargeId());
                    recordEntity.setMessageContent(content);
                    recordEntity.setNoticeMessageId(null);
                    recordEntity.setNoticeNode(null);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setPlanEndTime(task.getPlanEndTime());
                    recordEntity.setProductId(productId);
                    recordEntity.setProductName(productInfoEntity.getName());
                    recordEntity.setTaskId(task.getId());
                    recordEntity.setTaskName(task.getName());
                    recordEntity.setChargeName(task.getChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
        }
        //保存发送消息通知记录
        noticeMessageRecordService.saveBatch(messageRecordList);
        return true;
    }


    /**
     * 排期任务提交
     *
     * @param
     * @return void
     * @author yl
     * @date 2023-02-17 18:18
     */
    @Override
    @Async("customExecutor")
    public void scheduleTaskSubmit(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        //排期任务 通知节点
        String noticeFlag = NoticeEnum.SCHEDULE_TASK_SUBMIT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(noticeFlag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);

            if (CollectionUtils.isEmpty(taskList)) {
                return;
            }
            //需要通知所有的人
            List<String> allNoticeUserIds = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds.addAll(noticeUserIds);
                }
            }

            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            //消息内容
            String messageContent = String.format(NoticeMessageConstant.SCHEDULE_TASK_CONTENT, userName, taskList.size());
            String taskName = "-";

            /**
             * 获取到提交排期任务卡片的主内容
             */
            String scheduleTaskSubmitCard = getScheduleTaskSubmitCard(taskName, product.getName(), product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, scheduleTaskSubmitCard, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //发送成功
            if (sendResult) {
                //消息通知记录
                List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(noticeFlag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskName(taskName);
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }
                //保存发送消息通知记录
                noticeMessageRecordService.saveBatch(messageRecordList);

            }

        }
    }


    /**
     * 排期任务审核结果
     *
     * @param userName
     * @param taskList
     * @param productId
     * @param auditResult 审核结果你
     * @param remark      不通过原因
     * @return void
     * @author yl
     * @date 2023-02-20 15:13
     */
    @Override
    @Async("customExecutor")
    public void scheduleTaskAudit(String userName, List<ProjectTaskEntity> taskList, String productId, String auditResult, String remark) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        //排期审核 通知节点
        String noticeFlag = NoticeEnum.SCHEDULE_TASK_AUDIT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(noticeFlag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);

            if (CollectionUtils.isEmpty(taskList)) {
                return;
            }
            //需要通知所有的人
            List<String> allNoticeUserIds = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds.addAll(noticeUserIds);
                }
            }
            String taskName = taskList.stream().map(ProjectTaskEntity::getName).collect(Collectors.joining(","));

            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = "";
            String auditPassStatus = BaseStatusEnum.AUDIT_PASS.getStatus();
            if (auditPassStatus.equals(auditResult)) {
                //审核通过
                messageContent = String.format(NoticeMessageConstant.SCHEDULE_TASK_PASS_CONTENT, userName);
            } else {
                //审核不通过
                messageContent = String.format(NoticeMessageConstant.SCHEDULE_TASK_NO_PASS_CONTENT, userName, remark);
            }

            /**
             * 获取到提交排期任务卡片的主内容
             */
            String scheduleTaskAudit = String.format(NoticeMessageConstant.SCHEDULE_TASK_AUDIT_CARD, taskList.size(), product.getName(), product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, scheduleTaskAudit, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //发送成功
            if (sendResult) {
                //消息通知记录
                List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(noticeFlag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskName(taskName);
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }
                //保存发送消息通知记录
                noticeMessageRecordService.saveBatch(messageRecordList);
            }
        }
    }


    /**
     * 排期变更
     *
     * @param userName
     * @param taskList
     * @param productId
     * @return void
     * @author yl
     * @date 2023-02-20 16:10
     */
    @Override
    @Async("customExecutor")
    public void changeScheduleTask(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        //排期任务 通知节点
        String noticeFlag = NoticeEnum.SCHEDULE_TASK_CHANGE.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(noticeFlag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            if (CollectionUtils.isEmpty(taskList)) {
                return;
            }
            //需要通知的任务负责人
            List<String> taskChargeIdList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                String chargeId = task.getChargeId();
                //如果包含任务责任人的话
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    taskChargeIdList.addAll(chargeIdList);
                }
            }
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();

            //当不为空的时候 就要循环发送 任务负责人 发送 消息消息
            if (CollectionUtils.isNotEmpty(taskChargeIdList)) {
                List<FindUserDTO> userList = sysUserFeign.getUserList();
                //去重
                taskChargeIdList = taskChargeIdList.stream().distinct().collect(Collectors.toList());
                for (String taskChargeId : taskChargeIdList) {
                    long count = taskList.stream().filter(obj ->
                            StringUtils.isNotBlank(obj.getChargeId()) &&
                                    Arrays.asList(obj.getChargeId().split(",")).
                                            contains(taskChargeId)
                    ).count();
                    //消息内容
                    String messageContent = String.format(NoticeMessageConstant.SCHEDULE_TASK_CHANGE_CONTENT, count);
                    String taskName = "-";
                    String taskChargeName = userList.stream().filter(u -> taskChargeId.
                            equals(u.getUserId())).findFirst().flatMap(data -> Optional.ofNullable(data.getUserName())).orElse("");
                    /**
                     * 获取到提交排期任务卡片的主内容
                     */
                    String scheduleTaskChangeCard = String.format(NoticeMessageConstant.SCHEDULE_TASK_CHANGE_CARD, taskName, product.getName(), taskChargeName);
                    Map<String,Object> contentMap = getCardMessageMap(messageContent, scheduleTaskChangeCard, fsAppUrl);
                    Boolean sendResult = sendFsMessage(noticeMessageId, Arrays.asList(taskChargeId), unionIdList, contentMap);
                    //发送成功
                    if (sendResult) {
                        //消息通知记录
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(noticeFlag);
                        recordEntity.setNoticeUserId(taskChargeId);
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskName(taskName);
                        recordEntity.setChargeName(taskChargeName);
                        messageRecordList.add(recordEntity);
                    }
                }
            }
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            //有没有其他人员
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            if (CollectionUtils.isNotEmpty(noticeUserIds)) {
                //消息内容
                String messageContent = String.format(NoticeMessageConstant.SCHEDULE_TASK_CHANGE_CONTENT, taskList.size());
                String taskName = "-";
                String taskChargeName = taskList.stream().map(ProjectTaskEntity::getChargeName).distinct().collect(Collectors.joining(";"));
                String scheduleTaskChangeCard = String.format(NoticeMessageConstant.SCHEDULE_TASK_CHANGE_CARD, taskName, product.getName(), taskChargeName);
                Map<String,Object> contentMap = getCardMessageMap(messageContent, scheduleTaskChangeCard, fsAppUrl);
                Boolean sendResult = sendFsMessage(noticeMessageId, noticeUserIds, unionIdList, contentMap);
                if (sendResult) {
                    List<String> acceptUserIds = noticeUserIds.stream().distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        //消息通知记录
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(noticeFlag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskName(taskName);
                        recordEntity.setChargeName(taskChargeName);
                        messageRecordList.add(recordEntity);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(messageRecordList)) {
                //保存发送消息通知记录
                noticeMessageRecordService.saveBatch(messageRecordList);
            }

        }

    }


    /**
     * 发送飞书消息
     */
    public Boolean sendFsMessage(String noticeMessageId, List<String> noticeUserIds, List<ThirdUnionDTO> unionIdList, Map<String,Object> contentMap) {
        //排除关闭 任务负责人 关闭通知的人员 并去重
        List<String> noticeList = eliminateCloseNotice(noticeMessageId, noticeUserIds);
        //任务负责人通知的相关信息
        List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
        FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
        List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
        sendMessage.setUnionIds(unionIds);
        sendMessage.setContentMap(contentMap);
        //发送消息的结果
        Boolean sendResult = fsService.sendMessage(sendMessage);
        return sendResult;
    }


    /**
     * 任务排期 审核人的通知
     *
     * @param userName
     * @param taskList
     * @param productId
     * @param noticeList
     * @return void
     * @author yl
     * @date 2023-02-20 16:47
     */
    @Override
    public void scheduleTaskAuditor(String userName, List<ProjectTaskEntity> taskList, String productId, List<String> noticeList) {
        if (CollectionUtils.isEmpty(noticeList)) {
            return;
        }
        //排期任务 通知节点
        String noticeFlag = NoticeEnum.SCHEDULE_TASK_SUBMIT.getFlag();
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(noticeFlag);
        if (Objects.isNull(notice)) {
            return;
        }
        //项目人员
        String itemPeoples = notice.getItemPeople();
        if (StringUtils.isEmpty(itemPeoples)) {
            return;
        }
        List<String> itemPeopleList = Arrays.asList(itemPeoples.split(","));

        //如果不包含 审核人
        if (!itemPeopleList.contains(NoticeItemPeopleEnum.AUDITOR.getFlag())) {
            return;
        }
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
        List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
        FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
        List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
        sendMessage.setUnionIds(unionIds);
        //消息内容
        String messageContent = String.format(NoticeMessageConstant.SCHEDULE_TASK_CONTENT, userName, taskList.size());
        String taskName = "-";

        /**
         * 获取到提交排期任务卡片的主内容
         */
        String scheduleTaskSubmitCard = getScheduleTaskSubmitCard(taskName, product.getName(), product.getProductChargeName());
        Map<String,Object> contentMap = getCardMessageMap(messageContent, scheduleTaskSubmitCard, fsAppUrl);
        sendMessage.setContentMap(contentMap);
        //发送消息的结果
        Boolean sendResult = fsService.sendMessage(sendMessage);
        //发送成功
        if (sendResult) {
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
            for (String userId : acceptUserIds) {
                NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                recordEntity.setMessageContent(messageContent);
                recordEntity.setNoticeMessageId("1");
                recordEntity.setNoticeNode(noticeFlag);
                recordEntity.setNoticeUserId(userId);
                recordEntity.setProductId(product.getProductId());
                recordEntity.setProductName(product.getName());
                recordEntity.setTaskName(taskName);
                recordEntity.setChargeName(product.getProductChargeName());
                messageRecordList.add(recordEntity);
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);

        }

    }

    @Override
    public NoticeMessageEntity getByNodeFlag(NoticeEnum flagEnum) {
        return baseMapper.getByNodeFlag(flagEnum.getFlag());
    }

    @Override
    public NoticeMessageEntity view(String id) {
        NoticeMessageEntity entity = this.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "未找到通知详情id=" + id));
        return entity;
    }


    /**
     * 获取到 提交排期审核任务消息卡片主体
     *
     * @param taskName    任务名
     * @param productName 产品名
     * @param chargeName  产品经理
     * @return
     */
    private String getScheduleTaskSubmitCard(String taskName, String productName, String chargeName) {
        String content = String.format(NoticeMessageConstant.SCHEDULE_TASK_SUBMIT_CARD, taskName, productName, chargeName);
        return content;
    }


    /**
     * 部分完成任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean portionFinishTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        String flag = NoticeEnum.FINISH_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            List<PreTaskEntity> preTaskList = preTaskService.getPreTaskListByPreTaskIds(taskIdList);
            List<ProjectTaskEntity> projectTaskList = projectTaskService.getByProductId(productId);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                //所有的通知用户人
                List<String> allNoticeUserIds = new ArrayList<>();
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds = noticeUserIds;
                }
                //获取对应的前置关系
                PreTaskEntity preTask = preTaskList.stream().filter(p -> p.getPreTaskId().equals(task.getId())).findFirst().orElse(null);
                if (!Objects.isNull(preTask)) {
                    String taskId = preTask.getTaskId();
                    ProjectTaskEntity taskEntity = projectTaskList.stream().filter(p -> p.getId().equals(taskId)).findFirst().orElse(null);
                    if (!Objects.isNull(taskEntity) && StringUtils.isNotBlank(taskEntity.getChargeId())) {
                        List<String> preTaskChargeIdList = Arrays.asList(taskEntity.getChargeId().split(","));
                        allNoticeUserIds.addAll(preTaskChargeIdList);
                    }

                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                String messageContent = String.format(NoticeMessageConstant.PORTION_FINISH_TASK, userName);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    /**
     * 关闭 任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean closeTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.CLOSE_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
                //所有的通知用户人
                List<String> allNoticeUserIds = new ArrayList<>();
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds = noticeUserIds;
                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                String messageContent = String.format(NoticeMessageConstant.CLOSE_TASK, userName);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 审核任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean approvalTaskNotice(String userName, List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.APPROVAL_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
            List<String> noticeUserIds = getSetNotice(notice, product, taskIdList);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            //审核不通过
            Integer approvalNoPass = TaskStateEnum.APPROVAL_NO_PASS.getCode();
            for (ProjectTaskEntity task : taskList) {
                //所有的通知用户人
                List<String> allNoticeUserIds = new ArrayList<>();
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    allNoticeUserIds.addAll(noticeUserIds);
                    allNoticeUserIds.addAll(chargeIdList);
                } else {
                    allNoticeUserIds = noticeUserIds;
                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                //任务状态
                Integer taskState = task.getStatus();
                String approvalResult = "审核通过";
                if (approvalNoPass.equals(taskState)) {
                    approvalResult = "审核不通过";
                }

                String messageContent = String.format(NoticeMessageConstant.APPROVAL_TASK, userName, approvalResult);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(product.getProductId());
                        recordEntity.setProductName(product.getName());
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }

                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 编辑任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean editTaskNotice(String userName, ProjectTaskEntity task, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.EDIT_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        //所有的通知用户人
        List<String> allNoticeUserIds = new ArrayList<>();
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, Arrays.asList(task.getId()));
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            String chargeId = task.getChargeId();
            if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                allNoticeUserIds.addAll(noticeUserIds);
                allNoticeUserIds.addAll(chargeIdList);
            } else {
                allNoticeUserIds = noticeUserIds;
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.EDIT_TASK, userName, task.getName());
            String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(task.getChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setPlanEndTime(task.getPlanEndTime());
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId(task.getId());
                    recordEntity.setTaskName(task.getName());
                    recordEntity.setChargeName(task.getChargeName());
                    messageRecordList.add(recordEntity);
                }

            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 删除任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean deleteTaskNotice(String userName, ProjectTaskEntity task, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.DELETE_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        //所有的通知用户人
        List<String> allNoticeUserIds = new ArrayList<>();
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();

            List<String> noticeUserIds = getSetNotice(notice, product, Arrays.asList(task.getId()));
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            String chargeId = task.getChargeId();
            if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                allNoticeUserIds.addAll(noticeUserIds);
                allNoticeUserIds.addAll(chargeIdList);
            } else {
                allNoticeUserIds = noticeUserIds;
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), allNoticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.DELETE_TASK, userName, task.getName());
            String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(task.getChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setPlanEndTime(task.getPlanEndTime());
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId(task.getId());
                    recordEntity.setTaskName(task.getName());
                    recordEntity.setChargeName(task.getChargeName());
                    messageRecordList.add(recordEntity);
                }

            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 新建产品发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean newProductNotice(String userName, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.NEW_PRODUCT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            if (isContainsTaskCharge) {
                List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
                for (ProjectTaskEntity task : taskList) {
                    String chargeId = task.getChargeId();
                    if (StringUtils.isNotBlank(chargeId)) {
                        List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                        noticeUserIds.addAll(chargeIdList);
                    }
                }
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.NEW_PRODUCT, userName, product.getName());
            String projectContent = getProjectContent(product.getName(), PRODUCT_CHARGE, product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setIsTask(0);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }

            }

            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    /**
     * 产品立项 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean projectApprovalNotice(String userName, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.PROJECT_APPROVAL.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            if (isContainsTaskCharge) {
                List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
                for (ProjectTaskEntity task : taskList) {
                    String chargeId = task.getChargeId();
                    if (StringUtils.isNotBlank(chargeId)) {
                        List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                        noticeUserIds.addAll(chargeIdList);
                    }
                }
            }

            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.PROJECT_APPROVAL, userName, product.getName());
            String projectContent = getProjectContent(product.getName(), PRODUCT_CHARGE, product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setIsTask(0);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }

            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    /**
     * 启动项目 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean startProjectNotice(String userName, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.START_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            if (isContainsTaskCharge) {
                List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
                for (ProjectTaskEntity task : taskList) {
                    String chargeId = task.getChargeId();
                    if (StringUtils.isNotBlank(chargeId)) {
                        List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                        noticeUserIds.addAll(chargeIdList);
                    }
                }
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.START_PROJECT, userName, product.getName());
            String projectContent = getProjectContent(product.getName(), PRODUCT_CHARGE, product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setIsTask(0);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 开始项目 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean beginProjectNotice(String userName, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.BEGIN_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            if (isContainsTaskCharge) {
                List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
                for (ProjectTaskEntity task : taskList) {
                    String chargeId = task.getChargeId();
                    if (StringUtils.isNotBlank(chargeId)) {
                        List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                        noticeUserIds.addAll(chargeIdList);
                    }
                }
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.BEGIN_PROJECT, userName, product.getName());
            String projectContent = getProjectContent(product.getName(), PRODUCT_CHARGE, product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setIsTask(0);
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    @Override
    @Async("customExecutor")
    public Boolean approveProductNotice(String userName, ProductDetailEntity entity) {
        ProductShowDTO product = productInfoService.getProductInfo(entity.getProductId());
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.APPROVE_PRODUCT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(product.getProductId());
            for (ProjectTaskEntity task : taskList) {
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    noticeUserIds.addAll(chargeIdList);
                }
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.APPROVE_PRODUCT, entity.getSkuNo());
            String projectContent = getProjectContent(product.getName(), PRODUCT_CHARGE, product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setIsTask(0);
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 完成项目 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean finishProjectNotice(String userName, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.FINISH_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
            for (ProjectTaskEntity task : taskList) {
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    noticeUserIds.addAll(chargeIdList);
                }
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.FINISH_PROJECT, userName, product.getName());
            String projectContent = getProjectContent(product.getName(), PRODUCT_CHARGE, product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setIsTask(0);
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    /**
     * 归档项目 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean archiveProjectNotice(String userName, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.ARCHIVE_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
            for (ProjectTaskEntity task : taskList) {
                String chargeId = task.getChargeId();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                    noticeUserIds.addAll(chargeIdList);
                }
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.ARCHIVE_PROJECT, userName, product.getName());
            String projectContent = getProjectContent(product.getName(), PRODUCT_CHARGE, product.getProductChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setIsTask(0);
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setChargeName(product.getProductChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    /**
     * 评论提醒 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean remindRemarkNotice(String taskCommentId, String userName, String productId, String taskId, String comment, List<String> refUserIdList, List<FindUserDTO> refUserList) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.REMIND_REMARK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, Arrays.asList(taskId));
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());

            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            ProjectTaskEntity task = projectTaskService.getById(taskId);
            if (Objects.isNull(task)) {
                return false;
            }

            String chargeId = task.getChargeId();
            if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                noticeUserIds.addAll(chargeIdList);
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);

            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.REMIND_REMARK, userName, comment);
            String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setPlanEndTime(product.getEndTime());
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId(task.getId());
                    recordEntity.setTaskName(task.getName());
                    recordEntity.setChargeName(task.getChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);

            //评论的@的人员
            if (CollectionUtils.isNotEmpty(refUserIdList)) {
                FsBatchSendMessageDTO sendRefMessage = new FsBatchSendMessageDTO();
                List<ThirdUnionDTO> noticeRefUnionList = getNoticeUnionIds(unionIdList, refUserIdList);
                List<String> refUnionIds = noticeRefUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendRefMessage.setUnionIds(refUnionIds);
                String refUserName = refUserList.stream().filter(r -> refUserIdList.contains(r.getUserId())).map(FindUserDTO::getUserName).collect(Collectors.joining(","));

                String refMessageContent = String.format(NoticeMessageConstant.REMIND_REMARK_REF, userName, refUserName, comment);
                Map refContentMap = getCardMessageMap(refMessageContent, projectContent, fsAppUrl);
                sendRefMessage.setContentMap(refContentMap);
                //发送消息的结果
                Boolean sendRefResult = fsService.sendMessage(sendRefMessage);
                if (sendRefResult) {
                    List<String> acceptUserIds = noticeRefUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    //更改发送结果
                    taskCommentRefService.updateSendResult(acceptUserIds,taskCommentId,taskId,sendRefResult);
                }

            }
        }


        return true;
    }

    /**
     * 定时任务发送任务预警信息  每天下午17点
     * 发送通知
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-11-16 10:43
     */
    @Override
    public void sendEarlyWarning() {
        //获取即将到期的 任务列表
        Date nowDay = new Date();
        int flagDays = 3;
        List<ProjectTaskEntity> beAlmostExpireTaskList = projectTaskService.getExpireTaskList(nowDay, flagDays);
        String flag = NoticeEnum.EARLY_WARNING.getFlag();
        //今天的开始时间
        Date nowDayStartTime = DateUtil.getStartTime(nowDay);
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            List<String> productIds = beAlmostExpireTaskList.stream().map(ProjectTaskEntity::getProductId).distinct().collect(Collectors.toList());
            List<ProductShowDTO> productList = productInfoService.getProductInfoByIds(productIds);
            for (ProjectTaskEntity task : beAlmostExpireTaskList) {
                ProductShowDTO product = productList.stream().filter(p -> p.getProductId().equals(task.getProductId())).findFirst().orElse(null);
                if(org.springframework.util.ObjectUtils.isEmpty(product)) {
                    throw new ServiceException(ApiError.NOT_EXIST_BILL,"产品信息");
                }

                List<String> noticeUserIds = getSetNotice(notice, product, new ArrayList<>());
                String chargeId = task.getChargeId();
                List<String> chargeIdList = new ArrayList<>();
                if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                    chargeIdList = Arrays.asList(chargeId.split(","));
                    noticeUserIds.addAll(chargeIdList);
                }
                //排除关闭通知的人员 并去重
                List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
                List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
                FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
                List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
                sendMessage.setUnionIds(unionIds);
                Date planEndTime = Date.from(task.getPlanEndTime().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                ;
                //获取计划时间的开始时间
                Date planEndStartTime = DateUtil.getStartTime(planEndTime);
                //比较差值
                int diffDay = DateUtil.getDiffDay(nowDayStartTime, planEndStartTime);
                //表示 计划结束时间大于今天时间
                String warning = "";
                if (diffDay > 0) {
                    warning = task.getName() + " 即将" + diffDay + "天 后过期";
                } else {
                    warning = task.getName() + " 已过期" + Math.abs(diffDay) + "天";
                }
                String messageContent = String.format(NoticeMessageConstant.EARLY_WARNING, warning);
                String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
                Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.sendMessage(sendMessage);
                //当发送成功后
                if (sendResult) {
                    List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                    for (String userId : acceptUserIds) {
                        NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                        recordEntity.setChargeId(task.getChargeId());
                        recordEntity.setMessageContent(messageContent);
                        recordEntity.setNoticeMessageId(noticeMessageId);
                        recordEntity.setNoticeNode(flag);
                        recordEntity.setNoticeUserId(userId);
                        recordEntity.setPlanEndTime(task.getPlanEndTime());
                        recordEntity.setProductId(task.getProductId());
                        recordEntity.setProductName("");
                        recordEntity.setTaskId(task.getId());
                        recordEntity.setTaskName(task.getName());
                        recordEntity.setChargeName(task.getChargeName());
                        messageRecordList.add(recordEntity);
                    }
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }

    }


    /**
     * 变更 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean docChangesNotice(String userName, String productId, String taskId, String docName) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.DOC_CHANGES.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product, Arrays.asList(taskId));
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            ProjectTaskEntity task = projectTaskService.getById(taskId);
            if (Objects.isNull(task)) {
                return false;
            }

            String chargeId = task.getChargeId();
            if (isContainsTaskCharge && StringUtils.isNotBlank(chargeId)) {
                List<String> chargeIdList = Arrays.asList(chargeId.split(","));
                noticeUserIds.addAll(chargeIdList);
            }
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            String messageContent = String.format(NoticeMessageConstant.DOC_CHANGES, userName, docName);
            String projectContent = getTaskProjectContent(task.getName(), product.getName(), LocalDateTimeUtil.format(task.getPlanEndTime(), DateUtil.fmt_day), TASK_CHARGE, task.getChargeName());
            Map<String,Object> contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            sendMessage.setUnionIds(unionIds);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(product.getProductChargeId());
                    recordEntity.setMessageContent(messageContent);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setPlanEndTime(product.getEndTime());
                    recordEntity.setProductId(product.getProductId());
                    recordEntity.setProductName(product.getName());
                    recordEntity.setTaskId(task.getId());
                    recordEntity.setTaskName(task.getName());
                    recordEntity.setChargeName(task.getChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }


    /**
     * 根据第三方信息  获取到用户的unionid
     *
     * @param unionIdList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-15 11:14
     */
    public List<ThirdUnionDTO> getNoticeUnionIds(List<ThirdUnionDTO> unionIdList, List<String> userIds) {
        List<ThirdUnionDTO> unionIds = unionIdList.stream().filter(u -> userIds.contains(u.getUserId())).collect(Collectors.toList());
        return unionIds;
    }

    /**
     * 根据t通知消息表id
     * 以及要通知的人员 剔除掉关闭人的消息
     *
     * @param noticeId
     * @param allNoticeUserIds
     * @return void
     * @author yl
     * @date 2022-11-15 10:09
     */
    private List<String> eliminateCloseNotice(String noticeId, List<String> allNoticeUserIds) {
        //获取取消通知的用户id
        List<String> cancelNoticeUserIds = userCancelNoticeService.cancelNoticeUserIds(noticeId);
        List<String> resultList = allNoticeUserIds.stream().filter(n -> !cancelNoticeUserIds.contains(n)).distinct().collect(Collectors.toList());
        return resultList;
    }


    /**
     * 获取系统设置的通知人员
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-14 16:16
     */
    public List<String> getSetNotice(NoticeMessageEntity notice, ProductShowDTO product, List<String> taskIdList) {
        List<String> resultList = new ArrayList<>();
        if (!Objects.isNull(notice)) {
            //其它人
            String otherPeoples = notice.getOtherPeople();
            if (StringUtils.isNotBlank(otherPeoples)) {
                List<String> otherPeopleIds = Arrays.asList(otherPeoples.split(","));
                resultList.addAll(otherPeopleIds);
            }
            //项目人员
            String itemPeoples = notice.getItemPeople();
            if (StringUtils.isNotEmpty(itemPeoples)) {
                List<String> itemPeopleList = Arrays.asList(itemPeoples.split(","));

                //这个是项目经理
                if (itemPeopleList.contains(NoticeItemPeopleEnum.ITEM_MANAGER.getFlag()) && !Objects.isNull(product)) {
                    //项目负责人
                    String projectChargeId = product.getProjectChargeId();
                    if (StringUtils.isNotBlank(projectChargeId)) {
                        List<String> projectChargeIdList = Arrays.asList(projectChargeId.split(","));
                        resultList.addAll(projectChargeIdList);
                    }
                }

                //这个是产品经理
                if (itemPeopleList.contains(NoticeItemPeopleEnum.PRODUCT_MANAGER.getFlag()) && !Objects.isNull(product)) {
                    String productChargeId = product.getProductChargeId();
                    if (StringUtils.isNotBlank(productChargeId)) {
                        List<String> productChargeIdList = Arrays.asList(productChargeId.split(","));
                        resultList.addAll(productChargeIdList);
                    }
                }

                //这个是关注人
                if (itemPeopleList.contains(NoticeItemPeopleEnum.FOLLOWER.getFlag()) && CollectionUtils.isNotEmpty(taskIdList)) {
                    List<TaskFollowerEntity> taskConcernEntities = taskFollowerService.listByTaskIds(taskIdList);
                    if (CollectionUtils.isNotEmpty(taskConcernEntities)) {
                        List<String> userIdList = taskConcernEntities.stream().map(TaskFollowerEntity::getUserId).distinct().collect(Collectors.toList());
                        resultList.addAll(userIdList);
                    }
                }

                //这个是审核人
                if (itemPeopleList.contains(NoticeItemPeopleEnum.AUDITOR.getFlag()) && CollectionUtils.isNotEmpty(taskIdList)) {
                    List<ProjectTaskEntity> taskEntityList = projectTaskService.listByTaskIds(taskIdList);
                    if (CollectionUtils.isNotEmpty(taskEntityList)) {
                        for (ProjectTaskEntity taskEntity : taskEntityList) {
                            List<AuditorHandleDTO> historyTaskByProcessId = workflowFeign.getHistoryTaskByProcessId(taskEntity.getProcessId());
                            List<String> userIdList = historyTaskByProcessId.stream().map(AuditorHandleDTO::getHandleUserId).distinct().collect(Collectors.toList());
                            resultList.addAll(userIdList);
                        }
                    }
                }
            }
        }
        return resultList;
    }


    /**
     * 检查节点是否已用过
     *
     * @param nodeId
     * @return void
     * @author yl
     * @date 2022-11-07 14:28
     */
    private void checkIfExist(String nodeId, String id) {
        LambdaQueryWrapper<NoticeMessageEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoticeMessageEntity::getNodeId, nodeId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(NoticeMessageEntity::getId, id);
        }
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_95053);
        }
    }


    /**
     * 获取飞书消息卡片信息
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-18 12:31
     */
    public Map<String, Object> getCardMessageMap(String messageContent, String productContent, String url) {
        Map<String, Object> cardMap = new LinkedHashMap<>();
        Map<String, Boolean> configMap = new HashMap<>();
        configMap.put("wide_screen_mode", true);
        cardMap.put("config", configMap);
        Map<String, Object> headerMap = new HashMap<>();
        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("tag", "plain_text");
        titleMap.put("content", messageContent);
        headerMap.put("title", titleMap);
        cardMap.put("header", headerMap);
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> fieldAllMap = new LinkedHashMap<>();
        fieldAllMap.put("tag", "div");
        List<Map<String, Object>> fieldMapList = new ArrayList<>();
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("is_short", true);
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("tag", "lark_md");
        textMap.put("content", productContent);
        fieldMap.put("text", textMap);
        fieldMapList.add(fieldMap);
        fieldAllMap.put("fields", fieldMapList);
        elements.add(fieldAllMap);
        Map<String, Object> actionAllMap = new LinkedHashMap<>();
        actionAllMap.put("tag", "action");
        actionAllMap.put("layout", "bisected");
        List<Map<String, Object>> actionList = new ArrayList<>();
        Map<String, Object> actionMap = new LinkedHashMap<>();
        actionMap.put("tag", "button");
        actionMap.put("url", url);
        actionMap.put("type", "primary");
        Map<String, Object> actionTextMap = new HashMap<>();
        actionTextMap.put("tag", "plain_text");
        actionTextMap.put("content", "查看详情");
        actionMap.put("text", actionTextMap);
        Map<String, Object> actionValueMap = new HashMap<>();
        actionValueMap.put("chosen", "approve");
        actionMap.put("value", actionValueMap);
        actionList.add(actionMap);
        actionAllMap.put("actions", actionList);
        elements.add(actionAllMap);
        cardMap.put("elements", elements);
        return cardMap;
    }

    @Override
    public Boolean approvePilotApplicationNotice(String userName, PilotApplicationDTO.ApprovePilotNoticeDTO entity, Boolean isCompeletd) {
        String flag = NoticeEnum.AUDIT_PILOT_APPLICATION.getFlag();
        if(isCompeletd){
            flag = NoticeEnum.AUDIT_COMPLETED_PILOT_APPLICATION.getFlag();
        }
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetPilotNotice(notice, entity, isCompeletd);
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            //排除关闭通知的人员 并去重
            List<String> noticeList = eliminateCloseNotice(notice.getId(), noticeUserIds);
            List<ThirdUnionDTO> noticeUnionList = getNoticeUnionIds(unionIdList, noticeList);
            FsBatchSendMessageDTO sendMessage = new FsBatchSendMessageDTO();
            List<String> unionIds = noticeUnionList.stream().map(ThirdUnionDTO::getThirdUnionId).distinct().collect(Collectors.toList());
            sendMessage.setUnionIds(unionIds);
            //标题
            String title = String.format("试产量产单【%s】已在数大臣提交审核，请尽快审核",entity.getCode());
            if(isCompeletd){
                title = String.format("试产量产单【%s】已在数大臣完成审核，请知悉",entity.getCode());
            }
            //消息内容
            String chargeName = Arrays.asList(entity.getChargeName().split(",")).stream().distinct().collect(Collectors.joining(";"));
            String skuNo = Arrays.asList(entity.getSkuNo().split(",")).stream().distinct().collect(Collectors.joining(";"));
            String message = String.format(NoticeMessageConstant.AUDIT_PILOT_MSG_CONTENT,isCompeletd ? NoticeEnum.AUDIT_COMPLETED_PILOT_APPLICATION.getName() : NoticeEnum.AUDIT_PILOT_APPLICATION.getName(),chargeName,skuNo);
            String url =fsAppUrl;
            PlmCfgSettingEntity pilotApplicationNoticeUrl = cfgSettingService.lambdaQuery().eq(PlmCfgSettingEntity::getKey, "pilotApplicationNoticeUrl").one();
            if(null != pilotApplicationNoticeUrl){
                Map<String, Object> dataJson = pilotApplicationNoticeUrl.getDataJson();
                boolean uat = BusinessCommonConstants.hasProfile("uat");
                boolean dev = BusinessCommonConstants.hasProfile("dev");
                boolean test = BusinessCommonConstants.hasProfile("test");
                boolean prod = BusinessCommonConstants.hasProfile("prod");
                if(uat){
                    url = String.valueOf(dataJson.get("uat"));
                }else  if(dev||test){
                    url = String.valueOf(dataJson.get("test"));
                }else if(prod){
                    url = String.valueOf(dataJson.get("prod"));
                }
            }
            Map<String,Object> contentMap = getCardMessageMap(title, message, url);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.sendMessage(sendMessage);
            //当发送成功后
            if (sendResult) {
                List<String> acceptUserIds = noticeUnionList.stream().map(ThirdUnionDTO::getUserId).distinct().collect(Collectors.toList());
                for (String userId : acceptUserIds) {
                    NoticeMessageRecordEntity recordEntity = new NoticeMessageRecordEntity();
                    recordEntity.setChargeId(entity.getChargeId());
                    recordEntity.setMessageContent(message);
                    recordEntity.setNoticeMessageId(noticeMessageId);
                    recordEntity.setNoticeNode(flag);
                    recordEntity.setNoticeUserId(userId);
                    recordEntity.setProductId(entity.getProductId());
                    recordEntity.setProductName(entity.getSpuName());
                    recordEntity.setTaskId("");
                    recordEntity.setTaskName("");
                    recordEntity.setIsTask(0);
                    recordEntity.setChargeName(entity.getChargeName());
                    messageRecordList.add(recordEntity);
                }
            }
            //保存发送消息通知记录
            noticeMessageRecordService.saveBatch(messageRecordList);
        }
        return true;
    }

    /**
     * 试产量产：获取系统设置的通知人员
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author jack
     * @date 2024-09-23
     */
    public List<String> getSetPilotNotice(NoticeMessageEntity notice, PilotApplicationDTO.ApprovePilotNoticeDTO entity, Boolean isCompeletd) {
        if (org.springframework.util.ObjectUtils.isEmpty(entity)) {
            return Collections.emptyList();
            
        }
        List<String> resultList = new ArrayList<>();
        if (!Objects.isNull(notice)) {
            //其它人
            String otherPeoples = notice.getOtherPeople();
            if (StringUtils.isNotBlank(otherPeoples)) {
                List<String> otherPeopleIds = Arrays.asList(otherPeoples.split(","));
                resultList.addAll(otherPeopleIds);
            }
            //获取所有审核人员
            if(isCompeletd){
                List<ProcessTaskManagementDTO.ApproveHistoryDTO> approveHistoryList = processTaskManagementFeign.listApproveHistory(entity.getId());
                approveHistoryList = approveHistoryList.stream().filter(item -> item.getApproveUserName().equals(item.getCurApproveName())).collect(Collectors.toList());
                List<String> all = approveHistoryList.stream().filter(item -> item.getApproveUserName().equals(item.getCurApproveName())).map(ProcessTaskManagementDTO.ApproveHistoryDTO::getApproveUserId).distinct().collect(Collectors.toList());
                resultList.addAll(all);
            }
            //项目人员
            String itemPeoples = notice.getItemPeople();
            if (StringUtils.isNotEmpty(itemPeoples)) {
                List<String> itemPeopleList = Arrays.asList(itemPeoples.split(","));
                //这个是产品经理
                if (itemPeopleList.contains(NoticeItemPeopleEnum.PRODUCT_MANAGER.getFlag()) && !Objects.isNull(entity)) {
                    String productChargeId = entity.getChargeId();
                    if (StringUtils.isNotBlank(productChargeId)) {
                        List<String> productChargeIdList = Arrays.asList(productChargeId.split(","));
                        resultList.addAll(productChargeIdList);
                    }
                }

                //这个是审核人
                if (!isCompeletd&&itemPeopleList.contains(NoticeItemPeopleEnum.AUDITOR.getFlag())) {
                    List<ProcessTaskManagementDTO.ApproveHistoryDTO> approveHistoryList = processTaskManagementFeign.listApproveHistory(entity.getId());
                    List<String> collect = approveHistoryList.stream().filter(v -> v.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())).map(ProcessTaskManagementDTO.ApproveHistoryDTO::getCurApproveId).collect(Collectors.toList());
                    resultList.addAll(collect);
                }
            }
        }
        return resultList;
    }
}




