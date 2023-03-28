package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
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
import com.erp.server.scm.constant.ScmConstant;
import com.erp.server.scm.mapper.PurchasePriceChangeMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        String approveStatus = purchasePrice.getApproveStatus().getStatus();
        if (!approveStatus.equals(ApproveStatusEnum.APPROVE.getStatus())) {
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

            //添加价格变更明细
            purchasePriceChangeDetailService.addPriceChangeDetail(id, dto.getPurchasePriceChangeDetailList());

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
    public PurchasePriceChangeDTO.ViewDTO view(String id) {
        PurchasePriceChangeEntity changeEntity = this.getById(id);
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.ERROR_98028);
        }
        PurchasePriceChangeDTO.ViewDTO viewDTO = new PurchasePriceChangeDTO.ViewDTO();
        BeanMapper.copy(changeEntity, viewDTO);
        //附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(id);
        List<String> attachmentUrlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachmentNameList(attachmentNameList);
        viewDTO.setAttachmentUrlList(attachmentUrlList);
        //获取明细信息
        List<PurchasePriceChangeDetailDTO.ViewDTO> purchasePriceDetailList = purchasePriceChangeDetailService.getByPriceChangeId(id);
        viewDTO.setPurchasePriceChangeDetailList(purchasePriceDetailList);
        return viewDTO;
    }


    /**
     * 修改采购价目变更
     *
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceChangeEntity
     * @author yl
     * @date 2023-03-28 16:40
     */
    @Override
    public PurchasePriceChangeEntity updatePurchasePriceChange(PurchasePriceChangeDTO.UpdateDTO dto) {
        return null;
    }


    /**
     * 删除 采购价目变更
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:42
     */
    @Override
    public Boolean deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceChangeEntity> priceChangeList = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = priceChangeList.stream().filter(p -> !p.getApproveStatus().getStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除价目表
        Boolean result = this.removeByIds(ids);
        if (result) {
            //添加日志
            String content = "删除价目表[%s]";
            List<Pair<String, String>> pairList = priceChangeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "删除");
            attachmentService.deleteByBusinessIds(ids);
        }

        return result;
    }


    /**
     * 采购价目变更 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:47
     */
    @Override
    public Boolean submitApprove(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceChangeEntity> priceChangeList = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = priceChangeList.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        Boolean result = this.updateApproveStatus(priceChangeList, ApproveStatusEnum.getByStatus(ingStatus));
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            List<Pair<String, String>> pairList = priceChangeList.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(waitSubmitStatus))).
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "状态变更");

            //审核不通过
            String rejectContent = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.REJECT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            List<Pair<String, String>> rejectPairList = priceChangeList.stream().filter(s -> s.getApproveStatus().equals(ApproveStatusEnum.getByStatus(rejectStatus))).
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(rejectContent, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), rejectPairList, "状态变更");

        }

        return null;
    }


    /**
     * 采购价目变更 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:52
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<PurchasePriceChangeEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String ingStatusName = ApproveStatusEnum.APPROVE_ING.getName();
        //意见
        String comment = dto.getComment();
        Boolean result = true;
        String content = "";
        Boolean isPass = false;
        if (dto.getType().equals(ScmConstant.PASS)) {
            isPass = true;
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus));
            content = String.format("状态由[%s]变更为[%s]", ingStatusName, ApproveStatusEnum.APPROVE.getName());
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus));
            content = String.format("状态由[%s]变更为[%s] 【不通过原因:%s】", ingStatusName, ApproveStatusEnum.REJECT.getName(), comment);
        }
        if (result) {
            //当是审核通过的时候 就要去复写 且添加历史数据
             if(isPass){

             }

            //添加日志
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "状态变更");
        }

        return result;
    }


    /**
     * 取消流程
     * @author yl
     * @date 2023-03-28 16:56
     * @param ids
     * @return java.lang.Boolean
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<PurchasePriceChangeEntity> list = this.listByIds(ids);
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));
        if (result) {
            String content = String.format("状态由[%s]变更为[%s] ", ApproveStatusEnum.APPROVE_ING.getName(), ApproveStatusEnum.WAIT_SUBMIT.getName());
            List<Pair<String, String>> pairList = list.stream().
                    map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            batchAddModuleOperateLog(content, ModuleTypeEnum.PURCHASE_PRICE_CHANGE.getCode(), pairList, "取消流程");
        }
        return result;
    }


    /**
     * 修改状态
     *
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 16:50
     */
    private Boolean updateApproveStatus(List<PurchasePriceChangeEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return false;
    }


    /**
     * 批量添加日志
     *
     * @param content
     * @param code
     * @param pairList
     * @param operation
     * @return void
     * @author yl
     * @date 2023-03-28 16:46
     */
    private void batchAddModuleOperateLog(String content, String code, List<Pair<String, String>> pairList, String operation) {
        moduleOperateLogService.batchAddModuleOperateLog(content, code, pairList, operation);

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
