package com.erp.server.bi.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 分类 目标设置表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface BiTargetCategorySettingMapper extends BaseMapper<BiTargetCategorySettingEntity> {

    /**
     * 分页
     * @param query
     * @param params
     * @return
     */
    IPage<BiTargetCategorySettingDTO.PagingViewDTO> paging(Page query, @Param("params")BiTargetYearDTO.PagingParamDTO params);
}
