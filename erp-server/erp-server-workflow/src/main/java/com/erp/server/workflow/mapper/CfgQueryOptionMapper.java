package com.erp.server.workflow.mapper;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 查询option配置表(数大臣单据字段) Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
@Mapper
public interface CfgQueryOptionMapper extends BaseMapper<CfgQueryOptionEntity> {

    List<CfgQueryOptionDTO.ListDTO> proDropDown(@Param("bussinessKey")String bussinessKey,@Param("useType") String useType);

    List<CfgQueryOptionEntity> getSystemfield(@Param("bussinessKey")String bussinessKey,@Param("useType")String useType);

    List<CfgQueryOptionEntity> listByMqParams(@Param("params") CfgQueryOptionDTO.MqParamsDTO mqParamsDTO);

    List<CfgQueryOptionDTO.ListDTO> proDropDownByMain(@Param("bussinessKey")String bussinessKey,@Param("useType")String useType);
}
