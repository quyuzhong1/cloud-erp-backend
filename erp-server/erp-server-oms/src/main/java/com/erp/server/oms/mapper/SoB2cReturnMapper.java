package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * b2c退货订单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
@Mapper
public interface SoB2cReturnMapper extends BaseMapper<SoB2cReturnEntity> {

    IPage<SoB2cReturnDTO.PagingViewDTO> paging(Page query, SoB2cReturnDTO.PagingParamDTO params);

    List<SoB2cReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(@Param("ids") List<String> ids);

    List<SoB2cReturnDTO.BindReturnInstockViewDTO> bindReturnInstockView(@Param("ids")List<String> ids);

    List<SoB2cReturnDetailEntity> listDetailBySoIds(@Param("ids")List<String> soIds);

    List<SoB2cReturnDTO.ReturnLogisticsDTO> selectLogisticsCodePreview(@Param("ids") List<String> ids);

    List<SoB2cReturnDTO.ReturnInstockDTO> selectReturnInstockPreview(@Param("detailIds") List<String> detailIds);

    List<SoDetailDTO.AddDetailView> listAddDetailView(@Param("dto") listAddDetailViewDTO dto);
}
