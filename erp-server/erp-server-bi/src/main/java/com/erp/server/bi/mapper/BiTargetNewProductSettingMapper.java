package com.erp.server.bi.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetNewProductSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 新品目标设置表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface BiTargetNewProductSettingMapper extends BaseMapper<BiTargetNewProductSettingEntity> {

    IPage<BiTargetNewProductSettingDTO.PagingViewDTO> paging(Page query, @Param("params")BiTargetYearDTO.PagingParamDTO params);

    /**
     * 根据年月维度查询目标
     * @Author Luo_WG
     * @Date 2023/9/15 16:54
     * @param year 年
     * @param month 月
     * @param metrics 维度
     * @return com.erp.model.bi.entity.BiTargetNewProductSettingEntity
     **/
    BiTargetNewProductSettingEntity getTargetByParams(@Param("year") Integer year, @Param("month") Integer month, @Param("metrics") String metrics);
}
