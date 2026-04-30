package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 报关单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
@Mapper
public interface TmsDeclareBillMapper extends BaseMapper<TmsDeclareBillEntity> {

    List<TmsDeclareBillDTO.TabListDTO> tabList(String code,String permissionSql);

    IPage<TmsDeclareBillDTO.PagingVO> paging(Page<TmsDeclareBillDTO.PagingVO> query,@Param("params") TmsDeclareBillDTO.PagingParamDTO params);

    List<TmsDeclareBillDTO.StatisticsAllDTO> statistics(@Param("params") TmsDeclareBillDTO.StatisticsDTO build,String permissionSql);

    List<TmsDeclareBillDTO.ExportDTO> exportDeclare(@Param("params") TmsDeclareBillDTO.PagingParamDTO pagingParamDTO);
    /**
     * b2b报关单分页查询
     * @author will
     * @date 2026/4/16 14:42
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.tms.dto.TmsDeclareBillDTO.PagingVO>
     */
    IPage<TmsDeclareBillDTO.PagingVO> b2bDeclarePaging(Page query,@Param("params") TmsDeclareBillDTO.PagingParamDTO params);
    /**
     * 头程报关单分页查询
     * @author will
     * @date 2026/4/16 14:44
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.tms.dto.TmsDeclareBillDTO.PagingVO>
     */
    IPage<TmsDeclareBillDTO.PagingVO> firstMilePaging(Page query,@Param("params") TmsDeclareBillDTO.PagingParamDTO params);
    /**
     * 根据报关单号id查询来源信息
     * @author will
     * @date 2026/4/20 16:42
     * @param declareBillId
     * @return com.erp.model.tms.dto.TmsDeclareBillDTO.BillSourceDTO
     */
    List<TmsDeclareBillDTO.BillSourceDTO> listSourceByDeclareBillId(String declareBillId);
    /**
     * 查询未生成报关单的头程发货明细信息
     * @author will
     * @date 2026/4/22 10:26
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.NotGenerateDetailDTO>
     */
    IPage<TmsDeclareBillDTO.NotGenerateDetailDTO> listNotGenerateDeclareFmDetail(Page query,@Param("params") TmsDeclareBillDTO.NotGenerateParamDTO params);
    /**
     * 查询未生成报关单的b2b发货通知单明细信息
     * @author will
     * @date 2026/4/22 10:26
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.NotGenerateDetailDTO>
     */
    IPage<TmsDeclareBillDTO.NotGenerateDetailDTO> listNotGenerateB2bDetailPaging(Page query,@Param("params") TmsDeclareBillDTO.NotGenerateParamDTO params);
}
