package com.erp.server.oms.mapper;

import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 发货通知单主表明细表 Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoReturnDetailMapper extends BaseMapper<SoReturnDetailEntity> {

    List<SoReturnDetailEntity> listDetailBySourceId(@Param("ids") List<String> sourceIds);

    List<SoReturnDetailEntity> listDetailByIds(@Param("ids") List<String> ids);

    List<SoDetailDTO.AddDetailView> listAddDetailView(@Param("id") String id);

    SoDetailDTO.AddDetailView listAddDetailViewById(@Param("id") String id);
}
