package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 物理商表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Mapper
public interface TransferLogisticsSupplierMapper extends BaseMapper<TransferLogisticsSupplierEntity> {

    /**
     * tab页汇总数量
     * @Author Luo_WG
     * @Date 2024/1/19 15:54
     * @param permissionSql
     * @return java.util.List<com.erp.model.tms.dto.TransferLogisticsSupplierDTO.TabListDTO>
     **/
    List<TransferLogisticsSupplierDTO.TabListDTO> tabList(@Param("permissionSql") String permissionSql);

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2024/1/19 16:01
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage
     **/
    IPage<TransferLogisticsSupplierDTO.PagingViewDTO> paging(Page query, @Param("params") TransferLogisticsSupplierDTO.PagingParamDTO params);

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2024/1/19 17:06
     * @param dto
     * @return java.util.List<com.erp.model.tms.dto.TransferLogisticsSupplierDTO.PagingViewDTO>
     **/
    List<TransferLogisticsSupplierDTO.PagingViewDTO> listExport(@Param("params") TransferLogisticsSupplierDTO.ExportDTO dto);


    /**
     * 获取所有中转商的 授权信息
     * @description
     * @param
     * @return
     * @date 2024-01-27 12:26
     * @author Lambda
     */
    List<TransferLogisticsSupplierDTO.AuthDTO> listAllAuth();

    /**
     * 根据主表信息获取到授权信息
     * @param transferSupplierIdList
     * @return
     */
    List<TransferLogisticsSupplierDTO.AuthDTO> listAuthByMainIds(@Param("mainIdList") List<String> transferSupplierIdList);
}
