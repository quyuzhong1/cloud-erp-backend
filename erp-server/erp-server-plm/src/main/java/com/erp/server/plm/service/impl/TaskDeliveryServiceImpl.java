package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.DocsPermissionEntity;
import com.erp.model.plm.entity.TaskDeliveryDocsEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TaskDocsMapper;
import com.erp.server.plm.service.DocsPermissionService;
import com.erp.server.plm.service.RoleRefMemberService;
import com.erp.server.plm.service.TaskDeliveryService;
import com.erp.server.plm.service.TaskDocsFinishService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    public void saveDeliveryDocs(String userId, String taskId, String productId, List<DocsDTO> deliveryDocsList) {
        if (CollectionUtils.isNotEmpty(deliveryDocsList)) {
            //这个id 可能是系统的
            List<String> docsId = deliveryDocsList.stream().map(DocsDTO::getId).collect(Collectors.toList());
            //根据任务id 获取到已存在的文档id
            List<TaskDeliveryDocsEntity> existDocsList = getExistDocs(taskId);
            List<String> existDocsIds = existDocsList.stream().map(TaskDeliveryDocsEntity::getId).collect(Collectors.toList());
            List<String> parameterIds = deliveryDocsList.stream().map(DocsDTO::getId).collect(Collectors.toList());
            //如果是一样 没有改变文档 返回
            if (existDocsIds.size() == parameterIds.size() && existDocsIds.containsAll(parameterIds) && parameterIds.containsAll(existDocsIds)) {
                return;
            }
            //先删除文档 不存在的数据
            removeTaskDocs(taskId,existDocsIds, docsId);

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
                entity.setIsSys(item.getIsSys());
                saveList.add(entity);
            }
            Boolean flag = this.saveBatch(saveList);
            if (flag) {
                //保存他的权限
                List<DocsPermissionEntity> docsPermissionList = new LinkedList<>();
                for (TaskDeliveryDocsEntity item : saveList) {
                    DocsPermissionEntity docsPermission = new DocsPermissionEntity();
                    docsPermission.setDeliveryDocsId(item.getId());
                    docsPermission.setQueryUserId(userId);
                    docsPermissionList.add(docsPermission);
                }
                docsPermissionService.saveBatch(docsPermissionList);
            }
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
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        String userId = "";
        if (loginUser != null) {
            userId = loginUser.getUid();
        }
        //根据当前登录人 查看它能查看的文档
        List<String> ids = docsPermissionService.getDocsIdsByUserId(userId);
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, ids);
        return new PagingVO(pageData);
    }

    @Override
    public void setPower(SetDocsPowerDTO dto) {
        //保存他的权限
        List<DocsPermissionEntity> docsPermissionList = new LinkedList<>();
        String roleId = dto.getRoleId();
        String docsId = dto.getId();
        if (StringUtils.isNotBlank(roleId)) {
            List<RoleRefMemberDTO> refMembers = roleRefMemberService.getByRoleIds(Arrays.asList(roleId));
            for (RoleRefMemberDTO item : refMembers) {
                DocsPermissionEntity docsPermission = new DocsPermissionEntity();
                docsPermission.setQueryUserId(item.getMembersId());
                docsPermission.setDeliveryDocsId(docsId);
                docsPermissionList.add(docsPermission);
            }
        } else {
            DocsPermissionEntity save = new DocsPermissionEntity();
            save.setDeliveryDocsId(docsId);
            save.setQueryUserId("");
            docsPermissionList.add(save);
        }

        docsPermissionService.saveBatch(docsPermissionList);

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
        return baseMapper.getByTaskId(taskId);
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
            removeTaskDocs(taskId,existDocsIds, parameterIds);
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
    public void saveTaskDeliveryDocs(String productId, String taskId, String sysTaskId) {
        //根据系统的任务id 获取到交付文档
        List<TaskDeliveryDocsEntity> list = getListByTaskId(sysTaskId);
        List<TaskDeliveryDocsEntity> saveList = new LinkedList<>();
        for (TaskDeliveryDocsEntity item : list) {
            TaskDeliveryDocsEntity entity = new TaskDeliveryDocsEntity();
            entity.setProductId(productId);
            entity.setDocsNameId(item.getDocsNameId());
            entity.setDocsName(item.getDocsName());
            entity.setTaskId(taskId);
            entity.setIsSys(item.getIsSys());
            saveList.add(entity);
        }
        this.saveBatch(saveList);

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
        LambdaQueryWrapper<TaskDeliveryDocsEntity> queryWrapper = new LambdaQueryWrapper<>();
        //表示有交集 的不能删除
        if (intersectionList.size() != 0) {
            if (CollectionUtils.isNotEmpty(existDocsIds)) {
                queryWrapper.eq(TaskDeliveryDocsEntity::getTaskId, taskId);
                queryWrapper.notIn(TaskDeliveryDocsEntity::getId, intersectionList);
                //去差集
                List<String> subtractList = (List<String>) CollectionUtils.subtract(existDocsIds,docsIds);
                taskDocsFinishService.removeByDocsIds(taskId,subtractList);
                this.remove(queryWrapper);
            }
        }else{
            if (CollectionUtils.isNotEmpty(existDocsIds)) {
                //表示没有交集 所有都要删除
                queryWrapper.eq(TaskDeliveryDocsEntity::getTaskId, taskId);
                queryWrapper.in(TaskDeliveryDocsEntity::getId,existDocsIds);
                this.remove(queryWrapper);
                taskDocsFinishService.removeByDocsIds(taskId,existDocsIds);
            }
        }
    }

    public static void main(String[] args) {
        List<String> list1 = new LinkedList<>();
        list1.add("1");
        list1.add("2");
        list1.add("3");


        List<String> list2 = new LinkedList<>();
        list2.add("4");
        list2.add("5");


        System.out.println(CollectionUtils.intersection(list2,list1));
    }
}
