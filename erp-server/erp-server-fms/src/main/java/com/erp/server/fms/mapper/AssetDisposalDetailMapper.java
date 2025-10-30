package com.erp.server.fms.mapper;
import com.erp.model.fms.dto.AssetDisposalDetailDTO;
import com.erp.model.fms.entity.AssetDisposalDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 资产处置单资产明细表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-10-29
 */
@Mapper
public interface AssetDisposalDetailMapper extends BaseMapper<AssetDisposalDetailEntity> {

    List<AssetDisposalDetailDTO.ViewDTO> listByMainId(@Param("mainId") String mainId);
}
