package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.ThirdLogisticsDTO;
import com.erp.model.dmp.entity.ThirdLogisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 三方渠道表 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-12-11
 */
@Mapper
public interface ThirdLogisticsMapper extends BaseMapper<ThirdLogisticsEntity> {

    IPage<ThirdLogisticsDTO.PageSelectDTO> pagingSelect(Page query, @Param("params")ThirdLogisticsDTO.SelectDTO params);
}
