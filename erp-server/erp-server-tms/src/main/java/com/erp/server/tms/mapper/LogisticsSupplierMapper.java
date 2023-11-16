package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物理商表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface LogisticsSupplierMapper extends BaseMapper<LogisticsSupplierEntity> {

    /**
     * 获取到tab页数据
     * @param permissionSql
     * @return
     */
    List<LogisticsSupplierDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页获取
     * @param query
     * @param params
     * @return
     */
    IPage<LogisticsSupplierDTO.PagingViewDTO> paging(Page query, @Param("params")LogisticsSupplierDTO.PagingParamDTO params);

    /**
     * 导出
     * @param dto
     * @return
     */
    List<LogisticsSupplierDTO.PagingViewDTO> listExport(@Param("params") LogisticsSupplierDTO.ExportDTO dto);

    /**
     * 获取渠道信息
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-16
     */
    LogisticsSupplierEntity getById(@Param("id") String id);
}
