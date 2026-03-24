package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.wms.entity.QcStandardDetailEntity;
import com.erp.server.wms.mapper.QcStandardDetailMapper;
import com.erp.server.wms.service.QcStandardDetailService;
import org.springframework.stereotype.Service;

/**
 * 质检项目明细表 Service 实现类
 *
 * @author jack
 * @since 2026-03-22
 */
@Service
public class QcStandardDetailServiceImpl extends ServiceImpl<QcStandardDetailMapper, QcStandardDetailEntity> implements QcStandardDetailService {
}
