package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.TransferInDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.entity.TransferInEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 分布式调入单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface TransferInMapper extends BaseMapper<TransferInEntity> {

    /**
     * 获取审核状态数据
     * @author yl
     * @date 2023-05-24 15:20
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.ApproveCountDTO>
     */
    List<TransferInDTO.ApproveCountDTO> listApproveCount();

    IPage<TransferInDTO.PagingViewDTO> paging(Page query, @Param("params")TransferInDTO.PagingParamDTO params, @Param("approveList") List<String> approveList);

    /**
     * 获取导出数据
     * @param dto
     * @param approveList
     * @return
     */
    List<TransferInDTO.PagingViewDTO> listExport(@Param("params") TransferInDTO.ExportDTO dto, @Param("approveList") List<String> approveList);

    /**
     * 获取到对应参数
     * @author yl
     * @date 2023-05-29 14:44
     * @param idList
     * @return java.util.List<com.erp.model.wms.dto.inventory.InOutStockDTO>
     */
    List<InOutStockDTO> listInventoryInOut(@Param("ids") List<String> idList);
}
