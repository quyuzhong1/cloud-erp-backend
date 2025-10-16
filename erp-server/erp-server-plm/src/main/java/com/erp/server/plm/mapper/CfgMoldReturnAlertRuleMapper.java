package com.erp.server.plm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;
import com.erp.model.plm.entity.CfgMoldReturnAlertRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * <p>
 * 模具返还策略 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-10-15
 */
@Mapper
public interface CfgMoldReturnAlertRuleMapper extends BaseMapper<CfgMoldReturnAlertRuleEntity> {

    List<CfgMoldReturnAlertRuleDTO.TabListDTO> tabList(@Param("params")  CfgMoldReturnAlertRuleDTO.PagingParamDTO params);

    IPage<CfgMoldReturnAlertRuleDTO.ListDTO> paging(Page query,@Param("params") CfgMoldReturnAlertRuleDTO.PagingParamDTO params);
}
