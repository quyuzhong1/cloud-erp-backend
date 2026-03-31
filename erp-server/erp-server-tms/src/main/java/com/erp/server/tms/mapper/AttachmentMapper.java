package com.erp.server.tms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.tms.entity.TmsAttachmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 公共附件表 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<TmsAttachmentEntity> {

}
