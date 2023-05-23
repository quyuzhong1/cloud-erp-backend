package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.entity.SalesDemandDetailEntity;

import java.util.List;

/**
 * <p>
 * 销售需求明细表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface SalesDemandDetailService extends SuperService<SalesDemandDetailEntity> {
    /**
     * @description: 新增明细
     * @author Will
     * @date: 2023/3/17 15:45
     * @param details
     * @param salesDemandId

     */
    void add(List<SalesDemandDetailDTO.AddDTO> details, String salesDemandId);
    /**
     * @description: 修改明细
     * @author Will
     * @date: 2023/3/17 16:20
     * @param details

     */
    void update(List<SalesDemandDetailDTO.UpdateDTO> details,String salesDemandId);
    /**
     * @description: 根据主表id查询明细数据
     * @author Will
     * @date: 2023/3/17 16:43
     * @param salesDemandId
     * @return List<SalesDemandDetailEntity>
     */
    List<SalesDemandDetailEntity> listBySalesDemandId(String salesDemandId);
    /**
     * @description: 根据主表ids删除明细
     * @author Will
     * @date: 2023/3/20 10:27
     * @param ids
     */
    void removeBySalesDemandIds(List<String> ids);
    /**
     * @description: 根据主表id和sku编码查询
     * @author Will
     * @date: 2023/3/22 9:51
     * @param salesDemandId
     * @param skuNo
     * @return SalesDemandDetailEntity
     */
    SalesDemandDetailEntity getBySalesDemandIdAndSkuId(String salesDemandId, String skuNo);
    /**
     * @description: 根据来源明细ids查询
     * @author Will
     * @date: 2023/5/23 9:56
     * @param sourceDetailIds
     * @return List<SalesDemandDetailEntity>
     */
    List<SalesDemandDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds);
}
