package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.entity.TransferDeclareEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 中转报关表 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Mapper
public interface TransferDeclareMapper extends BaseMapper<TransferDeclareEntity> {

    /**
     * 分页列表查询
     * @Author Luo_WG
     * @Date 2024/1/20 14:53
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.tms.dto.TransferDeclareDTO.ListDTO>
     **/
    IPage<TransferDeclareDTO.ListDTO> paging(Page query, @Param("params") TransferDeclareDTO.PagingParamDTO params);

    /**
     * 查询上传状态数量
     * @Author Luo_WG
     * @Date 2024/1/20 17:09
     * @param pagingParamDTO
     * @return java.lang.Integer
     **/
    Integer listUploadStatusCount(@Param("params") TransferDeclareDTO.PagingParamDTO pagingParamDTO);

    /**
     * 查询中转状态数量
     * @Author Luo_WG
     * @Date 2024/1/20 17:09
     * @param pagingParamDTO
     * @return java.lang.Integer
     **/
    Integer listTransferStatusCount(@Param("params")TransferDeclareDTO.PagingParamDTO pagingParamDTO);

    /**
     * 导出excel查询
     * @Author Luo_WG
     * @Date 2024/1/24 18:48
     * @param dto
     * @return java.util.List<com.erp.model.tms.dto.TransferDeclareDTO.ListDTO>
     **/
    List<TransferDeclareDTO.ExportListDTO> listExportExcel(@Param("params")TransferDeclareDTO.PagingParamDTO dto);
    Page<TransferDeclareDTO.ExportListDTO> listExportExcel(@Param("page") Page<TransferDeclareDTO.ExportListDTO> page, @Param("params")TransferDeclareDTO.PagingParamDTO dto);


    /**
     * @description
     * @param soId
     * @author Lambda
     * @return
     * @create 2024-01-26 9:38
     */
    TransferDeclareEntity getBySoId(@Param("soId") String soId);
}
