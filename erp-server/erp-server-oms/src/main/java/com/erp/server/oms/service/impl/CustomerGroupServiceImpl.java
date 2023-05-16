package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerGroupDTO;
import com.erp.model.oms.entity.CustomerGroupEntity;
import com.erp.server.oms.mapper.CustomerGroupMapper;
import com.erp.server.oms.service.CustomerGroupService;
import com.erp.server.oms.service.CustomerInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户分组表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
public class CustomerGroupServiceImpl extends SuperServiceImpl<CustomerGroupMapper, CustomerGroupEntity> implements CustomerGroupService {


    @Resource
    private CustomerInfoService customerInfoService;

    /**
     * 保存或者修改分组
     *
     * @param groupList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-11 17:48
     */
    @Override
    public Boolean saveOrUpdateBatchGroup(ValidList<CustomerGroupDTO.AddOrUpdateDTO> groupList) {
        if (CollectionUtils.isEmpty(groupList)) {
            throw new ServiceException(ApiError.ERROR_92000);
        }
        List<CustomerGroupEntity> dbList = this.list();
        //检查名称
        checkName(groupList, dbList);
        //获取到删除的 等级id
        List<String> deleteIdList = getDeleteIds(groupList, dbList);
        //获取到所有 客户 用过的分组
        List<String> groupIdList = customerInfoService.listGroup();
        long count = groupIdList.stream().filter(g -> deleteIdList.contains(g)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92002);
        }
        List<CustomerGroupEntity> batchGroupList = BeanMapper.copyList(groupList, CustomerGroupEntity.class);
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        return this.saveOrUpdateBatch(batchGroupList);

    }


    /**
     * 获取客户分组列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerGroupDTO.ListDTO>
     * @author yl
     * @date 2023-05-11 18:21
     */
    @Override
    public List<CustomerGroupDTO.ListDTO> listGroup() {
        List<CustomerGroupEntity> dbList = this.list();
        //获取到所有 客户 用过的分组
        List<String> groupIdList = customerInfoService.listGroup();
        List<CustomerGroupDTO.ListDTO> resultList = BeanMapper.copyList(dbList, CustomerGroupDTO.ListDTO.class);
        for (CustomerGroupDTO.ListDTO item : resultList) {
            if (groupIdList.contains(item.getId())) {
                item.setDisabled(true);
            } else {
                item.setDisabled(false);
            }
        }
        return resultList;
    }


    /**
     * 获取客户分组信息
     *
     * @param groupId
     * @return java.util.List<com.erp.model.oms.entity.CustomerGroupEntity>
     * @author yl
     * @date 2023-05-12 15:25
     */
    @Override
    public List<CustomerGroupEntity> listById(String groupId) {
        if (StringUtils.isBlank(groupId)) {
            return this.list();
        }
        return this.lambdaQuery().eq(CustomerGroupEntity::getId,groupId).list();
    }


    /**
     * 获取到要删除的数据
     *
     * @param groupList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-11 18:04
     */
    private List<String> getDeleteIds(ValidList<CustomerGroupDTO.AddOrUpdateDTO> groupList, List<CustomerGroupEntity> dbList) {
        List<String> ids = groupList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CustomerGroupDTO.AddOrUpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(CustomerGroupEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    /**
     * 检查分组名称是否重复
     *
     * @param groupList
     * @param dbList
     * @return void
     * @author yl
     * @date 2023-05-11 17:54
     */
    private void checkName(ValidList<CustomerGroupDTO.AddOrUpdateDTO> groupList, List<CustomerGroupEntity> dbList) {
        //这个是参数传来的名称
        List<String> nameList = groupList.stream().map(CustomerGroupDTO.AddOrUpdateDTO::getName).
                collect(Collectors.toList());
        //这个是数据库包含的
        List<CustomerGroupEntity> containsNameList = dbList.stream().filter(d -> nameList.contains(d.getName())).collect(Collectors.toList());

        int count = 0;
        for (CustomerGroupEntity item : containsNameList) {
            String name = item.getName();
            String id = groupList.stream().filter(g -> g.getName().equals(name)).findFirst().flatMap(obj ->
                    Optional.ofNullable(obj.getId())).orElse("");
            if (!item.getId().equals(id)) {
                count++;
            }
        }
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92001);
        }

    }
}
