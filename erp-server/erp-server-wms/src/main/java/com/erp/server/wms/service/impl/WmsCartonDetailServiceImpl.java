package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
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
    private WmsCartonService wmsCartonService;
    @Autowired
    private OperateLogService operateLogService;
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WmsCartonSpecDTO.AddDTO addDTO, String cartonId, String sourceId, String sourceType) {
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
        handleData(detailEntityList, cartonId);

        log.info("开始新增发货单箱子信息单");
        boolean save = super.saveOrUpdateBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
        //箱子信息明细
        this.firstMileCartonBillSave(addDTO.getBoxQty(), detailEntityList, cartonId, sourceId);
    }

    @Override
    public List<WmsCartonDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonDetailEntity::getMainId, cartonIds).remove();
    }

    @Override
    public List<WmsCartonSpecDTO.PackingItemDTO> boxInfoBySourceId(String sourceId) {
        return this.baseMapper.boxInfoBySourceId(sourceId);
    }


    @Override
    public void add(List<WmsCartonDetailDTO.AddDTO> detailList, WmsCartonEntity wmsCartonEntity, WmsCartonSpecEntity wmsCartonSpecEntity) {
        //校验必填
        for (WmsCartonDetailDTO.AddDTO detail : detailList) {
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

        List<WmsCartonDetailEntity> detailEntityList = BeanMapper.copyList(detailList, WmsCartonDetailEntity.class);
        // 数据处理
        handleData(detailEntityList, wmsCartonEntity.getId());

        log.info("开始新增发货单箱子信息单");
        boolean save = super.saveOrUpdateBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("发货单箱子信息单保存失败");
        }
//        String msg = StrUtil.format("用户【{}】新增【{}】单据ID为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱信息" , wmsCartonEntity.getId());
//        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CARTON.getCode(), taskId, "新增操作");
        //箱子信息明细
//        this.firstMileCartonBillSave(addDTO.getBoxQty(), detailEntityList, cartonId, sourceId);
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
