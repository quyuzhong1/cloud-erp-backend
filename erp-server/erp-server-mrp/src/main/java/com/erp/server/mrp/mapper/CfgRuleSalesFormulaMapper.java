package com.erp.server.mrp.mapper;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 销量公式（规则设置） Mapper 接口
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Mapper
public interface CfgRuleSalesFormulaMapper extends BaseMapper<CfgRuleSalesFormulaEntity> {
    /**
     * 根据关联id集合查询
     * @author will
     * @date 2024/9/6 16:31
     * @param refIdList
     * @return List<SalesFormulaExportDTO>
     */
    List<CfgRuleSalesFormulaDTO.SalesFormulaExportDTO> listFormulaByRefIdList(@Param("refIdList") List<String> refIdList);
}
