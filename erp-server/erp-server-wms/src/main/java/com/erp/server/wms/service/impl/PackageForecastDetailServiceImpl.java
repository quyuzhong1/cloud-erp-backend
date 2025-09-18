package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.tms.entity.SettingForecastEntity;
import com.erp.model.tms.enums.TransferOutstockStatusEnum;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.HandoverSubStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.ForecastFeign;
import com.erp.server.wms.mapper.PackageForecastDetailMapper;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.SoOutstockService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 组包预报详情 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@Service
public class PackageForecastDetailServiceImpl extends SuperServiceImpl<PackageForecastDetailMapper, PackageForecastDetailEntity> implements PackageForecastDetailService {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private ForecastFeign forecastFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(String mainId, List<PackageForecastDetailDTO.AddDTO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<PackageForecastDetailEntity> detailEntityList = BeanMapperUtils.copyList(PackageForecastDetailEntity.class, detailList);
        handleDataList(mainId, detailEntityList);
        Boolean result = this.saveBatch(detailEntityList);
        if (result) {
            UpdateStateDTO.UpdateByStrStatusDTO dto = new UpdateStateDTO.UpdateByStrStatusDTO();
            dto.setIds(detailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList()));
            dto.setStatus(PackageStatusEnum.ALREADY.getCode());
            soB2cFeign.updatePackageStatus(dto);
        }
    }

    /**
     * 处理数据
     *
     * @param mainId
     * @param detailEntityList
     */
    private void handleDataList(String mainId, List<PackageForecastDetailEntity> detailEntityList) {
        for (PackageForecastDetailEntity item : detailEntityList) {
            item.setMainId(mainId);
        }
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Override
    public Boolean update(PackageForecastEntity entity, List<String> detailIdList) {
        String mainId = entity.getId();
        String logisticsSupplierId = entity.getLogisticsSupplierId();
        List<String> idList = detailIdList.stream().filter(d -> CharSequenceUtil.isNotBlank(d)).collect(Collectors.toList());
        List<PackageForecastDetailEntity> dbList = this.listDbByMainId(mainId);
        //删除的信息
        List<PackageForecastDetailEntity> deleteList = dbList.stream().filter(s -> !idList.contains(s.getId())).collect(Collectors.toList());
        //删除的id
        List<String> deleteIdList = deleteList.stream().map(PackageForecastDetailEntity::getId).collect(Collectors.toList());
        this.removeByIds(deleteIdList);
        //表示没有删除的
        List<PackageForecastDetailEntity> notDeleteList = dbList.stream().filter(s -> idList.contains(s.getId())).collect(Collectors.toList());
        entity.setTotalPackageQty(notDeleteList.size());
        BigDecimal notDeleteTotalPackageWeight = notDeleteList.stream().map(PackageForecastDetailEntity::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        entity.setTotalPackageWeight(notDeleteTotalPackageWeight);

        //销售订单id
        List<String> soIdList = deleteList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());

        List<SoB2cEntity> soB2cList = soB2cFeign.listByIds(soIdList);

        SettingForecastEntity settingForecast = forecastFeign.getSettingForecastByLogisticsSupplierId(logisticsSupplierId);
        //表示没有设置 那就是无需组包
        if (Objects.isNull(settingForecast)) {
            updatePackageStatus(soIdList, PackageStatusEnum.NOT.getCode());
        } else {
            //表示需要q组包
            Boolean isMustPackage = settingForecast.getIsMustPackage();
            //表示强制组包
            if (isMustPackage) {
                //组包时间
                LocalDateTime enablePackageTime = settingForecast.getEnablePackageTime();
                //如果为空就都要组包
                if (Objects.isNull(enablePackageTime)) {
                    updatePackageStatus(soIdList, PackageStatusEnum.WAIT.getCode());
                } else {
                    List<String> waitSoIdList = soB2cList.stream().filter(s -> s.getCreateTime().compareTo(enablePackageTime) > 0).
                            map(SoB2cEntity::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(waitSoIdList)) {
                        updatePackageStatus(waitSoIdList, PackageStatusEnum.WAIT.getCode());
                    }
                    List<String> notSoIdList = soB2cList.stream().filter(s -> s.getCreateTime().compareTo(enablePackageTime) <= 0).
                            map(SoB2cEntity::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(notSoIdList)) {
                        updatePackageStatus(notSoIdList, PackageStatusEnum.NOT.getCode());
                    }
                }
            }else{
                updatePackageStatus(soIdList, PackageStatusEnum.NOT.getCode());
            }
        }


        return Boolean.TRUE;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void updatePackageStatus(List<String> soIdList, String status) {
        UpdateStateDTO.UpdateByStrStatusDTO updatePackageStatus = new UpdateStateDTO.UpdateByStrStatusDTO();
        updatePackageStatus.setStatus(status);
        updatePackageStatus.setIds(soIdList);
        soB2cFeign.updatePackageStatus(updatePackageStatus);
    }


    @Override
    public List<PackageForecastDetailDTO.ViewDTO> listDetailViewByMainId(String id) {
        List<PackageForecastDetailEntity> detailList = this.listDbByMainId(id);
        List<PackageForecastDetailDTO.ViewDTO> resultList = BeanMapperUtils.copyList(PackageForecastDetailDTO.ViewDTO.class, detailList);
        List<String> soIdList = detailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIdList);
        for (PackageForecastDetailDTO.ViewDTO item : resultList) {
            String soId = item.getSoId();
            SoB2cEntity soB2cEntity = soB2cEntityList.stream()
                    .filter(req -> req.getId().equals(soId)
                            && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                item.setOutstockStatusName("已出库");
            } else {
                item.setOutstockStatusName("未出库");
            }
            String trackNo = item.getTrackNo();
            String transportNo = item.getTransportNo();
            if(CharSequenceUtil.isBlank(trackNo)){
                trackNo=transportNo;
            }
            item.setTrackNo(trackNo);
            String handoverStatus = item.getHandoverStatus();
            String handoverStatusName = HandoverSubStatusEnum.getByCode(handoverStatus);
            item.setHandoverStatusName(handoverStatusName);
        }

        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void removeByMainId(String mainId,String logisticsSupplierId) {
        List<PackageForecastDetailEntity> detailList = this.listDbByMainId(mainId);

        //销售订单id
        List<String> soIdList = detailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        Boolean result = this.lambdaUpdate().eq(PackageForecastDetailEntity::getMainId, mainId).remove();
        if (!result) {
            return;
        }

        List<SoB2cEntity> soB2cList = soB2cFeign.listByIds(soIdList);

        SettingForecastEntity settingForecast = forecastFeign.getSettingForecastByLogisticsSupplierId(logisticsSupplierId);
        //表示没有设置 那就是无需组包
        if (Objects.isNull(settingForecast)) {
            updatePackageStatus(soIdList, PackageStatusEnum.NOT.getCode());
        } else {
            //表示需要q组包
            Boolean isMustPackage = settingForecast.getIsMustPackage();
            //表示强制组包
            if (isMustPackage) {
                //组包时间
                LocalDateTime enablePackageTime = settingForecast.getEnablePackageTime();
                //如果为空就都要组包
                if (Objects.isNull(enablePackageTime)) {
                    updatePackageStatus(soIdList, PackageStatusEnum.WAIT.getCode());
                } else {
                    List<String> waitSoIdList = soB2cList.stream().filter(s -> s.getCreateTime().compareTo(enablePackageTime) > 0).
                            map(SoB2cEntity::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(waitSoIdList)) {
                        updatePackageStatus(waitSoIdList, PackageStatusEnum.WAIT.getCode());
                    }
                    List<String> notSoIdList = soB2cList.stream().filter(s -> s.getCreateTime().compareTo(enablePackageTime) <= 0).
                            map(SoB2cEntity::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(notSoIdList)) {
                        updatePackageStatus(notSoIdList, PackageStatusEnum.NOT.getCode());
                    }
                }

            }else{
                updatePackageStatus(soIdList, PackageStatusEnum.NOT.getCode());
            }
        }


    }

    @Override
    public List<PackageForecastDetailDTO.ViewDTO> detailQuery(PackageForecastDTO.DetailQueryParamDTO dto) {
        List<PackageForecastDetailEntity> detailList = this.lambdaQuery().
                eq(PackageForecastDetailEntity::getMainId, dto.getId()).
                in(CollectionUtils.isNotEmpty(dto.getSoCodeList()), PackageForecastDetailEntity::getSoCode, dto.getSoCodeList()).
                in(CollectionUtils.isNotEmpty(dto.getHandoverStatusList()), PackageForecastDetailEntity::getHandoverStatus, dto.getHandoverStatusList()).
                like(CharSequenceUtil.isNotBlank(dto.getTrackNo()), PackageForecastDetailEntity::getTransportNo, dto.getTrackNo()).
                list();
        List<PackageForecastDetailDTO.ViewDTO> resultList = BeanMapperUtils.copyList(PackageForecastDetailDTO.ViewDTO.class, detailList);
        List<String> soIdList = detailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIdList);
        for (PackageForecastDetailDTO.ViewDTO item : resultList) {
            String soId = item.getSoId();
            SoB2cEntity soB2cEntity = soB2cEntityList.stream()
                    .filter(req -> req.getId().equals(soId)
                            && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                item.setOutstockStatusName("已出库");
            } else {
                item.setOutstockStatusName("未出库");
            }
            String handoverStatus = item.getHandoverStatus();
            String handoverStatusName =HandoverSubStatusEnum.getByCode(handoverStatus);
            item.setHandoverStatusName(handoverStatusName);
            String trackNo = item.getTrackNo();
            String transportNo = item.getTransportNo();
            if(CharSequenceUtil.isBlank(trackNo)){
                trackNo=transportNo;
            }
            item.setTrackNo(trackNo);
        }
        return resultList;
    }

    @Override
    public void updateStatusByOrderCode(String orderCode, String status) {
        if (StringUtils.isEmpty(orderCode) || StringUtils.isEmpty(status)) {
            return;
        }
        this.lambdaUpdate().set(PackageForecastDetailEntity::getHandoverStatus, status)
                .eq(PackageForecastDetailEntity::getSourceCode, orderCode).eq(PackageForecastDetailEntity::getIsDeleted, false)
                .update();
    }

    @Override
    public List<PackageForecastDTO.ExportViewDTO> listPackageForecastBySoIdList(List<String> soIdList) {
        if (CollectionUtils.isEmpty(soIdList)) {
            return  Collections.emptyList();
        }
        return baseMapper.listPackageForecastBySoIdList(soIdList);
    }

    @Override
    public List<PackageForecastDetailEntity> listDbByMainId(String mainId) {
        return this.lambdaQuery().eq(PackageForecastDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<PackageForecastDetailEntity> listDbByMainIds(List<String> mainIds) {
        if(CollectionUtils.isEmpty(mainIds)){
            return new ArrayList<>();
        }
        return this.lambdaQuery().in(PackageForecastDetailEntity::getMainId, mainIds).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(PackageForecastDetailEntity packageForecastDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
