package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.dto.CfgNoticeDTO;
import com.erp.model.mrp.entity.CfgNoticeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 通知配置表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2025-02-13
 */
@Mapper
public interface CfgNoticeMapper extends BaseMapper<CfgNoticeEntity> {
    /**
     * 分页查询
     * @Auther will
     * @Date 2025/2/13 14:54
     * @param params
     * @return CfgNoticeDTO.ListDTO
     */
    IPage<CfgNoticeDTO.ListDTO> paging(Page query, @Param("params") CfgNoticeDTO.SearchParamDTO params);
}
