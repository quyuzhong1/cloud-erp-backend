package com.erp.server.bi.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.entity.BiTargetNewProductSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


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

    IPage<BiTargetNewProductSettingDTO.PagingViewDTO> paging(Page<Object> query, @Param("params")BiTargetYearDTO.PagingParamDTO params,@Param("multiplyNum") BigDecimal multiplyNum);

    /**
     * 查询部门新品目标
     * @Author Luo_WG
     * @Date 2023/9/18 12:06
     * @param params
     * @return java.util.List<com.erp.model.bi.dto.BiTargetNewProductSettingDTO.DeptTargetDTO>
     **/
    List<BiTargetNewProductSettingDTO.DeptTargetDTO> listDeptTarget(@Param("params") BiTargetNewProductSettingDTO.TargetParamDTO params);

    /**
     * 查询用户新品目标
     * @Author Luo_WG
     * @Date 2023/9/18 12:06
     * @param params
     * @return java.util.List<com.erp.model.bi.dto.BiTargetNewProductSettingDTO.UserTargetDTO>
     **/
    List<BiTargetNewProductSettingDTO.UserTargetDTO> listUserTarget(@Param("params") BiTargetNewProductSettingDTO.TargetParamDTO params);
    /**
     * 分页统计
     * @param dto
     * @return
     */
    BiTargetYearDTO.PagingTotalDTO pagingTotal(@Param("params") BiTargetYearDTO.PagingParamDTO dto,@Param("multiplyNum") BigDecimal multiplyNum);

    /**
     * 根据年和部门查询数据
     * @param year
     * @param deptId
     * @return
     */
    List<BiTargetNewProductSettingDTO.ListDetailDTO> listByYearAndDept(@Param("year") Integer year, @Param("deptId")String deptId);
}
