package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * B2B虚拟仓订单跟踪 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Mapper
public interface SoB2bProcessingMapper extends BaseMapper<SoB2bProcessingEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:25
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<SoB2bProcessingDTO.ListDTO> paging(Page<SoB2bProcessingDTO.PagingParamDTO> page,@Param("params") SoB2bProcessingDTO.PagingParamDTO params);
    /**
     * 查询b2b销售订单跟踪数据
     * @author will
     * @date 2024/12/18 18:44
     * @param startDate
     * @return List<SoB2bProcessingEntity>
     */
    List<SoB2bProcessingEntity> listSoB2bProcessing(@Param("startDate") LocalDate startDate);
    /**
     * 删除多余b2c订单
     * @Auther will
     * @Date 2025/2/6 16:32
     */
    void deleteB2bOrder(@Param("startDate") LocalDate startDate);
    /**
     * 删除b2b销售订单跟踪数据
     * @author will
     * @date 2025/2/18 10:05
     * @param params
     * @return java.lang.Boolean
     */
    Boolean deleteB2bProcessing(@Param("params") SoB2bProcessingDTO.DeleteDTO params);
    /**
     * b2b汇总查询
     * @author will
     * @date 2025/8/20 16:03
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ReportProcessingDTO.ListDTO> b2bTotalPaging(Page<ReportProcessingDTO.PagingParamDTO> page, @Param("params") ReportProcessingDTO.PagingParamDTO params);
}
