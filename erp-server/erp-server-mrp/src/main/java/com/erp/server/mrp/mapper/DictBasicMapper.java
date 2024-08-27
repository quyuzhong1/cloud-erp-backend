package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.entity.DictBasicEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface DictBasicMapper extends BaseMapper<DictBasicEntity> {
    /**
     * 查询规则设置初始数据
     * @author will
     * @date 2024/8/26 11:37
     * @param platformType
     * @param isDisableOverseas
     * @return List<ViewDTO>
     */
    List<CfgRuleCommonDTO.ViewDTO> listRuleCommonTree(@Param("platformType") String platformType,@Param("isDisableOverseas") Boolean isDisableOverseas);
}
