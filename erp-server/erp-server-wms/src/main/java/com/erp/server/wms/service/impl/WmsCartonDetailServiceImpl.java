package com.erp.server.wms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.WmsCartonBillEntity;
import com.erp.model.wms.entity.WmsCartonDetailEntity;
import com.erp.server.wms.mapper.WmsCartonDetailMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.WmsCartonDetailService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WmsCartonBillService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private WmsCartonBillService wmsCartonBillService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WmsCartonDTO.AddDTO addDTO, String cartonId, String sourceId, String sourceType) {
        //校验必填
        for (WmsCartonDetailDTO.AddDTO detail : addDTO.getDetailList()) {
            if (StringUtils.isBlank(detail.getSkuId()) || StringUtils.isBlank(detail.getSkuNo())) {
                throw new ServiceException(ApiError.PACKING_SKU_IS_NOT_NULL, addDTO.getBoxSpecNo());
            }
            if (detail.getPackQty() == null || detail.getPackQty() <= 0) {
                throw new ServiceException(ApiError.PACKING_SKU_PACK_QTY_IS_NOT_NULL, addDTO.getBoxSpecNo(), detail.getSkuNo());
            }
            if (addDTO.getBoxQty() == null || addDTO.getBoxQty() <= 0) {
                throw new ServiceException(ApiError.PACKING_SKU_BOX_QTY_IS_NOT_NULL, addDTO.getBoxSpecNo());
            }
        }

        List<WmsCartonDetailEntity> detailEntityList = BeanMapper.copyList(addDTO.getDetailList(), WmsCartonDetailEntity.class);
        // 数据处理
        handleData(detailEntityList, cartonId, sourceId, sourceType);

        log.info("开始新增发货单箱子信息单");
        boolean save = super.saveOrUpdateBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
        //箱子信息明细
        this.firstMileCartonBillSave(addDTO.getBoxQty(), detailEntityList, cartonId, sourceId);
    }

    @Override
    public List<WmsCartonDetailEntity> listByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonDetailEntity::getCartonId, cartonIds).list();
    }

    @Override
    public List<WmsCartonDetailEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonDetailEntity::getSourceId, sourceIds).list();
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonDetailEntity::getCartonId, cartonIds).remove();
    }

    @Override
    public Boolean deleteBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonDetailEntity::getSourceId, sourceIds).remove();
    }

    @Override
    public List<WmsCartonDTO.PackingItemDTO> boxInfoBySourceId(String sourceId) {
        return this.baseMapper.boxInfoBySourceId(sourceId);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<WmsCartonDetailEntity> detailEntityList, String cartonId, String sourceId, String sourceType) {
        for (WmsCartonDetailEntity wmsCartonDetailEntity : detailEntityList) {
            wmsCartonDetailEntity.setCartonId(cartonId);
            wmsCartonDetailEntity.setSourceId(sourceId);
            wmsCartonDetailEntity.setSourceType(sourceType);
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
        List<WmsCartonBillEntity> firstMileCartonBillEntities = wmsCartonBillService.listBySourceIds(Arrays.asList(sourceId));
        Integer maxBoxNo = 0;
        if (CollectionUtils.isNotEmpty(firstMileCartonBillEntities)) {
            maxBoxNo = firstMileCartonBillEntities.stream().max(Comparator.comparingInt(req -> Integer.valueOf(req.getBoxNo()))).map(req -> Integer.valueOf(req.getBoxNo())).get();
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < detailEntityList.size(); i++) {
            sb.append(detailEntityList.get(i).getSkuNo());
            sb.append(StringPool.ASTERISK);
            sb.append(detailEntityList.get(i).getPackQty());
            sb.append(StringPool.SEMICOLON);
        }

        for (Integer i = maxBoxNo+1; i <= maxBoxNo+boxQty; i++) {
            FirstMileCartonBillDTO.AddDTO billAdd = new FirstMileCartonBillDTO.AddDTO();
            billAdd.setBoxDesc(sb.toString());
            billAdd.setBoxNo(String.valueOf(i));
            billAdd.setCartonId(cartonId);
            billAdd.setSourceId(sourceId);
            wmsCartonBillService.add(billAdd);
        }
    }
}
