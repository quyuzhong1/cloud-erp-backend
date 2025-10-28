package com.erp.server.plm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.dto.excel.SkuStdCostChangeExcelDTO;
import com.erp.model.plm.dto.excel.SkuStdCostUpdateExcelDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.erp.model.plm.entity.SkuStdCostEntity;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.model.plm.enums.*;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.listener.SkuStdCostChangeExcelListener;
import com.erp.server.plm.listener.SkuStdCostUpdateExcelListener;
import com.erp.server.plm.mapper.SkuStdCostDetailMapper;
import com.erp.server.plm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private BomSkuService bomSkuService;
    @Resource
    private OperateLogService operateLogService;

    private static final String CLASSPATH = String.valueOf(SkuStdCostDetailEntity.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAddFirst(ProductDetailEntity entity, LocalDate lastOutstockDate) {
        Integer count = skuStdCostService.lambdaQuery()
                .eq(SkuStdCostEntity::getSkuId, entity.getId())
                .count();
        if (count > 0) {
            return;
        }
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomSkuService.listBomChildBySkuIds(Collections.singletonList(entity.getId()))
                .stream()
                .filter(b ->  BomTypeEnum.COMBINATION.getType().equals(b.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)) {
            // 销售套装不添加
            return;
        }

        SkuStdCostEntity skuStdCostEntity = new SkuStdCostEntity()
                .setSkuId(entity.getId())
                .setSkuNo(entity.getSkuNo())
                .setIsComb(false)
                .setLastOutstockDate(lastOutstockDate)
                ;
        skuStdCostService.save(skuStdCostEntity);
        SkuStdCostDetailEntity detailEntity = new SkuStdCostDetailEntity()
                .setMainId(skuStdCostEntity.getId())
                .setStdCostPrice(BigDecimal.ZERO)
                .setStdSalePrice(BigDecimal.ZERO)
                .setCurrency(CurrencyEnum.CNY.getCurrencyCode())
                .setRemark("")
                .setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT)
                ;
        this.save(detailEntity);
    }

    @Override
    public List<SkuStdCostDetailDTO.ListDTO> listDTOByParams(SkuStdCostDetailDTO.ParamsDTO dto) {
        return baseMapper.listDTOByParams(dto);
    }


    @Override
    public void validateChangeParams(SkuStdCostDetailDTO.ChangeCommonDTO commonDTO, SkuStdCostDetailDTO.ListDTO listDTO, SkuStdCostDetailDTO.ListDTO lastListDTO) {
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
    }


    @Override
    public SkuStdCostDetailEntity buildChangeEntity(SkuStdCostDetailDTO.ChangeCommonDTO commonDTO, String mainId) {
        SkuStdCostDetailEntity newDetailEntity = new SkuStdCostDetailEntity();
        newDetailEntity.setStdCostPrice(commonDTO.getStdCostPrice());
        newDetailEntity.setEffectiveDate(commonDTO.getEffectiveDate());
        newDetailEntity.setCurrency(commonDTO.getCurrency());
        newDetailEntity.setMainId(mainId);
        return newDetailEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @GlobalTransactional(rollbackFor = Exception.class, propagation = io.seata.tm.api.transaction.Propagation.REQUIRES_NEW)
    public BatchResultDTO changeAdd(SkuStdCostDetailDTO.ChangeCommonDTO addDTO, SkuStdCostDetailDTO.ListDTO listDTO, SkuStdCostDetailDTO.ListDTO lastListDTO) {
        // 数据校验
        validateChangeParams(addDTO, listDTO, lastListDTO);
        // 构建实体
        SkuStdCostDetailEntity newDetailEntity = buildChangeEntity(addDTO, listDTO.getMainId());


        log.info("开始新增sku标准成本单");
        boolean save = super.save(newDetailEntity);
        if (!save) {
            throw new ServiceException("sku标准成本单保存失败");
        }
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】,sku:【{}】,生效日期：【{}】, 标准成本(不含税)【{}】",
                UserContext.getDefaultLoginUser().getUserName(),
                "sku标准成本表价格变更",
                listDTO.getSkuNo(),
                addDTO.getEffectiveDate(),
                addDTO.getStdCostPrice() + addDTO.getCurrency()
                );
        //操作日志
        operateLogService.addSysLogBySave(msg, CLASSPATH, newDetailEntity.getId(), listDTO.getSkuId());

        return BatchResultDTO.success(newDetailEntity.getId(), listDTO.getSkuNo());
    }


    /**
     * 修改处理数据
     */
    @Override
    public SkuStdCostDetailEntity updateHandleData(SkuStdCostDetailEntity old, SkuStdCostDetailDTO.UpdateCommonDTO addOrUpdateDTO) {
        SkuStdCostDetailEntity newEntity = new SkuStdCostDetailEntity();
        // 复制旧数据
        BeanUtil.copyProperties(old, newEntity, true);
        // 验证数据 & 数据赋值
        newEntity.setStdCostPrice(addOrUpdateDTO.getStdCostPrice());
        newEntity.setCurrency(addOrUpdateDTO.getCurrency());
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            ServiceException.runError("非【待提交】【审核不通过】状态不能修改");
        }
        // 校验日期
        if (old.getEffectiveDate() == null && addOrUpdateDTO.getEffectiveDate() == null) {
            ServiceException.runError("【生效日期】不能为空");
        }
        if (old.getEffectiveDate() != null && addOrUpdateDTO.getEffectiveDate() != null) {
            ServiceException.runError("【生效日期】不可修改");
        }
        // 设置值（只有新增时才会走到这里）
        if (old.getEffectiveDate() == null) {
            newEntity.setEffectiveDate(addOrUpdateDTO.getEffectiveDate());
        }
        return newEntity;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public Boolean update(SkuStdCostDetailDTO.UpdateDTO addOrUpdateDTO) {
        SkuStdCostDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "sku标准成本单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        return updateAndLog(addOrUpdateDTO, old);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateAndLog(SkuStdCostDetailDTO.UpdateCommonDTO addOrUpdateDTO, SkuStdCostDetailEntity old) {
        // 数据处理
        SkuStdCostDetailEntity newEntity = updateHandleData(old, addOrUpdateDTO);
        SkuStdCostEntity mainEntity = skuStdCostService.getByIdOpt(old.getMainId()).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "sku标准成本主单"));
        log.info("编辑 开始修改sku标准成本单数据，单号：【{}】", old.getId());
        boolean update = super.updateById(newEntity);
        if (!update) {
            throw new ServiceException("sku标准成本单保存失败");
        }
        String msg = CharSequenceUtil.format("编辑【{}】sku标准成本", mainEntity.getSkuNo());
        //操作日志
        operateLogService.addSysLogByUpdate(old, newEntity, CLASSPATH, old.getId(), mainEntity.getSkuId(), msg);
        return update;
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
    public BatchResultDTO submitEntity(SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到sku标准成本单数据");
        }
        String id = entity.getId();
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改sku标准成本单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // 启动流程（如果需要的话）
        log.info("提交 开始启动sku标准成本单流程，id=：【{}】", entity.getId());
        startProcess(entity);

        if (ObjectUtil.isEmpty(mainEntity)) {
           throw new ServiceException(ApiError.NOT_EXIST_BILL, "sku标准成本主单");
        }

        //操作日志
        String content = StrUtil.format("用户【{}】SKU为【{}】生效时间【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), mainEntity.getSkuNo(), null == entity.getEffectiveDate() ? "空":entity.getEffectiveDate());
        OperateLogEntity operateLogEntity = new OperateLogEntity().setContent(content)
                .setBusinessId(entity.getId())
                .setPid(mainEntity.getSkuId())
                .setOperation("状态变更")
                .setClassPath(CLASSPATH);
        operateLogService.addSysLogByOther(operateLogEntity);

        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.SUBMIT);
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto, SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        // 审核中的数据允许审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        validateDetail(entity);

        // 调用流程审核
        approveProcess(entity, dto);

        //操作日志
        String content = StrUtil.format("用户【{}】SKU为【{}】生效时间【{}】单据审核通过 ", UserContext.getDefaultLoginUser().getUserName(), mainEntity.getSkuNo(), null == entity.getEffectiveDate() ? "空":entity.getEffectiveDate());
        OperateLogEntity operateLogEntity = new OperateLogEntity().setContent(content)
                .setBusinessId(entity.getId())
                .setPid(mainEntity.getSkuId())
                .setOperation("状态变更")
                .setClassPath(CLASSPATH);
        operateLogService.addSysLogByOther(operateLogEntity);
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
    public BatchResultDTO disApprove(SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity) {

        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(entity.getId(), ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        String content = StrUtil.format("用户【{}】SKU为【{}】生效时间【{}】单据反审核 ", UserContext.getDefaultLoginUser().getUserName(), mainEntity.getSkuNo(), null == entity.getEffectiveDate() ? "空":entity.getEffectiveDate());
        OperateLogEntity operateLogEntity = new OperateLogEntity().setContent(content)
                .setBusinessId(entity.getId())
                .setPid(mainEntity.getSkuId())
                .setOperation("状态变更")
                .setClassPath(CLASSPATH);
        operateLogService.addSysLogByOther(operateLogEntity);
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
    public BatchResultDTO delete(String id, SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity) {
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）
        // 删除主单数据
        log.info("删除 开始删除sku标准成本单主单数据，id：【{}】", id);
        super.removeById(id);
        //操作日志
        String content = StrUtil.format("用户【{}】SKU为【{}】生效时间【{}】单据删除 ", UserContext.getDefaultLoginUser().getUserName(), mainEntity.getSkuNo(), null == entity.getEffectiveDate() ? "空":entity.getEffectiveDate());
        OperateLogEntity operateLogEntity = new OperateLogEntity().setContent(content)
                .setBusinessId(entity.getId())
                .setPid(mainEntity.getSkuId())
                .setOperation("状态变更")
                .setClassPath(CLASSPATH);
        operateLogService.addSysLogByOther(operateLogEntity);
        return BatchResultDTO.success(entity.getId(), mainEntity.getSkuNo(), OperationTypeEnum.DELETE);
    }

    /**
     * 撤销
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto, SkuStdCostDetailEntity entity, SkuStdCostEntity mainEntity) {
        String id = dto.getId();
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //  撤销流程
        log.info("撤销 开始撤销流程，id：【{}】", id);

        log.info("撤销 开始修改sku标准成本单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(entity.getId());
        // 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(SourceTypeEnum.SKU_STD_COST_DETAIL.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);

        //操作日志
        String content = StrUtil.format("用户【{}】SKU为【{}】生效时间【{}】单据撤销 ", UserContext.getDefaultLoginUser().getUserName(), mainEntity.getSkuNo(), null == entity.getEffectiveDate() ? "空":entity.getEffectiveDate());
        OperateLogEntity operateLogEntity = new OperateLogEntity().setContent(content)
                .setBusinessId(entity.getId())
                .setPid(mainEntity.getSkuId())
                .setOperation("状态变更")
                .setClassPath(CLASSPATH);
        operateLogService.addSysLogByOther(operateLogEntity);
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
        // 明细数据处理 上下游数据处理
        // 查询最近已审核为空的记录设置失效时间
        SkuStdCostDetailEntity lastEntity = lambdaQuery()
                .eq(SkuStdCostDetailEntity::getMainId, entity.getMainId())
                .eq(SkuStdCostDetailEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                .ne(SkuStdCostDetailEntity::getId, entity.getId())
                .isNull(SkuStdCostDetailEntity::getExpireDate)
                .last(" limit 1")
                .one();
        if (null != lastEntity) {
            lastEntity.setExpireDate(entity.getEffectiveDate());
            this.updateById(lastEntity);
        }
        return Boolean.TRUE;
    }


    @Override
    public SkuStdCostDetailDTO.ViewDTO view(String id) {
        List<SkuStdCostDetailDTO.ListDTO> listDTOS = baseMapper.listDTOByParams(new SkuStdCostDetailDTO.ParamsDTO(Collections.singletonList(id), null, null));
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
        validateDetail(entity);
    }

    /**
     * 校验明细
     */
    private void validateDetail(SkuStdCostDetailEntity entity) {
        if (null == entity.getEffectiveDate()){
            throw new ServiceException("【提交】生效日期不能为空");
        }
        if (null == entity.getStdCostPrice()){
            throw new ServiceException("【提交】标准成本不能为空");
        }
        if (BigDecimal.ZERO.compareTo(entity.getStdCostPrice()) == 0) {
            throw new ServiceException("【提交】标准成本不能为0");
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

    @Override
    public boolean importExcel(SkuStdCostDetailDTO.ExcelImportDTO importDTO) {
        SkuStdCostImportTypeEnum typeEnum = SkuStdCostImportTypeEnum.getByCode(importDTO.getImportType());
        BaseDTO.ImportTypeDTO dto = new BaseDTO.ImportTypeDTO(importDTO.getFileUrl(), importDTO.getImportType(), "");
        downloadTaskFeign.saveImportTask(CharSequenceUtil.format("SKU标准成本【{}】", typeEnum.getName()), FileTaskEventEnum.IMPORT_PLM_SKU_STD_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void importChangeSkuStdCostDetail(BaseDTO.ImportTypeDTO dto) {
        //SKU
        Map<String, String> skuMap = productDetailService.list().stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, ProductDetailEntity::getId, (o1, o2) -> o1));
        SkuStdCostChangeExcelListener excelListenerUtil = new SkuStdCostChangeExcelListener(dto.getTaskId(), skuMap);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SkuStdCostChangeExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        }catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        //导出错误数据
        List<SkuStdCostChangeExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "SKU标准成本变更导入错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SkuStdCostChangeExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    public void importUpdateSkuStdCostDetail(BaseDTO.ImportTypeDTO dto) {
        //SKU
        Map<String, String> skuMap = productDetailService.list().stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, ProductDetailEntity::getId, (o1, o2) -> o1));
        SkuStdCostUpdateExcelListener excelListenerUtil = new SkuStdCostUpdateExcelListener(dto.getTaskId(), skuMap);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SkuStdCostUpdateExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        }catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        //导出错误数据
        List<SkuStdCostUpdateExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "SKU标准成本更新导入错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SkuStdCostUpdateExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    public SkuStdCostDetailDTO.SkuStdCostContext loadByDetailIds(List<String> ids) {
        // 明细
        List<SkuStdCostDetailEntity> details = lambdaQuery().in(SkuStdCostDetailEntity::getId, ids).list();

        Map<String, SkuStdCostDetailEntity> idEntityMap = details.stream()
                .collect(Collectors.toMap(SkuStdCostDetailEntity::getId, Function.identity()));

        // 主表
        Map<String, SkuStdCostEntity> mainEntityMap = Collections.emptyMap();
        if (!details.isEmpty()) {
            List<String> mainIds = details.stream()
                    .map(SkuStdCostDetailEntity::getMainId)
                    .distinct()
                    .collect(Collectors.toList());
            mainEntityMap = skuStdCostService.mapByIds(mainIds);
        }

        return new SkuStdCostDetailDTO.SkuStdCostContext(details, idEntityMap, mainEntityMap);
    }
}
