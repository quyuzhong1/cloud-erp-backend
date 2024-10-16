package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.ReportOrderDemandDTO;
import com.erp.model.wms.entity.ReportOrderDemandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-09-23
 */
@Mapper
public interface ReportOrderDemandMapper extends BaseMapper<ReportOrderDemandEntity> {
    /**
     *  分页查询
     * @author will
     * @date 2024/9/24 12:01
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ReportOrderDemandDTO.ListDTO> paging(Page query, @Param("params") ReportOrderDemandDTO.PagingParamDTO params);
    /**
     * 删除
     * @author will
     * @date 2024/9/27 12:08
     */
    void deleteAll();
}
