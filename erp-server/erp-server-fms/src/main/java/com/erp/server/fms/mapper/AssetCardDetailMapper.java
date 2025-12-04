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

    /**
     * 根据盘点方案的资产范围查询符合条件的资产卡片明细（关联查询）
     * @param cardCodeStart 卡片编码起始值
     * @param cardCodeEnd 卡片编码结束值
     * @return 资产卡片明细列表
     */
    List<AssetCardDetailEntity> queryCardDetailsByScopeWithJoin(
            @Param("cardCodeStart") String cardCodeStart,
            @Param("cardCodeEnd") String cardCodeEnd
    );
}
