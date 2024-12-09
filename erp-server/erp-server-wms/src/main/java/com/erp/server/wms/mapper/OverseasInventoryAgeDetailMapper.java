package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.OverseasInventoryAgeDetailDTO;
import com.erp.model.wms.entity.OverseasInventoryAgeDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 海外仓库存库龄明细表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2024-12-06
 */
@Mapper
public interface OverseasInventoryAgeDetailMapper extends BaseMapper<OverseasInventoryAgeDetailEntity> {

    List<OverseasInventoryAgeDetailDTO.AgeRangeViewDTO> getAgeRangeViewByMainIds(@Param(value = "mainIds")List<String> mainIds,@Param(value = "nowDate") LocalDate nowDate);

    IPage<OverseasInventoryAgeDetailDTO.ListDTO> paging(Page<?> query, @Param(value = "params")OverseasInventoryAgeDetailDTO.PagingParamDTO params);
}
