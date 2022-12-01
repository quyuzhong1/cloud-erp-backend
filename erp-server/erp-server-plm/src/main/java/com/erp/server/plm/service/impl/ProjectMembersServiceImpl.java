package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.enums.ProjectTemplateTypeEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.ProjectMembersMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private ProjectTaskService projectTaskService;

    @Autowired
    private RoleRefMemberService roleRefMemberService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private ProjectTemplateService projectTemplateService;

    @Autowired
    private TemplateRoleService templateRoleService;

    @Autowired
    private TemplateMembersService templateMembersService;

    @Autowired
    private TemplateRoleRefMembersService templateRoleRefMembersService;

    @Autowired
    private CommonService commonService;


    /**
     * 启动项目 添加成员
     *
     * @param productId
     * @param projectId
     * @param members
     * @return void
     * @author yl
     * @date 2022-10-27 19:01
     */
    @Override
    public void add(String productId, String projectId, List<String> members) {
        List<ProjectMembersEntity> existList = this.getListByProductId(productId);
        List<ProjectMembersEntity> addList = new LinkedList<>();
        List<FindUserDTO> findUsers = sysUserFeign.getUserList();
        for (String item : members) {
            long count = existList.stream().filter(p -> p.getMemberId().equals(item)).count();
            //表示没有重复
            if (count == 0) {
                ProjectMembersEntity entity = new ProjectMembersEntity();
                String userId = item;
                entity.setMemberId(userId);
                FindUserDTO user = findUsers.stream().filter(u -> userId.equals(u.getUserId())).findFirst().orElse(null);
                if (!Objects.isNull(user)) {
                    entity.setMemberName(user.getUserName());
                } else {
                    entity.setMemberName("");
                }
                entity.setProjectId(projectId);
                entity.setProductId(productId);
                addList.add(entity);
            }

        }
        this.saveBatch(addList);
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
                entity.setId(IdWorker.getIdStr());
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
    public Boolean saveOrUpdateMember(SaveOrUpdateProjectMemberDTO dto) {
        LoginUser loginUser = commonService.getUserInfo();
        List<String> userIdList = dto.getUserIdList();
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        String id = dto.getId();
        roleRefMemberService.checkRoleMember(dto.getRoleRefMemberId(), dto.getRoleId(), dto.getUserIdList(),dto.getProductId());
        List<ProjectMembersEntity> addList = new ArrayList<>(userIdList.size());
        for (String userId : userIdList) {
            ProjectMembersEntity entity = new ProjectMembersEntity();
            entity.setProductId(dto.getProductId());
            FindUserDTO user = userList.stream().filter(u -> userId.equals(u.getUserId())).findFirst().orElse(null);
            String userName = "";
            if (user != null) {
                userName = user.getUserName();
            }
            entity.setMemberName(userName);
            entity.setMemberId(userId);
            entity.setId(id);
            entity.setCreateUserId(loginUser.getUid());
            entity.setCreateUserName(loginUser.getUserName());
            addList.add(entity);
        }

        Boolean flag = this.saveOrUpdateBatch(addList);
        //保存成功就要去保存关系表
        if (flag) {
            List<String> membersTableIds = addList.stream().map(ProjectMembersEntity::getId).collect(Collectors.toList());
            roleRefMemberService.saveRef(membersTableIds, dto.getRoleId(), dto.getProductId());
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
        String productId = params.getProductId();
        List<String> roleIds = new ArrayList<>();
        String roleId = params.getProjectRoleId();
        IPage pageData = new Page(); //查看所有的人
        if (StringUtils.isBlank(roleId)) {
            List<ProjectRoleEntity> roleList = projectRoleService.listByProductId(productId);
            List<RoleRefMemberEntity> refList = roleRefMemberService.getByProductId(productId);
            //查看所有的人
            pageData = baseMapper.allPaging(query, productId);
            List<MemberPagingShowDTO> list = pageData.getRecords();
            if (CollectionUtils.isNotEmpty(list)) {
                //获取到任务处理的情况
                List<TaskConductDTO> conductList = projectTaskService.getTaskConductList(params.getProductId());
                for (MemberPagingShowDTO item : list) {
                    String membersId = item.getId();
                    RoleRefMemberEntity ref = refList.stream().filter(r -> r.getMembersId().equals(membersId)).findFirst().orElse(null);
                    if (ref != null) {
                        item.setRoleId(ref.getRoleId());
                        ProjectRoleEntity role = roleList.stream().filter(r -> r.getId().equals(ref.getRoleId())).findFirst().orElse(null);
                        item.setRoleRefMemberId(ref.getId());
                        if (role != null) {
                            item.setRoleName(role.getName());
                        }
                    }
                    Integer totalTaskCount = 0;
                    Integer finishTaskCount = 0;
                    Integer ingTaskCount = 0;
                    Integer postponeTaskCount = 0;
                    TaskConductDTO taskConduct = conductList.stream().filter(c -> c.getMembersId().equals(item.getMemberId())).findFirst().orElse(null);
                    if (!Objects.isNull(taskConduct)) {
                        totalTaskCount = taskConduct.getTotalTaskCount();
                        finishTaskCount = taskConduct.getFinishTaskCount();
                        ingTaskCount = totalTaskCount - finishTaskCount;
                        postponeTaskCount = taskConduct.getPostponeTaskCount();
                    }
                    item.setTotalTaskCount(totalTaskCount);
                    item.setFinishTaskCount(finishTaskCount);
                    item.setIngTaskCount(ingTaskCount);
                    item.setPostponeTaskCount(postponeTaskCount);
                }
            }

        } else {
            roleIds.add(roleId);
            pageData = baseMapper.paging(query, productId, roleIds);
            List<MemberPagingShowDTO> list = pageData.getRecords();
            if (CollectionUtils.isNotEmpty(list)) {
                //获取到任务处理的情况
                List<TaskConductDTO> conductList = projectTaskService.getTaskConductList(params.getProductId());
                for (MemberPagingShowDTO item : list) {
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
                List<ProjectMembersEntity> projectMembers = item.getValue();
                ProjectMembersEntity filterEntity = projectMembers.stream().filter(p -> memberId.equals(p.getMemberId())).findFirst().orElse(null);
                if (filterEntity != null) {
                    dto.setName(filterEntity.getMemberName());
                } else {
                    dto.setName("");
                }
                dto.setCount(projectMembers.size());
                dto.setProductIds(projectMembers.stream().map(ProjectMembersEntity::getProductId).collect(Collectors.toList()));
                resultList.add(dto);
            }
        }


        return resultList;
    }

    /**
     * 删除成员
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-11 11:50
     */

    @Override
    @Transactional
    public Boolean removeMembers(RemoveProjectMemberDTO dto) {
        boolean flag = this.removeById(dto.getId());
        roleRefMemberService.removeById(dto.getRoleRefMemberId());
        return flag;
    }


    /**
     * 获取到 所有 是负责人的成员
     *
     * @return
     */
    @Override
    public List<ProjectMembersEntity> getChargeList(String productId) {
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getIsCharge, IsConstant.YES);
        queryWrapper.eq(ProjectMembersEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    @Override
    public List<ProjectMemberDTO> memberList(String productId) {
        List<ProjectMemberDTO> resultList = new ArrayList<>();
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getProductId, productId);
        List<ProjectMembersEntity> list = this.list(queryWrapper);
        resultList = BeanMapper.copyList(list, ProjectMemberDTO.class);
        if (CollectionUtils.isNotEmpty(resultList)) {
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
            for (ProjectMemberDTO item : resultList) {
                long taskCount = taskList.stream().filter(t -> t.getChargeId().contains(item.getMemberId())).count();
                item.setTaskCount(taskCount);
            }
        }
        return resultList;
    }

    @Override
    public Boolean ifProjectMember(String userId, String productId) {
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getProductId, productId);
        queryWrapper.eq(ProjectMembersEntity::getMemberId, userId);
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public List<TaskConductDTO> getUserTaskConduct(List<FindUserDTO> userList) {
        Date date = new Date();
        List<TaskConductDTO> resultList = new ArrayList<>();
        List<ProjectTaskEntity> list = projectTaskService.list();
        Integer finishState = TaskStateEnum.FINISH.getCode();
        Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
        Integer ing = TaskStateEnum.ING.getCode();
        for (FindUserDTO item : userList) {
            List<ProjectTaskEntity> taskList = list.stream().filter(t -> t.getChargeId().contains(item.getUserId())).collect(Collectors.toList());
            //完成任务数
            int finishTaskCount = taskList.stream().filter(t -> finishState.equals(t.getStatus()) || approvalPass.equals(t.getStatus())).collect(Collectors.toList()).size();
            //进行中
            int ingTaskCount = taskList.stream().filter(t -> ing.equals(t.getStatus())).collect(Collectors.toList()).size();
            //总任务数
            int totalTaskCount = taskList.size();
            int unfinishedTaskCount = taskList.stream().filter(t -> !finishState.equals(t.getStatus()) && !approvalPass.equals(t.getStatus())).collect(Collectors.toList()).size();
            //延期的任务数
            int postponeTaskCount = 0;
            postponeTaskCount = taskList.stream().filter(t -> t.getPlanEndTime() != null && date.compareTo(t.getPlanEndTime()) == 1).collect(Collectors.toList()).size();
            TaskConductDTO dto = new TaskConductDTO();
            dto.setMembersId(item.getUserId());
            dto.setMembersName(item.getUserName());
            dto.setTotalTaskCount(totalTaskCount);
            dto.setFinishTaskCount(finishTaskCount);
            dto.setIngTaskCount(ingTaskCount);
            dto.setPostponeTaskCount(postponeTaskCount);
            dto.setUnfinishedTaskCount(unfinishedTaskCount);
            resultList.add(dto);
        }

        return resultList;
    }

    @Override
    @Transactional
    public void addRoleAndMembersByApproval(String productId) {
        LambdaQueryWrapper<ProjectTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTemplateEntity::getType, ProjectTemplateTypeEnum.APPROVAL_TEMPLATE.getCode());
        ProjectTemplateEntity entity = projectTemplateService.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(entity)) {
            //模板角色成员关联表
            List<TemplateRoleRefMembersEntity> oldRefList = templateRoleRefMembersService.getByTemplateId(entity.getId());
            List<TemplateRoleEntity> newRoleList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(oldRefList)) {
                //模板角色
                List<TemplateRoleEntity> oldRoleList = templateRoleService.getByTemplateId(entity.getId());
                //模板成员
                List<TemplateMembersEntity> oldMembersList = templateMembersService.getByTemplateId(entity.getId());
                List<Pair<String, String>> addRoleList = new ArrayList<>();
                oldRefList.stream().forEach(obj -> {
                    TemplateRoleEntity oldRole = oldRoleList.stream().filter(e -> e.getId().equals(obj.getRoleId()) && e.getTemplateId().equals(obj.getTemplateId())).findAny().orElse(null);

                    TemplateMembersEntity oldMembers = oldMembersList.stream().filter(e -> e.getId().equals(obj.getMembersId()) && e.getTemplateId().equals(obj.getTemplateId())).findAny().orElse(null);
                    //新增角色
                    if (CollectionUtils.isNotEmpty(addRoleList)) {
                        //如果已经新增过则无需再次新增
                        Pair<String, String> pair = addRoleList.stream().filter(e -> e.getKey().equals(oldRole.getId())).findAny().orElse(null);
                        if (pair != null) {
                            obj.setRoleId(pair.getValue());
                        } else {
                            //新增角色，防止重复新增先存储List中
                            ProjectRoleEntity newRole = new ProjectRoleEntity();
                            BeanMapperUtils.copy(oldRole, newRole);
                            newRole.setProductId(productId);
                            newRole.setId(null);
                            projectRoleService.save(newRole);
                            obj.setRoleId(newRole.getId());
                            Pair<String, String> newPair = new Pair<>(oldRole.getId(), newRole.getId());
                            if (!addRoleList.contains(newPair)) {
                                addRoleList.add(newPair);
                            }
                        }
                    } else {
                        //新增角色，防止重复新增先存储List中
                        ProjectRoleEntity newRole = new ProjectRoleEntity();
                        BeanMapperUtils.copy(oldRole, newRole);
                        newRole.setProductId(productId);
                        newRole.setId(null);
                        projectRoleService.save(newRole);
                        obj.setRoleId(newRole.getId());
                        Pair<String, String> pair = new Pair<>(oldRole.getId(), newRole.getId());
                        if (!addRoleList.contains(pair)) {
                            addRoleList.add(pair);
                        }
                    }
                    //新增成员
                    ProjectMembersEntity newMembers = new ProjectMembersEntity();
                    BeanMapperUtils.copy(oldMembers, newMembers);
                    newMembers.setProductId(productId);
                    newMembers.setId(null);
                    this.save(newMembers);
                    obj.setMembersId(newMembers.getId());
                });
                List<RoleRefMemberEntity> roleRefMemberList = BeanMapperUtils.copyList(RoleRefMemberEntity.class, oldRefList);
                roleRefMemberList.stream().forEach(obj -> obj.setProductId(productId).setId(null));
                roleRefMemberService.saveBatch(roleRefMemberList);
            }
        }

    }


    /**
     * 根据成员获取 信息
     *
     * @return java.util.List<com.erp.model.plm.entity.ProjectMembersEntity>
     * @author yl
     * @date 2022-12-01 14:04
     */
    @Override
    public List<ProjectMembersEntity> getByMemberIds(List<String> memberIds, String productId) {
        if (CollectionUtils.isNotEmpty(memberIds)) {
            LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProjectMembersEntity::getProductId, productId);
            queryWrapper.in(ProjectMembersEntity::getMemberId, memberIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


}
