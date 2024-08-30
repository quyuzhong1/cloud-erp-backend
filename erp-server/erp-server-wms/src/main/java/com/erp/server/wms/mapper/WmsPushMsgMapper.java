package com.erp.server.wms.mapper;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
public interface WmsPushMsgMapper extends BaseMapper<WmsPushMsgEntity> {

}
