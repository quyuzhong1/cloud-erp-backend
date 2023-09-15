package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
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
    /**
     * @description: 根据指标查询品类目标值
     * @author Will
     * @date: 2023/9/15 11:36
     * @param dto
     * @return List<BiTargetCategorySettingEntity>
     */
    List<BiTargetCategorySettingEntity> listTargetFinish(@Param("params")TargetFinishDTO.ParamDTO dto);
}
