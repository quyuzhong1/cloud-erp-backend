package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CfgMaskWordDTO;
import com.erp.model.sys.entity.CfgMaskWordEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 脱敏词典表 Mapper
 *
 * @author cloud-erp
 */
@Mapper
public interface CfgMaskWordMapper extends BaseMapper<CfgMaskWordEntity> {

    /**
     * 分页查询
     */
    IPage<CfgMaskWordDTO.ListDTO> paging(Page<?> page, @Param("params") CfgMaskWordDTO.SearchParamDTO params);

    /**
     * 拉取所有"启用且未删除"的词典（缓存全量推送 / 冷启动用）
     */
    List<CfgMaskWordEntity> listAllAlive();
}
