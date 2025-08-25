package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


/**
 * <p>
 * 样品库存统计 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Mapper
public interface SampleLedgerMapper extends BaseMapper<SampleLedgerEntity> {



    /**
     * 根据用户ID查询台账列表
     * @param dto
     * @return
     */
    @MapKey("skuNo")
    Map<String, Integer> listSkuAvailableQtyByUserId(@Param("dto") SampleLedgerDTO.SearchDTO dto);


    IPage<SampleLedgerDTO.SkuAvailableQtyDTO> listSku(Page query, @Param("params") SampleLedgerDTO.SearchDTO params);
}
