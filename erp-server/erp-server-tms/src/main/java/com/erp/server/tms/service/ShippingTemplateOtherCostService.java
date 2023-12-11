package com.erp.server.tms.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
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
    /**
     * @description: 运费计算列表
     * @author Will
     * @date: 2023/11/20 16:32
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ShippingCalculationDTO.ListDTO> paging(Page query, ShippingCalculationDTO.PagingParamDTO params);
    /**
     * @description: 运费计算导出
     * @author Will
     * @date: 2023/11/20 16:34
     * @param params
     * @return List<ListDTO>
     */
    List<ShippingCalculationDTO.ListDTO> listByExportExcel(ShippingCalculationDTO.PagingParamDTO params);

    /**
     * 根据条件获取到费用相关的信息
     * @param params
     * @return
     */
    List<ShippingCalculationDTO.ListDTO> listRefCost(SoB2cDTO.ShippingCalculationDTO params);
}
