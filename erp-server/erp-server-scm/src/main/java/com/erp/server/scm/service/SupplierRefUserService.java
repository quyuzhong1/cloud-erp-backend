package com.erp.server.scm.service;
import com.erp.model.scm.entity.SupplierRefUserEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.scm.dto.SupplierRefUserDTO;
import com.erp.model.scm.vo.SupplierRefUserVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zdy
 * @since 2024-01-05
 */
public interface SupplierRefUserService extends SuperService<SupplierRefUserEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-01-05
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SupplierRefUserDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-01-05
    * @param dto
    * @return
    */
    Boolean update(SupplierRefUserDTO.UpdateDTO dto);

    /**
     * 根据物流商id获取用户列表
     * @param supplierIds
     * @param isSuper 是否是超级管理员
     * @return
     */
    List<SupplierRefUserVO> getUserIdsBySupplierIds(List<String> supplierIds, Boolean isSuper);

    /**
     * 获取用户原来的 供应商关系
     * @param uid
     * @return
     */
    SupplierRefUserEntity getSupplierRelUserByUid(String uid);

    /**
     * 根据用户id删除关联记录
     * @param uids
     */
    void deleteRefByUids(List<String> uids);

    /**
     * 根据用户获取供应商信息
     * @param uids
     * @return
     */
    List<SupplierRefUserVO> getSupplierRefByUids(List<String> uids);
}
