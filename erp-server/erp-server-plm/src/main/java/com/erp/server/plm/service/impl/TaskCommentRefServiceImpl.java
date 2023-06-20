package com.erp.server.plm.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.plm.dto.MemberPagingShowDTO;
import com.erp.model.plm.dto.ProductMemberDTO;
import com.erp.model.plm.entity.TaskCommentRefEntity;
import com.erp.server.plm.constant.ProductConstant;
import com.erp.server.plm.mapper.TaskCommentRefMapper;
import com.erp.server.plm.service.ProjectMembersService;
import com.erp.server.plm.service.TaskCommentRefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
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
        List<MemberPagingShowDTO> projectMembersList = projectMembersService.listByMembers(productId);

        List<String> chargeStatus = Arrays.asList(ProductConstant.PRODUCT_CHARGE, ProductConstant.PROJECT_CHARGE);
        List<MemberPagingShowDTO> chargeList = projectMembersList.stream().filter(p -> chargeStatus.contains(p.getRoleName())).collect(Collectors.toList());
//        List<ProductOverviewDTO.ProductMemberDTO> chargeMemberList = getProductMember(chargeList, userDeptList, taskList);
//        List<MemberPagingShowDTO> otherList = projectMembersList.stream().filter(p -> !chargeStatus.contains(p.getRoleName())).collect(Collectors.toList());
//        List<ProductOverviewDTO.ProductMemberDTO> productMemberList = getProductMember(otherList, userDeptList, taskList);
        return null;
    }
}
