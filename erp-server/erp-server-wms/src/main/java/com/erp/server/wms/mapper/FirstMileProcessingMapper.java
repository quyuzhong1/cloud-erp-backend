package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 头程虚拟仓订单跟踪 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Mapper
public interface FirstMileProcessingMapper extends BaseMapper<FirstMileProcessingEntity> {
    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:48
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<FirstMileProcessingDTO.ListDTO> paging(Page<FirstMileProcessingDTO.PagingParamDTO> page,@Param("params") FirstMileProcessingDTO.PagingParamDTO params);
    /**
     * 导出分页查询
     * @author will
     * @date 2024/12/18 11:48
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<FirstMileProcessingDTO.ListDTO> exportPaging(Page<FirstMileProcessingDTO.PagingParamDTO> page,@Param("params") FirstMileProcessingDTO.PagingParamDTO params);
    /**
     * 查询头程跟踪信息
     * @author will
     * @date 2024/12/19 10:20
     * @param startDate
     * @return List<FirstMileProcessingEntity>
     */
    List<FirstMileProcessingEntity> listFirstMileProcessing(@Param("startDate")LocalDate startDate);

    /**
     * 删除头程数据
     * @Auther will
     * @Date 2025/2/6 16:34
     */
    void deleteFirstMileOrder(@Param("startDate") LocalDate startDate);
    /**
     * 删除
     * @author will
     * @date 2025/2/18 09:57
     * @param params
     * @return java.lang.Boolean
     */
    Boolean deleteFirstMileProcessing(@Param("params") FirstMileProcessingDTO.DeleteDTO params);
    /**
     * 头程数据分页合计
     * @author will
     * @date 2025/8/20 16:06
     * @param page
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ReportProcessingDTO.ListDTO> firstMileTotalPaging(Page<ReportProcessingDTO.PagingParamDTO> page, @Param("params") ReportProcessingDTO.PagingParamDTO params);
}
