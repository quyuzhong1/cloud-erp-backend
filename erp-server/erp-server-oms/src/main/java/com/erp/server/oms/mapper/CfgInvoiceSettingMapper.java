package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CfgInvoiceSettingDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
@Mapper
public interface CfgInvoiceSettingMapper extends BaseMapper<CfgInvoiceSettingEntity> {
    /**
     * @description: 根据条件分页查询（支持高级查询）
     * @param: query 分页参数
     * @param: params 查询参数
     * @author: hcg
     * @date: 2025/4/9 14:44
     * @param:
     * @return: IPage<CfgInvoiceSettingDTO.PagingViewDTO>
     **/
    IPage<CfgInvoiceSettingDTO.PagingViewDTO> paging(@Param("query") Page<CfgInvoiceSettingDTO.PagingViewDTO> query, @Param("params") CfgInvoiceSettingDTO.PagingParamDTO params);
}
