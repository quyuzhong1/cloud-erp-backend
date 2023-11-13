package com.erp.server.tms.service;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.common.business.service.SuperService;
import com.erp.model.tms.dto.ShippingTemplateOtherCostDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
public interface ShippingTemplateOtherCostService extends SuperService<ShippingTemplateOtherCostEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-03
    * @param otherCostList
    * @param mainId
    * @return
    */
    Boolean add(List<ShippingTemplateOtherCostDTO.AddDTO> otherCostList, String mainId);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-03
    * @param otherCostList
    * @param mainId
    * @return
    */
    Boolean update(List<ShippingTemplateOtherCostDTO.UpdateDTO> otherCostList, String mainId);

    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/11/8 10:55
     * @param mainId
     * @return List<ShippingTemplateOtherCostEntity>
     */
    List<ShippingTemplateOtherCostEntity> listByMainId(String mainId);
    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/11/8 14:18
     * @param mainId
     */
    void deleteByMainId(String mainId);

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/11/13 10:27
     * @param mainIdList
     * @return List<ShippingTemplateOtherCostEntity>
     */
    List<ShippingTemplateOtherCostEntity> listByMainIds(List<String> mainIdList);
}
