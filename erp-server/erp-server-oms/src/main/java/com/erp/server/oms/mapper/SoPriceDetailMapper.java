package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 销售价目表明细 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Mapper
public interface SoPriceDetailMapper extends BaseMapper<SoPriceDetailEntity> {
    /**
     * @description: 根据供应商id和skuId查询有效单价
     * @author Will
     * @date: 2023/3/27 9:41
     * @param dto
     * @return SoTaxPriceViewDTO
     */
    List<SoPriceDetailDTO.SoTaxPriceViewDTO> getTaxPrice(@Param("params") SoPriceDetailDTO.SoTaxPriceSearchDTO dto);


    /**
     * 根据供应商、组织、SKU查询
     * @author yl
     * @date 2023-04-06 9:52
     * @param customerIdList
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     */
    List<SoPriceDetailDTO.ViewDTO> listCheckSoPriceDetail(@Param("customerIdList") List<String> customerIdList, @Param("statusList") List<String> statusList, @Param("soOrgId") String soOrgId, @Param("skuIdList") List<String> skuIdList);

    /**
     * 根据客户状态查询
     * @param customerId
     * @param soOrgId
     * @param statusList
     * @return SoPriceDetailEntity
     */
    List<SoPriceDetailEntity> getByCustomerIdAndStatus(@Param("customerId") String customerId, @Param("soOrgId") String soOrgId, @Param("statusList") List<String> statusList);
    /**
     * 根据主标查询明细
     * @Author Luo_WG
     * @Date 2024/1/9 16:41
     * @param dto
     * @return java.util.List<com.erp.model.scm.entity.SoPriceDetailEntity>
     **/
    List<SoPriceDetailEntity> listGetListBySoPriceIds(@Param("params") SoPriceChangeDetailDTO.SkuChangeParamDTO dto);

    /**
     * 根据id查询详情
     * @Author Luo_WG
     * @Date 2024/1/16 11:25
     * @param soPriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.SoPriceDetailEntity>
     **/
    List<SoPriceDetailEntity> listDetailByIds(@Param("soPriceDetailIds") List<String> soPriceDetailIds);
    /**
     * 批量查询
     * @author will
     * @date 2025/3/27 12:18
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SoPriceDetailDTO.SoTaxPriceBatchViewDTO>
     */
    List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> batchGetTaxPrice(@Param("params")SoPriceDetailDTO.SoTaxPriceBatchSearchDTO dto);
}
