package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.BomOperateLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * bom 操作记录日志表(BomOperateLog)表数据库访问层
 *
 * @author yl
 * @since 2023-01-09 11:45:23
 */
@Mapper
public interface BomOperateLogMapper  extends BaseMapper<BomOperateLogEntity> {

    

}

