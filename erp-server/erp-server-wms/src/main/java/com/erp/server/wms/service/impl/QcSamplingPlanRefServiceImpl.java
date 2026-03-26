package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.entity.*;
import com.erp.server.wms.mapper.QcSamplingPlanRefMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcSamplingPlanRefService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.QcStandardImageRefService;
import com.erp.server.wms.service.QcStandardRefService;
import io.seata.common.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2026-03-25
 */
@Slf4j
@Service
public class QcSamplingPlanRefServiceImpl extends SuperServiceImpl<QcSamplingPlanRefMapper, QcSamplingPlanRefEntity> implements QcSamplingPlanRefService {

    @Resource
    private QcStandardRefService qcStandardRefService;

    @Resource
    private QcStandardImageRefService qcStandardImageRefService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    @Transactional
    public void add(String billId, QcNoticeDTO.QcStandardAddDTO qcStandardAddDTO) {
        QcSamplingPlanRefEntity qcSamplingPlanRefEntity = new QcSamplingPlanRefEntity();
        BeanUtils.copyProperties(qcStandardAddDTO,qcSamplingPlanRefEntity);
        QcSamplingPlanRefEntity oldEntity = null;

        if (StringUtils.isBlank(qcStandardAddDTO.getId())) {
            UpdateWrapper<QcSamplingPlanRefEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("main_id", qcStandardAddDTO.getId());
            this.saveOrUpdate(qcSamplingPlanRefEntity,updateWrapper);

            //质检项目
            List<QcStandardRefEntity> qcStandardRefs = new ArrayList<>();
            if (!qcStandardAddDTO.getQcInspectItemAddDTOList().isEmpty()) {
                for (QcNoticeDTO.QcInspectItemAddDTO qcInspectItemAddDTO : qcStandardAddDTO.getQcInspectItemAddDTOList()) {
                    QcStandardRefEntity qcStandardRef = new QcStandardRefEntity();
                    BeanUtils.copyProperties(qcInspectItemAddDTO,qcStandardRef);
                    qcStandardRefs.add(qcStandardRef);
                }
                qcStandardRefService.saveBatch(qcStandardRefs);
            }

            //参考图片
            List<QcStandardImageRefEntity> qcStandardImageRefs = new ArrayList<>();
            if (!qcStandardAddDTO.getQcImageAddDTOList().isEmpty()) {
                for (QcNoticeDTO.QcImageAddDTO qcImageAddDTO : qcStandardAddDTO.getQcImageAddDTOList()) {
                    QcStandardImageRefEntity qcStandardImageRefEntity = new QcStandardImageRefEntity();
                    BeanUtils.copyProperties(qcImageAddDTO,qcStandardImageRefEntity);
                    qcStandardImageRefs.add(qcStandardImageRefEntity);
                }
                qcStandardImageRefService.saveBatch(qcStandardImageRefs);
            }
        }

        //操作日志
        operateLogService.addModuleOperateLogByObj(oldEntity, qcSamplingPlanRefEntity, ModuleTypeEnum.QC_ORDER.getCode(), billId, "", "编辑了质检单的质检信息");
    }
}
