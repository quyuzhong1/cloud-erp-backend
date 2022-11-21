package com.erp.server.plm.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.constant.ThirdConstants;
import com.common.core.utils.date.DateUtil;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.dto.base.UpdateStateDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.modules.third.dto.FsBatchSendMessageDTO;
import com.erp.common.modules.third.dto.ThirdUnionDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.dto.ProductShowDTO;
import com.erp.model.plm.dto.UserNoticeNodeDTO;
import com.erp.model.plm.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.NoticeMessageConstant;
import com.erp.server.plm.enums.NoticeEnum;
import com.erp.server.plm.enums.NoticeItemPeopleEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.NoticeMessageMapper;
import com.erp.server.plm.service.*;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class NoticeMessageServiceImpl extends ServiceImpl<NoticeMessageMapper, NoticeMessageEntity>
        implements NoticeMessageService {

    @Autowired
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

    @Value("${third.fs.appUrl}")
    private String fsAppUrl;

    private String taskCharge = "任务负责人";
    private String productCharge = "产品经理";

    @Override
    public PagingVO<List<NoticeMessageDTO>> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
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
        return new PagingVO(pageData);
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
    public Boolean newTaskNotice(List<ProjectTaskEntity> taskList, String productId) {
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
            List<String> noticeUserIds = getSetNotice(notice, product);
            //如果包含任务负责人的话
            boolean isContainsTaskCharge = notice.getItemPeople().contains(NoticeItemPeopleEnum.TASK_CHARGE.getFlag());
            //获取飞书的unionid 与用户关系
            List<ThirdUnionDTO> unionIdList = sysUserFeign.getThirdUnionId(ThirdConstants.FS_PLATFORM);
            //消息通知记录
            List<NoticeMessageRecordEntity> messageRecordList = new ArrayList<>();
            for (ProjectTaskEntity task : taskList) {
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
                String messageContent = String.format(NoticeMessageConstant.NEW_TASK, task.getCreateUserName());
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    private String getProjectContent(String taskName, String productName, String date, String chargeFlag, String chargeName) {
        String projectContent = String.format(NoticeMessageConstant.PROJECT_CONTENT, taskName, productName, date, chargeFlag, chargeName);
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
    public Boolean releaseTaskNotice(List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.RELEASE_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
                String messageContent = String.format(NoticeMessageConstant.RELEASE_TASK, loginUser.getUserName());
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
     * 取消发布任务发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean cancelReleaseTaskNotice(List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.CANCEL_RELEASE.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        LoginUser loginUser = commonService.getUserInfo();
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
                String messageContent = String.format(NoticeMessageConstant.CANCEL_RELEASE, loginUser.getUserName());
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean startTaskNotice(List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.START_TASK.getFlag();
        LoginUser loginUser = commonService.getUserInfo();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
                String messageContent = String.format(NoticeMessageConstant.START_TASK, loginUser.getUserName());
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean finishTaskNotice(List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        List<String> taskIdList = taskList.stream().map(ProjectTaskEntity::getId).collect(Collectors.toList());
        String flag = NoticeEnum.FINISH_TASK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        LoginUser loginUser = commonService.getUserInfo();
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
                String messageContent = String.format(NoticeMessageConstant.FINISH_TASK, loginUser.getUserName());
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean closeTaskNotice(List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.CLOSE_TASK.getFlag();
        LoginUser loginUser = commonService.getUserInfo();

        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
                String messageContent = String.format(NoticeMessageConstant.CLOSE_TASK, loginUser.getUserName());
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean approvalTaskNotice(List<ProjectTaskEntity> taskList, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.APPROVAL_TASK.getFlag();
        LoginUser loginUser = commonService.getUserInfo();

        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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

                String messageContent = String.format(NoticeMessageConstant.APPROVAL_TASK, loginUser.getUserName(), approvalResult);
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean editTaskNotice(ProjectTaskEntity task, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.EDIT_TASK.getFlag();
        LoginUser loginUser = commonService.getUserInfo();

        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        //所有的通知用户人
        List<String> allNoticeUserIds = new ArrayList<>();
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.EDIT_TASK,loginUser.getUserName(), task.getName());
            String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean deleteTaskNotice(ProjectTaskEntity task, String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        String flag = NoticeEnum.DELETE_TASK.getFlag();
        LoginUser loginUser = commonService.getUserInfo();

        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        //所有的通知用户人
        List<String> allNoticeUserIds = new ArrayList<>();
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.DELETE_TASK, loginUser.getUserName(), task.getName());
            String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean newProductNotice(String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.NEW_PRODUCT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.NEW_PRODUCT, loginUser.getUserName(), product.getName());
            String projectContent = getProjectContent("", product.getName(), DateUtil.conversionDate(product.getEndTime(), ""), productCharge, product.getProductChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean projectApprovalNotice(String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.PROJECT_APPROVAL.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.PROJECT_APPROVAL, loginUser.getUserName(), product.getName());
            String projectContent = getProjectContent("", product.getName(), DateUtil.conversionDate(product.getEndTime(), ""), productCharge, product.getProductChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean startProjectNotice(String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.START_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.START_PROJECT, loginUser.getUserName(), product.getName());
            String projectContent = getProjectContent("", product.getName(), DateUtil.conversionDate(product.getEndTime(), ""), productCharge, product.getProductChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean beginProjectNotice(String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.BEGIN_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.BEGIN_PROJECT, loginUser.getUserName(), product.getName());
            String projectContent = getProjectContent("", product.getName(), DateUtil.conversionDate(product.getEndTime(), ""), productCharge, product.getProductChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
     * 完成项目 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean finishProjectNotice(String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.FINISH_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.FINISH_PROJECT, loginUser.getUserName(), product.getName());
            String projectContent = getProjectContent("", product.getName(), DateUtil.conversionDate(product.getEndTime(), ""), productCharge, product.getProductChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
     * 归档项目 发送通知
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-11 10:27
     */
    @Override
    @Async("customExecutor")
    public Boolean archiveProjectNotice(String productId) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.ARCHIVE_PROJECT.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.ARCHIVE_PROJECT, loginUser.getUserName(), product.getName());
            String projectContent = getProjectContent("", product.getName(), DateUtil.conversionDate(product.getEndTime(), ""), productCharge, product.getProductChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean remindRemarkNotice(String productId, String taskId, String comment) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.REMIND_REMARK.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.REMIND_REMARK, loginUser.getUserName(), comment);
            String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
                List<String> noticeUserIds = getSetNotice(notice, product);
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
                Date planEndTime = task.getPlanEndTime();
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
                String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
                Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
                sendMessage.setContentMap(contentMap);
                //发送消息的结果
                Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public Boolean docChangesNotice(String productId, String taskId, String docName) {
        ProductShowDTO product = productInfoService.getProductInfo(productId);
        if (Objects.isNull(product)) {
            return false;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String flag = NoticeEnum.DOC_CHANGES.getFlag();
        //根据节点标示获取到通知消息实体
        NoticeMessageEntity notice = baseMapper.getByNodeFlag(flag);
        if (!Objects.isNull(notice)) {
            String noticeMessageId = notice.getId();
            List<String> noticeUserIds = getSetNotice(notice, product);
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
            String messageContent = String.format(NoticeMessageConstant.DOC_CHANGES, loginUser.getUserName(), docName);
            String projectContent = getProjectContent(task.getName(), product.getName(), DateUtil.conversionDate(task.getPlanEndTime(), ""), taskCharge, task.getChargeName());
            Map contentMap = getCardMessageMap(messageContent, projectContent, fsAppUrl);
            sendMessage.setContentMap(contentMap);
            //发送消息的结果
            Boolean sendResult = fsService.batchSendMessage(sendMessage);
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
    public List<String> getSetNotice(NoticeMessageEntity notice, ProductShowDTO product) {
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
                if (itemPeopleList.contains(NoticeItemPeopleEnum.ITEM_MANAGER.getFlag())) {
                    if (!Objects.isNull(product)) {
                        //项目负责人
                        String projectChargeId = product.getProjectChargeId();
                        if (StringUtils.isNotBlank(projectChargeId)) {
                            List<String> projectChargeIdList = Arrays.asList(projectChargeId.split(","));
                            resultList.addAll(projectChargeIdList);
                        }

                        //这个是产品经理
                        if (itemPeopleList.contains(NoticeItemPeopleEnum.PRODUCT_MANAGER.getFlag())) {
                            String productChargeId = product.getProductChargeId();
                            if (StringUtils.isNotBlank(productChargeId)) {
                                List<String> productChargeIdList = Arrays.asList(productChargeId.split(","));
                                resultList.addAll(productChargeIdList);
                            }
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
        List<Map> elements = new ArrayList<>();
        Map<String, Object> fieldAllMap = new LinkedHashMap<>();
        fieldAllMap.put("tag", "div");
        List<Map> fieldMapList = new ArrayList<>();
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("is_short", true);
        Map textMap = new HashMap();
        textMap.put("tag", "lark_md");
        textMap.put("content", productContent);
        fieldMap.put("text", textMap);
        fieldMapList.add(fieldMap);
        fieldAllMap.put("fields", fieldMapList);
        elements.add(fieldAllMap);
        Map<String, Object> actionAllMap = new LinkedHashMap<>();
        actionAllMap.put("tag", "action");
        actionAllMap.put("layout", "bisected");
        List<Map> actionList = new ArrayList<>();
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
}




