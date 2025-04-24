package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiTargetStaffSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.TargetFinishDTO;
import com.erp.model.bi.entity.BiTargetStaffSettingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 人员目标设置表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Mapper
public interface BiTargetStaffSettingMapper extends BaseMapper<BiTargetStaffSettingEntity> {

    /**
     * 根据年会获取到对应值
     * @param year
     * @return
     */
    List<BiTargetStaffSettingDTO.ListDetailDTO> listByYear(@Param("year") Integer year);

    IPage<BiTargetStaffSettingDTO.PagingViewDTO> paging(Page<Object> query, @Param("params")BiTargetYearDTO.PagingParamDTO params,@Param("multiplyNum") BigDecimal multiplyNum);
    /**
     * @description: 根据指标查询人员目标值
     * @author Will
     * @date: 2023/9/15 12:13
     * @param dto
     * @return List<TargetFinishDTO.ViewDTO>
     */
    List<TargetFinishDTO.ViewDTO> listUserTargetFinish(@Param("params")TargetFinishDTO.ParamDTO dto);
    /**
     * @description: 根据指标查询部门目标值
     * @author Will
     * @date: 2023/9/15 12:13
     * @param dto
     * @return List<TargetFinishDTO.ViewDTO>
     */
    List<TargetFinishDTO.ViewDTO> listDeptTargetFinish(@Param("params")TargetFinishDTO.ParamDTO dto);

    /**
     * 分页统计
     * @param dto
     * @return
     */
    BiTargetYearDTO.PagingTotalDTO pagingTotal(@Param("params")BiTargetYearDTO.PagingParamDTO dto,@Param("multiplyNum") BigDecimal multiplyNum);
}
