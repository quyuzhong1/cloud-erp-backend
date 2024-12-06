package com.erp.server.plm.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.MouldRefProductEntity;
import com.erp.server.plm.mapper.MouldRefProductMapper;
import com.erp.server.plm.service.MouldRefProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 关联下单产品 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
 */
@Slf4j
@Service
public class MouldRefProductServiceImpl extends SuperServiceImpl<MouldRefProductMapper, MouldRefProductEntity> implements MouldRefProductService {

}
