package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.DocsPermissionEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.model.plm.entity.TaskDocsNameEntity;
import com.erp.server.plm.constant.AdminUserConstant;
import com.erp.server.plm.constant.IsConstant;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.TaskDocsMapper;
import com.erp.server.plm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname TaskDocsServiceImpl
 * @Description TODO
 * @Date 2022-09-22 9:43
 * @Created by yl
 */
@Service
public class TaskDeliveryServiceImpl extends ServiceImpl<TaskDocsMapper, TaskDeliveryDocsEntity> implements TaskDeliveryService {

    @Autowired
    private DocsPermissionService docsPermissionService;

    @Autowired
    private RoleRefMemberService roleRefMemberService;

    @Autowired
    private TaskDocsFinishService taskDocsFinishService;


    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private ProjectMembersService projectMembersService;


    /**
     * 获取任务的需要交付的文档数
     *
     * @param taskId
     * @return void
     * @author yl
     * @date 2022-09-22 9:54
     */
    @Override
    public List<CountDTO> getTaskDocsCount(List<String> taskId) {
        return baseMapper.getTaskDocsCount(taskId);
    }

    @Override
    public void saveDeliveryDocs(String taskId, String productId, List<DocsDTO> deliveryDocsList) {

        if (CollectionUtils.isNotEmpty(deliveryDocsList)) {
            //这个id 可能是系统的
            List<String> docsId = deliveryDocsList.stream().map(DocsDTO::getId).collect(Collectors.toList());
            //根据任务id 获取到已存在的文档id
            List<TaskDeliveryDocsEntity> existDocsList = getExistDocs(taskId);
            //存在的文档id
            List<String> existDocsIds = existDocsList.stream().map(TaskDeliveryDocsEntity::getId).collect(Collectors.toList());
            //传过来的文档Id集合
            List<String> parameterIds = deliveryDocsList.stream().map(DocsDTO::getId).collect(Collectors.toList());
            //如果是一样 没有改变文档 返回
            if (existDocsIds.size() == parameterIds.size() && existDocsIds.containsAll(parameterIds) && parameterIds.containsAll(existDocsIds)) {
                return;
            }
            //先删除文档 不存在的数据
            removeTaskDocs(taskId, existDocsIds, docsId);
            //需要过滤一下的
            deliveryDocsList = deliveryDocsList.stream().filter(c -> !existDocsIds.contains(c.getId())).collect(Collectors.toList());
            //保存交付文档
            List<TaskDeliveryDocsEntity> saveList = new LinkedList<>();
            for (DocsDTO item : deliveryDocsList) {
                TaskDeliveryDocsEntity entity = new TaskDeliveryDocsEntity();
                entity.setProductId(productId);
                entity.setDocsName(item.getName());
                entity.setTaskId(taskId);
                entity.setDocsNameId(item.getId());
                saveList.add(entity);
            }
            Boolean flag = this.saveBatch(saveList);
            if (flag) {
                //先删除 不在的数据
                docsPermissionService.removePermission(taskId);
                List<DocsPermissionEntity> docsPermissionList = new LinkedList<>();
                for (TaskDeliveryDocsEntity item : saveList) {
                    DocsPermissionEntity docsPermission = new DocsPermissionEntity();
                    docsPermission.setDeliveryDocsId(item.getId());
                    docsPermission.setQueryRoleId("");
                    docsPermission.setProductId(productId);
                    docsPermission.setTaskId(taskId);
                    docsPermissionList.add(docsPermission);
                }
                docsPermissionService.saveBatch(docsPermissionList);
            }
        } else {
            //当传来空 删除所有的
            removeByTaskId(taskId);
            taskDocsFinishService.removeByTaskId(taskId);
        }
    }


    /**
     * 根据任务id 获取已存在的文档
     *
     * @param taskId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-24 17:43
     */

