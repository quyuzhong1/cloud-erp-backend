package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoB2cRefundDTO;
import com.erp.model.oms.entity.SoB2cRefundDetailEntity;
import com.erp.model.oms.entity.SoB2cRefundEntity;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.mapper.SoB2cRefundMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cRefundDetailService;
import com.erp.server.oms.service.SoB2cRefundService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_BI_RETURN_INFO;

/**
 * <p>
 * 退款订单 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-25
 */
@Slf4j
@Service
public class SoB2cRefundServiceImpl extends SuperServiceImpl<SoB2cRefundMapper, SoB2cRefundEntity> implements SoB2cRefundService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SoB2cRefundDetailService soB2cRefundDetailService;


    @Resource
    private WorkflowFeign workflowFeign;

    /**
     * 售后订单分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RefundOrderDTO.PagingViewDTO>
     * @author yl
     * @date 2023-08-25 14:09
     */
    @Override
    public PagingVO<SoB2cRefundDTO.PagingViewDTO> paging(PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto) {
        SoB2cRefundDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<SoB2cRefundDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SoB2cRefundDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<SoB2cRefundDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportExcel(SoB2cRefundDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("退款订单导出", EXPORT_BI_RETURN_INFO.getCode(), dto);
    }

    @Override
    public PagingVO<SoB2cRefundDTO.PagingViewDTO> exportRefund(PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto) {
        SoB2cRefundDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<SoB2cRefundDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SoB2cRefundDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<SoB2cRefundDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //填充数据
        fillDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(SoB2cRefundEntity soB2cRefundEntity, List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_TKD);
        soB2cRefundEntity.setCode(code);
        boolean save = this.save(soB2cRefundEntity);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_1019);
        }

        //操作日志
        operateLogService.addModuleOperateLog(String.format("新增退款单【%s】", code), ModuleTypeEnum.REFUND_ORDER.getCode(), soB2cRefundEntity.getId(), "新增操作");
        if(CollectionUtils.isEmpty(soB2cRefundDetailEntityList)){
            return new BaseResultDTO.AddDTO(soB2cRefundEntity.getId(), code);
        }
        soB2cRefundDetailEntityList.forEach(v->v.setMainId(soB2cRefundEntity.getId()));
        soB2cRefundDetailService.saveBatch(soB2cRefundDetailEntityList);
        return new BaseResultDTO.AddDTO(soB2cRefundEntity.getId(), code);
    }

    @Override
    public SoB2cRefundEntity getByPlatformRefundCode(String platformRefundNo) {
        if(StringUtils.isBlank(platformRefundNo)){
            return null;
        }
        return lambdaQuery().eq(SoB2cRefundEntity::getPlatformRefundNo,platformRefundNo).last( SqlConstants.LIMIT_1).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(SoB2cRefundEntity entity, Boolean isNeedProcess) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //未作废、待提交、审核不通过才可以提交
        if ((!entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                && !entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //提交流程
        if(isNeedProcess){
            startProcess(entity);
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("提交了一个售后订单【%s】",entity.getCode()), ModuleTypeEnum.SO_B2C_REFUND.getCode(),entity.getId(), "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoB2cRefundEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .eq(SoB2cRefundEntity::getId, entity.getId())
                .update();
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    /**
     * 启动流程
     * @author will
     * @date 2025/10/24 12:07
     * @param entity
     * @return void
     */
    public void startProcess(SoB2cRefundEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SO_B2C_REFUND.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(entity.getCreateUserId());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(startDTO);
        if (!listApiResult.isSuccess()) {
            throw new ServiceException(listApiResult.getMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SoB2cRefundEntity entity, ApproveOneDTO dto) {
        //判断是否是审核中的状态
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException("只有审核中的数据允许审核");
        }
        //调用审核流程
        approveProcess(entity,dto);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个售后订单【%s】", ApproveTypeEnum.getName(dto.getType()),entity.getCode()).concat(CharSequenceUtil.isNotBlank(dto.getComment()) ? String.format(",意见：%s", dto.getComment()) : ""), ModuleTypeEnum.SO_B2C_REFUND.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    /**
     * 审核流程调用
     * @author will
     * @date 2025/10/22 16:23
     * @param entity
     * @param dto
     * @return void
     */
    private void approveProcess(SoB2cRefundEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SO_B2C_REFUND.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> listApiResult = workflowFeign.approve(approveDTO);
        Integer code = listApiResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = listApiResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    public Boolean approveEnd(ApproveOneDTO dto, SoB2cRefundEntity entity) {
        //意见
        if (ApproveTypeEnum.PASS.getStatus().equals(dto.getType())) {
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            //审核通过
            lambdaUpdate().set(SoB2cRefundEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoB2cRefundEntity::getApproveUserId, userInfo.getUid())
                    .set(SoB2cRefundEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoB2cRefundEntity::getApproveTime, LocalDateTime.now())
                    .eq(SoB2cRefundEntity::getId, entity.getId())
                    .update();
        } else {
            //审核不通过
            lambdaUpdate().set(SoB2cRefundEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .eq(SoB2cRefundEntity::getId, entity.getId())
                    .update();
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void autoAddApprove(SoB2cRefundEntity soB2cRefundEntity, List<SoB2cRefundDetailEntity> soB2cRefundDetailEntityList) {
        BaseResultDTO.AddDTO addDTO = this.add(soB2cRefundEntity, soB2cRefundDetailEntityList);
        SoB2cRefundEntity submitEntity = this.getById(addDTO.getId());
        BatchResultDTO submit = this.submit(submitEntity, Boolean.FALSE);
        if (!submit.getSuccess()) {
            throw new ServiceException(ApiError.ERROR_1042,"售后订单");
        }
        SoB2cRefundEntity approveEntity = this.getById(addDTO.getId());
        BatchResultDTO approve = this.approve(approveEntity, new ApproveOneDTO(approveEntity.getId(), ApproveTypeEnum.PASS.getStatus(), "自动审核"));
        if (!approve.getSuccess()) {
            throw new ServiceException(ApiError.ERROR_BILL_APPROVE,"售后订单");
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SoB2cRefundEntity entity) {
        //已审核支持反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoB2cRefundEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoB2cRefundEntity::getId, entity.getId())
                .update();
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个售后订单【%s】", entity.getCode()), ModuleTypeEnum.SO_B2C_REFUND.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(SoB2cRefundEntity entity) {
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }

        log.info("撤销 开始修改售后订单状态，id：【{}】", entity.getId());
        //修改状态为待提交
        lambdaUpdate().set(SoB2cRefundEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .eq(SoB2cRefundEntity::getId, entity.getId())
                .update();

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", entity.getId());
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "售后订单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_REFUND.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SO_B2C_REFUND.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    /**
     * 填充数据
     *
     * @param list
     */
    private void fillDb(List<SoB2cRefundDTO.PagingViewDTO> list) {
        List<String> soIds = list.stream().map(v->v.getSoId()).distinct().collect(Collectors.toList());
        List<String> skuIds = list.stream().map(v->v.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVoList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<SoOutstockDetailEntity> allOutList = soOutstockFeign.listDetailBySoIds(soIds);
        for (SoB2cRefundDTO.PagingViewDTO item : list) {
            String dictPlatform = item.getDictPlatform();
            item.setPlatformName(PlatformDictEnum.getNameByCode(dictPlatform));
            String status = item.getStatus();
            String name = RefundOrderStatusEnum.getName(status);
            item.setStatusName(name);
            SkuVO skuVO = skuVoList.stream().filter(s->s.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            item.setProductName(skuVO.getSkuName());
            List<SoOutstockDetailEntity> outList = allOutList.stream().filter(s->s.getSoId().equals(item.getSoId()) && s.getSkuId().equals(item.getSkuId())).collect(Collectors.toList());
            item.setOutQty(outList.stream().map(v->v.getActualQty()).reduce(MathUtil.ZERO, Integer::sum));
        }
    }
}
