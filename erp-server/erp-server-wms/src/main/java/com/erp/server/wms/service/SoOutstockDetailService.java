package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
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
    void add(String mainId, List<SoOutstockDetailDTO.AddDTO> detailList);

    
    /**
     * 根据 main id  获取对应数据
     * @author yl
     * @date 2023-05-19 11:32
     * @param mainId
     * @param warehouseId
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDetiailDTO.ViewDTO>
     */
    List<SoOutstockDetailDTO.ViewDTO> listByMainId(String mainId, String warehouseId);

    
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
    void checkOutQty(String warehouseId,String soId,String sourceId, String sourceType, List<SoOutstockDetailDTO.UpdateDTO> detailList);

    
    /**
     * 修改明细信息
     * @author yl
     * @date 2023-05-22 18:19
     * @param id
     * @param detailList
     * @return void
     */
    void updateDetail(String id, List<SoOutstockDetailDTO.UpdateDTO> detailList);

    /**
     * 获取到销售出库明细 根据主表id
     * @author yl
     * @date 2023-05-23 9:28
     * @param noticeSoOutstockIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     */
    List<SoOutstockDetailEntity> listByMainIds(List<String> noticeSoOutstockIds);

    /**
     * 根据销售订单详情id获取到对应的下推数量
     * @author yl
     * @date 2023-05-25 10:33
     * @param soDetailIds
     * @return java.lang.Integer
     */
    Integer getPushDownCountBySoDetailIds(List<String> soDetailIds);

    /**
     * 销售订单ids获取销售出库单的数据
     * @Author Luo_WG
     * @Date 2023/5/25 15:33
     * @param soIds soIds
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.SoRefDTO>
     **/
    List<SoOutstockDetailEntity> listDetailBySoIds(List<String> soIds);

    /**
     * 关闭关联单据的关闭状态
     *
     * @param soDetailIds
     * @return void
     * @author yl
     * @date 2023-05-25 19:25
     */
    void closeBySoDetailIds(List<String> soDetailIds);


    /**
     * 根据销售订单详情ids 获取对应的出库详情
     * @author yl
     * @date 2023-05-29 17:32
     * @param soDetailIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     */
    List<SoOutstockDetailDTO.DeliveryQtyDTO> listDetailBySoDetailIds(List<String> soDetailIds);
}
