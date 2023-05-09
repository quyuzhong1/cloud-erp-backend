package com.erp.server.wms.service.impl;

import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.inventory.InStockOrOutStockDTO;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InventoryInStockOrOutStockDTO;
import com.erp.model.wms.entity.InstockForcastDetailEntity;
import com.erp.model.wms.entity.InstockForcastEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.InstockForcastMapper;
import com.erp.server.wms.service.InstockForcastDetailService;
import com.erp.server.wms.service.InstockForcastService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.InventoryTransCoreService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 入库预报表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-09
 */
@Service
public class InstockForcastServiceImpl extends SuperServiceImpl<InstockForcastMapper, InstockForcastEntity> implements InstockForcastService {

    @Autowired
    private InstockForcastDetailService instockForcastDetailService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateByPurchaseOrder(InstockForcastDTO.AddDTO dto) {
        InstockForcastEntity instockForcastEntity = new InstockForcastEntity();
        // 生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.RKYB, BusinessNoTypeEnum.CODE_RKYB.getCode()));
        // feign调用取不到登录人信息，已解决
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        instockForcastEntity.setCreateUserId(loginUser.getUid());
        instockForcastEntity.setCreateUserName(loginUser.getUserName());
        instockForcastEntity.setUpdateUserId(loginUser.getUid());
        instockForcastEntity.setUpdateUserName(loginUser.getUserName());

        // 默认审核通过
        instockForcastEntity.setApproveStatus(ApproveStatusEnum.APPROVE.getStatus());
        instockForcastEntity.setApproveTime(LocalDateTime.now());
        instockForcastEntity.setApproveUserId(loginUser.getUid());
        instockForcastEntity.setApproveUserName(loginUser.getUserName());

        instockForcastEntity.setCode(code);
        instockForcastEntity.setPurchaseOrderId(dto.getPurchaseOrderId());
        instockForcastEntity.setOrgId(dto.getOrgId());
        instockForcastEntity.setWarehouseId(dto.getWarehouseId());
        instockForcastEntity.setBillDate(LocalDate.now());// 单据日期取当前时间

        // 保存主单信息
        this.save(instockForcastEntity);
        // 保存入库预报明细
        List<InstockForcastDetailEntity> instockForcastDetails = instockForcastDetailService.add(dto, instockForcastEntity.getId());
        // 调用库存组件，更新库存信息
        InventoryInStockOrOutStockDTO inventoryDto = new InventoryInStockOrOutStockDTO();
        inventoryDto.setBusinessType(InventoryBusinessTypeEnum.INSTOCK_FORCAST.getCode());
        List<InStockOrOutStockDTO> inventorySkus = Lists.newArrayListWithExpectedSize(instockForcastDetails.size());
        instockForcastDetails.stream().forEach(instockForcastDetailEntity -> {
            InStockOrOutStockDTO inStockOrOutStockDTO = new InStockOrOutStockDTO();
            inStockOrOutStockDTO.setOrgId(instockForcastEntity.getOrgId());
            inStockOrOutStockDTO.setWarehouseId(instockForcastEntity.getWarehouseId());
            inStockOrOutStockDTO.setSourceType(InventorySourceTypeEnum.INSTOCK_FORCAST);
            inStockOrOutStockDTO.setSourceId(instockForcastEntity.getId());
            inStockOrOutStockDTO.setSourceCode(instockForcastEntity.getCode());
            inStockOrOutStockDTO.setBillDate(instockForcastEntity.getBillDate());
            inStockOrOutStockDTO.setSourceDetailId(instockForcastDetailEntity.getId());
            inStockOrOutStockDTO.setSkuId(instockForcastDetailEntity.getSkuId());
            inStockOrOutStockDTO.setSkuNo(instockForcastDetailEntity.getSkuNo());
            inStockOrOutStockDTO.setQty(instockForcastDetailEntity.getQty());
            inventorySkus.add(inStockOrOutStockDTO);
        });
        inventoryDto.setSkus(inventorySkus);
        inventoryTransCoreService.approveInOutStockByType(inventoryDto);
    }


}
