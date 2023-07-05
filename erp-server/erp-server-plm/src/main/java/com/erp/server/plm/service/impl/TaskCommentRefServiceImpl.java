package com.erp.server.plm.service.impl;

import com.common.business.dto.FindUserDTO;
import com.common.business.service.SuperServiceImpl;
import com.erp.model.plm.dto.MemberPagingShowDTO;
import com.erp.model.plm.dto.ProductMemberDTO;
import com.erp.model.plm.entity.TaskCommentRefEntity;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.mapper.TaskCommentRefMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.TaskCommentRefService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务评论关联表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
 */
@Slf4j
@Service
public class TaskCommentRefServiceImpl extends SuperServiceImpl<TaskCommentRefMapper, TaskCommentRefEntity> implements TaskCommentRefService {

    @Resource
    private ProjectMembersService projectMembersService;

    @Resource
    private UserInfoFeign userInfoFeign;


    /**
     * 任务评论@后获取到对应的数据
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.ProductMemberDTO.TaskRefDTO>
     * @author yl
     * @date 2023-06-20 11:49
     */
    @Override
    public List<ProductMemberDTO.TaskRefDTO> listMemberByProductId(String productId) {
        List<ProductMemberDTO.TaskRefDTO> resultList = new ArrayList<>(4);
        //获取到第三方绑定的信息
        List<FindUserDTO> userList = userInfoFeign.listThirdBindUserInfo();
        List<String> userIdList = userList.stream().map(FindUserDTO::getUserId).collect(Collectors.toList());
        List<MemberPagingShowDTO> projectMembersList = projectMembersService.listByMembers(productId);
        projectMembersList = projectMembersList.stream().filter(p -> userIdList.contains(p.getMemberId())).collect(Collectors.toList());

        List<String> chargeStatus = Arrays.asList(ProductConstant.PRODUCT_CHARGE, ProductConstant.PROJECT_CHARGE);
        List<MemberPagingShowDTO> productChargeList = projectMembersList.stream().filter(p -> ProductConstant.PRODUCT_CHARGE.equals(p.getRoleName())).collect(Collectors.toList());
        List<MemberPagingShowDTO> projectChargeList = projectMembersList.stream().filter(p -> ProductConstant.PROJECT_CHARGE.equals(p.getRoleName())).collect(Collectors.toList());
        List<MemberPagingShowDTO> otherList = projectMembersList.stream().filter(p -> !chargeStatus.contains(p.getRoleName())).collect(Collectors.toList());

        //项目成员
        ProductMemberDTO.TaskRefDTO itemMember = getTaskMember("itemMember", "项目成员", otherList);
        resultList.add(itemMember);

        //产品经理
        ProductMemberDTO.TaskRefDTO productChargeMemberList = getTaskMember("productCharge", ProductConstant.PRODUCT_CHARGE, productChargeList);
        resultList.add(productChargeMemberList);

        //项目经理
        ProductMemberDTO.TaskRefDTO projectChargeMemberList = getTaskMember("projectCharge", ProductConstant.PROJECT_CHARGE, projectChargeList);
        resultList.add(projectChargeMemberList);

        ProductMemberDTO.TaskRefDTO allMember = new ProductMemberDTO.TaskRefDTO();
        allMember.setMemberId("all");
        allMember.setMemberName("所有成员");
        List<ProductMemberDTO.TaskRefDTO> allList = new ArrayList<>(userList.size());
        for (FindUserDTO member : userList) {
            ProductMemberDTO.TaskRefDTO memberDTO = new ProductMemberDTO.TaskRefDTO();
            memberDTO.setMemberId(member.getUserId());
            memberDTO.setMemberName(member.getUserName());
            allList.add(memberDTO);
        }
        allMember.setChildrenList(allList);
        allMember.setCount(userList.size());
        resultList.add(allMember);
        return resultList;
    }


    /**
     * 添加任务关注人信息
     *
     * @param refUserIdList
     * @param taskId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCommentRef(List<String> refUserIdList, String taskId, String commentId, List<FindUserDTO> userList) {
        if (CollectionUtils.isNotEmpty(refUserIdList)) {
            List<TaskCommentRefEntity> addList = new ArrayList<>(refUserIdList.size());
            for (String refUserId : refUserIdList) {
                TaskCommentRefEntity addEntity = new TaskCommentRefEntity();
                addEntity.setRefUserId(refUserId);
                addEntity.setTaskId(taskId);
                addEntity.setTaskCommentId(commentId);
                String userName = userList.stream().filter(u -> u.getUserId().equals(refUserId)).findFirst().
                        map(FindUserDTO::getUserName).orElse("");
                addEntity.setRefUserName(userName);
                addList.add(addEntity);
            }
            this.saveBatch(addList);
        }

    }

    /**
     * 更改发送结果
     *
     * @param refUserIds
     * @param taskCommentId
     * @param taskId
     * @param sendRefResult
     * @return void
     * @author yl
     * @date 2023-06-21 11:26
     */
    @Override
    public void updateSendResult(List<String> refUserIds, String taskCommentId, String taskId, Boolean sendRefResult) {
        if (CollectionUtils.isNotEmpty(refUserIds)) {
            this.lambdaUpdate().set(TaskCommentRefEntity::getSendNoticeResult, sendRefResult).
                    eq(TaskCommentRefEntity::getTaskCommentId, taskCommentId).
                    in(TaskCommentRefEntity::getRefUserId, refUserIds).update();
        }
    }


    /**
     * 根据评论id 获取到对应的信息
     *
     * @param commentIdList
     * @return java.util.List<com.erp.model.plm.entity.TaskCommentRefEntity>
     * @author yl
     * @date 2023-06-21 11:45
     */
    @Override
    public List<TaskCommentRefEntity> listByCommentIdList(List<String> commentIdList) {
        if (CollectionUtils.isEmpty(commentIdList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(TaskCommentRefEntity::getTaskCommentId,commentIdList).list();
    }

    private ProductMemberDTO.TaskRefDTO getTaskMember(String type, String typeName, List<MemberPagingShowDTO> list) {
        ProductMemberDTO.TaskRefDTO itemMember = new ProductMemberDTO.TaskRefDTO();
        itemMember.setMemberId(type);
        itemMember.setMemberName(typeName);
        itemMember.setCount(list.size());
        List<ProductMemberDTO.TaskRefDTO> memberList = new ArrayList<>(list.size());
        for (MemberPagingShowDTO member : list) {
            ProductMemberDTO.TaskRefDTO memberDTO = new ProductMemberDTO.TaskRefDTO();
            memberDTO.setMemberId(member.getMemberId());
            memberDTO.setMemberName(member.getMemberName());
            memberList.add(memberDTO);
        }
        itemMember.setChildrenList(memberList);
        return itemMember;
    }
}
