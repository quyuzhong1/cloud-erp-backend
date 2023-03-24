package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceMapper;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.SupplierService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 * 采购价目表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceServiceImpl extends SuperServiceImpl<PurchasePriceMapper, PurchasePriceEntity> implements PurchasePriceService {


    @Resource
    private SupplierService supplierService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private PurchasePriceDetailService priceDetailService;

    @Resource
    private AttachmentService attachmentService;

    /**
     * 添加采购价目表
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     * @author yl
     * @date 2023-03-24 12:22
     */
    @Override
    public PurchasePriceEntity add(PurchasePriceDTO.AddDTO dto) {
        //供应商id
        String supplierId = dto.getSupplierId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //检查sku 区间报价
        priceDetailService.checkSkuInterval(dto.getPurchasePriceDetailList());
        PurchasePriceEntity purchasePrice = new PurchasePriceEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, purchasePrice);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.GYS, BusinessNoTypeEnum.CODE_CGJM.getCode()));
        purchasePrice.setCode(code);
        purchasePrice.setId(id);

        Boolean addResult = this.save(purchasePrice);
        //保存成功
        if (addResult) {

        }


        return null;
    }
}
