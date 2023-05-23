package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SoOutstockDetiailDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;

import java.util.List;

/**
 * <p>
 * 销售订单出库明细 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoOutstockDetailService extends SuperService<SoOutstockDetailEntity> {
    /**
     * 根据来源明细id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/15 14:56
     * @param sourceDetailId sourceDetailId
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     **/
    List<SoOutstockDetailEntity> listDetailBySourceDetailId(List<String> sourceDetailId);

    
    /**
     * 保存销售出库单明细
     * @author yl
     * @date 2023-05-19 10:18
     * @param mainId
     * @param detailList
     * @return void
     */
    void add(String mainId, List<SoOutstockDetiailDTO.AddDTO> detailList);

    
    /**
     * 根据 main id  获取对应数据
     * @author yl
     * @date 2023-05-19 11:32
     * @param mainId
     * @param warehouseId
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDetiailDTO.ViewDTO>
     */
    List<SoOutstockDetiailDTO.ViewDTO> listByMainId(String mainId,String warehouseId);

    
    /**
     * 删除明细
     * @author yl
     * @date 2023-05-19 12:28
     * @param ids
     * @return void
     */
    void removeByMainIdList(List<String> ids);

    
    /**
     * 检查数量
     * @author yl
     * @date 2023-05-22 15:54
     * @param sourceId
     * @param sourceType
     * @param detailList
     * @return void
     */
    void checkOutQty(String warehouseId,String soId,String sourceId, String sourceType, List<SoOutstockDetiailDTO.UpdateDTO> detailList);

    
    /**
     * 修改明细信息
     * @author yl
     * @date 2023-05-22 18:19
     * @param id
     * @param detailList
     * @return void
     */
    void updateDetail(String id, List<SoOutstockDetiailDTO.UpdateDTO> detailList);

    /**
     * 获取到销售出库明细 根据主表id
     * @author yl
     * @date 2023-05-23 9:28
     * @param noticeSoOutstockIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     */
    List<SoOutstockDetailEntity> listByMainIds(List<String> noticeSoOutstockIds);
}
