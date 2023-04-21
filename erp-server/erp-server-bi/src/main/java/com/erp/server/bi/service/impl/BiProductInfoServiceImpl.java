package com.erp.server.bi.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.erp.model.bi.entity.BiProductInfoEntity;
import com.erp.server.bi.mapper.BiProductInfoMapper;
import com.erp.server.bi.service.BiProductInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 产品信息表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-21
 */
@Service
public class BiProductInfoServiceImpl extends SuperServiceImpl<BiProductInfoMapper, BiProductInfoEntity> implements BiProductInfoService {

}
