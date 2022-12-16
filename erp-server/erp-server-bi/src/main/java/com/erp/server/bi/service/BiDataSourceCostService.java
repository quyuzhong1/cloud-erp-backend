package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.model.dmp.entity.BiDataSourceCostEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:37
 */
public interface BiDataSourceCostService
        extends IService<BiDataSourceCostEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 16:43
     * @param dto
     * @return PagingVO<BiDataSourceCostDTO>
     */
    PagingVO<BiDataSourceCostDTO> paging(PagingDTO<BiDataSourceCostSearchDTO> dto);

    /**
     * 统计销售毛利润
     * @param dto
     * @return
     */
    TargetSaleSumVO sumSalesProfit(BiFilterDTO dto);

    /**
     * 统计毛利率
     * @param dto
     * @return
     */
    TargetSaleSumVO sumSalesRatio(BiFilterDTO dto);

    /**
     * 统计主营收入
     * @param dto
     * @return
     */
    TargetSaleSumVO sumMainRevenue(BiFilterDTO dto);
}
