package com.erp.server.wms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.CartonDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.erp.model.wms.entity.WmsCartonSpecEntity;
import com.erp.server.wms.mapper.WmsCartonDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.erp.server.wms.service.WmsCartonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货单箱子信息表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsCartonDetailServiceImpl extends SuperServiceImpl<WmsCartonDetailMapper, WmsCartonDetailEntity> implements WmsCartonDetailService {
    @Autowired
    private WmsCartonService wmsCartonService;
    @Autowired
    private OperateLogService operateLogService;

    @Override
    public List<WmsCartonDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public List<WmsCartonDetailEntity> listByTaskIds(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)){
            return Collections.emptyList();
        }
        return baseMapper.listByTaskIds(taskIds);
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonDetailEntity::getMainId, cartonIds).remove();
    }

    @Override
    public List<WmsCartonSpecDTO.PackingItemDTO> boxInfoBySourceIds(List<String> sourceId) {
        return this.baseMapper.boxInfoBySourceId(sourceId);
    }


    @Override
    public void add(WmsCartonSpecDTO.AddDTO addDTO, WmsCartonEntity wmsCartonEntity, WmsCartonSpecEntity wmsCartonSpecEntity) {
        //校验必填
        for (WmsCartonDetailDTO.AddDTO detail : addDTO.getDetailList()) {
            if (StringUtils.isBlank(detail.getSkuId()) || StringUtils.isBlank(detail.getSkuNo())) {
                throw new ServiceException(ApiError.PACKING_SKU_IS_NOT_NULL, wmsCartonSpecEntity.getBoxSpecNo());
            }
            if (detail.getPackQty() == null || detail.getPackQty() <= 0) {
                throw new ServiceException(ApiError.PACKING_SKU_PACK_QTY_IS_NOT_NULL, wmsCartonSpecEntity.getBoxSpecNo(), detail.getSkuNo());
            }
            if (wmsCartonSpecEntity.getBoxQty() == null || wmsCartonSpecEntity.getBoxQty() <= 0) {
                throw new ServiceException(ApiError.PACKING_SKU_BOX_QTY_IS_NOT_NULL, wmsCartonSpecEntity.getBoxSpecNo());
            }
        }

        List<WmsCartonDetailEntity> detailEntityList = BeanMapper.copyList(addDTO.getDetailList(), WmsCartonDetailEntity.class);
        // 数据处理
        handleData(detailEntityList, wmsCartonEntity.getId());
        if(CollectionUtils.isNotEmpty(detailEntityList)){
            log.info("开始新增发货单箱子信息单");
            boolean save = super.saveOrUpdateBatch(detailEntityList);
            if(!save) {
                throw new ServiceException("发货单箱子信息单保存失败");
            }
            String msg = "【"+addDTO.getContent() + "】新增装箱明细【"+wmsCartonEntity.getBoxNo()+"】【%s】";
            List<Pair<String, String>> addPairList = detailEntityList.stream().map(obj -> new Pair<>(wmsCartonEntity.getPackingTaskId(), obj.getSkuNo() + "*"+ obj.getPackQty())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.CARTON_DETAIL.getCode(), addPairList, addDTO.getOperation());
        }
    }

    @Override
    public List<WmsCartonDetailDTO.BoxDTO> listCartonDetailByMainIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)){
            return Collections.emptyList();
        }
        return baseMapper.listCartonDetailByMainIds(cartonIds);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<WmsCartonDetailEntity> detailEntityList, String mainId) {
        for (WmsCartonDetailEntity wmsCartonDetailEntity : detailEntityList) {
            wmsCartonDetailEntity.setMainId(mainId);
            if (StringUtils.isBlank(wmsCartonDetailEntity.getWeightUnit())){
                wmsCartonDetailEntity.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
            }
        }
    }

    /**
     * 保存箱子明细信息
     * @Author Luo_WG
     * @Date 2023/11/28 16:36
     * @param boxQty 箱数
     * @param detailEntityList 包装信息
     * @param cartonId 箱规id
     * @return void
     **/
    private void firstMileCartonBillSave(Integer boxQty, List<WmsCartonDetailEntity> detailEntityList, String cartonId, String sourceId) {
        List<WmsCartonEntity> firstMileCartonBillEntities = wmsCartonService.listByTaskIds(Arrays.asList(sourceId));
        Integer maxBoxNo = 0;
        if (CollectionUtils.isNotEmpty(firstMileCartonBillEntities)) {
            maxBoxNo = firstMileCartonBillEntities.stream().max(Comparator.comparingInt(WmsCartonEntity::getBoxNo)).map(WmsCartonEntity::getBoxNo).get();
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < detailEntityList.size(); i++) {
            sb.append(detailEntityList.get(i).getSkuNo());
            sb.append(StringPool.ASTERISK);
            sb.append(detailEntityList.get(i).getPackQty());
            sb.append(StringPool.SEMICOLON);
        }

        for (Integer i = maxBoxNo+1; i <= maxBoxNo+boxQty; i++) {
            CartonDTO.AddDTO billAdd = new CartonDTO.AddDTO();
            billAdd.setBoxDesc(sb.toString());
            billAdd.setBoxNo(String.valueOf(i));
//            billAdd.setCartonId(cartonId);
            billAdd.setSourceId(sourceId);
            wmsCartonService.add(billAdd);
        }
    }
}
