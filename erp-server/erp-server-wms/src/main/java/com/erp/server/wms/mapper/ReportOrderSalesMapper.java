package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.ReportOrderSalesDTO;
import com.erp.model.wms.entity.ReportOrderSalesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 订单销量表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Mapper
public interface ReportOrderSalesMapper extends BaseMapper<ReportOrderSalesEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/9/24 10:01
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ReportOrderSalesDTO.ListDTO> paging(Page query,@Param("params") ReportOrderSalesDTO.PagingParamDTO params,@Param("otherParamDTO") ReportOrderSalesDTO.PagingOtherParamDTO otherParamDTO);
    /**
     * 删除
     * @author will
     * @date 2024/9/27 14:43
     */
    void deleteAll();
}
