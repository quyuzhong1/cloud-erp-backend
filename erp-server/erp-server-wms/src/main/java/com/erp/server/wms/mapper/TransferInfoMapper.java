package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 直接调拨单主表
 *
 * @author will
 * @since 2023-05-10
 */
@Mapper
public interface TransferInfoMapper extends BaseMapper<TransferInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/5/15 11:31
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<TransferInfoDTO.ListDTO> paging(Page query,@Param("params") TransferInfoDTO.SearchParamDTO params);
    /**
     * @description: 查询数量
     * @author Will
     * @date: 2023/5/15 11:34
     * @param params
     * @return Integer
     */
    Integer listCount(@Param("params") TransferInfoDTO.SearchParamDTO params);
    /**
     * @description: 导出数据查询
     * @author Will
     * @date: 2023/5/15 12:14
     * @param params
     * @return List<ListDTO>
     */
    List<TransferInfoDTO.ListDTO> listExportExcel(@Param("params") TransferInfoDTO.SearchParamDTO params);
    Page<TransferInfoDTO.ListDTO> listExportExcel(@Param("page") Page<TransferInfoDTO.ListDTO> page, @Param("params") TransferInfoDTO.SearchParamDTO params);

    /**
     * pda:列表查询
     * @Author Luo_WG
     * @Date 2023/8/24 15:45
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.TransferInfoDTO.PdaListDTO>
     **/
    IPage<TransferInfoDTO.PdaListDTO> pdaPaging(Page query, @Param("params") TransferInfoDTO.PdaSearchParamDTO params);

}
