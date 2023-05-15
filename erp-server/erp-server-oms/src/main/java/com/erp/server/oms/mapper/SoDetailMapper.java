package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单详情 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoDetailMapper extends BaseMapper<SoDetailEntity> {

    /**
     * 添加详情按钮-列表查询
     * @Author Luo_WG
     * @Date 2023/5/15 16:05
     * @param id id
     * @return java.util.List<com.erp.model.oms.dto.SoDetailDTO.AddDetailView>
     **/
    List<SoDetailDTO.AddDetailView> listAddDetailView(@Param("id") String id);
}
