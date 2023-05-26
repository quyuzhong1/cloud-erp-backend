package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.model.wms.entity.TransferOutEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 分布式调出单 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface TransferOutMapper extends BaseMapper<TransferOutEntity> {

    /**
     * 分页查询
     * @param query
     * @param params
     * @return
     */
    IPage<TransferOutDTO.PagingViewDTO> paging(Page query, @Param("params") TransferOutDTO.PagingParamDTO params);

    /**
     * 导出查询
     * @param params
     * @return
     */
    List<TransferOutDTO.PagingViewDTO> exportList(@Param("params") TransferOutDTO.ExportDTO params);

    /**
     * 状态数量
     * @param params
     * @return
     */
    List<ApproveStatusQtyDTO> listCount(@Param("params") TransferOutDTO.PagingParamDTO params);

}
