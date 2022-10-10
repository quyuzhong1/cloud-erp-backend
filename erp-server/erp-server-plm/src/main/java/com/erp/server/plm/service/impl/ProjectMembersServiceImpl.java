package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProjectInfoEntity;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.ProjectRoleEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProjectMembersMapper;
import com.erp.server.plm.service.ProjectInfoService;
import com.erp.server.plm.service.ProjectMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.plm.service.ProjectTaskService;

import com.erp.server.plm.service.RoleRefMemberService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 项目成员表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectMembersServiceImpl extends ServiceImpl<ProjectMembersMapper, ProjectMembersEntity> implements ProjectMembersService {


    @Autowired
    private ProjectInfoService projectInfoService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private RoleRefMemberService roleRefMemberService;


    @Override
    public void add(String productId, String projectId, List<ProjectMemberDTO> members) {
        List<ProjectMembersEntity> addList = new LinkedList<>();
        for (ProjectMemberDTO item : members) {
            ProjectMembersEntity entity = new ProjectMembersEntity();
            entity.setMemberId(item.getUserId());
            entity.setMemberName(item.getUsetName());
            entity.setProjectId(projectId);
            entity.setProductId(productId);
            addList.add(entity);
        }
        this.saveBatch(addList);
    }


    /**
     * 保存模板的成员
     *
     * @param flagId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-20 14:52
     */
    @Override
    public void saveMember(String flagId, String productId) {
        List<ProjectMembersEntity> list = getListByProductId(productId);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity entity : list) {
                entity.setFlagId(flagId);
            }
            this.saveBatch(list);
        }
    }


    /**
     * 保存项目成员 从项目复制
     *
     * @param projectId
     * @param flagId
     * @return void
     * @author yl
     * @date 2022-09-20 17:36
     */
    @Override
    public void saveMemberByProject(String productId, String projectId, String flagId) {
        List<ProjectMembersEntity> list = getListByProductId(flagId);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity entity : list) {
                entity.setProjectId(projectId);
                entity.setProductId(productId);
            }
            this.saveBatch(list);
        }


    }

    @Override
    public void saveMemberByTemplate(String productId, String projectId, String flagId) {
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getFlagId, flagId);
        List<ProjectMembersEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectMembersEntity entity : list) {
                entity.setProjectId(projectId);
                entity.setProductId(productId);
            }
            this.saveBatch(list);
        }

    }


    @Override
    public List<ProjectMembersEntity> getListByProductId(String productId) {
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


    @Override
    @Transactional
    public Boolean saveOrUpdateMember(saveOrUpdateProjectMemberDTO dto) {
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        Boolean flag = false;
        String id = dto.getId();
        ProjectMembersEntity entity = new ProjectMembersEntity();
        entity.setProductId(dto.getProductId());
        entity.setProjectId(dto.getProjectId());
        entity.setMemberName(dto.getUseName());
        entity.setMemberId(dto.getUserId());
        entity.setId(id);
        if (loginUser != null) {
            entity.setCreateUserId(loginUser.getUid());
            entity.setCreateUserName(loginUser.getUserName());
        }
        Integer isCharge = dto.getIsCharge();
        flag = this.saveOrUpdate(entity);
        //当保存成功且是项目负责人 就要去更改项目负责人
        Boolean isUpdate = false;
        if (StringUtils.isNotBlank(id) && flag) {
            isUpdate = true;
        }
        //保存成功就要去保存关系表
        if (flag) {
            roleRefMemberService.saveOrUpdateRef(dto.getRoleRefMemberId(), entity.getId(), dto.getRoleId());
        }
        if (IsConstant.YES.equals(isCharge) && flag) {
            projectInfoService.updateCharge(dto.getProjectId(), dto.getUseName(), dto.getUserId(), isUpdate);
        }

        return flag;

    }


    /**
     * 分页获取项目成员
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO
     * @author yl
     * @date 2022-09-26 18:29
     */
    @Override
    public PagingVO<List<MemberPagingShowDTO>> paging(PagingDTO<MemberPagingDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        MemberPagingDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        List<MemberPagingShowDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            ProjectInfoEntity projectInfo = projectInfoService.getById(params.getProjectId());
            String chargeId = projectInfo.getChargeId();
            //获取到任务处理的情况
            List<TaskConductDTO> conductList = projectTaskService.getTaskConductList(params.getProjectId());
            for (MemberPagingShowDTO item : list) {
                //如果包含该员工 就是 项目负责人
                if (chargeId.contains(item.getMemberId())) {
                    item.setIsCharge(IsConstant.YES);
                } else {
                    item.setIsCharge(IsConstant.NO);
                }
                TaskConductDTO taskConduct = conductList.stream().filter(c -> c.getMembersId().equals(item.getMemberId())).findFirst().orElse(null);
                Integer totalTaskCount = 0;
                Integer finishTaskCount = 0;
                Integer ingTaskCount = 0;
                Integer postponeTaskCount = 0;
                if (!Objects.isNull(taskConduct)) {
                    totalTaskCount = taskConduct.getTotalTaskCount();
                    finishTaskCount = taskConduct.getFinishTaskCount();
                    ingTaskCount = taskConduct.getIngTaskCount();
                    postponeTaskCount = taskConduct.getPostponeTaskCount();
                }
                item.setTotalTaskCount(totalTaskCount);
                item.setFinishTaskCount(finishTaskCount);
                item.setIngTaskCount(ingTaskCount);
                item.setPostponeTaskCount(postponeTaskCount);
            }
        }
        return new PagingVO(pageData);

    }


    /**
     * 根据成员id 获取到 成员名 以及它参与了多少项目
     *
     * @param memberList
     * @return java.util.List<com.erp.model.plm.dto.ProductRoleMemberDTO>
     * @author yl
     * @date 2022-10-10 11:37
     */
    @Override
    public List<ProductRoleMemberDTO> getProductCountByMemberList(List<String> memberList) {
        List<ProductRoleMemberDTO> resultList = new LinkedList<>();
        //当不为空
        if (CollectionUtils.isNotEmpty(memberList)) {
            LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectMembersEntity::getMemberId, memberList);
            List<ProjectMembersEntity> list = this.list(queryWrapper);
            //以成员id 分类
            Map<String, List<ProjectMembersEntity>> memberMap = list.parallelStream().collect(Collectors.groupingBy(ProjectMembersEntity::getMemberId));
            for (Map.Entry<String, List<ProjectMembersEntity>> item : memberMap.entrySet()) {
                ProductRoleMemberDTO dto = new ProductRoleMemberDTO();
                String memberId = item.getKey();
                dto.setMemberId(memberId);
                List<ProjectMembersEntity> projectMembers=item.getValue();
                ProjectMembersEntity filterEntity = projectMembers.stream().filter(p -> memberId.equals(p.getMemberId())).findFirst().orElse(null);
                if (filterEntity != null) {
                    dto.setMemberName(filterEntity.getMemberName());
                } else {
                    dto.setMemberName("");
                }
                dto.setProductCount(projectMembers.size());
                dto.setProductIds(projectMembers.stream().map(ProjectMembersEntity::getProductId).collect(Collectors.toList()));
                resultList.add(dto);
            }
        }


        return resultList;
    }


}
