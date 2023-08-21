package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.mapper.SoB2cDetailMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单明细表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@Service
public class SoB2cDetailServiceImpl extends SuperServiceImpl<SoB2cDetailMapper, SoB2cDetailEntity> implements SoB2cDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public Boolean add(List<SoB2cDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
           throw new ServiceException(ApiError.ERROR_1040, SourceTypeEnum.SO_B2C.getName());
        }
        List<SoB2cDetailEntity> list = BeanMapperUtils.copyList(SoB2cDetailEntity.class, detailList);
        //处理明细中的数据id
        handleDetailList(list,mainId,Boolean.TRUE);


        return null;
    }

    @Override
    public Boolean update(List<SoB2cDetailDTO.UpdateDTO> detailList, String mainId) {
        return null;
    }


    private void handleDetailList (List<SoB2cDetailEntity> list,String mainId,Boolean isAdd) {

        //产品信息
        List<String> skuIds = list.stream().map(SoB2cDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isNotEmpty(skuList)) {
            log.error("未找到SKU，warehouseIds = {}",skuList);
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //仓库信息
        List<String> warehouseIdList = list.stream().map(SoB2cDetailEntity::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);
        if (CollectionUtils.isEmpty(warehouseList)) {
            log.error("未找到仓库，warehouseIdList = {}",warehouseIdList);
            throw new ServiceException(ApiError.ERROR_99002);
        }
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            log.error("未找到核算公司，orgIdList = {}",orgIdList);
            throw new ServiceException(ApiError.ERROR_9014);
        }


        //SKU对照表信息 TODO

        for (SoB2cDetailEntity detailEntity :list) {

            //产品信息
            String skuNo = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getSkuNo()))
                    .orElse("");
            detailEntity.setSkuNo(skuNo);

            //仓库名称
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream()
                    .filter(obj -> obj.getId().equals(detailEntity.getWarehouseId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(updateDTO)) {
                log.error("未找到仓库，warehouseId = {}",detailEntity.getWarehouseId());
                throw new ServiceException(ApiError.ERROR_99002);
            }
            detailEntity.setWarehouseName(updateDTO.getName());

            //库存组织
            BaseIdDTO.CodeDTO companyDTO = accountingCompanyList.stream()
                    .filter(obj -> obj.getId().equals(updateDTO.getOrgId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(companyDTO)) {
                log.error("未找到核算公司，orgId = {}",updateDTO.getOrgId());
                throw new ServiceException(ApiError.ERROR_9014);
            }
            detailEntity.setWarehouseOrgId(updateDTO.getOrgId());
            detailEntity.setWarehouseOrgName(companyDTO.getName());
        }



        //添加操作日志
        List<SoB2cDetailEntity> addList = list.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList) && !isAdd) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.SO_B2C.getCode(), addPairList, "编辑操作");
        }
    }
}
