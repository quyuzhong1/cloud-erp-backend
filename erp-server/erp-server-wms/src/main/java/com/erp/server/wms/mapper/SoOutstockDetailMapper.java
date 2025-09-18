package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售订单出库明细 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoOutstockDetailMapper extends BaseMapper<SoOutstockDetailEntity> {
    /**
     * 根据来源明细id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/15 15:14
     * @param sourceDetailIds sourceDetailIds
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockDetailEntity>
     **/
    List<SoOutstockDetailEntity> listSoOutstockBySourceDetailId(@Param("sourceDetailIds") List<String> sourceDetailIds);

    /**
     * 根据销售单id查询出库详情
     * @Author Luo_WG
     * @Date 2023/5/25 15:36
     * @param ids
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockDetailEntity>
     **/
    List<SoOutstockDetailEntity> listDetailBySoIds(@Param("ids") List<String> ids);
    /**
     * @description: 根据销售订单明细ids查询
     * @author Will
     * @date: 2023/11/1 15:42
     * @param soDetailIdList
     * @return List<SoOutstockDetailEntity>
     */
    List<SoOutstockDetailEntity> listBySoDetailIds(@Param("soDetailIdList")List<String> soDetailIdList);

    /**
     * 根据主表id分组sku查询发货及待装箱数
     * @Author Luo_WG
     * @Date 2024/3/21 14:14
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.GroupSkuDTO>
     **/
    List<SoOutstockDTO.GroupSkuDTO> listGroupSkuByMainId(@Param("mainId") String mainId);
    /**
     * 根据来源明细id查询
     * @author will
     * @date 2024/12/19 18:27
     * @param sourceIdList
     * @return List<ResponseDTO>
     */
    List<SoB2bProcessingDTO.ResponseDTO> listSoOutstockBySourceIdList(@Param("sourceIdList") List<String> sourceIdList);

    /**
     * 根据skuId查询Doris最新出库时间
     * @author Jim
     * @date 2025-08-13
     * @return
     */
    List<SoOutstockDTO.LastBillDateDTO> mapLastOutstockDateBySkuIds(@Param("skuIds") List<String> skuIds);
}
