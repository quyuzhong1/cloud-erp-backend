package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cLogisticsDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.oms.mapper.SoB2cLogisticsMapper;
import com.erp.server.oms.service.CommonService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单物流信息表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cLogisticsServiceImpl extends SuperServiceImpl<SoB2cLogisticsMapper, SoB2cLogisticsEntity> implements SoB2cLogisticsService {

    @Resource
    private CommonService commonService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Override
    public Boolean add(SoB2cLogisticsDTO.AddDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO,entity);
        entity.setMainId(mainId);
        return this.save(entity);
    }

    @Override
    public Boolean update(SoB2cLogisticsDTO.UpdateDTO logisticsDTO, String mainId) {
        SoB2cLogisticsEntity old = super.getById(logisticsDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单物流信息表"));
        SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
        BeanMapperUtils.copy(logisticsDTO,entity);
        entity.setMainId(mainId);
        handleLogisticsData(entity);

        boolean update = this.updateById(entity);

        //主表信息
        SoB2cEntity soB2cEntity = soB2cService.getById(old.getMainId());
        // 记录主单操作日志
        log.info("编辑 开始记录B2C销售订单表日志数据，单号：【{}】", soB2cEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cEntity.getCode(), "B2C销售订单表");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), msg);
        return update;
    }

    /**
     * @description: 数据处理
     * @author Will
     * @date: 2023/11/10 15:59
     * @param entity
     */
    private void handleLogisticsData (SoB2cLogisticsEntity entity) {
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(Arrays.asList(entity.getAccessoriesSkuId()));
        if (CollectionUtils.isEmpty(skuList)) {
            return;
        }
        entity.setAccessoriesSkuNo(skuList.get(0).getSkuNo());
    }

    @Override
    public SoB2cLogisticsEntity getByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cLogisticsEntity::getMainId,mainId).one();
    }

    private List<SoB2cLogisticsEntity> getListByMainId(String mainId) {
        return lambdaQuery().eq(SoB2cLogisticsEntity::getMainId,mainId).list();
    }

    @Override
    public List<SoB2cLogisticsEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2cLogisticsEntity::getMainId,mainIds).list();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        return lambdaUpdate().in(SoB2cLogisticsEntity::getMainId,mainIds).remove();
    }

    @Override
    public Boolean updateLogisticsCode(String mainId, String logisticsCode) {
        return lambdaUpdate().eq(SoB2cLogisticsEntity::getMainId,mainId).set(SoB2cLogisticsEntity::getCode,logisticsCode).update(new SoB2cLogisticsEntity());
    }

    @Override
    public void saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity) {
        if (Objects.isNull(mainEntity) || StrUtil.isBlank(mainEntity.getId())) return;
        List<PlatformOrderLogisticsDTO> logisticsList = dto.getLogisticsList();
        List<LogisticsBillDTO.AddDTO> addDTOList = new ArrayList<>();
        boolean isShopee = LogisticsPlatformEnum.SHOPEE.getCode().equals(dto.getDictPayMethod());
        if (CollectionUtils.isEmpty(logisticsList)){
            //清空主表下的物流信息
            deleteByMainIds(Collections.singletonList(mainEntity.getId()));
        }else {
            //获取主表下物流记录
            List<SoB2cLogisticsEntity> listByMainId = getListByMainId(mainEntity.getId());
            if (CollectionUtils.isEmpty(listByMainId)){
                //新增
                logisticsList.forEach(platformOrderLogisticsDTO -> {
                    SoB2cLogisticsEntity entity = new SoB2cLogisticsEntity();
                    BeanMapperUtils.copy(platformOrderLogisticsDTO, entity);
                    this.saveOrUpdate(entity);
                    if (isShopee){
                        addDTOList.add(buildLogisticsBill(entity, mainEntity));
                    }

                });
            }else {
                //转map 比较是否存在记录 不存在则删除 存在则更新
                Map<String, SoB2cLogisticsEntity> map = listByMainId.stream().collect(Collectors.toMap(SoB2cLogisticsEntity::getCode, Function.identity()));
                logisticsList.forEach(platformOrderLogisticsDTO -> {
                    SoB2cLogisticsEntity entity = map.get(platformOrderLogisticsDTO.getCode());
                    if (Objects.isNull(entity)){
                        entity = new SoB2cLogisticsEntity();
                        BeanMapperUtils.copy(platformOrderLogisticsDTO, entity);
                        this.saveOrUpdate(entity);
                        if (isShopee){
                            addDTOList.add(buildLogisticsBill(entity, mainEntity));
                        }
                    }else {
                        SoB2cLogisticsEntity entity2 = new SoB2cLogisticsEntity();
                        BeanMapperUtils.copy(platformOrderLogisticsDTO, entity2);
                        entity2.setId(entity.getId());
                        this.saveOrUpdate(entity2);
                        if (isShopee){
                            addDTOList.add(buildLogisticsBill(entity, mainEntity));
                        }
                    }
                });
            }
        }
        //虾皮物流订单新增 TMS物流单号记录
        if (isShopee){
            try {
                logisticsBillFeign.logisticsBillBatchSave(addDTOList);
            }catch (Exception e){
                log.error("同步物流单异常：{}", addDTOList);
            }

        }
    }

    private LogisticsBillDTO.AddDTO buildLogisticsBill(SoB2cLogisticsEntity entity,SoB2cEntity mainEntity){
        LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
        addDTO.setShopId(mainEntity.getShopId());
        addDTO.setShopName(mainEntity.getShopName());
        addDTO.setSalesPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        addDTO.setDeliveryTime(entity.getDeliveryTime());
        addDTO.setOrderTime(mainEntity.getPayTime());
        addDTO.setTransportNo(entity.getCode());
        addDTO.setDetailList(buildDetailList(entity));
        addDTO.setSourceId(mainEntity.getId());
        addDTO.setSourceCode(mainEntity.getPlatformCode());
        addDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        addDTO.setOutstockId("");
        addDTO.setOutstockCode("");
        addDTO.setChannelId(entity.getDictLogisticsMethod());
        return addDTO;
    }

    private List<LogisticsBillDetailDTO.AddDTO> buildDetailList(SoB2cLogisticsEntity entity){
        List<LogisticsBillDetailDTO.AddDTO> addDTOList = new ArrayList<>();
        LogisticsBillDetailDTO.AddDTO addDTO = new LogisticsBillDetailDTO.AddDTO();
        addDTO.setTrackNo(entity.getCode());
        addDTO.setTrackStatus("0");
        addDTOList.add(addDTO);
        return addDTOList;
    }
}
