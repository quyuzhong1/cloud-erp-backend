package com.erp.server.plm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.dto.excel.SkuStdCostChangeExcelDTO;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.erp.model.plm.entity.SkuStdCostEntity;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.enums.SkuStdCostImportTypeEnum;
import com.erp.model.plm.enums.SkuStdCostTabEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.model.tms.dto.excel.FirstMileReconciliationStandardExcelDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.listener.SkuStdCostChangeExcelListener;
import com.erp.server.plm.mapper.SkuStdCostDetailMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.SkuStdCostDetailService;
import com.erp.server.plm.service.SkuStdCostService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_COUNTRY;

/**
 * <p>
 * sku标准成本明细表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Slf4j
@Service
public class SkuStdCostDetailServiceImpl extends SuperServiceImpl<SkuStdCostDetailMapper, SkuStdCostDetailEntity> implements SkuStdCostDetailService {

    @Resource
    private WorkflowFeign workflowFeign;
    @Autowired
    private CommonService commonService;
    @Resource
    private SkuStdCostService skuStdCostService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @Override
    public List<SkuStdCostDetailDTO.ListDTO> listDTOByIds(BaseIdsDTO.IdsDTO dto) {
        return baseMapper.listDTOByIds(dto);
    }


    /**
     * 校验单据是否可以变更
     *
     * @param commonDTO   公共修改参数
     * @param listDTO     当前提交的sku标准成本信息
     * @param lastListDTO 最新sku标准成本信息
     */
    private SkuStdCostDetailEntity validateChange(SkuStdCostDetailDTO.ChangeCommonDTO commonDTO, SkuStdCostDetailDTO.ListDTO listDTO, SkuStdCostDetailDTO.ListDTO lastListDTO) {
        LocalDate submitEffectiveDate = commonDTO.getEffectiveDate();
        // 1.校验状态：仅支持【已审核】变更
        if (!ApproveStatusEnum.APPROVE.getCode().equalsIgnoreCase(listDTO.getApproveStatus())) {
            ServiceException.runError("SKU={}:仅支持【已审核】变更", listDTO.getSkuNo());
        }
        // 2.校验日期：生效日期必须大于历史日期
        if (!submitEffectiveDate.isAfter(listDTO.getEffectiveDate())) {
            ServiceException.runError("生效日期必须大于历史日期：当前生效日期={}, 最新生效日期={}", submitEffectiveDate, listDTO.getEffectiveDate());
        }

        // 3.限制类型：组合品不支持变更
        if (listDTO.getIsComb()) {
            ServiceException.runError("SKU={}:组合品不支持变更", listDTO.getSkuNo());
        }
        if (null != lastListDTO) {
            if (!ApproveStatusEnum.APPROVE.getCode().equalsIgnoreCase(lastListDTO.getApproveStatus())) {
                ServiceException.runError("SKU={}:存在【未审核】记录,不支持变更", lastListDTO.getSkuNo());
            }
            // 4.限制历史：历史单据不支持变更-只有最新的SKU支持变更
            if (!listDTO.getId().equals(lastListDTO.getId())) {
                ServiceException.runError("历史单据不支持变更-只有最新的SKU支持变更:sku={},最新生效时间={}", listDTO.getSkuNo(), lastListDTO.getEffectiveDate());
            }
        }
        SkuStdCostDetailEntity newDetailEntity = new SkuStdCostDetailEntity();
        newDetailEntity.setStdCostPrice(commonDTO.getStdCostPrice());
        newDetailEntity.setEffectiveDate(commonDTO.getEffectiveDate());
        newDetailEntity.setCurrency(commonDTO.getCurrency());
        newDetailEntity.setMainId(listDTO.getMainId());
        return newDetailEntity;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO changeAdd(SkuStdCostDetailDTO.ChangeDTO addDTO, SkuStdCostDetailDTO.ListDTO listDTO, SkuStdCostDetailDTO.ListDTO lastListDTO) {
        // 数据校验
        SkuStdCostDetailEntity newDetailEntity = validateChange(addDTO, listDTO, lastListDTO);

        log.info("开始新增sku标准成本单");
        boolean save = super.save(newDetailEntity);
        if (!save) {
            throw new ServiceException("sku标准成本单保存失败");
        }
        return BatchResultDTO.success(newDetailEntity.getId(), "");
    }


    /**
     * 修改处理数据
     */
    private void updateHandleData(SkuStdCostDetailEntity old, SkuStdCostDetailDTO.UpdateDTO addOrUpdateDTO) {
        // 验证数据 & 数据赋值
        old.setStdCostPrice(addOrUpdateDTO.getStdCostPrice());
        // 校验日期
        if (old.getEffectiveDate() == null && addOrUpdateDTO.getEffectiveDate() == null) {
            ServiceException.runError("【生效日期】不能为空");
        }
        if (old.getEffectiveDate() != null && addOrUpdateDTO.getEffectiveDate() != null) {
            ServiceException.runError("【生效日期】不可修改");
        }
        // 设置值（只有新增时才会走到这里）
        if (old.getEffectiveDate() == null) {
            old.setEffectiveDate(addOrUpdateDTO.getEffectiveDate());
        }
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SkuStdCostDetailDTO.UpdateDTO addOrUpdateDTO) {
        SkuStdCostDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "sku标准成本单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        // 数据处理
        updateHandleData(old, addOrUpdateDTO);
        log.info("编辑 开始修改sku标准成本单数据，单号：【{}】", old.getId());
        boolean save = super.updateById(old);
        if (!save) {
            throw new ServiceException("sku标准成本单保存失败");
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SkuStdCostDetailDTO.ListDTO> paging(PagingDTO<SkuStdCostDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SkuStdCostDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SkuStdCostDetailDTO.TabListDTO> tabList(PermissionsDTO param) {
        SkuStdCostDetailDTO.PagingParamDTO searchParam = new SkuStdCostDetailDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SkuStdCostDetailDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        Map<String, Integer> approveStatutCountMap = list.stream().collect(Collectors.toMap(SkuStdCostDetailDTO.TabListDTO::getTabFlag, SkuStdCostDetailDTO.TabListDTO::getCount));
        // 获取状态列表
        List<String> tabCodeList = SkuStdCostTabEnum.getCodeList();
        // 查询每个sku最新
        Long allNewCount = baseMapper.allNewCount(searchParam);

        List<SkuStdCostDetailDTO.TabListDTO> resultList = new LinkedList<>();
        tabCodeList.forEach(tabCode -> {
            //  全部=业务要求最新 ALL("all", "全部"),
            if (SkuStdCostTabEnum.ALL.getCode().equals(tabCode)) {
                // 计算合计数量
                resultList.add(new SkuStdCostDetailDTO.TabListDTO(SkuStdCostTabEnum.ALL.getCode(), SkuStdCostTabEnum.ALL.getName(), allNewCount.intValue()));
                return;
            }
            // TO_BE_APPROVE("toBeApprove", "待我审核"),
            if (SkuStdCostTabEnum.TO_BE_APPROVE.getCode().equals(tabCode)) {
                //需要审核的业务ids
                List<String> businessIds = commonService.listProcessCurBusinessIds(SourceTypeEnum.SKU_STD_COST_DETAIL.getCode());
                int waitApproveCount = 0;
                if (CollectionUtils.isNotEmpty(businessIds)) {
                    List<SkuStdCostDetailEntity> changeEntityList = lambdaQuery().in(SkuStdCostDetailEntity::getId, businessIds).list();
                    changeEntityList = changeEntityList.stream().filter(v -> v.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)).collect(Collectors.toList());
                    waitApproveCount = changeEntityList.size();
                }
                SkuStdCostDetailDTO.TabListDTO tabListDTO = new SkuStdCostDetailDTO.TabListDTO(SkuStdCostTabEnum.TO_BE_APPROVE.getCode(), SkuStdCostTabEnum.TO_BE_APPROVE.getName(), waitApproveCount);
                resultList.add(tabListDTO);
                return;
            }
            //  APPROVE("approve", "已审核"),
            if (SkuStdCostTabEnum.APPROVE.getCode().equals(tabCode)) {
                Integer count = approveStatutCountMap.getOrDefault(tabCode, 0);
                // 计算合计数量
                resultList.add(new SkuStdCostDetailDTO.TabListDTO(SkuStdCostTabEnum.APPROVE.getCode(), SkuStdCostTabEnum.APPROVE.getName(), count));
                return;
            }
            //  REJECT("reject", "不通过"),
            if (SkuStdCostTabEnum.REJECT.getCode().equals(tabCode)) {
                Integer count = approveStatutCountMap.getOrDefault(tabCode, 0);
                // 计算合计数量
                resultList.add(new SkuStdCostDetailDTO.TabListDTO(SkuStdCostTabEnum.REJECT.getCode(), SkuStdCostTabEnum.REJECT.getName(), count));
                return;
            }
            //  HISTORY("history", "历史价格")(所有)
            if (SkuStdCostTabEnum.HISTORY.getCode().equals(tabCode)) {
                // 计算合计数量
                resultList.add(new SkuStdCostDetailDTO.TabListDTO(SkuStdCostTabEnum.HISTORY.getCode(), SkuStdCostTabEnum.HISTORY.getName(), list.stream().mapToInt(SkuStdCostDetailDTO.TabListDTO::getCount).sum()));
                return;
            }
        });
        return resultList;
    }

    @Override
    public void exportList(SkuStdCostDetailDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("SKU标准成本Excel导出", FileTaskEventEnum.EXPORT_PLM_SKU_STD_COST.getCode(), dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SkuStdCostDetailEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到sku标准成本单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改sku标准成本单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动sku标准成本单流程，id=：【{}】", entity.getId());
        startProcess(entity);

        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.SUBMIT);
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SkuStdCostDetailDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SkuStdCostDetailEntity entity = getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到sku标准成本单数据"));
        SkuStdCostEntity mainEntity = skuStdCostService.getByIdOpt(entity.getMainId()).orElseThrow(() -> new ServiceException("未找到sku标准成本单主单数据"));
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);

        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), mainEntity.getSkuNo(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(SkuStdCostDetailEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(SourceTypeEnum.SKU_STD_COST_DETAIL.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(SkuStdCostDetailEntity entity) {
        SkuStdCostEntity mainEntity = skuStdCostService.getByIdOpt(entity.getMainId()).orElseThrow(() -> new ServiceException("未找到sku标准成本单主单数据"));

        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(entity.getId(), ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        return BatchResultDTO.success(entity.getId(), mainEntity.getSkuNo(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SkuStdCostDetailEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SkuStdCostDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到sku标准成本单数据"));
        SkuStdCostEntity mainEntity = skuStdCostService.getByIdOpt(entity.getMainId()).orElseThrow(() -> new ServiceException("未找到sku标准成本单主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）
        // 删除主单数据
        log.info("删除 开始删除sku标准成本单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        return BatchResultDTO.success(entity.getId(), mainEntity.getSkuNo(), OperationTypeEnum.DELETE);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SkuStdCostDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到sku标准成本单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        SkuStdCostEntity mainEntity = skuStdCostService.getByIdOpt(entity.getMainId()).orElseThrow(() -> new ServiceException("未找到sku标准成本单主单数据"));
        //  撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改sku标准成本单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(SourceTypeEnum.SKU_STD_COST_DETAIL.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), mainEntity.getSkuNo(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SkuStdCostDetailEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }


    @Override
    public SkuStdCostDetailDTO.ViewDTO view(String id) {
        List<SkuStdCostDetailDTO.ListDTO> listDTOS = baseMapper.listDTOByIds(new BaseIdsDTO.IdsDTO(Collections.singletonList(id)));
        if (CollectionUtils.isEmpty(listDTOS)) {
            ServiceException.runError("未找到sku标准成本单数据");
        }
        SkuStdCostDetailDTO.ListDTO listDTO = listDTOS.get(0);
        SkuStdCostDetailDTO.ViewDTO data = BeanMapperUtils.map(SkuStdCostDetailDTO.ViewDTO.class, listDTO);
        // 数据填充处理
        fillOne(data);

        return data;
    }

    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(SkuStdCostDetailEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getId());
        // 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(SourceTypeEnum.SKU_STD_COST_DETAIL.getCode());
        startDTO.setBusinessName(entity.getId());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    private void fillOne(SkuStdCostDetailDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        // 审核状态
        data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        // 销售状态名称
        data.setSaleStateName(SaleStateEnum.getNameByCode(data.getSaleState()));
        // 币种符号
        if (StringUtils.isNotBlank(data.getCurrency())) {
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Collections.singletonList(data.getCurrency()));
            data.setCurrencySymbol(currencyList.get(0).getSymbol());
        } else {
            data.setCurrencySymbol("");
        }
    }

    /**
     * 审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SkuStdCostDetailEntity::getId, id)
                .set(SkuStdCostDetailEntity::getApproveUserId, userInfo.getUid())
                .set(SkuStdCostDetailEntity::getApproveUserName, userInfo.getUserName())
                .set(SkuStdCostDetailEntity::getApproveStatus, approveStatus)
                .set(SkuStdCostDetailEntity::getApproveTime, LocalDateTime.now())
                .update(new SkuStdCostDetailEntity());
    }

    /**
     * 反审核更新审核信息
     *
     * @param id
     * @param approveStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SkuStdCostDetailEntity::getId, id)
                .set(SkuStdCostDetailEntity::getApproveUserId, "")
                .set(SkuStdCostDetailEntity::getApproveUserName, "")
                .set(SkuStdCostDetailEntity::getApproveStatus, approveStatus)
                .set(SkuStdCostDetailEntity::getApproveTime, null)
                .update(new SkuStdCostDetailEntity());
    }

    /**
     * 更新审核状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SkuStdCostDetailEntity::getId, id)
                .set(SkuStdCostDetailEntity::getApproveStatus, approveStatus)
                .update(new SkuStdCostDetailEntity());
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<SkuStdCostDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //币种信息
        List<String> currencyIdList = list.stream().map(SkuStdCostDetailDTO.ListDTO::getCurrency)
                .distinct()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Map<String, String> currencyMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(currencyIdList)) {
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
            currencyMap = currencyList.stream().collect(Collectors.toMap(CurrencyDTO.ViewDTO::getId, CurrencyDTO.ViewDTO::getSymbol));
        }

        // 属性赋值
        for (SkuStdCostDetailDTO.ListDTO data : list) {
            // 审核状态
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // 销售状态名称
            data.setSaleStateName(SaleStateEnum.getNameByCode(data.getSaleState()));
            // 币种
            data.setCurrencySymbol(currencyMap.getOrDefault(data.getCurrency(), ""));
        }
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void validateSubmit(SkuStdCostDetailEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if (!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
    }

    @Override
    public boolean importFile(SkuStdCostDetailDTO.ExcelImportDTO importDTO, HttpServletResponse response) {
        switch (importDTO.getImportType()) {
            case CHANGE:
                return importChangeFile(importDTO.getExcelFile(), response);
            case UPDATE:
                return importUpdateFile(importDTO.getExcelFile(), response);
            default:
                throw new ServiceException("输入导入的类型有误");
        }
    }

    @Override
    public PagingVO<SkuStdCostDetailDTO.ListDTO> historyPaging(PagingDTO<SkuStdCostDetailDTO.HistoryPagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SkuStdCostDetailDTO.ListDTO> pageData = baseMapper.historyPaging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public Map<String, SkuStdCostDetailDTO.ListDTO> mapLastBySkuIds(List<String> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        List<SkuStdCostDetailDTO.ListDTO> lastList = baseMapper.lastList(skuIds, null);
        return lastList.stream().collect(Collectors.toMap(SkuStdCostDetailDTO.ListDTO::getSkuId, e -> e));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO comboRecalculate(SkuStdCostDetailDTO.ListDTO listDTO) {
        validateComboRecalculate(listDTO);

        // TODO 重算组合品单据并更新

        return BatchResultDTO.success(listDTO.getId(), listDTO.getSkuNo(), OperationTypeEnum.UPDATE);
    }

    private void validateComboRecalculate(SkuStdCostDetailDTO.ListDTO listDTO) {
        if (!listDTO.getIsComb()){
            ServiceException.runError("仅支持组合品价格重算");
        }
        if (!ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(listDTO.getApproveStatus())){
            ServiceException.runError("仅支持【待提交】组合品价格重算");
        }
    }

    @Override
    public PagingVO<SkuStdCostDetailDTO.ListDTO> listExport(PagingDTO<SkuStdCostDetailDTO.ExportDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SkuStdCostDetailDTO.ListDTO> pageData = baseMapper.listExport(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private boolean importUpdateFile(@NotNull(message = "导入文件不能为空") MultipartFile excelFile, HttpServletResponse response) {
        SkuStdCostChangeExcelListener excelListenerUtil = new SkuStdCostChangeExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FirstMileReconciliationStandardExcelDTO.class, excelListenerUtil).sheet(0).headRowNumber(2) .doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        SkuStdCostChangeExcelDTO importDTO = new SkuStdCostChangeExcelDTO();
        //导入数据处理
        List<SkuStdCostChangeExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<SkuStdCostChangeExcelDTO> errorList = excelListenerUtil.getErrorList();
        //导入数据保存
//        List<TmsFirstMileReconciliationDetailDTO.ListDTO> successImportList = handleImportData(successList, errorList);

        String url = "";
        if (!org.springframework.util.CollectionUtils.isEmpty(errorList)) {
            String fileName = "SKU标准成本变更导入错误数据.xlsx";
            // 根据 no 进行数值排序
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SkuStdCostChangeExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        String excelPath = "excel/skuStdCostError.xlsx";
        String name = "skuStdCostError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
        return Boolean.FALSE;
    }

    private boolean importChangeFile(@NotNull(message = "导入文件不能为空") MultipartFile excelFile, HttpServletResponse response) {
        return false;
    }
}
