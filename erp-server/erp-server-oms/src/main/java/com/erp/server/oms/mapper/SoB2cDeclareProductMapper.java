package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * B2C销售订单申报产品信息表 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2024-05-09
 */
@Mapper
public interface SoB2cDeclareProductMapper extends BaseMapper<SoB2cDeclareProductEntity> {

    List<SoB2cDeclareProductDTO.ViewDTO> listViewBySoIds(@Param("ids") List<String> ids);
    Page<SoB2cDeclareProductDTO.ViewDTO> listViewBySoIds(@Param("page") Page<SoB2cDeclareProductDTO.ViewDTO> page, @Param("ids") List<String> ids);
}
