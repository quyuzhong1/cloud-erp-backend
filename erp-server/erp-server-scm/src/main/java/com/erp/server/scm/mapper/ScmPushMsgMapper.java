package com.erp.server.scm.mapper;
import com.erp.model.scm.entity.ScmPushMsgEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 本地推送消息表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-08-29
 */
@Mapper
public interface ScmPushMsgMapper extends BaseMapper<ScmPushMsgEntity> {

}
