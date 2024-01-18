package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSkuCostCustomEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSkuCostCustomDTO;

import java.util.List;

/**
 * <p>
 * sku自定义成本表 服务类
 * </p>
 *
 * @author will
 * @since 2024-01-04
 */
public interface DmpSkuCostCustomService extends SuperService<DmpSkuCostCustomEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-01-04
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSkuCostCustomDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-01-04
    * @param dto
    * @return
    */
    Boolean update(DmpSkuCostCustomDTO.UpdateDTO dto);
    /**
     * @description: 根据sku编号查询
     * @author Will
     * @date: 2024/1/4 16:57
     * @param skuNo
     * @return DmpSkuCostCustomEntity
     */
    DmpSkuCostCustomEntity getBySkuNo(String skuNo);
    /**
     * @description: 根据sku编码集合查询
     * @author Will
     * @date: 2024/1/10 19:51
     * @param redisSkuNoList
     * @return List<DmpSkuCostCustomEntity>
     */
    List<DmpSkuCostCustomEntity> listDmpSkuCostCustomBySkuNoList(List<String> redisSkuNoList);
}
