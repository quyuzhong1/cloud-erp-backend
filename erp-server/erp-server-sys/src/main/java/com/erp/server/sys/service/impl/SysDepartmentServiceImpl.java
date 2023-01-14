package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.server.sys.mapper.SysDepartmentMapper;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysDepartmentUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartmentEntity> implements SysDepartmentService {

    @Autowired
    private SysDepartmentUserService sysDepartmentUserService;

    @Override
    public void removeByIdList(List<String> ids) {
        LambdaQueryWrapper<SysDepartmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysDepartmentEntity::getParentId, ids);
        int count = this.count(queryWrapper);
        //表示有父类的id
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_9013);
        }
        boolean flag = this.removeByIds(ids);
        //删除成功就要去移除对应的员工
        if (flag) {
            sysDepartmentUserService.removeByDepartmentIds(ids);
        }
    }

    /**
     * 获取部门树结构
     *
     * @return
     */
    @Override
    public List<DepartmentDTO> findDepartmentTree() {
        List<SysDepartmentEntity> allList = this.list();
        //获取所有部门人员
        List<SysDepartmentUserNumberDTO> userNumberList = sysDepartmentUserService.findUserNumber();
        List<DepartmentDTO> departList = BeanMapperUtils.copyList(DepartmentDTO.class, allList);
        List<SysDepartmentTreeDTO> flagList = baseMapper.findTree();
        List<DepartmentDTO> treeList = departList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .map(item -> {
                    item.setParentName("");
                    //根据用数据库查询的 树结构数据 获取到 该部门id 下有多少子的部门id
                    List<String> childrenDepartIds = getAllDepartIdsById(item.getId(), flagList);
                    item.setChildrenList(getChildren(item, departList, userNumberList, flagList));
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    item.setUserNumber(userNumber);
                    return item;
                }).collect(Collectors.toList());

        return treeList;
    }


    /**
     * 根据部门id 获取下面有多少的 子集部门
     *
     * @param departId
     * @param treeList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-08-01 14:24
     */
    private List<String> getAllDepartIdsById(String departId, List<SysDepartmentTreeDTO> treeList) {
        List<String> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(treeList)) {
            for (SysDepartmentTreeDTO vo : treeList) {
                //如果路径包含了 就说有
                if (vo.getPath().contains(departId)) {
                    resultList.add(vo.getId());
                }

            }
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }


    /**
     * 批量保存部门树结构
     *
     * @param sysDepartmentTree
     */
    @Override
    public void saveBatchDepartment(List<SysDepartmentDTO> sysDepartmentTree) {
        List<SysDepartmentEntity> batchList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(sysDepartmentTree)) {
            for (SysDepartmentDTO dto : sysDepartmentTree) {
                getSaveTree("0", batchList, dto);
            }
            this.saveOrUpdateBatch(batchList);
        }

    }


    /**
     * 根据部门id 获取到父级id 是部门id 的所有集合
     *
     * @param departmentId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-07-18 11:44
     */
    @Override
    public List<String> getDepartmentIds(String departmentId) {
        List<SysDepartmentTreeDTO> treeList = baseMapper.findTree();
        List<String> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(treeList)) {
            for (SysDepartmentTreeDTO vo : treeList) {
                //如果路径包含了 就说有
                if (vo.getPath().contains(departmentId)) {
                    resultList.add(vo.getId());
                }
            }
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public SysDepartmentDTO getDepartmentById(String deptId) {
        SysDepartmentEntity sysDepartmentEntity = this.getById(deptId);
        SysDepartmentDTO dto = new SysDepartmentDTO();
        if (ObjectUtils.isNotEmpty(sysDepartmentEntity)) {
            BeanMapperUtils.copy(sysDepartmentEntity, dto);
        }
        return dto;
    }

    @Override
    public List<SysDepartmentEntity> listDept() {
        List<SysDepartmentEntity> list = lambdaQuery()
                .in(SysDepartmentEntity::getType, new ArrayList<>(Arrays.asList(1, 2)))
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list;
    }


    /**
     * 根据部门名 获取部门id 以及下面的部门id
     *
     * @param deptName
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-12-28 15:26
     */
    @Override
    public List<String> getDeptIds(String deptName) {
        List<String> resultList = new ArrayList<>();
        LambdaQueryWrapper<SysDepartmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDepartmentEntity::getType, 1);
        queryWrapper.eq(SysDepartmentEntity::getName, deptName);
        queryWrapper.last("LIMIT 1");
        SysDepartmentEntity dept = this.getOne(queryWrapper);
        if (dept != null) {
            List<SysDepartmentTreeDTO> flagList = baseMapper.findTree();
            String deptId = dept.getId();
            //根据用数据库查询的 树结构数据 获取到 该部门id 下有多少子的部门id
            List<String> childrenDepartIds = getAllDepartIdsById(deptId, flagList);
            resultList.add(deptId);
            resultList.addAll(childrenDepartIds);
        }
        return resultList;
    }

    @Override
    public List<SysDepartmentDTO> getDeptList() {

        return baseMapper.getDeptList();
    }


    /**
     * 部门人员
     *
     * @param
     * @return java.util.List<com.erp.model.sys.dto.DeptUserDTO>
     * @author yl
     * @date 2023-01-06 17:26
     */
    @Override
    public List<DeptUserDTO> deptUserTree() {
        List<SysDepartmentEntity> allList = this.list();
        //获取所有部门人员
        List<SysDepartmentUserNumberDTO> userNumberList = sysDepartmentUserService.findUserNumber();
        List<DeptUserDTO> departList = new ArrayList<>(allList.size());
        for (SysDepartmentEntity item : allList) {
            DeptUserDTO dto = new DeptUserDTO();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setParentId(item.getParentId());
            dto.setType(item.getType());
            departList.add(dto);
        }
        List<SysDepartmentTreeDTO> flagList = baseMapper.findTree();
        List<DeptUserDTO> treeList = departList.stream().
                filter(item -> "0".equals(item.getParentId()))
                .map(item -> {
                    item.setParentName("");
                    //根据用数据库查询的 树结构数据 获取到 该部门id 下有多少子的部门id
                    List<String> childrenDepartIds = getAllDepartIdsById(item.getId(), flagList);
                    item.setChildrenList(getDeptUserChildren(item, departList, userNumberList, flagList));
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    List<SysDepartmentUserNumberDTO> userList = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.toList());
                    item.setUserNumber(userNumber);
                    item.setUserList(userList);
                    return item;
                }).collect(Collectors.toList());

        return treeList;
    }

    @Override
    public SysDepartmentEntity getParentDepartmentById(String departmentId) {
        SysDepartmentEntity sysDepartmentEntity = this.getById(departmentId);
        if (ObjectUtils.isEmpty(sysDepartmentEntity)) {
                return sysDepartmentEntity;
        }
        return this.getById(sysDepartmentEntity.getParentId());
    }

    /**
     * 递归获取批量保存的是数据
     *
     * @param parentId  父级id
     * @param batchList xuyao 保存的数据
     * @param item      参数
     * @return void
     * @author yl
     * @date 2022-07-11 17:56
     */
    private void getSaveTree(String parentId, List<SysDepartmentEntity> batchList, SysDepartmentDTO item) {
        SysDepartmentEntity entity = new SysDepartmentEntity();
        BeanMapperUtils.copy(item, entity);
        entity.setParentId(parentId);
        String id = item.getId();
        if (StringUtils.isBlank(id)) {
            id = IdWorker.getIdStr();
        }
        entity.setId(id);
        batchList.add(entity);
        List<SysDepartmentDTO> subList = item.getChildrenList();
        if (CollectionUtils.isNotEmpty(subList)) {
            for (SysDepartmentDTO item1 : subList) {
                this.getSaveTree(id, batchList, item1);
            }
        }
    }

    private List<DepartmentDTO> getChildren(DepartmentDTO item, List<DepartmentDTO> departList, List<SysDepartmentUserNumberDTO> userNumberList, List<SysDepartmentTreeDTO> flagList) {
        List<DepartmentDTO> collect = departList.stream().filter(dept -> item.getId().equals(dept.getParentId()))
                .map(d -> {
                    List<String> childrenDepartIds = getAllDepartIdsById(d.getId(), flagList);
                    d.setParentName(item.getName());
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    d.setUserNumber(userNumber);
                    d.setChildrenList(getChildren(d, departList, userNumberList, flagList));
                    return d;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;
    }


    private List<DeptUserDTO> getDeptUserChildren(DeptUserDTO item, List<DeptUserDTO> departList, List<SysDepartmentUserNumberDTO> userNumberList, List<SysDepartmentTreeDTO> flagList) {
        List<DeptUserDTO> collect = departList.stream().filter(dept -> item.getId().equals(dept.getParentId()))
                .map(d -> {
                    List<String> childrenDepartIds = getAllDepartIdsById(d.getId(), flagList);
                    d.setParentName(item.getName());
                    int userNumber = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.groupingBy(SysDepartmentUserNumberDTO::getUserId)).size();
                    d.setUserNumber(userNumber);
                    List<SysDepartmentUserNumberDTO> userList = userNumberList.stream().filter(u -> childrenDepartIds.contains(u.getDepartmentId())).collect(Collectors.toList());
                    d.setUserList(userList);
                    d.setChildrenList(getDeptUserChildren(d, departList, userNumberList, flagList));
                    return d;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;
    }


}