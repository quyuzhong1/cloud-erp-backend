package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单变更 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoChangeMapper extends BaseMapper<SoChangeEntity> {

    
    /**
     * 获取审核状态数据
     * @author yl
     * @date 2023-05-24 15:20
     * @param
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.ApproveCountDTO>
     */
    List<SoChangeDTO.ApproveCountDTO> listApproveCount();

    IPage<SoChangeDTO.PagingViewDTO> paging(Page query, @Param("params")SoChangeDTO.PagingParamDTO params, @Param("approveList") List<String> approveList);

    
    /**
     * 导出数据
     * @author yl
     * @date 2023-05-24 17:49
     * @param dto
     * @param approveList
     * @return java.util.List<com.erp.model.oms.dto.SoChangeDTO.PagingViewDTO>
     */
    List<SoChangeDTO.PagingViewDTO> listExport(@Param("params") SoChangeDTO.ExportDTO dto,@Param("approveList") List<String> approveList);

    List<SoChangeDTO.SoRefDTO> listSoRefSoChangeBySoId(@Param("soId") String soId);
}
