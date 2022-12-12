package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.entity.BiDictEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * bi系统字典表(BiDict)表数据库访问层
 *
 * @author yl
 * @since 2022-12-08 14:24:02
 */
@Mapper
public interface BiDictMapper extends BaseMapper<BiDictEntity> {

    

}

