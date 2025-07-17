package com.erp.server.workflow.mapper;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 流程设置字段配置 Mapper 接口
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Mapper
public interface CfgProcessFieldMapMapper extends BaseMapper<CfgProcessFieldMapEntity> {

}
