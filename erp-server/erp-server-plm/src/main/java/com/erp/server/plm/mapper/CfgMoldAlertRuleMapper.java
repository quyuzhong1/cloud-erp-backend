package com.erp.server.plm.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.CfgMoldAlertRuleDTO;
import com.erp.model.plm.entity.CfgMoldAlertRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 模具预警策略 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2025-10-20
 */
@Mapper
public interface CfgMoldAlertRuleMapper extends BaseMapper<CfgMoldAlertRuleEntity> {

    List<CfgMoldAlertRuleDTO.TabListDTO> tabList(@Param("params")  CfgMoldAlertRuleDTO.PagingParamDTO params);

    IPage<CfgMoldAlertRuleDTO.ListDTO> paging(Page query, @Param("params")  CfgMoldAlertRuleDTO. PagingParamDTO params);

    List<CfgMoldAlertRuleDTO.ListDTO> listAll( @Param("ids")List<String> ids , @Param("today") LocalDate today, @Param("oneMonthLater")  LocalDate oneMonthLater);
}
