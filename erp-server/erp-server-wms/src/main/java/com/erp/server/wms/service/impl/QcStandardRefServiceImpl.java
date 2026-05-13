package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.entity.QcResultEntity;
import com.erp.model.wms.entity.QcStandardRefEntity;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcStandardRefMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcStandardImageRefService;
import com.erp.server.wms.service.QcStandardRefService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2026-03-23
 */
@Slf4j
@Service
public class QcStandardRefServiceImpl extends SuperServiceImpl<QcStandardRefMapper, QcStandardRefEntity> implements QcStandardRefService {


}
