package com.erp.server.plm.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.IsConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.DistributionTypeEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.model.plm.vo.ItemMemberVO;
import com.erp.model.sys.dto.UserSuperiorDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProjectMembersMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private TaskChargeDistributionService taskChargeDistributionService;

    @Resource
    private ProductInfoService productInfoService;


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
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        List<String> userIdList = dto.getUserIdList();
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        String id = dto.getId();
        List<String> members = roleRefMemberService.checkRoleMember(dto.getRoleRefMemberId(), dto.getRoleId(), dto.getUserIdList(), dto.getProductId());
        if (CollectionUtils.isNotEmpty(members)) {
            userIdList = userIdList.stream().filter(e -> !members.contains(e)).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(userIdList) && MathUtil.ONE.equals(dto.getFlag())) {
            throw new ServiceException(ApiError.ERROR_95021);
        }
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
        ProjectRoleEntity projectRoleEntity = projectRoleService.getById(dto.getRoleId());
        if (ObjectUtils.isEmpty(projectRoleEntity)) {
            throw new ServiceException(ApiError.ERROR_9021);
        }
        //成员新增成功后，需要更新产品下面的未发布、未开始、进行中的任务
        List<ProjectTaskEntity> projectTaskList = projectTaskService.listByProductId(dto.getProductId());
        if (CollectionUtils.isEmpty(projectTaskList)) {
            return flag;
        }
        List<ProjectTaskEntity> list = projectTaskList.stream().filter(obj -> (TaskStateEnum.TO_BE_RELEASED.getCode().equals(obj.getStatus()) || TaskStateEnum.NOT_START.getCode().equals(obj.getStatus()) || TaskStateEnum.ING.getCode().equals(obj.getStatus()))).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return flag;
        }
        List<ProjectTaskEntity> addTaskList = new ArrayList<>();
        List<TaskChargeDistributionEntity> addTaskChargeDistributionList = new ArrayList<>();
        list.forEach(obj -> {
            if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(obj.getDistributionType())) {
                List<String> collect = Arrays.stream(obj.getRoleName().split(",")).collect(Collectors.toList());
                if (collect.contains(projectRoleEntity.getName())) {
                    //更新任务负责人
                    if (StringUtils.isNotBlank(obj.getChargeId())) {
                        List<String> chargetIds = Arrays.stream(obj.getChargeId().split(",")).collect(Collectors.toList());
                        for (String userId : dto.getUserIdList()) {
                            if (!chargetIds.contains(userId)) {
                                chargetIds.add(userId);
                            }
                        }
                        chargetIds = chargetIds.stream().distinct().collect(Collectors.toList());
                        List<String> finalChargetIds = chargetIds;
                        //人员名称
                        List<String> userNameList = userList.stream().filter(e -> finalChargetIds.contains(e.getUserId())).map(FindUserDTO::getUserName).collect(Collectors.toList());
                        obj.setChargeId(StringUtils.join(chargetIds, ","));
                        if (CollectionUtils.isNotEmpty(userNameList)) {
                            obj.setChargeName(StringUtils.join(userNameList, ","));
                        }
                    } else {
                        obj.setChargeId(StringUtils.join(dto.getUserIdList(), ","));
                        //人员名称
                        List<String> userNameList = userList.stream().filter(e -> dto.getUserIdList().contains(e.getUserId())).map(FindUserDTO::getUserName).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(userNameList)) {
                            obj.setChargeName(StringUtils.join(userNameList, ","));
                        }
                    }
                    addTaskList.add(obj);
                }
            }
            //查询任务下审核人
            List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, obj.getId());
            if (CollectionUtils.isNotEmpty(taskChargeDistributionList)) {
                //根据分配类型查询模板中的数据
                for (TaskChargeDistributionEntity taskChargeDistributionEntity : taskChargeDistributionList) {
                    if (StringUtils.isBlank(taskChargeDistributionEntity.getCharges())) {
                        throw new ServiceException(ApiError.ERROR_95097);
                    }
                    List<String> chargeList = Arrays.stream(taskChargeDistributionEntity.getCharges().split(",")).collect(Collectors.toList());
                    //按角色分配
                    if (DistributionTypeEnum.DISTRIBUTION_ROLE.getCode().equals(taskChargeDistributionEntity.getDistributionType()) && chargeList.contains(projectRoleEntity.getName())) {
                        //更新任务审核人
                        if (StringUtils.isNotBlank(taskChargeDistributionEntity.getChargeIds())) {
                            List<String> chargetIds = Arrays.stream(taskChargeDistributionEntity.getChargeIds().split(",")).collect(Collectors.toList());
                            for (String userId : dto.getUserIdList()) {
                                if (!chargetIds.contains(userId)) {
                                    chargetIds.add(userId);
                                }
                            }
                            taskChargeDistributionEntity.setChargeIds(StringUtils.join(chargetIds, ","));
                        } else {
                            taskChargeDistributionEntity.setChargeIds(StringUtils.join(dto.getUserIdList(), ","));
                        }
                        addTaskChargeDistributionList.add(taskChargeDistributionEntity);
                    }
                    //按上级分配
                    if (DistributionTypeEnum.DISTRIBUTION_SUPERIOR.getCode().equals(taskChargeDistributionEntity.getDistributionType()) && StringUtils.isNotBlank(obj.getChargeId())) {
                        //查询对应负责人的上级
                        List<String> ids = Arrays.stream(obj.getChargeId().split(",")).collect(Collectors.toList());
                        List<UserSuperiorDTO> userSuperiorDTOS = sysUserFeign.listSuperiorByUserIds(ids);
                        if (CollectionUtils.isNotEmpty(userSuperiorDTOS)) {
                            for (String superiorType : chargeList) {
                                List<String> userIds = userSuperiorDTOS.stream().filter(e -> e.getSuperiorType().equals(superiorType)).map(UserSuperiorDTO::getUserId).collect(Collectors.toList());
                                //判断是否存在上级
                                if (CollectionUtils.isNotEmpty(userIds)) {
                                    if (StringUtils.isNotBlank(taskChargeDistributionEntity.getChargeIds())) {
                                        List<String> chargetIds = Arrays.stream(taskChargeDistributionEntity.getChargeIds().split(",")).collect(Collectors.toList());
                                        userIds.addAll(chargetIds);
                                        userIds = userIds.stream().distinct().collect(Collectors.toList());
                                    }
                                    taskChargeDistributionEntity.setChargeIds(StringUtils.join(userIds, ","));
                                    addTaskChargeDistributionList.add(taskChargeDistributionEntity);
                                }
                            }
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(addTaskChargeDistributionList)) {
                    taskChargeDistributionService.updateBatchById(addTaskChargeDistributionList);
                }
            }
        });
        if (CollectionUtils.isNotEmpty(addTaskList)) {
            projectTaskService.updateBatchById(addTaskList);
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
    public PagingVO<MemberPagingShowDTO> paging(PagingDTO<MemberPagingDTO> dto) {
        Page<MemberPagingDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        MemberPagingDTO params = dto.getParams();
        String productId = params.getProductId();
        List<String> roleIds = new ArrayList<>();
        String roleId = params.getProjectRoleId();
        IPage<MemberPagingShowDTO> pageData; //查看所有的人
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

        return new PagingVO<>(pageData);

    }


    /**
     * 根据成员id 获取到 成员名 以及它参与了多少项目
     *
     * @param memberList    成员表 table id
     * @param productIdList 产品表id
     * @return java.util.List<com.erp.model.plm.dto.ProductRoleMemberDTO>
     * @author yl
     * @date 2022-10-10 11:37
     */
    @Override
    public List<ProductRoleMemberDTO> getProductCountByMemberList(List<String> memberList, List<String> productIdList) {
        List<ProductRoleMemberDTO> resultList = new LinkedList<>();
        //当不为空
        if (CollectionUtils.isNotEmpty(memberList)) {
            LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectMembersEntity::getId, memberList);
            if (CollectionUtils.isNotEmpty(productIdList)) {
                queryWrapper.in(ProjectMembersEntity::getProductId, productIdList);
            }
            List<ProjectMembersEntity> list = this.list(queryWrapper);
            //以成员id 分类
            Map<String, List<ProjectMembersEntity>> memberMap = list.parallelStream().collect(Collectors.groupingBy(ProjectMembersEntity::getMemberId));
            for (Map.Entry<String, List<ProjectMembersEntity>> item : memberMap.entrySet()) {
                ProductRoleMemberDTO dto = new ProductRoleMemberDTO();
                //成员id
                String memberId = item.getKey();
                List<ProjectMembersEntity> projectMembers = item.getValue();
                ProjectMembersEntity filterEntity = projectMembers.stream().filter(p -> memberId.equals(p.getMemberId())).findFirst().orElse(null);
                if (filterEntity != null) {
                    dto.setName(filterEntity.getMemberName());
                } else {
                    dto.setName("");
                }
                List<String> productIds = projectMembers.stream().map(ProjectMembersEntity::getProductId).distinct().collect(Collectors.toList());
                dto.setCount(productIds.size());
                dto.setProductQuantity(productIds.size());
                dto.setProductIds(productIds);
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
        RoleRefMemberEntity roleRefMemberEntity = roleRefMemberService.getById(dto.getRoleRefMemberId());
        if (ObjectUtils.isEmpty(roleRefMemberEntity)) {
            return Boolean.TRUE;
        }
        //删除
        boolean flag = this.removeById(dto.getId());
        //删除关联表信息
        roleRefMemberService.removeById(dto.getRoleRefMemberId());
        //判断是否需要删除角色
        List<RoleRefMemberEntity> list = roleRefMemberService.getByRoleIdsAndProductId(Arrays.asList(roleRefMemberEntity.getRoleId()), roleRefMemberEntity.getProductId());
        if (CollectionUtils.isEmpty(list)) {
            //如果是角色关联的最后一个成员，则删除角色
            projectRoleService.removeById(roleRefMemberEntity.getRoleId());
        }
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
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectMembersEntity::getProductId, productId);
        List<ProjectMembersEntity> list = this.list(queryWrapper);
        List<ProjectMemberDTO> resultList = BeanMapper.copyList(list, ProjectMemberDTO.class);
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
    public List<TaskConductDTO> getUserTaskConduct(List<FindUserDTO> userList, List<Integer> stateList) {
        LocalDateTime date = LocalDateTime.now();
        List<TaskConductDTO> resultList = new ArrayList<>();
        List<ProjectTaskEntity> list = projectTaskService.list();
        Integer finishState = TaskStateEnum.FINISH.getCode();
        Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
        Integer ing = TaskStateEnum.ING.getCode();
        for (FindUserDTO item : userList) {
            List<ProjectTaskEntity> taskList = list.stream().filter(
                    t -> StringUtils.isNotBlank(t.getChargeId()) &&
                            Arrays.asList(t.getChargeId().split(","))
                                    .contains(item.getUserId())
            ).collect(Collectors.toList());
            //完成任务数
            int finishTaskCount = taskList.stream().filter(t -> finishState.equals(t.getStatus()) || approvalPass.equals(t.getStatus())).collect(Collectors.toList()).size();
            //进行中
            int ingTaskCount = taskList.stream().filter(t -> ing.equals(t.getStatus())).collect(Collectors.toList()).size();


            //总任务数
            int totalTaskCount = taskList.size();
            int unfinishedTaskCount = taskList.stream().filter(t -> !finishState.equals(t.getStatus()) && !approvalPass.equals(t.getStatus())).collect(Collectors.toList()).size();
            int flagTaskCount = unfinishedTaskCount;
            if (CollectionUtils.isNotEmpty(stateList)) {
                flagTaskCount = (int) taskList.stream().filter(t -> stateList.contains(t.getStatus())).count();
            }

            //延期的任务数
            int postponeTaskCount = 0;
            postponeTaskCount = taskList.stream().filter(t -> t.getPlanEndTime() != null && date.compareTo(LocalDateTimeUtil.of(t.getPlanEndTime())) > MathUtil.ZERO).collect(Collectors.toList()).size();
            TaskConductDTO dto = new TaskConductDTO();
            dto.setMembersId(item.getUserId());
            dto.setMembersName(item.getUserName());
            dto.setTotalTaskCount(totalTaskCount);
            dto.setFinishTaskCount(finishTaskCount);
            dto.setIngTaskCount(ingTaskCount);
            dto.setPostponeTaskCount(postponeTaskCount);
            dto.setUnfinishedTaskCount(unfinishedTaskCount);
            dto.setFlagTaskCount(flagTaskCount);
            resultList.add(dto);
        }

        return resultList;
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

    @Override
    public List<ProjectMembersEntity> listByRoleIds(List<String> roleIdList, String productId) {
        List<RoleRefMemberEntity> list = roleRefMemberService.getByRoleIdsAndProductId(roleIdList, productId);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> membersIds = list.stream().map(RoleRefMemberEntity::getMembersId).collect(Collectors.toList());
        return this.listByIds(membersIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByRoleAndMembers(String productId, String projectId, String roleName, List<String> memberList) {
        ProjectRoleEntity found = projectRoleService.getByRoleName(productId, roleName);
        if (ObjectUtils.isEmpty(found)) {
            //查询产品经理角色，不存在则新增
            found = new ProjectRoleEntity();
            found.setProductId(productId);
            found.setProjectId(projectId);
            found.setName(roleName);
            projectRoleService.save(found);
        }
        SaveOrUpdateProjectMemberDTO saveOrUpdateProjectMemberDTO = new SaveOrUpdateProjectMemberDTO();
        saveOrUpdateProjectMemberDTO.setProductId(productId);
        saveOrUpdateProjectMemberDTO.setRoleId(found.getId());
        saveOrUpdateProjectMemberDTO.setUserIdList(memberList);
        //新增或修改产品经理角色和对应成员
        return this.saveOrUpdateMember(saveOrUpdateProjectMemberDTO);
    }


    /**
     * 根据产品id 集合获取对应项目角色成员
     *
     * @param productIds
     * @return java.util.List<com.erp.model.plm.vo.ItemMemberVO>
     * @author yl
     * @date 2023-02-23 16:59
     */
    @Override
    public List<ItemMemberVO> getByProductIds(List<String> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return new ArrayList<>(1);
        }
        String productManager = "产品经理";
        String projectManager = "项目经理";
        List<RoleRefMemberEntity> roleRefMemberList = roleRefMemberService.getByProductIds(productIds);
        List<ItemMemberVO> resultList = new ArrayList<>(roleRefMemberList.size());
        //获取对应项目角色集合
        List<ProjectRoleEntity> projectRoleList = projectRoleService.getByProductIds(productIds);

        List<ProjectMembersEntity> projectMembersList = this.getProductIdsMembers(productIds);
        for (String productId : productIds) {
            List<RoleRefMemberEntity> productRoleRefMemberList = roleRefMemberList.stream().
                    filter(ref -> productId.equals(ref.getProductId())).collect(Collectors.toList());
            //当有对应的关系时候
            if (CollectionUtils.isNotEmpty(productRoleRefMemberList)) {
                //一个角色下可能有多个用户
                Map<String, List<RoleRefMemberEntity>> roleRefMemberMap = productRoleRefMemberList.stream().
                        collect(Collectors.groupingBy(RoleRefMemberEntity::getRoleId));
                for (Map.Entry<String, List<RoleRefMemberEntity>> item : roleRefMemberMap.entrySet()) {
                    //角色id
                    String roleId = item.getKey();
                    ProjectRoleEntity roleEntity = projectRoleList.stream().
                            filter(role -> roleId.equals(role.getId()) &&
                                    !productManager.equals(role.getName()) &&
                                    !projectManager.equals(role.getName())).
                            findFirst().orElse(null);
                    //获取到对应的成员表id
                    List<String> memberTableIdList = item.getValue().stream().map(RoleRefMemberEntity::getMembersId).collect(Collectors.toList());
                    //成员id
                    for (String memberTableId : memberTableIdList) {
                        ProjectMembersEntity members = projectMembersList.stream().filter(m -> memberTableId.equals(m.getId())).findFirst().orElse(null);
                        if (roleEntity != null && members != null) {
                            ItemMemberVO memberVO = new ItemMemberVO();
                            memberVO.setProductId(productId);
                            memberVO.setRoleId(roleId);
                            memberVO.setRoleName(roleEntity.getName());
                            memberVO.setMemberId(members.getMemberId());
                            memberVO.setMemberName(members.getMemberName());
                            resultList.add(memberVO);
                        }

                    }

                }
            }
        }
        return resultList;
    }


    /**
     * 根据产品集合 获取对应数据
     *
     * @param productIds
     * @return java.util.List<com.erp.model.plm.entity.ProjectMembersEntity>
     * @author yl
     * @date 2023-02-23 17:24
     */
    private List<ProjectMembersEntity> getProductIdsMembers(List<String> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return new ArrayList<>(1);
        }
        LambdaQueryWrapper<ProjectMembersEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProjectMembersEntity::getProductId, productIds);
        return this.list(queryWrapper);
    }

    /**
     * 根据角色名称和模板id查询人员
     * @Author Luo_WG
     * @Date 2023/3/27 12:01
     * @param roles roles
     * @param templateId templateId
     * @return java.util.List<com.erp.model.plm.entity.ProjectMembersEntity>
     **/
    public List<MemberPagingShowDTO> listByRoleNames( List<String> roles, String templateId, String roleName) {
        return baseMapper.listByRoleNames(roles, templateId, roleName);
    }

    /**
     * 根据产品id查询角色人员
     * @Author Luo_WG
     * @Date 2023/3/27 18:54
     * @param productId productId
     * @return java.util.List<com.erp.model.plm.dto.MemberPagingShowDTO>
     **/
    @Override
    public List<MemberPagingShowDTO> listByMembers(String productId) {
        return baseMapper.listByMembers(productId);
    }
}
