package com.erp.server.srm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.srm.entity.SrmPushMsgEntity;
import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 本地推送消息表 Mapper 接口
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
 */
@Mapper
public interface SrmPushMsgMapper extends BaseMapper<SrmPushMsgEntity> {

}
