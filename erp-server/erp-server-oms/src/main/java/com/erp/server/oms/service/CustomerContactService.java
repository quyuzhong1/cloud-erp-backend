package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.entity.CustomerContactEntity;

import java.util.List;

/**
 * <p>
 * 客户联系人信息 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerContactService extends SuperService<CustomerContactEntity> {

    /**
     * 检查客户默认联系人是否多个
     * @author yl
     * @date 2023-05-12 14:00
     * @param contactList
     * @return void
     */
    void checkIsDefault(List<CustomerContactDTO.AddDTO> contactList);

    
    /**
     * 保存联系人信息
     * @author yl
     * @date 2023-05-12 15:53
     * @param id
     * @param contactList
     * @return void
     */
    void saveBatchContact(String id, List<CustomerContactDTO.AddDTO> contactList);

    
    
    /**
     *
     * 获取联系人信息
     * @author yl
     * @date 2023-05-15 9:53
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.CustomerContactDTO.ViewDTO>
     */
    List<CustomerContactDTO.ViewDTO> listByMainId(String mainId);

    /**
     * 获取联系人信息
     * @Author Luo_WG
     * @Date 2023/6/9 14:34
     * @param mainId mainId
     * @return java.util.List<com.erp.model.oms.entity.CustomerContactEntity>
     **/
    List<CustomerContactEntity> listEntityByMainId(String mainId);

    
    /**
     * 修改联系人信息
     * @author yl
     * @date 2023-05-15 10:57
     * @param mainId
     * @param contactList
     * @return void
     */
    void updateBatchContact(String mainId, List<CustomerContactDTO.ViewDTO> contactList);
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
