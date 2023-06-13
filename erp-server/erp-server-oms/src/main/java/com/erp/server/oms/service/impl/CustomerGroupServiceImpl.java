package com.erp.server.oms.service.impl;

import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerGroupDTO;
import com.erp.model.oms.entity.CustomerGroupEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerGroupService;
import com.erp.server.oms.mapper.CustomerGroupMapper;
import com.erp.server.oms.service.CustomerGroupService;
import com.erp.server.oms.service.CustomerInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
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

    @Resource
    private SyncKingdeeCustomerGroupService syncKingdeeCustomerGroupService;

    /**
     * 保存或者修改分组
     *
     * @param groupList
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-11 17:48
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        for (CustomerGroupEntity groupEntity : batchGroupList) {
            CustomerGroupEntity entity = dbList.stream().filter(req -> req.getId().equals(groupEntity.getId())).findFirst().orElse(new CustomerGroupEntity());
            groupEntity.setSyncKingdeeId(entity.getSyncKingdeeId());
        }
        boolean flag = this.saveOrUpdateBatch(batchGroupList);
        //审核通过发送金蝶
        batchGroupList.forEach(obj -> syncKingdeeCustomerGroupService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode()));

        return flag;

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
        return this.lambdaQuery().eq(CustomerGroupEntity::getId, groupId).list();
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
        int size = nameList.size();
        int distinctSize = nameList.stream().distinct().collect(Collectors.toList()).size();
        if(size!=distinctSize){
            throw new ServiceException(ApiError.ERROR_92001);
        }

    }

    @Override
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId, String syncOperate) {
        return this.lambdaUpdate()
                .eq(CustomerGroupEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), CustomerGroupEntity::getSyncKingdeeStatus, syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus), CustomerGroupEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId), CustomerGroupEntity::getSyncKingdeeId, syncKingdeeId)
                .set(StringUtils.isNotBlank(syncOperate), CustomerGroupEntity::getSyncOperate, syncOperate)
                .update();
    }

}
