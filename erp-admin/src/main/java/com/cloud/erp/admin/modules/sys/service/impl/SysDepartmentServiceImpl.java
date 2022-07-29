package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.dto.SysDepartmentDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysDepartmentMapper;
import com.cloud.erp.admin.modules.sys.service.SysDepartmentService;
import com.cloud.erp.admin.modules.sys.service.SysDepartmentUserService;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentUserNumber;
import com.cloud.erp.admin.modules.sys.vo.SysDepartmentVO;
import com.commm.core.utils.BeanMapperUtils;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
    public List<SysDepartmentVO> findDepartmentTree() {
        List<SysDepartmentEntity> allList = this.list();
        //获取所有部门人员
       List<SysDepartmentUserNumber> userNumberList= sysDepartmentUserService.findUserNumber();
        List<SysDepartmentVO> departList = BeanMapperUtils.copyList(SysDepartmentVO.class, allList);
        List<SysDepartmentVO> treeList = departList.stream().
                filter(item ->"0".equals(item.getParentId()))
                .map(item -> {
                    item.setParentName("");
                    item.setChildrenList(getChildren(item, departList,userNumberList));
                    Integer userNumber=0;
                    SysDepartmentUserNumber vo= userNumberList.stream().filter(u->u.getDepartmentId().equals(item.getId())).findFirst().orElse(null);
                    if(!Objects.isNull(vo)){
                        userNumber=vo.getUserNumber();
                    }
                    item.setUserNumber(userNumber);
                    return item;
                }).collect(Collectors.toList());

        return treeList;
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
        LambdaQueryWrapper<SysDepartmentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysDepartmentEntity::getId);
        queryWrapper.eq(SysDepartmentEntity::getParentId,departmentId);
        List<Object> list=baseMapper.selectObjs(queryWrapper);
        int size=list.size()+1;
        List<String> resultList=new ArrayList<>(size);
        resultList=BeanMapperUtils.copyList(String.class,list);
        resultList.add(departmentId);
        return  resultList;
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

    private List<SysDepartmentVO> getChildren(SysDepartmentVO item, List<SysDepartmentVO> departList,List<SysDepartmentUserNumber> userNumberList) {
        List<SysDepartmentVO> collect = departList.stream().filter(dept -> item.getId().equals(dept.getParentId()))
                .map(d -> {
                    Integer userNumber=0;
                    SysDepartmentUserNumber vo= userNumberList.stream().filter(u->u.getDepartmentId().equals(item.getId())).findFirst().orElse(null);
                    if(!Objects.isNull(vo)){
                        userNumber=vo.getUserNumber();
                    }
                    d.setParentName(item.getName());
                    d.setUserNumber(userNumber);
                    d.setChildrenList(getChildren(d, departList,userNumberList));

                    return d;
                }).collect(Collectors.toList());
        return  CollectionUtils.isEmpty(collect)?null:collect;
    }


}