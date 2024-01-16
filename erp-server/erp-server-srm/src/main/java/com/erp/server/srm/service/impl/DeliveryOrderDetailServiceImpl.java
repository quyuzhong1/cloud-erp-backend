package com.erp.server.srm.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.server.srm.mapper.DeliveryOrderDetailMapper;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
/**
 * <p>
 * 送货单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@Service
public class DeliveryOrderDetailServiceImpl extends SuperServiceImpl<DeliveryOrderDetailMapper, DeliveryOrderDetailEntity> implements DeliveryOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Transactional(rollbackFor = Exception.class)
    public void add(List<DeliveryOrderDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<DeliveryOrderDetailEntity> list = BeanMapperUtils.copyList(DeliveryOrderDetailEntity.class, detailList);

        //处理明细数据
        handleData(list,mainId,Boolean.FALSE);

        this.saveBatch(list);
    }

    @Override
    public List<DeliveryOrderDetailEntity> listByMainId(String mainId) {
        return this.lambdaQuery().eq(DeliveryOrderDetailEntity::getMainId, mainId).list();
    }

//    /**
//    * 修改
//    */
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public Boolean update(DeliveryOrderDetailDTO.UpdateDTO updateDTO) {
//        DeliveryOrderDetailEntity old = super.getById(updateDTO.getId());
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "送货单明细"));
//        DeliveryOrderDetailEntity deliveryOrderDetailEntity =  BeanMapperUtils.map(DeliveryOrderDetailEntity.class, updateDTO);
//
//        // 数据处理
//        handleData(deliveryOrderDetailEntity,true);
//        log.info("编辑 开始修改送货单明细数据，id：【{}】", old.getId());
//        boolean save = super.updateById(deliveryOrderDetailEntity);
//        if(!save) {
//            throw new ServiceException("送货单明细保存失败");
//        }
//
//        // 记录主单操作日志
//        log.info("编辑 开始记录送货单明细日志数据，id：【{}】", deliveryOrderDetailEntity.getId());
//        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), deliveryOrderDetailEntity.getId(), "送货单明细");
//        operateLogService.addModuleOperateLogByObj(old, deliveryOrderDetailEntity, null, deliveryOrderDetailEntity.getId(), msg);
//        return Boolean.TRUE;
//    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<DeliveryOrderDetailEntity> detailList, String mainId,Boolean isUpdate) {
        detailList.forEach(v->v.setMainId(mainId));
        //添加操作日志
        if (CollectionUtils.isNotEmpty(detailList) && isUpdate) {
            List<Pair<String, String>> addPairList = detailList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), addPairList, "编辑操作");
        }
    }
}