    private List<TaskDeliveryDocsEntity> getExistDocs(String taskId) {
        LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDeliveryDocsEntity::getTaskId, taskId);
        return this.list(queryWrapper);
    }


    /**
     * 获取输出物文档
     *
     * @param dto
     * @return com.erp.common.vo.PagingVO
     * @author yl
     * @date 2022-09-23 10:59
     */
    @Override
    public PagingVO<List<DeliveryDocsDTO>> paging(PagingDTO<BaseSearchDTO> dto) {

        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        List<String> findDeliveryDocsIds = setTaskDeliveryAuth(params);
        IPage pageData = new Page();
        if (CollectionUtils.isNotEmpty(findDeliveryDocsIds)) {
            pageData = baseMapper.paging(query, params, findDeliveryDocsIds);
        }
        List<DeliveryDocsDTO> list = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(list)) {
            Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(params.getFlagId());
            for (DeliveryDocsDTO item : list) {
                ProjectTaskEntity entity = taskList.stream().filter(d -> d.getId().equals(item.getTaskId())).findFirst().orElse(null);
                //当没审核通过
                if (Objects.isNull(entity) || !entity.getStatus().equals(approvalPass)) {
                    item.setFileUrl(item.getOldFileUrl());
                    item.setFileName(item.getOldFileName());
                    item.setUploadType(item.getOldUploadType());
                }
            }
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<DeliveryDocsDTO> listProductDocs(String productId) {
        BaseSearchDTO params = new BaseSearchDTO();
        params.setFlagId(productId);
        List<String> findDeliveryDocsIds = setTaskDeliveryAuth(params);
        if (CollectionUtils.isEmpty(findDeliveryDocsIds)) {
            return new ArrayList<>();
        }
        List<DeliveryDocsDTO> list = baseMapper.list(params, findDeliveryDocsIds);
        if (CollectionUtils.isNotEmpty(list)) {
            Integer approvalPass = TaskStateEnum.APPROVAL_PASS.getCode();
            List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(params.getFlagId());
            for (DeliveryDocsDTO item : list) {
                ProjectTaskEntity entity = taskList.stream().filter(d -> d.getId().equals(item.getTaskId())).findFirst().orElse(null);
                //当没审核通过
                if (Objects.isNull(entity) || !entity.getStatus().equals(approvalPass)) {
                    item.setFileUrl(item.getOldFileUrl());
                    item.setFileName(item.getOldFileName());
                    item.setUploadType(item.getOldUploadType());
                }
            }
        }
        return list;
    }

    /**
     * 查看用户 是不是 任务负责人 如果是 就加文档id
     *
     * @param productId
     * @param userId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-11-01 18:42
     */
    private List<String> getTaskChargeDeliveryDocsIds(String productId, String userId) {
        List<String> resultList = new ArrayList<>();
        List<TaskDeliveryDocsEntity> deliveryDocsList = this.getByProductId(productId);
        //这是任务的
        List<ProjectTaskEntity> taskList = projectTaskService.getByProductId(productId);
        for (ProjectTaskEntity item : taskList) {
            //任务负责人
            String chargeId = item.getChargeId();
            //如果不为空 并且 包含这个人
            if (StringUtils.isNotBlank(chargeId) && chargeId.contains(userId)) {
                List<String> deliveryDocsIds = deliveryDocsList.stream().filter(d -> d.getTaskId().equals(item.getId())).map(TaskDeliveryDocsEntity::getId).collect(Collectors.toList());
                resultList.addAll(deliveryDocsIds);
            }
        }
        return resultList;
    }

    @Override
    public void setPower(SetDocsPowerDTO dto) {
        //保存他的权限
        TaskDeliveryDocsEntity deliveryDocsEntity = this.getById(dto.getId());
        List<DocsPermissionEntity> docsPermissionList = new LinkedList<>();
        List<String> roleIds = dto.getRoleIdList();
        String docsId = dto.getId();
        if (deliveryDocsEntity != null) {
            //先删除所有的
            docsPermissionService.removeByDeliveryDocsId(Arrays.asList(docsId));
            if (CollectionUtils.isNotEmpty(roleIds)) {
                for (String roleId : roleIds) {
                    DocsPermissionEntity docsPermission = new DocsPermissionEntity();
                    docsPermission.setQueryRoleId(roleId);
                    docsPermission.setDeliveryDocsId(deliveryDocsEntity.getId());
                    docsPermission.setProductId(deliveryDocsEntity.getProductId());
                    docsPermission.setTaskId(deliveryDocsEntity.getTaskId());
                    docsPermissionList.add(docsPermission);
                }
                docsPermissionService.saveBatch(docsPermissionList);
            }
        }


    }


    /**
     * 根据任务id 获取到对应的要上交的文档
     *
     * @param taskId
     * @return java.util.List<com.erp.model.plm.dto.DeliveryDocsDTO>
     * @author yl
     * @date 2022-09-23 15:46
     */
    @Override
    public List<DeliveryDocsDTO> getByTaskId(String taskId) {
        List<DeliveryDocsDTO> list = baseMapper.getByTaskId(taskId);
        ProjectTaskEntity task = projectTaskService.getById(taskId);
        Integer finish = TaskStateEnum.FINISH.getCode();
        for (DeliveryDocsDTO item : list) {
            if (task != null && finish.equals(task.getStatus())) {
                item.setOldFileName(item.getFileName());
                item.setOldFileUrl(item.getFileUrl());
                item.setOldUploadType(item.getUploadType());
            }
        }
        return list;
    }


    /**
     * 保存系统任务交付的文档
     *
     * @param taskId
     * @param docsList
     * @return void
     * @author yl
     * @date 2022-09-28 15:07
     */
    @Override
    public void saveSysDeliveryDocs(String taskId, List<DocsDTO> docsList) {
        //保存交付文档
        if (CollectionUtils.isNotEmpty(docsList)) {
            //根据任务id 获取到已存在的文档id
            List<TaskDeliveryDocsEntity> existDocsList = getExistDocs(taskId);
            List<String> existDocsIds = existDocsList.stream().map(TaskDeliveryDocsEntity::getId).collect(Collectors.toList());
            List<String> parameterIds = docsList.stream().map(DocsDTO::getId).collect(Collectors.toList());
            //如果是一样 没有改变文档 返回
            if (existDocsIds.size() == parameterIds.size() && existDocsIds.contains(parameterIds) && parameterIds.contains(existDocsIds)) {
                return;
            }
            //先删除文档
            removeTaskDocs(taskId, existDocsIds, parameterIds);
            //需要过滤一下的
            docsList = docsList.stream().filter(c -> !existDocsIds.contains(c.getId())).collect(Collectors.toList());

            List<TaskDeliveryDocsEntity> saveList = new LinkedList<>();
            for (DocsDTO item : docsList) {
                TaskDeliveryDocsEntity entity = new TaskDeliveryDocsEntity();
                entity.setDocsName(item.getName());
                entity.setTaskId(taskId);
                entity.setProductId("");
                entity.setDocsNameId(item.getId());
                entity.setIsSys(IsConstant.YES);
                saveList.add(entity);
            }
            this.saveBatch(saveList);
        } else {//当传来空 删除所有的
            removeByTaskId(taskId);
            taskDocsFinishService.removeByTaskId(taskId);
        }


    }


    /**
     * 添加产品的时候 复制产品任务过来
     *
     * @param productId
     * @param taskId
     * @param sysTaskId
     * @return void
     * @author yl
     * @date 2022-09-28 15:32
     */
    @Override
    public void saveTaskDeliveryDocs(String productId, String taskId, String sysTaskId, List<TaskDocsNameEntity> docsNameList) {
        //根据任务id 获取到交付文档
        List<TaskDeliveryDocsEntity> list = getListByTaskId(sysTaskId);
        List<TaskDeliveryDocsEntity> saveList = new LinkedList<>();
        for (TaskDeliveryDocsEntity item : list) {
            TaskDeliveryDocsEntity entity = new TaskDeliveryDocsEntity();
            entity.setProductId(productId);
            TaskDocsNameEntity docsName = docsNameList.stream().filter(d -> d.getName().
                    equals(item.getDocsName()) && productId.equals(d.getProductId())).
                    findFirst().orElse(null);
            if (docsName != null) {
                entity.setDocsNameId(docsName.getId());
                entity.setDocsName(docsName.getName());
            } else {
                entity.setDocsNameId(item.getDocsNameId());
                entity.setDocsName(item.getDocsName());
            }
            entity.setTaskId(taskId);
            entity.setIsSys(item.getIsSys());
            saveList.add(entity);
        }
        this.saveBatch(saveList);


        //保存他的权限
        List<DocsPermissionEntity> docsPermissionList = new LinkedList<>();
        for (TaskDeliveryDocsEntity item : saveList) {
            DocsPermissionEntity docsPermission = new DocsPermissionEntity();
            docsPermission.setDeliveryDocsId(item.getId());
            docsPermission.setQueryRoleId("");
            docsPermission.setProductId(productId);
            docsPermission.setTaskId(taskId);
            docsPermissionList.add(docsPermission);
        }

        docsPermissionService.saveBatch(docsPermissionList);
    }


    @Override
    public void removeByTaskId(String taskId) {
        LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDeliveryDocsEntity::getTaskId, taskId);
        this.remove(queryWrapper);

    }


    /**
     * 根据产品id 分组获取到对应的需要交付的文档数
     *
     * @param
     * @return java.util.List<com.erp.model.plm.dto.TaskDocsCountDTO>
     * @author yl
     * @date 2022-10-08 19:51
     */
    @Override
    public List<CountDTO> getTaskDocsCountByProductId() {
        return baseMapper.getTaskDocsCountByProductId();
    }


    @Override
    public List<DocsDTO> getDocsByTaskId(String taskId) {

        return baseMapper.getDocsByTaskId(taskId);
    }

    @Override
    public List<DocsDTO> getSysTaskFinishDocs(String taskId) {
        return baseMapper.getDocsByTaskId(taskId);
    }

    /**
     * 获取交付的文档
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.TaskDeliveryDocsEntity>
     * @author yl
     * @date 2022-10-27 16:16
     */
    @Override
    public List<TaskDeliveryDocsEntity> getByProductId(String productId) {
        LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDeliveryDocsEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    /**
     * 获取到任务id 交付名
     *
     * @param taskIds
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-28 10:26
     */
    @Override
    public List<String> getDocsNameByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isNotEmpty(taskIds)) {
            LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(TaskDeliveryDocsEntity::getDocsName);
            queryWrapper.in(TaskDeliveryDocsEntity::getTaskId, taskIds);
            return listObjs(queryWrapper, Object::toString);
        }
        return new ArrayList<>();
    }

    @Override
    public List<DeliveryDocsGroupDTO> listGroupByTaskId(String id) {
        List<DeliveryDocsGroupDTO> resultList = new ArrayList<>();
        List<DeliveryDocsDTO> list = this.getByTaskId(id);
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }
        Map<String, List<DeliveryDocsDTO>> map = list.stream().collect(Collectors.groupingBy(DeliveryDocsDTO::getId));
        for (Map.Entry<String, List<DeliveryDocsDTO>> entry : map.entrySet()) {
            DeliveryDocsGroupDTO deliveryDocsGroupDTO = new DeliveryDocsGroupDTO();
            List<DeliveryDocsDTO> value = entry.getValue();
            deliveryDocsGroupDTO.setId(entry.getKey());
            deliveryDocsGroupDTO.setDeliveryDocsName(value.get(0).getDeliveryDocsName());
            deliveryDocsGroupDTO.setTaskId(id);
            deliveryDocsGroupDTO.setTaskName(value.get(0).getTaskName());
            List<DeliveryDocsDTO> addList = value.stream().filter(obj -> StringUtils.isNotBlank(obj.getFinishDocsId())).collect(Collectors.toList());
            deliveryDocsGroupDTO.setDocsList(addList);
            resultList.add(deliveryDocsGroupDTO);
        }
        return resultList;
    }

    /**
     * 根据任务id 获取对应数据
     *
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TaskDeliveryDocsEntity>
     * @author yl
     * @date 2023-03-08 10:48
     */
    @Override
    public List<TaskDeliveryDocsEntity> geByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TaskDeliveryDocsEntity::getTaskId, taskIds);
        return this.list(queryWrapper);
    }


    /**
     * 根据任务id 获取对应要交付的文档
     *
     * @param
     * @return java.util.List<com.erp.model.plm.entity.TaskDeliveryDocsEntity>
     * @author yl
     * @date 2022-09-28 15:42
     */
    public List<TaskDeliveryDocsEntity> getListByTaskId(String taskId) {
        LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDeliveryDocsEntity::getTaskId, taskId);
        return this.list(queryWrapper);

    }


    /**
     * 根据任务id 删除 文档
     *
     * @param existDocsIds
     * @return void
     * @author yl
     * @date 2022-09-28 15:23
     */
    public void removeTaskDocs(String taskId, List<String> existDocsIds, List<String> docsIds) {
        List<String> intersectionList = (List<String>) CollectionUtils.intersection(existDocsIds, docsIds);
        //表示有交集 的不能删除
        if (intersectionList.size() != 0) {
            if (CollectionUtils.isNotEmpty(existDocsIds)) {
                //删除 存在的id 不包含交集的
                List<String> deleteIdList = existDocsIds.stream().filter(e -> !intersectionList.contains(e)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(deleteIdList)) {
                    LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(TaskDeliveryDocsEntity::getTaskId, taskId);
                    queryWrapper.in(TaskDeliveryDocsEntity::getId, deleteIdList);
                    taskDocsFinishService.removeByDocsIds(taskId, deleteIdList);
                    this.remove(queryWrapper);
                }
            }
        } else {
            LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
            if (CollectionUtils.isNotEmpty(existDocsIds)) {
                //表示没有交集 所有都要删除
                queryWrapper.eq(TaskDeliveryDocsEntity::getTaskId, taskId);
                queryWrapper.in(TaskDeliveryDocsEntity::getId, existDocsIds);
                this.remove(queryWrapper);
                taskDocsFinishService.removeByDocsIds(taskId, existDocsIds);
            }
        }
    }

    /**
     * 设置查看权限
     */
    private List<String> setTaskDeliveryAuth(BaseSearchDTO params) {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        String userAccount = "";
        String userId = "";
        if (loginUser != null) {
            userAccount = loginUser.getUserAccount();
            userId = loginUser.getUid();
        }

        String productId = params.getFlagId();
        List<String> findDeliveryDocsIds = new ArrayList<>();
        List<TaskDeliveryDocsEntity> deliveryDocsList = this.getByProductId(productId);
        List<String> allDeliveryDocsIds = deliveryDocsList.stream().map(TaskDeliveryDocsEntity::getId).collect(Collectors.toList());

        //如果是管理员
        if (userAccount.equals(AdminUserConstant.ACCOUNT)) {
            findDeliveryDocsIds.addAll(allDeliveryDocsIds);
        } else {
            /**
             * 根据产品id 获取当前登录人 是否是 任务负责人
             * 如果是就要添加对应的 文档id
             */
            List<String> taskChargeDeliveryDocsIds = getTaskChargeDeliveryDocsIds(params.getFlagId(), userId);
            if (CollectionUtils.isNotEmpty(taskChargeDeliveryDocsIds)) {
                findDeliveryDocsIds.addAll(taskChargeDeliveryDocsIds);
            }

            //查询当前用户的角色
            List<String> userRoleIds = roleRefMemberService.getUserRole(userId, params.getFlagId());

            //获取所有的设置文档的权限的文档id
            List<DocsPermissionEntity> allPermissionDeliveryDocsList = docsPermissionService.getAllDeliveryDocsIds(productId);
            for (TaskDeliveryDocsEntity item : deliveryDocsList) {
                String deliveryDocsId = item.getId();
                DocsPermissionEntity permission = allPermissionDeliveryDocsList.stream().filter(p -> p.getDeliveryDocsId().equals(deliveryDocsId)).
                        findFirst().orElse(null);
                //表示有权限
                if (permission != null) {
                    if (userRoleIds.contains(permission.getQueryRoleId())) {
                        findDeliveryDocsIds.add(deliveryDocsId);
                    }
                    if(StringUtils.isBlank(permission.getQueryRoleId())){
                        findDeliveryDocsIds.add(deliveryDocsId);
                    }
                } else {
                    //没有权限
                    findDeliveryDocsIds.add(deliveryDocsId);
                }

            }

        }

        findDeliveryDocsIds = findDeliveryDocsIds.stream().distinct().collect(Collectors.toList());

        return findDeliveryDocsIds;
    }

}
