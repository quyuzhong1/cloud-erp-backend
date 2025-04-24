package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetCategorySettingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


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
    IPage<BiTargetCategorySettingDTO.PagingViewDTO> paging(Page<Object> query, @Param("params")BiTargetYearDTO.PagingParamDTO params,@Param("multiplyNum") BigDecimal multiplyNum);
    /**
     * @description: 根据指标查询品类目标值
     * @author Will
     * @date: 2023/9/15 11:36
     * @param dto
     * @return List<TargetFinishDTO.ViewDTO>
     */
    List<TargetFinishDTO.ViewDTO> listTargetFinish(@Param("params")TargetFinishDTO.ParamDTO dto);

    /**
     * 根据部门和年查询数据
     * @param year
     * @param deptId
     * @return
     */
    List<BiTargetCategorySettingDTO.ListDetailDTO> listByYearAndDept(@Param("year") Integer year, @Param("deptId")String deptId);

    /**
     * 分页统计
     * @param dto
     * @return
     */
    BiTargetYearDTO.PagingTotalDTO pagingTotal(@Param("params")BiTargetYearDTO.PagingParamDTO dto,@Param("multiplyNum") BigDecimal multiplyNum);
}
