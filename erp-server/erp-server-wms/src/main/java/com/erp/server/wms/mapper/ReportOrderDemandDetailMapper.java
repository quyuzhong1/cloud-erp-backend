package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.ReportOrderDemandDetailDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 订单需求明细报表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Mapper
public interface ReportOrderDemandDetailMapper  {
    /**
     * 分页查询
     * @author will
     * @date 2024/9/24 12:00
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ReportOrderDemandDetailDTO.ListDTO> paging(Page query,@Param("params") ReportOrderDemandDetailDTO.PagingParamDTO params);
}
