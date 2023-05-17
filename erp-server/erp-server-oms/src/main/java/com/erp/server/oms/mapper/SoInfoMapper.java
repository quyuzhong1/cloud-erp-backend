package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售订单信息 Mapper 接口
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Mapper
public interface SoInfoMapper extends BaseMapper<SoInfoEntity> {

    IPage<SoInfoDTO.PagingViewDTO> paging(Page query, @Param("params") SoInfoDTO.PagingParamDTO params,@Param("detailIdList") List<String> paramDetailIds );
}
