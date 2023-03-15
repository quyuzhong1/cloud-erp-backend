package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.scm.entity.ScmPurchaseApplicationRefOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 采购申请单和采购订单关联表 Mapper 接口
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Mapper
public interface ScmPurchaseApplicationRefOrderMapper extends BaseMapper<ScmPurchaseApplicationRefOrderEntity> {

}
