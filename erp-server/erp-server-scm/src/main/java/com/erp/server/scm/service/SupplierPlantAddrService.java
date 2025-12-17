package com.erp.server.scm.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SupplierPlantAddrDTO;
import com.erp.model.scm.entity.SupplierPlantAddrEntity;

import java.util.List;

/**
 * <p>
 * 供应商工厂地信息 服务类
 * </p>
 *
 * @author will
 * @since 2025-07-21
 */
public interface SupplierPlantAddrService extends SuperService<SupplierPlantAddrEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SupplierPlantAddrDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2025-07-21
    * @param dto
    * @return
    */
    Boolean update(SupplierPlantAddrDTO.UpdateDTO dto);

    /**
     * 导入更新
     * @author will
     * @date 2025/7/24 19:08
     * @param supplierId
     * @param plantAddrList
     * @param type
     * @return void
     */
    void importUpdate(String supplierId, List<SupplierPlantAddrDTO.AddDTO> plantAddrList,String type);
    /**
     * 添加供应商工厂地
     * @author will
     * @date 2025/7/25 09:08
     * @param plantAddrList
     * @param supplierId
     * @return void
     */
    Boolean saveOrUpdateBatchPlantAddr(List<SupplierPlantAddrDTO.AddDTO> plantAddrList, String supplierId);
    /**
     * 根据供应商id查询
     * @author will
     * @date 2025/7/25 09:21
     * @param supplierId
     * @return List<ViewDTO>
     */
    List<SupplierPlantAddrEntity> listBySupplierId(String supplierId);
    /**
     * 根据供应商id集合查询
     * @author will
     * @date 2025/7/25 10:03
     * @param supplierIdList
     * @return List<SupplierPlantAddrDTO.ViewDTO>
     */
    List<SupplierPlantAddrDTO.ViewDTO> listViewBySupplierIdList(List<String> supplierIdList);
}
