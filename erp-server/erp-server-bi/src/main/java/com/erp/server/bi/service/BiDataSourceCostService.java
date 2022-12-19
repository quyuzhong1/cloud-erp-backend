package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiDataSourceCostSearchDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.TargetSaleSumVO;
import com.erp.model.dmp.entity.BiDataSourceCostEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;

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
     * @return PagingVO<LinkedHashMap<String,Object>>
     */
    PagingVO<LinkedHashMap<String,Object>> paging(PagingDTO<BiDataSourceCostSearchDTO> dto);

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

    /**
     * 销售成本统计
     * @param dto
     * @return
     */
    TargetSaleSumVO sumSalesCost(BiFilterDTO dto);

    /**
     * 导出
     */
    void exportExcel(BiDataSourceCostSearchDTO dto, HttpServletResponse response);

    /**
     * 导入
     */
    void importExcel(MultipartFile excelFile, HttpServletResponse response);
}
