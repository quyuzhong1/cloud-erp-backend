package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetShopSettingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 店铺目标设置表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface BiTargetShopSettingMapper extends BaseMapper<BiTargetShopSettingEntity> {

    /**
     * 分页
     * @param query
     * @param params
     * @return
     */
    IPage<BiTargetShopSettingDTO.PagingViewDTO> paging(Page query,@Param("params") BiTargetYearDTO.PagingParamDTO params);

    List<TargetFinishDTO.ViewDTO> listTargetFinish(@Param("params")TargetFinishDTO.ParamDTO dto);

    /**
     * 根据年份获取到对应值
     * @param year
     * @return
     */
    List<BiTargetShopSettingDTO.ListDetailDTO> listByYear(@Param("year") Integer year);
}
