package com.erp.server.oms.mapper;
import com.erp.model.oms.entity.KolSocialMediaEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.KolSocialMediaDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 达人社媒数据表 Mapper 接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Mapper
public interface KolSocialMediaMapper extends BaseMapper<KolSocialMediaEntity> {

    /**
     * 分页查询
     * @param page
     * @param params
     * @return
     */
    IPage<KolSocialMediaDTO.ListDTO> paging(Page<KolSocialMediaDTO.ListDTO> page, @Param("params") KolSocialMediaDTO.ParamDTO params);

}
