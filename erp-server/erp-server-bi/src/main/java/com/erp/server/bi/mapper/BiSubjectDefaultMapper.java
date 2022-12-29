package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.entity.BiSubjectDefaultEntity;
import com.erp.model.bi.entity.BiSubjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Classname BiSubjectDefaultMapper
 * @Description TODO
 * @Date 2022-12-09 14:17
 * @Created by yl
 */
@Mapper
public interface BiSubjectDefaultMapper  extends BaseMapper<BiSubjectDefaultEntity> {
    BiSubjectEntity getDefaultSubject(@Param("userId") String userId);
}
