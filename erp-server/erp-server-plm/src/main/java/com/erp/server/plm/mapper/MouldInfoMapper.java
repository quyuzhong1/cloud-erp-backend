package com.erp.server.plm.mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.entity.MouldInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 模具主表 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Mapper
public interface MouldInfoMapper extends BaseMapper<MouldInfoEntity> {

    Page<MouldInfoDTO.PagingViewDTO> paging(@Param("page") Page<MouldInfoDTO.PagingViewDTO> page, @Param("params") MouldInfoDTO.PagingParamDTO params);
}
