package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
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
    Page<LogisticsSupplierDTO.PagingViewDTO> listExport(@Param("page") Page<LogisticsSupplierDTO.PagingViewDTO> page, @Param("params") LogisticsSupplierDTO.ExportDTO dto);

    /**
     * 获取渠道信息
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-16
     */
    LogisticsSupplierDTO.AuthDTO getLogisticsSupplierAuthById(@Param("id") String id);
    /**
     * @description: 查询渠道
     * @author Will
     * @date: 2024/4/1 11:19
     * @param logisticsSupplierIdList
     * @return List<LogisticsSupplierListDTO>
     */
    List<LogisticsSupplierDTO.LogisticsSupplierListDTO> listLogisticsChannel(@Param("logisticsSupplierIdList")List<String> logisticsSupplierIdList);

    /**
     * 物流商详情
     * @param id 主键id
     * @return {@link LogisticsSupplierDTO.ViewDTO}
     */
    LogisticsSupplierDTO.ViewDTO detail(String id);
}
