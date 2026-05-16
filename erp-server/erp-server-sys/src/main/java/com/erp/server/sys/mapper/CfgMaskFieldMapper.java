package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CfgMaskFieldDTO;
import com.erp.model.sys.entity.CfgMaskFieldEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字段脱敏配置表 Mapper
 *
 * @author cloud-erp
 */
@Mapper
public interface CfgMaskFieldMapper extends BaseMapper<CfgMaskFieldEntity> {

    /**
     * 分页查询
     */
    IPage<CfgMaskFieldDTO.ListDTO> paging(Page<?> page, @Param("params") CfgMaskFieldDTO.SearchParamDTO params);

    /**
     * 拉取所有"启用且未删除"的配置（缓存全量推送 / 冷启动用）
     */
    List<CfgMaskFieldEntity> listAllAlive();
}
