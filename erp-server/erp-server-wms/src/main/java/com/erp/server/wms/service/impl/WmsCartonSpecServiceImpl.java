package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.enums.UnitEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.MeasureSourceEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.WmsCartonSpecMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 发货单箱规信息 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsCartonSpecServiceImpl extends SuperServiceImpl<WmsCartonSpecMapper, WmsCartonSpecEntity> implements WmsCartonSpecService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private WmsCartonDetailService wmsCartonDetailService;
    @Autowired
    private WmsCartonService wmsCartonService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private PackingTaskDetailService packingTaskDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(WmsCartonSpecDTO.AddDTO addDTO, String taskId) {
        WmsCartonSpecEntity wmsCartonSpecEntity = new WmsCartonSpecEntity();
        BeanMapperUtils.copy(addDTO, wmsCartonSpecEntity);
        // 数据处理
        handleData(wmsCartonSpecEntity, taskId);

        log.info("开始新增发货单箱规信息");
        boolean save = super.saveOrUpdate(wmsCartonSpecEntity);
        if (!save) {
            throw new ServiceException("发货单箱规信息保存失败");
        }
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "装箱箱规", wmsCartonSpecEntity.getBoxSpecNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CARTON_SPC.getCode(), taskId, "新增操作");
        //新增箱子信息
        wmsCartonService.add(addDTO, wmsCartonSpecEntity, taskId);
        return wmsCartonSpecEntity.getId();
    }

    @Override
    public List<WmsCartonSpecEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonSpecEntity::getMainId, mainIds)
                .orderByAsc(WmsCartonSpecEntity::getCreateTime, WmsCartonSpecEntity::getId)
                .list();
    }

    @Override
    public List<WmsCartonSpecDTO.PackingQtyDTO> listPackingQtyByMainId(String mainId, Integer boxSpecNo) {
        return baseMapper.listPackingQtyByMainId(mainId, boxSpecNo);
    }

    @Override
    public Boolean deleteBySourceIds(List<String> sourceIds) {
        if (CollectionUtil.isEmpty(sourceIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(WmsCartonSpecEntity::getMainId, sourceIds).remove();
    }

    @Override
    public List<WmsCartonSpecDTO.PackDateDTO> listPackDateByPackingTaskId(String packingTaskId) {
        if (StringUtils.isBlank(packingTaskId)) {
            return Collections.emptyList();
        }
        return baseMapper.listPackDateByMainId(packingTaskId);
    }

    @Override
    public Boolean packQtyCheck(String sourceId) {
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS = this.listPackDateByPackingTaskId(sourceId);
        for (WmsCartonSpecDTO.PackDateDTO packDateDTO : packDateDTOS) {
            //待装箱数量=发货数量-所有已装箱数量
            int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(packDateDTO.getSkuId())).mapToInt(req -> req.getBoxQty() * req.getPackQty()).sum();
            if (packDateDTO.getDeliveryQty() < packQtySum) {
                throw new ServiceException(ApiError.PACKING_QTY_NOT_GT_WAIT_PACKING_QTY, packDateDTO.getBoxSpecNo(), packDateDTO.getSkuNo());
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public WmsCartonSpecDTO.WmsCartonSpecView getCartonViewByPackingTaskId(PackingTaskEntity packingTaskEntity) {
        WmsCartonSpecDTO.WmsCartonSpecView view = new WmsCartonSpecDTO.WmsCartonSpecView();
        if (ObjectUtil.isEmpty(packingTaskEntity)){
            return view;
        }
        //装箱任务
        view.setTaskId(packingTaskEntity.getId());
        //查询装箱详情
        view.setSourceId(packingTaskEntity.getSourceId());
        view.setSourceCode(packingTaskEntity.getSourceCode());
        String taskEntityId = packingTaskEntity.getId();
        //查询箱规信息
        List<WmsCartonSpecEntity> wmsCartonSpecEntities = this.listByMainIds(Collections.singletonList(taskEntityId));
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskEntityId));
        List<WmsCartonSpecDTO.ViewDTO> wmsCartonSpecList = BeanMapper.copyList(wmsCartonSpecEntities, WmsCartonSpecDTO.ViewDTO.class);
        view.setWmsCartonSpecList(wmsCartonSpecList);
        List<PackingTaskDetailEntity> taskDetailEntityList = packingTaskDetailService.listByMainIds(Collections.singletonList(taskEntityId));
        //根据主表id分组sku查询发货及待装箱数
        List<WmsCartonSpecDTO.PackDateDTO> packDateDTOS = this.listPackDateByPackingTaskId(taskEntityId);
        //查询产品信息
        List<String> skuIdList = packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
        //查询箱规包含的产品信息
        for (WmsCartonSpecDTO.ViewDTO viewDTO : wmsCartonSpecList) {
            WmsCartonEntity cartonEntity = cartonEntityList.stream().filter(e -> e.getSpecId().equals(viewDTO.getId())).findFirst().orElse(new WmsCartonEntity());
            List<WmsCartonSpecDTO.PackDateDTO> packDateDTOList = packDateDTOS.stream().filter(req -> req.getBoxSpecNo().equals(viewDTO.getBoxSpecNo())).collect(Collectors.toList());
            List<WmsCartonDetailDTO.ViewDTO> detailList = BeanMapper.copyList(packDateDTOList, WmsCartonDetailDTO.ViewDTO.class);
            for (WmsCartonDetailDTO.ViewDTO dto : detailList) {
                int deliveryQty = taskDetailEntityList.stream().filter(req -> dto.getSkuId().equals(req.getSkuId())).mapToInt(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                dto.setDeliveryQty(deliveryQty);
                //待装箱数量=发货数量-所有已装箱数量
                int packQtySum = packDateDTOS.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).mapToInt(req -> req.getBoxQty() * req.getPackQty()).sum();
                dto.setWaitPackQty(deliveryQty - packQtySum);

                //匹配产品信息，设置中文名
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(dto.getSkuId())).findFirst().orElse(new SkuVO());
                dto.setProductName(skuVO.getSkuName());
                dto.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
            }
            viewDTO.setDetailList(detailList);
            viewDTO.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
            BigDecimal grossWeight= detailList.stream().map(WmsCartonDetailDTO.ViewDTO::getGrossWeight).reduce(BigDecimal.ZERO,BigDecimal::add);
            viewDTO.setGrossWeight(grossWeight);
            viewDTO.setBoxNo(cartonEntity.getBoxNo() != 0 ? cartonEntity.getBoxNo() : null);
            viewDTO.setPackingUserId(cartonEntity.getPackingUserId());
            viewDTO.setPackingUserName(cartonEntity.getPackingUserName());
            //TODO 预警提示：超重值：10KG，本次装箱预计已超重1KG！
            viewDTO.setWarnMsg("");
        }
        //装箱进度
        view.setDeliveryQty(taskDetailEntityList.stream().map(PackingTaskDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum));
        view.setPackTotalQty(packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getPackQty).reduce(MathUtil.ZERO,Integer::sum));
        view.setPackGrossWeight(packDateDTOS.stream().map(WmsCartonSpecDTO.PackDateDTO::getGrossWeight).reduce(BigDecimal.ZERO,BigDecimal::add));
        view.setPackWeightUnit(UnitEnum.WeightUnitEnum.KG.code);

        return view;
    }


    /**
     * 删除原装箱信息
     * @param taskId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCarton(String taskId) {
        List<WmsCartonSpecEntity> specEntityList = this.listByMainIds(Collections.singletonList(taskId));
        if (CollectionUtils.isNotEmpty(specEntityList)){
            List<String> specIds = specEntityList.stream().map(WmsCartonSpecEntity::getId).distinct().collect(Collectors.toList());
            this.removeByIds(specIds);
        }
        List<WmsCartonEntity> cartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(taskId));
        if (CollectionUtils.isNotEmpty(cartonEntityList)){
            List<String> cartonIds = cartonEntityList.stream().map(WmsCartonEntity::getId).distinct().collect(Collectors.toList());
            wmsCartonService.removeByIds(cartonIds);
            wmsCartonDetailService.deleteByCartonIds(cartonIds);
        }
    }

    @Override
    public void updateSpec(WmsCartonSpecDTO.SpecSaveDTO dto) {
        this.lambdaUpdate()
                .eq(WmsCartonSpecEntity::getId, dto.getSpecId())
                .set(WmsCartonSpecEntity::getPackageWeight, dto.getPackageWeight())
                .set(WmsCartonSpecEntity::getWeightUnit, dto.getWeightUnit())
                .set(WmsCartonSpecEntity::getBoxLength, dto.getBoxLength())
                .set(WmsCartonSpecEntity::getBoxWidth, dto.getBoxWidth())
                .set(WmsCartonSpecEntity::getBoxHeight, dto.getBoxHeight())
                .set(WmsCartonSpecEntity::getSizeUnit, dto.getSizeUnit())
                .set(WmsCartonSpecEntity::getMeasureSource, dto.getMeasureSource())
                .update();
    }

    @Override
    public WmsCartonSpecEntity getByTaskIdAndBoxNo(String packingTaskId, String boxNo) {
        if(com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(packingTaskId) || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(boxNo)){
            return null;
        }
        return lambdaQuery().eq(WmsCartonSpecEntity::getMainId,packingTaskId).eq(WmsCartonSpecEntity::getBoxSpecNo,boxNo).last("limit 1").one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsCartonSpecEntity wmsCartonSpecEntity, String taskId) {
        wmsCartonSpecEntity.setMainId(taskId);
        if (StringUtils.isBlank(wmsCartonSpecEntity.getMeasureSource())){
            wmsCartonSpecEntity.setMeasureSource(MeasureSourceEnum.MANUAL.getCode());
        }
        if (Objects.isNull(wmsCartonSpecEntity.getBoxQty())){
            wmsCartonSpecEntity.setBoxQty(MathUtil.ONE);
        }
        if (Objects.isNull(wmsCartonSpecEntity.getBoxSpecNo())){
            Integer boxSpecNo = baseMapper.selectBoxSpecNo(taskId);
            if (Objects.isNull(boxSpecNo)){
                wmsCartonSpecEntity.setBoxSpecNo(MathUtil.ONE);
            }else {
                wmsCartonSpecEntity.setBoxSpecNo(boxSpecNo + 1);
            }
        }
        wmsCartonSpecEntity.setBoxQty(MathUtil.ONE);

    }
}
