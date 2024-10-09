package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.WaveListEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 波次列表（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@Mapper
public interface WaveListPdaMapper extends BaseMapper<WaveListEntity> {
    IPage<WaveListEntity> paging(Page<Object> page, @Param("params") WaveListDTO.SearchParamDTO params);

    List<WaveListDTO.TabDTO> listTab();
}
