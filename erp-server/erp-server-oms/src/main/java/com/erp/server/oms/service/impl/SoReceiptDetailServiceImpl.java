package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.AttachDTO;
import com.erp.model.oms.entity.SoReceiptDetailEntity;
import com.erp.model.oms.entity.SoReceiptEntity;
import com.erp.server.oms.mapper.SoReceiptDetailMapper;
import com.erp.server.oms.service.OmsAttachmentService;
import com.erp.server.oms.service.SoReceiptDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoReceiptDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 收款单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
@Slf4j
@Service
public class SoReceiptDetailServiceImpl extends SuperServiceImpl<SoReceiptDetailMapper, SoReceiptDetailEntity> implements SoReceiptDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private OmsAttachmentService attachmentService;
    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoReceiptDetailDTO.UpdateDTO addOrUpdateDTO) {
        SoReceiptDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "收款单明细"));
        SoReceiptDetailEntity soReceiptDetailEntity =  BeanMapperUtils.map(SoReceiptDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(soReceiptDetailEntity);
        log.info("编辑 开始修改收款单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(soReceiptDetailEntity);
        if(!save) {
            throw new ServiceException("收款单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录收款单明细日志数据，id：【{}】", soReceiptDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soReceiptDetailEntity.getId(), "收款单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soReceiptDetailEntity, null, soReceiptDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addDetail(SoReceiptEntity soReceiptEntity, List<SoReceiptDetailDTO.AddDTO> detailList) {

        //付款流水号不能重复
        Set<String> paymentNoSet = new HashSet<>();
        for (SoReceiptDetailDTO.AddDTO dto : detailList) {
            if(!paymentNoSet.add(dto.getPaymentNo())){
                throw new ServiceException("付款流水号不能重复");
            }
        }
        //销售单号不能重复
        Set<String> soCodeSet = new HashSet<>();
        for (SoReceiptDetailDTO.AddDTO dto : detailList) {
            if(!soCodeSet.add(dto.getSoCode())){
                throw new ServiceException("销售单号不能重复");
            }
        }

        detailList.forEach(v->v.setMainId(soReceiptEntity.getId()));
        List<SoReceiptDetailEntity> saveList = BeanMapper.copyList(detailList, SoReceiptDetailEntity.class);
        this.saveBatch(saveList);
        List<AttachDTO> allAttachDTOS = new ArrayList<>();
        for (SoReceiptDetailEntity soReceiptDetailEntity : saveList) {
            SoReceiptDetailDTO.AddDTO addDTO = detailList.stream().filter(v -> v.getPaymentNo().equals(soReceiptDetailEntity.getPaymentNo())).findFirst().orElse(new SoReceiptDetailDTO.AddDTO());
            List<AttachDTO> attachDTOS = addDTO.getAttachmentList();
            attachDTOS.forEach(v->v.setBusinessId(soReceiptDetailEntity.getId()));
            allAttachDTOS.addAll(attachDTOS);
        }
        // 保存附件
        TableName tableName = SoReceiptDetailEntity.class.getDeclaredAnnotation(TableName.class);
        if(CollectionUtils.isNotEmpty(allAttachDTOS)){
            attachmentService.batchSaveOrUpdate(allAttachDTOS, tableName.value());
        }
        return true;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoReceiptDetailEntity soReceiptDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
