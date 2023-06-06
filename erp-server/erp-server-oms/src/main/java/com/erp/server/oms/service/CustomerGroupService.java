package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.erp.model.oms.dto.CustomerGroupDTO;
import com.erp.model.oms.entity.CustomerGroupEntity;

import java.util.List;

/**
 * <p>
 * 客户分组表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerGroupService extends SuperService<CustomerGroupEntity> {

    /**
     * 保存或者修改分组
     * @author yl
     * @date 2023-05-11 17:48
     * @param groupList
     * @return java.lang.Boolean
     */
    Boolean saveOrUpdateBatchGroup(ValidList<CustomerGroupDTO.AddOrUpdateDTO> groupList);

    
    /**
     * 获取客户分组列表
     * @author yl
     * @date 2023-05-11 18:21
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerGroupDTO.ListDTO>
     */
    List<CustomerGroupDTO.ListDTO> listGroup();

    
    /**
     * 获取客户分组信息
     * @author yl
     * @date 2023-05-12 15:25
     * @param groupId
     * @return java.util.List<com.erp.model.oms.entity.CustomerGroupEntity>
     */
    List<CustomerGroupEntity> listById(String groupId);

    /**
     * 修改金蝶同步信息
     * @Author Luo_WG
     * @Date 2023/5/25 10:43
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @param syncOperate
     * @return java.lang.Boolean
     **/
    Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus,String syncKingdeeId,String syncOperate);
}
