package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceMapper;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Transactional(rollbackFor = Exception.class)
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
        String pricingUserId = dto.getPricingUserId();
        FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
        purchasePrice.setPricingUserName(user != null ? user.getUserName() : "");
        String orgId = dto.getPurchaseOrgId();
        //获取组织
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            purchasePrice.setPurchaseOrgName(orgList.get(0).getName());
        }
        Boolean addResult = this.save(purchasePrice);
        //保存成功
        if (addResult) {
            Class<PurchasePriceEntity> credentialClass = PurchasePriceEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();

            //保存附件
            attachmentService.batchSave(dto.getAttachmentUrlList(),dto.getAttachmentNameList(), type, id);

            /**
             * 添加明细
             */
            priceDetailService.addPriceDetail(id, dto.getPurchasePriceDetailList());

            return purchasePrice;

        }
        return null;
    }

    /**
     * 获取采购价目详情
     *
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceDTO.ViewDTO
     * @author yl
     * @date 2023-03-27 9:11
     */
    @Override
    public PurchasePriceDTO.ViewDTO view(String id) {
        PurchasePriceEntity purchasePrice = this.getById(id);
        if (Objects.isNull(purchasePrice)) {
            throw new ServiceException(ApiError.ERROR_98023);
        }
        PurchasePriceDTO.ViewDTO viewDTO = new PurchasePriceDTO.ViewDTO();
        BeanMapper.copy(purchasePrice, viewDTO);
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachmentNameList(attachmentNameList);
        viewDTO.setAttachmentUrlList(attachmentUrlList);
        //获取明细信息
        List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList=priceDetailService.getByPurchasePriceId(id);
        viewDTO.setPurchasePriceDetailList(purchasePriceDetailList);
        return viewDTO;
    }
}
