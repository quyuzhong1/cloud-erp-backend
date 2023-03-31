package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.SearchType;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.erp.server.scm.constant.ScmConstant;
import com.erp.server.scm.mapper.SupplierPhaseMapper;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.SupplierPhaseService;
import com.erp.server.scm.service.SupplierService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 供应商升降级 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierPhaseServiceImpl extends SuperServiceImpl<SupplierPhaseMapper, SupplierPhaseEntity> implements SupplierPhaseService {


    @Resource
    private SupplierService supplierService;

    @Resource
    private AttachmentService attachmentService;

    /**
     * 添加供应商阶段
     *
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     * @author yl
     * @date 2023-03-23 12:20
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SupplierPhaseDTO.AddDTO dto) {
        SupplierPhaseEntity entity = new SupplierPhaseEntity();
        String supplierId = dto.getSupplierId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商的现阶段
        String phase = supplier.getPhase().getPhase();
        if (!phase.equals(dto.getCurrentPhase())) {
            throw new ServiceException(ApiError.ERROR_98020);
        }
        //检查阶段能否变更
        checkPhase(phase, dto.getTargetPhase(), dto.getOperateType());
        BeanUtil.copyProperties(dto, entity, dto.getCurrentPhase(), dto.getTargetPhase());
        String id = IdWorker.getIdStr();
        entity.setId(id);
        Boolean result = this.save(entity);
        if (result) {
            Class<SupplierPhaseEntity> credentialClass = SupplierPhaseEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件信息
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);
            return id;
        }
        return "";
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 16:34
     */
    @Override
    public Boolean addAndSubmit(SupplierPhaseDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 供应商阶段 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 16:50
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //启动流程 todo
        Boolean result = updateApproveStatus(list, ApproveStatusEnum.APPROVE_ING.getStatus());
        return result;
    }


    /**
     * 供应商详情阶段
     *
     * @param supplierPhaseId
     * @return com.erp.model.scm.dto.SupplierPhaseDTO.UpdateDTO
     * @author yl
     * @date 2023-03-23 17:12
     */
    @Override
    public SupplierPhaseDTO.UpdateDTO view(String supplierPhaseId) {
        SupplierPhaseEntity phase = this.getById(supplierPhaseId);
        if (Objects.isNull(phase)) {
            throw new ServiceException(ApiError.ERROR_98018);
        }
        SupplierPhaseDTO.UpdateDTO dto = new SupplierPhaseDTO.UpdateDTO();
        BeanMapper.copy(phase, dto);
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(supplierPhaseId));
        List<String> urlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> nameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        dto.setAttachmentUrlList(urlList);
        dto.setAttachmentNameList(nameList);
        return dto;
    }


    /**
     * 更改供应商阶段
     *
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     * @author yl
     * @date 2023-03-23 17:23
     */
    @Override
    public String updateSupplierPhase(SupplierPhaseDTO.UpdateDTO dto) {
        String id = dto.getId();
        SupplierPhaseEntity phase = this.getById(id);
        if (Objects.isNull(phase)) {
            throw new ServiceException(ApiError.ERROR_98018);
        }

        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        //只有待提交 和审核不通过 才能编辑
        if (!statusList.contains(phase.getApproveStatus())) {
            new ServiceException(ApiError.ERROR_98021);
        }

        //当前阶段
        String currentPhase = dto.getCurrentPhase();
        //目标阶段
        String targetPhase = dto.getTargetPhase();
        String type = dto.getOperateType();
        checkPhase(currentPhase, targetPhase, type);

        BeanMapper.copy(phase, dto);
        Boolean result = this.updateById(phase);
        if (result) {
            attachmentService.deleteByBusinessIds(Arrays.asList(id));
            Class<SupplierPhaseEntity> credentialClass = SupplierPhaseEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String attachmentType = tableName.value();
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), attachmentType, id);
            return id;
        }
        return "";
    }

    /**
     * 审核供应商阶段
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:43
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        /**
         * TODO
         * 还需要检查是否是自己能否审核
         */
        //审核通过
        if (dto.getType().equals(ApproveTypeEnum.PASS.getStatus())) {
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            Boolean result = this.updateApproveStatus(list, approveStatus);
            return result;
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            Boolean result = this.updateApproveStatus(list, rejectStatus);
            return result;
        }
    }


    /**
     * 删除供应商阶段
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:54
     */
    @Override
    public Boolean deleteByIds(List<String> ids) {
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        return this.removeByIds(ids);
    }


    /**
     * 取消流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:58
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 这里要调用工作流服务取消流程

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, waitSubmitStatus);

        return result;
    }


    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierPhaseDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-23 18:15
     */
    @Override
    public PagingVO<SupplierPhaseDTO.PagingViewDTO> paging(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        SupplierPhaseDTO.PagingParamDTO params = dto.getParams();
        params.setParam(dto.getParam());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());

        String searchType = params.getSearchType();
        List<String> supplierPhaseIdList = new ArrayList<>();

        //待我审核
        if (searchType.equals(SearchType.WAIT_APPROVE)) {

        }
        IPage pageData = baseMapper.paging(query, params, supplierPhaseIdList);
        List<SupplierPhaseDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        for (SupplierPhaseDTO.PagingViewDTO item : list) {
            String type = item.getOperateType();
            item.setTypeName(type.equals(ScmConstant.DEGRADE) ? "降级" : "升级");
            //当前阶段
            String currentPhase = item.getCurrentPhase();
            String currentPhaseName = SupplierPhaseEnum.getPhaseName(currentPhase);
            item.setCurrentPhaseName(currentPhaseName);

            //目标阶段
            String targetPhase = item.getTargetPhase();
            String targetPhaseName = SupplierPhaseEnum.getPhaseName(targetPhase);
            item.setTargetPhaseName(targetPhaseName);
            String approveStatus = item.getApproveStatus();
            item.setApproveStatusName(ApproveStatusEnum.getName(approveStatus));
        }
        return new PagingVO(pageData);
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-29 16:20
     */
    @Override
    public Boolean updateAndSubmit(SupplierPhaseDTO.UpdateDTO dto) {
        String id = this.updateSupplierPhase(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }


    /**
     * 获取阶段变更的时候 获取阶段列表
     *
     * @param dto
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     * @author yl
     * @date 2023-03-31 14:26
     */
    @Override
    public List<BaseDropDownDTO.CommonDTO> listByChange(SupplierPhaseDTO.ListDTO dto) {
        //当前等级
        String currentPhase = dto.getCurrentPhase();
        //操作
        String operateType = dto.getOperateType();
        List<BaseDropDownDTO.CommonDTO> resultList = new ArrayList<>(4);
        //潜在
        String potential = SupplierPhaseEnum.POTENTIAL.getPhase();
        //准入
        String access = SupplierPhaseEnum.ACCESS.getPhase();
        //合格
        String conform = SupplierPhaseEnum.CONFORM.getPhase();
        //淘汰
        String eliminate = SupplierPhaseEnum.ELIMINATE.getPhase();
        SupplierPhaseEnum[] phaseList = SupplierPhaseEnum.values();
        List<SupplierPhaseEnum> list = Arrays.asList(phaseList);

        //当 当前阶段为潜在
        if (currentPhase.equals(potential)) {
            //阶段降级
            if (operateType.equals(ScmConstant.DEGRADE)) {
                BaseDropDownDTO.CommonDTO common = new BaseDropDownDTO.CommonDTO();
                common.setCode(eliminate);
                common.setValue(SupplierPhaseEnum.getPhaseName(eliminate));
                resultList.add(common);
            } else {
                //升级 【潜在】只能升级为【准入】【合格】
                List<BaseDropDownDTO.CommonDTO> potentialList = list.stream().filter(p -> Arrays.asList(access, conform).contains(p.getPhase()))
                        .map(x -> new BaseDropDownDTO.CommonDTO(x.getPhase(), x.getName()))
                        .collect(Collectors.toList());
                resultList.addAll(potentialList);
            }
        }

        //当 当前阶段为准入
        if (currentPhase.equals(access)) {
            //阶段降级 【准入】只能降级【潜在】【淘汰】
            if (operateType.equals(ScmConstant.DEGRADE)) {
                List<BaseDropDownDTO.CommonDTO> accessList = list.stream().filter(p -> Arrays.asList(eliminate, potential).contains(p.getPhase()))
                        .map(x -> new BaseDropDownDTO.CommonDTO(x.getPhase(), x.getName()))
                        .collect(Collectors.toList());
                resultList.addAll(accessList);
            } else {
                //升级 为合格
                List<BaseDropDownDTO.CommonDTO> accessList = list.stream().filter(p -> p.getPhase().equals(conform))
                        .map(x -> new BaseDropDownDTO.CommonDTO(x.getPhase(), x.getName()))
                        .collect(Collectors.toList());
                resultList.addAll(accessList);

            }
        }

        //当 当前阶段为 合格的时候
        if (currentPhase.equals(conform)) {
            //阶段降级 【合格】只能降级【准入】【潜在】
            if (operateType.equals(ScmConstant.DEGRADE)) {
                List<BaseDropDownDTO.CommonDTO> conformList = list.stream().filter(p -> Arrays.asList(access, potential).contains(p.getPhase()))
                        .map(x -> new BaseDropDownDTO.CommonDTO(x.getPhase(), x.getName()))
                        .collect(Collectors.toList());
                resultList.addAll(conformList);
            }
        }

        //当 当前阶段为 淘汰的时候
        if (currentPhase.equals(eliminate)) {
            //阶段升级 【淘汰】升级只能选择【准入】【合格】
            if (!operateType.equals(ScmConstant.DEGRADE)) {
                List<BaseDropDownDTO.CommonDTO> eliminateList = list.stream().filter(p -> Arrays.asList(access, conform).contains(p.getPhase()))
                        .map(x -> new BaseDropDownDTO.CommonDTO(x.getPhase(), x.getName()))
                        .collect(Collectors.toList());
                resultList.addAll(eliminateList);
            }
        }
        return resultList;
}


    /**
     * 更改状态
     *
     * @param list
     * @param approveStatus
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:50
     */
    private Boolean updateApproveStatus(List<SupplierPhaseEntity> list, String approveStatus) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(approveStatus));
            return this.updateBatchById(list);
        }
        return false;
    }


    /**
     * 检查阶段能否变更
     *
     * @param currentPhase 当前阶段
     * @param targetPhase  目标阶段
     * @return void
     * @author yl
     * @date 2023-03-23 14:04
     */
    private void checkPhase(String currentPhase, String targetPhase, String type) {
        //潜在
        String potential = SupplierPhaseEnum.POTENTIAL.getPhase();
        //准入
        String access = SupplierPhaseEnum.ACCESS.getPhase();
        //合格
        String conform = SupplierPhaseEnum.CONFORM.getPhase();
        //淘汰
        String eliminate = SupplierPhaseEnum.ELIMINATE.getPhase();

        //当 当前阶段为潜在
        if (currentPhase.equals(potential)) {
            //阶段降级
            if (type.equals(ScmConstant.DEGRADE)) {
                if (!targetPhase.equals(eliminate)) {
                    throw new ServiceException(98018, "【潜在】只能降级为【淘汰】");
                }
            } else {
                //升级
                if (!Arrays.asList(access, conform).contains(targetPhase)) {
                    throw new ServiceException(98018, "【潜在】只能升级为【准入】【合格】");
                }
            }
        }

        //当 当前阶段为准入
        if (currentPhase.equals(access)) {
            //阶段降级
            if (type.equals(ScmConstant.DEGRADE)) {
                if (!Arrays.asList(eliminate, potential).contains(targetPhase)) {
                    throw new ServiceException(98018, "【准入】只能降级【潜在】【淘汰】");
                }
            } else {
                //升级 为合格
                if (!targetPhase.equals(conform)) {
                    throw new ServiceException(98018, "升级只能选择【合格】");
                }
            }
        }

        //当 当前阶段为 合格的时候
        if (currentPhase.equals(conform)) {
            //阶段降级
            if (type.equals(ScmConstant.DEGRADE)) {
                if (!Arrays.asList(access, potential).contains(targetPhase)) {
                    throw new ServiceException(98018, "【合格】只能降级【准入】【潜在】");
                }
            } else {
                throw new ServiceException(98018, "当前阶段不能升级");
            }
        }

        //当 当前阶段为 淘汰的时候
        if (currentPhase.equals(eliminate)) {
            //阶段降级
            if (type.equals(ScmConstant.DEGRADE)) {
                throw new ServiceException(98018, "当前阶段不能降级");
            } else {
                //升级
                if (!Arrays.asList(access, conform).contains(targetPhase)) {
                    throw new ServiceException(98018, "【淘汰】升级只能选择【准入】【合格】");
                }
            }
        }

    }


}
