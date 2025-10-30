package com.erp.server.fms.mapper;
import com.erp.model.fms.dto.AssetDisposalPhysicalDetailDTO;
import com.erp.model.fms.entity.AssetDisposalPhysicalDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 资产处置单实物明细表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-10-29
 */
@Mapper
public interface AssetDisposalPhysicalDetailMapper extends BaseMapper<AssetDisposalPhysicalDetailEntity> {

    List<AssetDisposalPhysicalDetailDTO.ViewDTO> listByMainId(@Param("mainId") String mainId);
}
