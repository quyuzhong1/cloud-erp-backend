package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.BomInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * bom 信息表(BomInfo)表数据库访问层
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Mapper
public interface BomInfoMapper  extends BaseMapper<BomInfoEntity> {

    

}

