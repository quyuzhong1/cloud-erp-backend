package com.erp.server.srm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.rpc.wms.feign.PurchaseOrderFeign;
import com.erp.server.srm.mapper.DeliveryOrderDetailMapper;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;

    @Transactional(rollbackFor = Exception.class)
    public void add(List<DeliveryOrderDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<DeliveryOrderDetailEntity> list = BeanMapperUtils.copyList(DeliveryOrderDetailEntity.class, detailList);

        //处理明细数据
        handleData(list,mainId);

        this.saveBatch(list);
    }

    @Override
    public List<DeliveryOrderDetailEntity> listByMainId(String mainId) {
        return this.lambdaQuery().eq(DeliveryOrderDetailEntity::getMainId, mainId).list();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<DeliveryOrderDetailDTO.UpdateDTO> updateDTOList,String mainId) {
        if(CollectionUtils.isEmpty(updateDTOList)){
            return true;
        }
        List<DeliveryOrderDetailEntity> oldDetailList = this.listByMainId(mainId);
        Set<String> existDetailIds = updateDTOList.stream().map(DeliveryOrderDetailDTO.UpdateDTO::getDetailId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        //处理删除的明细
        List<DeliveryOrderDetailEntity> needDeleteDetailList = oldDetailList.stream().filter(v->!existDetailIds.contains(v.getId())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(needDeleteDetailList)){
            if(!this.removeByIds(needDeleteDetailList.stream().map(BaseEntity::getId).collect(Collectors.toList()))){
                throw new ServiceException("送货单明细删除失败");
            }
        }
        //处理修改的明细
        List<DeliveryOrderDetailEntity> needUpdateDetailList = oldDetailList.stream().filter(v->existDetailIds.contains(v.getId())).collect(Collectors.toList());
        Map<String,DeliveryOrderDetailDTO.UpdateDTO> updateDTOMap = updateDTOList.stream().collect(Collectors.toMap(DeliveryOrderDetailDTO.UpdateDTO::getDetailId, Function.identity()));
        for(DeliveryOrderDetailEntity deliveryOrderDetailEntity : needUpdateDetailList){
            DeliveryOrderDetailDTO.UpdateDTO updateDTO = updateDTOMap.get(deliveryOrderDetailEntity.getId());
            BeanUtil.copyProperties(updateDTO,deliveryOrderDetailEntity);

        }
        if(CollectionUtils.isNotEmpty(needUpdateDetailList)){
            if(!this.updateBatchById(needUpdateDetailList)){
                throw new ServiceException("送货单明细更新失败");
            }
        }


        //处理新增的明细
        List<DeliveryOrderDetailDTO.UpdateDTO> needAddDTOList = updateDTOList.stream().filter(v->StringUtils.isBlank(v.getDetailId())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(needAddDTOList)){
            return true;
        }
        List<DeliveryOrderDetailEntity> list = BeanMapperUtils.copyList(DeliveryOrderDetailEntity.class, needAddDTOList);
        //处理明细数据
        handleData(list,mainId);
        if(! this.saveBatch(list)){
            throw new ServiceException("送货单明细新增失败");
        }
        return true;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(List<DeliveryOrderDetailEntity> detailList, String mainId) {
        detailList.forEach(v->v.setMainId(mainId));
        //添加操作日志
        if (CollectionUtils.isNotEmpty(detailList)) {
            List<Pair<String, String>> addPairList = detailList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.OVERSEAS_DELIVERY_PLAN.getCode(), addPairList, "编辑操作");
        }
    }
}
