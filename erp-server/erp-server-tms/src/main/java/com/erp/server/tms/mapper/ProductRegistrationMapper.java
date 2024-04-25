package com.erp.server.tms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 产品备案表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-03-14
 */
@Mapper
public interface ProductRegistrationMapper extends BaseMapper<ProductRegistrationEntity> {

    List<ProductRegistrationDTO.TabListDTO> tabList();

    IPage<ProductRegistrationDTO.PagingVO> paging(Page query, @Param("params") ProductRegistrationDTO.PagingParamDTO params);
}
