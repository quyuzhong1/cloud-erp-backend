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
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     */
    List<SoPriceDetailDTO.ViewDTO> listCheckSoPriceDetail(@Param("supplierId") String supplierId, @Param("statusList") List<String> statusList, @Param("SoOrgId") String SoOrgId, @Param("skuIdList") List<String> skuIdList);

    /**
     * 根据供应商id 获取到对应明细
     * @Author Luo_WG
     * @Date 2024/1/9 14:16
     * @param supplierIds
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     **/
    List<SoPriceDetailDTO.AddDTO> listBySupplierId(@Param("supplierIds") List<String> supplierIds,@Param("statusList") List<String> statusList,@Param("detailIds") List<String> detailIds,@Param("skuIdList") List<String> skuIdList);

    List<SoPriceDetailEntity> getBySupplierAndStatus(@Param("supplierId") String supplierId, @Param("SoOrgId") String SoOrgId, @Param("statusList") List<String> statusList);
    /**
     * 批量修改
     */
    List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> batchGetTaxPrice(@Param("params") SoPriceDetailDTO.SoTaxPriceBatchSearchDTO dto);

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
     * @param SoPriceDetailIds
     * @return java.util.List<com.erp.model.scm.entity.SoPriceDetailEntity>
     **/
    List<SoPriceDetailEntity> listDetailByIds(@Param("SoPriceDetailIds") List<String> SoPriceDetailIds);
}
