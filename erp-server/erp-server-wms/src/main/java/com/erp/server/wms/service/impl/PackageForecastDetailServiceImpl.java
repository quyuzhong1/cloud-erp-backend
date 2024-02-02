package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.ForecastFeign;
import com.erp.server.wms.mapper.PackageForecastDetailMapper;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.wms.service.SoOutstockService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PackageForecastDetailDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

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


    @GlobalTransactional(rollbackFor = Exception.class)
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
    @Override
    public Boolean update(String mainId,String logisticsSupplierId, List<String> detailIdList) {
        List<String> idList = detailIdList.stream().filter(d -> StringUtils.isNotBlank(d)).collect(Collectors.toList());
        List<PackageForecastDetailEntity> dbList = this.listDbByMainId(mainId);
        //删除的信息
        List<PackageForecastDetailEntity> deleteList=dbList.stream().filter(s -> !idList.contains(s.getId())).collect(Collectors.toList());
        //删除的id
        List<String> deleteIdList = deleteList.stream().map(PackageForecastDetailEntity::getId).collect(Collectors.toList());
        //销售订单id
        List<String> soIdList=deleteList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());

        //forecastFeign.getByLogisticsSupplier();



        return Boolean.TRUE;
    }


    @Override
    public List<PackageForecastDetailDTO.ViewDTO> listDetailViewByMainId(String id) {
        List<PackageForecastDetailEntity> detailList = this.listDbByMainId(id);
        List<PackageForecastDetailDTO.ViewDTO> resultList = BeanMapperUtils.copyList(PackageForecastDetailDTO.ViewDTO.class, detailList);
        List<String> soIdList = detailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockList = soOutstockService.listBySoIds(soIdList);
        for (PackageForecastDetailDTO.ViewDTO item : resultList) {
            String soId = item.getSoId();
            ApproveStatusEnum approveStatus = soOutstockList.stream().filter(s -> s.getSoId().equals(soId)).
                    map(SoOutstockEntity::getApproveStatus).findFirst().orElse(null);
            item.setOutstockStatusName("未出库");
            if (Objects.nonNull(approveStatus)) {
                if (ApproveStatusEnum.APPROVE.equals(approveStatus)) {
                    item.setOutstockStatusName("已出库");
                }
            }
        }

        return resultList;
    }


    public List<PackageForecastDetailEntity> listDbByMainId(String id) {
        return this.lambdaQuery().eq(PackageForecastDetailEntity::getMainId, id).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(PackageForecastDetailEntity packageForecastDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
