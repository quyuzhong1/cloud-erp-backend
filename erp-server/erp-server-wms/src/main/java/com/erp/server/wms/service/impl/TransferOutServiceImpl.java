package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.TransferOutEntity;
import com.erp.model.wms.enums.TransferTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.TransferOutMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.TransferOutService;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 分布式调出单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferOutServiceImpl extends SuperServiceImpl<TransferOutMapper, TransferOutEntity> implements TransferOutService {

    @Autowired
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Autowired
    private WarehouseService warehouseService;

    @Override
    public List<TransferOutEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery()
                .in(TransferOutEntity::getSourceId,ids)
                .eq(TransferOutEntity::getInvalidStatus,Boolean.FALSE)
                .list();
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(TransferOutDTO.AddDTO addDTO) {
        // 验证数据
        ValidatorUtil.validateEntity(addDTO);
        TransferOutEntity transferOutEntity = new TransferOutEntity();
        BeanMapperUtils.copy(addDTO, transferOutEntity);
        log.info("开始新增分步式调出单主单");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.FBDC, BusinessNoTypeEnum.CODE_FBDC.getCode()));
        transferOutEntity.setCode(code);
        boolean save = super.save(transferOutEntity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个分步式调出单【%s】", code), ModuleTypeEnum.TRANSFER_OUT.getCode(), transferOutEntity.getId(), "新增操作");
            //新增明细
            log.info("开始新增分步式调出单明细信息");
        }
    }


    private void handleData(TransferOutEntity transferOutEntity) {
        // 验证仓库信息
        WarehouseDTO.UpdateDTO warehouseIn = warehouseService.detailWithCache(transferOutEntity.getInWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouseIn) && StrUtils.isNotEmpty(warehouseIn.getId()),()->new ServiceException("调入仓库未找到"));
        transferOutEntity.setInWarehouseName(warehouseIn.getName());

        WarehouseDTO.UpdateDTO warehouseOut = warehouseService.detailWithCache(transferOutEntity.getOutWarehouseId());
        ValidatorUtil.isTrue(Objects.nonNull(warehouseOut) && StrUtils.isNotEmpty(warehouseOut.getId()),()->new ServiceException("调出仓库未找到"));
        transferOutEntity.setOutWarehouseName(warehouseOut.getName());

        //组织信息
        List<String> orgIds = Lists.newArrayList(warehouseIn.getOrgId(), warehouseOut.getOrgId()).stream().distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        Map<String,BaseIdDTO.CodeDTO> orgMap = accountingCompanyList.stream().collect(Collectors.toMap(BaseIdDTO.CodeDTO::getId, Function.identity()));
        transferOutEntity.setInOrgId(warehouseIn.getOrgId());
        transferOutEntity.setOutOrgId(warehouseOut.getOrgId());
        transferOutEntity.setInOrgName(orgMap.get(warehouseIn.getOrgId()).getName());
        transferOutEntity.setOutOrgName(orgMap.get(warehouseOut.getOrgId()).getName());

        //调拨类型
        if (Objects.equals(transferOutEntity.getInOrgId(), transferOutEntity.getOutOrgId()))  {
            transferOutEntity.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            transferOutEntity.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        //仓管员
        if (StringUtils.isNotBlank(transferOutEntity.getWarehouseKeeperId())) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(transferOutEntity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(userDTO)) {
                transferOutEntity.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
    }

}
