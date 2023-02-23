package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProductPlanApprovalTrendDTO;
import com.erp.model.plm.dto.ProductPlanGroupSerachDTO;
import com.erp.model.plm.dto.ProductPlanSearchDTO;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.entity.ProductPlanEntity;
import com.erp.model.plm.vo.ProductPlanGroupVO;
import com.erp.model.plm.vo.ProductPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 19:50
 */
@Mapper
public interface ProductPlanMapper  extends BaseMapper<ProductPlanEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/2/21 17:06
     * @param query
     * @param params
     * @return IPage<ProductPlanVO>
     */
    IPage<ProductPlanVO> paging(Page query, @Param("params") ProductPlanSearchDTO params);
    /**
     * @description: 导出数据查询
     * @author Will
     * @date: 2023/2/22 18:31
     * @param params
     * @return List<ProductPlanExcelDTO>
     */
    List<ProductPlanExcelDTO> listExportExcel(@Param("params") ProductPlanSearchDTO params);
    /**
     * @description: 查询指标数据数量
     * @author Will
     * @date: 2023/2/23 14:02
     * @param dto
     * @param startTime
     * @param endTime
     * @return Integer
     */
    Integer listProductPlanTotalCount(@Param("params")ProductPlanGroupSerachDTO dto,@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime);
   /**
    * @description: 根据状态查询数量
    * @author Will
    * @date: 2023/2/23 14:18
    * @param dto
    * @param startTime
    * @param endTime
    * @param type
    * @return Integer
    */
    Integer listProductPlanStatusCount(@Param("params")ProductPlanGroupSerachDTO dto,@Param("type") Integer type,@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime);
    /**
     * @description: 指标数据
     * @author Will
     * @date: 2023/2/23 15:26
     * @param params
     * @return List<ProductPlanGroupVO>
     */
    List<ProductPlanGroupVO> listProductPlanGroupTable(@Param("params") ProductPlanGroupSerachDTO params);

    List<ProductPlanApprovalTrendDTO> countApprovalTrend(ProductPlanGroupSerachDTO dto, LocalDateTime thisYearStart, LocalDateTime thisYearEnd);
}
