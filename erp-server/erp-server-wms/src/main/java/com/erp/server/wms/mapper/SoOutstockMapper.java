package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单出库单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoOutstockMapper extends BaseMapper<SoOutstockEntity> {

    IPage<SoOutstockDTO.PagingViewDTO> paging(Page query, @Param("params") SoOutstockDTO.PagingParamDTO params);

    List<SoOutstockDTO.PagingViewDTO> listExport(@Param("params") SoOutstockDTO.ExportDTO dto);

    List<InOutStockDTO> listInventoryInOut(@Param("ids") List<String> idList);

    List<SoOutstockDTO.SoRefDTO> listSoRefSoOutstockBySoId(@Param("soId") String soId);

    List<SoOutstockDTO.ApproveCountDTO> listApproveCount(@Param("permissionSql")String permissionSql);

    /**
     * 根据来源id查询出库id 临时使用修复数据
     * @return
     */
    List<String> getIdsByTemp();

    /**
     * PDA:分页列表
     * @Author Luo_WG
     * @Date 2023/8/22 14:42
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoOutstockDTO.PdaPagingViewDTO>
     **/
    IPage<SoOutstockDTO.PdaPagingViewDTO> pdaPaging(Page query, @Param("params") SoOutstockDTO.PdaPagingParamDTO params);

    /**
     * pda:列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/22 15:06
     * @param params
     * @return java.lang.Integer
     **/
    Integer listCount(@Param("params") SoOutstockDTO.PagingParamDTO params);


    /**
     * 临时接口批量更新
     * @param soOutstockList
     */
    void updateBatch(@Param("params") List<SoOutstockEntity> soOutstockList);
    /**
     * @description: 查询合计数据
     * @author Will
     * @date: 2023/11/1 14:25
     * @param params
     * @return PagingTotalDTO
     */
    SoOutstockDTO.PagingTotalDTO getTotalByQuery(@Param("params") SoOutstockDTO.PagingParamDTO params);
}
