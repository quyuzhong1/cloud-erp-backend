package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.QcReportDTO;
import com.erp.model.wms.dto.QcRuleDTO;
import com.erp.model.wms.entity.QcRuleEntity;
import com.erp.model.wms.enums.QcTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcRuleMapper;
import com.erp.server.wms.service.QcReportService;
import com.erp.server.wms.service.QcRuleService;
import org.apache.commons.collections4.CollectionUtils;
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
 * 质检规则 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-13
 */
@Service
public class QcRuleServiceImpl extends SuperServiceImpl<QcRuleMapper, QcRuleEntity> implements QcRuleService {


    @Resource
    private QcReportService qcReportService;

    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 添加质检规则
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-04-13 10:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(QcRuleDTO.AddDTO dto) {
        //TODO 产品等级 校验
        //是否有质检报告
        Boolean existReport = dto.getExistReport();
        //质检报告
        List<QcReportDTO.AddDTO> reportList = dto.getQcReportLList();
        //如果有 报告不能为空
        if (existReport) {
            if (CollectionUtils.isEmpty(reportList)) {
                throw new ServiceException(ApiError.ERROR_NO_EXIST_REPORT);
            }
        }
        QcRuleEntity rule = new QcRuleEntity();
        String id = IdWorker.getIdStr();
        BeanMapper.copy(dto, rule);
        List<String> gradeKeyList = dto.getProductGradeKeyList();
        if (CollectionUtils.isNotEmpty(gradeKeyList)) {
            rule.setProductGradeKey(gradeKeyList.stream().collect(Collectors.joining(",")));
        }
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QCGZ, BusinessNoTypeEnum.CODE_ZJGZ.getCode()));
        rule.setCode(code);
        rule.setId(id);
        Boolean addResult = this.save(rule);
        //添加成功
        if (addResult) {
            //添加质检报告
            qcReportService.addQcReport(id, reportList);
            return id;
        }
        return "";
    }


    /**
     * 质检规则详情
     *
     * @param id
     * @return com.erp.model.wms.dto.QcRuleDTO.ViewDTO
     * @author yl
     * @date 2023-04-13 14:11
     */
    @Override
    public QcRuleDTO.ViewDTO view(String id) {
        QcRuleEntity rule = this.getById(id);
        if (Objects.isNull(rule)) {
            throw new ServiceException(ApiError.ERROR_NO_EXIST_RULE);
        }
        QcRuleDTO.ViewDTO view = new QcRuleDTO.ViewDTO();
        BeanMapper.copy(rule, view);
        //等级
        String gradeKey = rule.getProductGradeKey();
        List<String> gradeKeyList = new ArrayList<>();
        if (StringUtils.isNotBlank(gradeKey)) {
            gradeKeyList = Arrays.asList(gradeKey.split(","));
        }
        view.setProductGradeKeyList(gradeKeyList);
        String qcType = rule.getQcType();
        String qcTypeName = QcTypeEnum.getTypeName(qcType);
        view.setQcTypeName(qcTypeName);
        String approveStatus = rule.getApproveStatus();
        String approveStatusName = ApproveStatusEnum.getName(approveStatus);
        view.setApproveStatusName(approveStatusName);
        List<QcReportDTO.UpdateDTO> qcReportLList = qcReportService.getByQcRuleId(id);
        view.setQcReportLList(qcReportLList);
        return view;
    }


    /**
     * 添加并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 14:34
     */
    @Override
    public Boolean addAndSubmit(QcRuleDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 14:36
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<QcRuleEntity> list = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        Boolean result = this.updateApproveStatus(list, ingStatus);
        return result;
    }


    /**
     * 修改质检规则
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-04-13 14:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateQcRule(QcRuleDTO.UpdateDTO dto) {
        //规则id
        String qcRuleId = dto.getId();
        QcRuleEntity qcRule = this.getById(qcRuleId);
        if (Objects.isNull(qcRule)) {
            throw new ServiceException(ApiError.ERROR_NO_EXIST_RULE);
        }
        String code = qcRule.getCode();
        List<String> gradeKeyList = dto.getProductGradeKeyList();
        BeanMapper.copy(dto, qcRule);
        if (CollectionUtils.isNotEmpty(gradeKeyList)) {
            qcRule.setProductGradeKey(gradeKeyList.stream().collect(Collectors.joining(",")));
        }
        qcRule.setCode(code);
        Boolean result = this.updateById(qcRule);
        if (result) {
            qcReportService.updateQcReport(qcRuleId, dto.getQcReportLList());
            return qcRuleId;
        }
        return "";
    }


    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 15:17
     */
    @Override
    public Boolean updateAndSubmit(QcRuleDTO.UpdateDTO dto) {
        String id = this.updateQcRule(dto);
        if (org.apache.commons.lang3.StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
    }


    /**
     * 审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 15:19
     */
    @Override
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> qcRuleIdList = dto.getIds();
        List<QcRuleEntity> list = this.listByIds(qcRuleIdList);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NO_EXIST_RULE);
        }
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        if (dto.getType().equals(WmsConstant.PASS)) {
            //审核通过
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
     * 反审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 15:23
     */
    @Override
    public Boolean disApprove(List<String> ids) {
        List<QcRuleEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_NO_EXIST_RULE);
        }
        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        List<String> statusList = new ArrayList<>(1);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99003);
        }
        Boolean result = this.updateApproveStatus(list, waitSubmitStatus);
        return result;
    }


    /**
     * 撤销流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 15:33
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<QcRuleEntity> list = this.listByIds(ids);
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, waitSubmitStatus);
        return result;
    }


    /**
     * 删除质检规则
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 15:44
     */
    @Override
    public Boolean deleteByIds(List<String> ids) {
        List<QcRuleEntity> list = this.listByIds(ids);
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !waitSubmitStatus.equals(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        return this.removeByIds(ids);
    }


    /**
     * 分页信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.QcRuleDTO.PagingViewDTO>
     * @author yl
     * @date 2023-04-13 16:02
     */
    @Override
    public PagingVO<QcRuleDTO.PagingViewDTO> paging(PagingDTO<QcRuleDTO.PagingParamDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query);
        List<QcRuleDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        for (QcRuleDTO.PagingViewDTO item : list) {
            String qcType = item.getQcType();
            String qcTypeName = QcTypeEnum.getTypeName(qcType);
            item.setQcTypeName(qcTypeName);
            String approveStatus = item.getApproveStatus();
            String approveStatusName = ApproveStatusEnum.getName(approveStatus);
            item.setApproveStatusName(approveStatusName);
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 更改启用禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 17:10
     */
    @Override
    public Boolean updateDisabledState(UpdateStateDTO dto) {
        String id = dto.getId();
        QcRuleEntity rule = this.getById(id);
        if (Objects.isNull(rule)) {
            throw new ServiceException(ApiError.ERROR_NO_EXIST_RULE);
        }
        rule.setDisabled(dto.getState());
        return this.updateById(rule);
    }


    /**
     * 更改审核状态
     *
     * @param list
     * @param status
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-13 14:40
     */
    private Boolean updateApproveStatus(List<QcRuleEntity> list, String status) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(status));
            return this.updateBatchById(list);
        }
        return true;
    }
}
