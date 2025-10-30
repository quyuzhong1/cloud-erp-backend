package com.erp.server.fms.mapper;
import com.erp.model.fms.dto.AssetCardDetailDTO;
import com.erp.model.fms.entity.AssetCardDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 资产卡片明细表 Mapper 接口
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Mapper
public interface AssetCardDetailMapper extends BaseMapper<AssetCardDetailEntity> {

    List<AssetCardDetailDTO.SearchCardDetailDTO> searchAssetCardDetail(@Param("params") AssetCardDetailDTO.SearchDTO dto);
}
