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
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.PurchasePriceChangeMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @GlobalTransactional
    public PurchasePriceChangeEntity add(PurchasePriceChangeDTO.AddDTO dto) {
        //采购价目表的id
        String priceId = dto.getPurchasePriceId();
        PurchasePriceEntity purchasePrice = purchasePriceService.getById(priceId);
        if (Objects.isNull(purchasePrice)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        String  approveStatus= purchasePrice.getApproveStatus().getStatus();
        if(!approveStatus.equals(ApproveStatusEnum.APPROVE.getStatus())){
            throw new ServiceException(ApiError.ERROR_98029);
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
        if (addResult) {
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
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 14:08
     */
    @Override
    public Boolean addAndSubmit(PurchasePriceChangeDTO.AddDTO dto) {
        PurchasePriceChangeEntity changeEntity = this.add(dto);
        Boolean result = true;
        if (changeEntity != null) {
            String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
            if (!changeEntity.getApproveStatus().equals(ApproveStatusEnum.getByStatus(waitSubmitStatus))) {
                result = updateSubmitApproveStatus(changeEntity, ApproveStatusEnum.APPROVE_ING.getStatus());
                if (result) {
                    //添加日志
                    String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
                    addModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), changeEntity.getId(), "状态变更");
                }
            }
        }
        return null;
    }

    /**
     * 采购价目变更详情
     *
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceChangeDTO.UpdateDTO
     * @author yl
     * @date 2023-03-28 14:24
     */
    @Override
    public PurchasePriceChangeDTO.UpdateDTO view(String id) {
        PurchasePriceChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        PurchasePriceChangeDTO.UpdateDTO updateDTO = new PurchasePriceChangeDTO.UpdateDTO();
        BeanMapper.copy(changeEntity,updateDTO);
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        updateDTO.setAttachmentNameList(attachmentNameList);
        updateDTO.setAttachmentUrlList(attachmentUrlList);
        //获取明细信息
        List<PurchasePriceChangeDetailDTO.UpdateDTO> purchasePriceDetailList = purchasePriceChangeDetailService.getByPriceChangeId(id);
        updateDTO.setPurchasePriceChangeDetailList(purchasePriceDetailList);
        return updateDTO;
    }


    /**
     * 修改状态
     *
     * @param changeEntity
     * @param status
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 14:12
     */
    private Boolean updateSubmitApproveStatus(PurchasePriceChangeEntity changeEntity, String status) {
        if (changeEntity != null) {
            changeEntity.setApproveStatus(ApproveStatusEnum.getByStatus(status));
            return this.updateById(changeEntity);
        }
        return false;
    }


    /**
     * 添加日志
     *
     * @param content
     * @param code
     * @param businessId
     * @param operation
     * @return void
     * @author yl
     * @date 2023-03-28 12:25
     */
    private void addModuleOperateLog(String content, String code, String businessId, String operation) {
        moduleOperateLogService.addModuleOperateLog(content, code, businessId, operation);

    }
}
