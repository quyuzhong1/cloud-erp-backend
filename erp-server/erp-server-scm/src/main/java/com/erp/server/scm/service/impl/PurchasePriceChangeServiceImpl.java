package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceChangeMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 采购价变更表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class PurchasePriceChangeServiceImpl extends SuperServiceImpl<PurchasePriceChangeMapper, PurchasePriceChangeEntity> implements PurchasePriceChangeService {

    @Resource
    private PurchasePriceService purchasePriceService;


    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;


    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private AttachmentService attachmentService;


    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    /**
     * 添加采购价目变更
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     * @author yl
     * @date 2023-03-28 11:49
     */
    @Override
    public PurchasePriceChangeEntity add(PurchasePriceChangeDTO.AddDTO dto) {
        //采购价目表的id
        String priceId = dto.getPurchasePriceId();
        PurchasePriceEntity purchasePrice = purchasePriceService.getById(priceId);
        if (Objects.isNull(purchasePrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        //检查区间报价是否重叠
        purchasePriceChangeDetailService.checkSkuInterval(dto.getPurchasePriceChangeDetailList());
        PurchasePriceChangeEntity changeEntity = new PurchasePriceChangeEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, changeEntity);
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.CGTJ, BusinessNoTypeEnum.CODE_CGTJ.getCode()));
        changeEntity.setCode(code);
        changeEntity.setId(id);
        String pricingUserId = dto.getAdjustUserId();
        FindUserDTO user = sysUserFeign.getUserByUserId(pricingUserId);
        changeEntity.setAdjustUserName(user != null ? user.getUserName() : "");
        String orgId = dto.getPurchaseOrgId();
        //获取组织
        List<BaseIdDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(orgId));
        if (CollectionUtils.isNotEmpty(orgList)) {
            changeEntity.setPurchaseOrgName(orgList.get(0).getName());
        }
        //保存成功
        Boolean addResult = this.save(changeEntity);
        if(addResult){
            Class<PurchasePriceChangeEntity> credentialClass = PurchasePriceChangeEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);

            //添加日志
            String content = String.format("新增了一个{%s}-采购调价-{%s}", ApproveStatusEnum.WAIT_SUBMIT.getName(), code);
            addModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), id, "新增操作");
            return changeEntity;
        }

        return null;
    }

    
    
    
    /**
     * 添加日志
     * @author yl
     * @date 2023-03-28 12:25
     * @param content
     * @param code
     * @param businessId
     * @param operation
     * @return void
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, operation);

    }
}
