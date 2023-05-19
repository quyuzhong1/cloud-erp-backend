package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetiailDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.SoOutstockMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 销售订单出库单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Service
@Slf4j
public class SoOutstockServiceImpl extends SuperServiceImpl<SoOutstockMapper, SoOutstockEntity> implements SoOutstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<SoOutstockEntity> listBySourceId(List<String> ids) {
        return lambdaQuery().in(SoOutstockEntity::getSourceId, ids).list();
    }


    /**
     * 添加销售出库单
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-19 9:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SoOutstockDTO.AddDTO dto) {
        //TODO 对应检查数量

        String id = IdWorker.getIdStr();
        SoOutstockEntity soOutstock = new SoOutstockEntity();
        BeanMapper.copy(dto, soOutstock);
        soOutstock.setId(id);
        List<SoOutstockDetiailDTO.AddDTO> detailList = dto.getDetailList();
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.XSCK, BusinessNoTypeEnum.CODE_XSCK.getCode()));
        soOutstock.setCode(code);
        //发货组织
        String deliveryOrgId = dto.getDeliveryOrgId();
        //仓库id
        String warehouseId = dto.getWarehouseId();
        //仓管员
        String warehouseKeeperId = dto.getWarehouseKeeperId();
        if (StringUtils.isNotBlank(warehouseKeeperId)) {
            //用户信息
            FindUserDTO userInfo = sysUserFeign.getUserByUserId(warehouseKeeperId);
            if (userInfo != null) {
                soOutstock.setWarehouseKeeperName(userInfo.getUserName());
            }
        }
        //组织列表
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(deliveryOrgId));
        String salesOrgName = orgList.stream().filter(d -> d.getId().equals(deliveryOrgId)).findFirst().
                flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        soOutstock.setDeliveryOrgName(salesOrgName);
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        soOutstock.setWarehouseName(warehouse.getName());
        Boolean addResult = this.save(soOutstock);
        //添加成功
        if (addResult) {

            soOutstockDetailService.add(id, detailList);
            //添加日志
            String content = String.format("新增了一个{%s}-销售出库单-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.SO_OUT_STOCK.getCode(), id, "新增操作");
            return id;
        }

        return "";
    }




    /**
     * 添加日志
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        operateLogService.addModuleOperateLog(content, code, businessId, operation);
    }
}
