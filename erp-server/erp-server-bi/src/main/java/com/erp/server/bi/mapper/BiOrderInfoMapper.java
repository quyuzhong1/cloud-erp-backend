package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.vo.DimensionSalesVO;
import com.erp.model.bi.vo.SalePriceDistributionVO;
import com.erp.model.bi.vo.SalesPriceRangeVO;
import com.erp.model.dmp.dto.DmpOrderInfoDTO;
import com.erp.model.dmp.dto.DmpOrderInfoExcelDTO;
import com.erp.model.dmp.dto.DmpOrderInfoSearchDTO;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpOrderInfo
 */
@Mapper
public interface BiOrderInfoMapper extends BaseMapper<BiOrderInfoEntity> {
    /**
     * @param query
     * @param params
     * @return IPage
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 15:50
     */
    IPage<DmpOrderInfoDTO> paging(Page query, @Param("params") DmpOrderInfoSearchDTO params);

    /**
     * @param params
     * @return List<DmpOrderInfoExcelDTO>
     * @description: 查询所有的订单数据
     * @author Will
     * @date: 2022/12/15 10:33
     */
    List<DmpOrderInfoExcelDTO> getAllDmpOrderInfo(@Param("params") DmpOrderInfoSearchDTO params);
    /**
     * @param params
     * @return List<DmpOrderInfoExcelDTO>
     * @description: 查询所有的订单数据
     * @author Will
     * @date: 2022/12/15 10:33
     */
    Page<DmpOrderInfoExcelDTO> getAllDmpOrderInfo(@Param("page") Page<DmpOrderInfoExcelDTO> page, @Param("params") DmpOrderInfoSearchDTO params);


    /**
     * 根据不同维度统计销售额
     *
     * @param dto
     * @param groupName
     * @return
     */
    List<DimensionSalesVO> sumByDeptAndCostType(@Param("params") BiFilterDTO dto, @Param("groupName") String groupName);

    /**
     * 根据不同维度统计销售额
     *
     * @param dto
     * @param settleRate
     * @return
     */
    BigDecimal sumSales(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate);

    /**
     * 获取销售单价分布 汇总
     *
     * @param dto
     * @return
     */
    SalePriceDistributionVO countSalePriceDistribution(@Param("params") BiFilterDTO dto, @Param("settleRate") String settleRate,
                                                       @Param("rangeVO") SalesPriceRangeVO rangeVO );
    /**
     * 根据不同维度统计销售量
     *
     * @param dto
     * @return
     */
    Integer countSalesVolume(@Param("params") BiFilterDTO dto);

    /**
     * @description: 查询销售额
     * @author Will
     * @date: 2023/9/25 9:34
     * @param dto
     * @param dataType sales  qty
     * @return List<ViewDTO>
     */
    List<TargetFinishDTO.ViewDTO>  listSalesBiFilter(@Param("params") TargetFinishDTO.ParamDTO dto,@Param("dataType") String dataType);

    /**
     * @description: 查询订单退款数据
     * @author Will
     * @date: 2023/10/8 14:30
     * @param dto
     * @param groupViewDTO
     * @return List<ViewDTO>
     */
    List<TargetFinishDTO.ViewDTO>  listRefundBiFilter(@Param("params") TargetFinishDTO.ParamDTO dto,@Param("viewParams") TargetFinishDTO.GroupViewDTO groupViewDTO);

}




