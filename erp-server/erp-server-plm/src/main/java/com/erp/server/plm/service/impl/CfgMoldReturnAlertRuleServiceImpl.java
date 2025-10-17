package com.erp.server.plm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.CfgMoldReturnAlertDetailDTO;
import com.erp.model.plm.dto.excel.CfgMoldReturnImportExcelDTO;
import com.erp.model.plm.entity.CfgMoldReturnAlertDetailEntity;
import com.erp.model.plm.entity.MoldInfoEntity;
import com.erp.model.plm.enums.CfgMoldReturnAlertRuleCountDimEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.CfgMoldReturnExcelListener;
import com.erp.server.plm.service.*;
import com.common.business.annotation.DistributeLocker;
import com.erp.model.plm.entity.CfgMoldReturnAlertRuleEntity;
import com.erp.server.plm.mapper.CfgMoldReturnAlertRuleMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 模具返还策略 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-15
 */
@Slf4j
@Service
public class CfgMoldReturnAlertRuleServiceImpl extends SuperServiceImpl<CfgMoldReturnAlertRuleMapper, CfgMoldReturnAlertRuleEntity> implements CfgMoldReturnAlertRuleService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CfgMoldReturnAlertDetailService cfgMoldReturnAlertDetailService;
    @Resource
    private MoldInfoService moldInfoService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private FileFeign fileFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgMoldReturnAlertRuleDTO.AddDTO addDTO) {
        CfgMoldReturnAlertRuleEntity cfgMoldReturnAlertRuleEntity = new CfgMoldReturnAlertRuleEntity();
        BeanMapperUtils.copy(addDTO, cfgMoldReturnAlertRuleEntity);

        Integer count = lambdaQuery()
                .eq(CfgMoldReturnAlertRuleEntity::getMoldId, cfgMoldReturnAlertRuleEntity.getMoldId())
                .eq(CfgMoldReturnAlertRuleEntity::getInvalidStatus, Boolean.FALSE)
                .count();
        if(count > 0){
            throw new ServiceException(ApiError.ERROR_HAS_EXIST,cfgMoldReturnAlertRuleEntity.getMoldCode());
        }

        // 数据处理
        handleData(cfgMoldReturnAlertRuleEntity);

        log.info("开始新增模具返还策略");
        boolean save = super.save(cfgMoldReturnAlertRuleEntity);
        if (!save) {
            throw new ServiceException("模具返还策略保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("新增了一个模具返还策略【{}】",cfgMoldReturnAlertRuleEntity.getMoldCode());
        operateLogService.addSysLogBySave(msg, "", cfgMoldReturnAlertRuleEntity.getId(), "");

        // 新增明细
        List<CfgMoldReturnAlertDetailDTO.AddDTO> detailList = addDTO.getDetailList();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException("明细不能为空");
        }
        //判断detailList中returnQtyLimit字段是否存在值相同的数据
        Set<Integer> returnQtyLimitSet = new HashSet<>();
        for (CfgMoldReturnAlertDetailDTO.AddDTO detail : detailList) {
            if (!returnQtyLimitSet.add(detail.getReturnQtyLimit())) {
                throw new ServiceException("返还数量上限不能存在相同的数据");
            }
            detail.setMainId(cfgMoldReturnAlertRuleEntity.getId());
        }
        // 按returnQtyLimit升序排序
        detailList = detailList.stream().sorted(Comparator.comparing(CfgMoldReturnAlertDetailDTO.AddDTO::getReturnQtyLimit)).collect(Collectors.toList());
        cfgMoldReturnAlertDetailService.saveBatch(BeanMapper.copyList(detailList, CfgMoldReturnAlertDetailEntity.class));
        return new BaseResultDTO.AddDTO(cfgMoldReturnAlertRuleEntity.getId(), cfgMoldReturnAlertRuleEntity.getId());
    }


    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgMoldReturnAlertRuleDTO.UpdateDTO addOrUpdateDTO) {
        CfgMoldReturnAlertRuleEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "模具返还策略"));
        CfgMoldReturnAlertRuleEntity cfgMoldReturnAlertRuleEntity = BeanMapperUtils.map(CfgMoldReturnAlertRuleEntity.class, addOrUpdateDTO);

        Integer count = lambdaQuery()
                .eq(CfgMoldReturnAlertRuleEntity::getMoldId, cfgMoldReturnAlertRuleEntity.getMoldId())
                .eq(CfgMoldReturnAlertRuleEntity::getInvalidStatus, Boolean.FALSE)
                .ne(CfgMoldReturnAlertRuleEntity::getId, cfgMoldReturnAlertRuleEntity.getId())
                .count();
        if(count > 0){
            throw new ServiceException(ApiError.ERROR_HAS_EXIST,cfgMoldReturnAlertRuleEntity.getMoldCode());
        }

        // 数据处理
        handleData(cfgMoldReturnAlertRuleEntity);

        log.info("编辑 开始修改模具返还策略数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgMoldReturnAlertRuleEntity);
        if (!save) {
            throw new ServiceException("模具返还策略保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录模具返还策略日志数据，id：【{}】", cfgMoldReturnAlertRuleEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑【{}】模具编号为【{}】的返还策略", UserContext.getDefaultLoginUser().getUserName(), "模具返还策略", old.getMoldCode());
        operateLogService.addSysLogByUpdate(old, cfgMoldReturnAlertRuleEntity, String.valueOf(CfgMoldReturnAlertRuleEntity.class), old.getId(), "", msg);

        //旧明细
        List<CfgMoldReturnAlertDetailEntity> oldList = cfgMoldReturnAlertDetailService.lambdaQuery().eq(CfgMoldReturnAlertDetailEntity::getMainId, old.getId()).list();

        // 新增明细
        List<CfgMoldReturnAlertDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException("明细不能为空");
        }
        //判断detailList中returnQtyLimit字段是否存在值相同的数据
        Set<Integer> returnQtyLimitSet = new HashSet<>();
        for (CfgMoldReturnAlertDetailDTO.UpdateDTO detail : detailList) {
            if (!returnQtyLimitSet.add(detail.getReturnQtyLimit())) {
                throw new ServiceException("返还数量上限不能存在相同的数据");
            }
            detail.setMainId(cfgMoldReturnAlertRuleEntity.getId());
        }
        // 按returnQtyLimit升序排序
        detailList = detailList.stream().sorted(Comparator.comparing(CfgMoldReturnAlertDetailDTO.UpdateDTO::getReturnQtyLimit)).collect(Collectors.toList());
        List<CfgMoldReturnAlertDetailEntity> cfgMoldReturnAlertDetailEntities= BeanMapper.copyList(detailList, CfgMoldReturnAlertDetailEntity.class);
        // 处理删除的数据
        if(CollUtil.isNotEmpty(oldList)){
            List<String> detailIds = detailList.stream().map(CfgMoldReturnAlertDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            List<CfgMoldReturnAlertDetailEntity> remove = oldList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                cfgMoldReturnAlertDetailService.removeByIds(remove.stream().map(CfgMoldReturnAlertDetailEntity::getId).collect(Collectors.toList()));
                //添加日志
                String removeMsg = "删除返还明细返还数量上限为【{}】返还金额为【{}】";
                for (CfgMoldReturnAlertDetailEntity detailEntity : remove) {
                    operateLogService.addSysLogBySave(StrUtil.format(removeMsg,detailEntity.getReturnQtyLimit(),detailEntity.getReturnPrice()), "", cfgMoldReturnAlertRuleEntity.getId(), "");
                }
            }
        }

        //处理需要新增的数据
        List<CfgMoldReturnAlertDetailEntity> addList = cfgMoldReturnAlertDetailEntities.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addList)){
            cfgMoldReturnAlertDetailService.saveBatch(addList);
            //添加日志
            String addMsg = "新增返还明细返还数量上限为【{}】返还金额为【{}】";
            for (CfgMoldReturnAlertDetailEntity detailEntity : addList) {
                operateLogService.addSysLogBySave(StrUtil.format(addMsg,detailEntity.getReturnQtyLimit(),detailEntity.getReturnPrice()), "", cfgMoldReturnAlertRuleEntity.getId(), "");
            }
        }

        //处理需要更新的数据
        List<CfgMoldReturnAlertDetailEntity> updateList = cfgMoldReturnAlertDetailEntities.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(updateList)){
            cfgMoldReturnAlertDetailService.updateBatchById(updateList);

            //添加日志
            for (CfgMoldReturnAlertDetailEntity detailEntity : updateList) {
                CfgMoldReturnAlertDetailEntity oldDetail = oldList.stream().filter(e -> Objects.equals(e.getId(), detailEntity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addSysLogByUpdate(oldDetail, detailEntity, String.valueOf(CfgMoldReturnAlertDetailEntity.class), cfgMoldReturnAlertRuleEntity.getId(), "", "编辑返还明细");
                }
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgMoldReturnAlertRuleEntity cfgMoldReturnAlertRuleEntity) {
        MoldInfoEntity moldInfoEntity = moldInfoService.getByIdOpt(cfgMoldReturnAlertRuleEntity.getMoldId()).orElseThrow(() -> new ServiceException("未找到模具档案数据"));
        //结束日期不能小于开始日期
        if (cfgMoldReturnAlertRuleEntity.getEndDate().isBefore(cfgMoldReturnAlertRuleEntity.getStartDate())) {
            throw new ServiceException(ApiError.ERROR_92008);
        }
        cfgMoldReturnAlertRuleEntity.setMoldCode(moldInfoEntity.getCode());
        cfgMoldReturnAlertRuleEntity.setMoldName(moldInfoEntity.getName());
        cfgMoldReturnAlertRuleEntity.setSupplierId(moldInfoEntity.getSupplierId());
        cfgMoldReturnAlertRuleEntity.setSupplierCode(moldInfoEntity.getSupplierCode());
        cfgMoldReturnAlertRuleEntity.setSupplierName(moldInfoEntity.getSupplierName());
    }

    @Override
    public List<CfgMoldReturnAlertRuleDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgMoldReturnAlertRuleDTO.PagingParamDTO searchParam = new CfgMoldReturnAlertRuleDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgMoldReturnAlertRuleDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<CfgMoldReturnAlertRuleDTO.TabListDTO> result = new ArrayList<>();
        result.add(new CfgMoldReturnAlertRuleDTO.TabListDTO("all", "全部", 0));
        CfgMoldReturnAlertRuleDTO.TabListDTO tTab = list.stream().filter(e -> e.getTabFlag().equals("t")).findFirst().orElse(new CfgMoldReturnAlertRuleDTO.TabListDTO("t", "", 0));
        tTab.setTabFlagName("禁用");
        result.add(tTab);
        CfgMoldReturnAlertRuleDTO.TabListDTO fTab = list.stream().filter(e -> e.getTabFlag().equals("f")).findFirst().orElse(new CfgMoldReturnAlertRuleDTO.TabListDTO("f", "", 0));
        fTab.setTabFlagName("启用");
        result.add(fTab);
        return result;
    }

    @Override
    public PagingVO<CfgMoldReturnAlertRuleDTO.ListDTO> paging(PagingDTO<CfgMoldReturnAlertRuleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgMoldReturnAlertRuleDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<CfgMoldReturnAlertRuleDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 属性赋值
        for (CfgMoldReturnAlertRuleDTO.ListDTO data : list) {
            data.setCountDimName(CfgMoldReturnAlertRuleCountDimEnum.getName(data.getCountDim()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setDisabledName(DisabledEnum.getName(data.getDisabled()));
        }
    }

    @Override
    public CfgMoldReturnAlertRuleDTO.ViewDTO view(String id) {
        CfgMoldReturnAlertRuleEntity cfgMoldReturnAlertRuleEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具返还策略数据"));
        CfgMoldReturnAlertRuleDTO.ViewDTO data = BeanMapperUtils.map(CfgMoldReturnAlertRuleDTO.ViewDTO.class, cfgMoldReturnAlertRuleEntity);
        // 数据填充处理
        fillOne(data);
        return data;
    }


    private void fillOne(CfgMoldReturnAlertRuleDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }

        data.setCountDimName(CfgMoldReturnAlertRuleCountDimEnum.getName(data.getCountDim()));
        data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
        data.setDisabledName(DisabledEnum.getName(data.getDisabled()));

        List<CfgMoldReturnAlertDetailEntity> list = cfgMoldReturnAlertDetailService.lambdaQuery()
                .eq(CfgMoldReturnAlertDetailEntity::getMainId, data.getId())
                .orderByAsc(CfgMoldReturnAlertDetailEntity::getReturnQtyLimit)
                .list();
        if (CollUtil.isNotEmpty(list)) {
            data.setDetailList(BeanMapper.copyList(list, CfgMoldReturnAlertDetailDTO.ViewDTO.class));
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgMoldReturnAlertRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具返还策略数据"));
        // 只有禁用数据才能删除
        if (Objects.equals(DisabledEnum.ENABLE.getCode(), entity.getDisabled())) {
            throw new ServiceException(ApiError.ERROR_1069);
        }
        // 删除主单数据
        log.info("删除 开始删除模具返还策略主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除明细数据
        cfgMoldReturnAlertDetailService.lambdaUpdate()
                .eq(CfgMoldReturnAlertDetailEntity::getMainId, id)
                .set(CfgMoldReturnAlertDetailEntity::getIsDeleted, true)
                .update();
        // 删除日志数据
        log.info("删除 开始删除模具返还策略日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】模具编码为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getMoldCode(), "模具返还策略");
        operateLogService.addSysLogBySave(msg, "", entity.getId(), "");
        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(String id, String remark) {
        CfgMoldReturnAlertRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具返还策略数据"));
        // 只有禁用数据才能作废
        if (Objects.equals(DisabledEnum.ENABLE.getCode(), entity.getDisabled())) {
            throw new ServiceException(ApiError.ERROR_1069);
        }
        log.info("作废 开始修改模具返还策略状态数据，id：【{}】", id);
        lambdaUpdate().eq(CfgMoldReturnAlertRuleEntity::getId, id)
                .set(CfgMoldReturnAlertRuleEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(CfgMoldReturnAlertRuleEntity::getInvalidRemark, remark)
                .update();
        // 作废日志数据
        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】模具编码为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getMoldCode(), "模具返还策略", remark);
        operateLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.INVALID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disabled(String id) {
        CfgMoldReturnAlertRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具返还策略数据"));
        // 只有启用数据才能禁用
        if (Objects.equals(DisabledEnum.DISABLED.getCode(), entity.getDisabled())) {
            throw new ServiceException(ApiError.ERROR_DISABLE_FAIL);
        }
        CfgMoldReturnAlertRuleServiceImpl bean = ApplicationContextUtils.getBean(CfgMoldReturnAlertRuleServiceImpl.class);
        bean.changeDisable(entity);
        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.DISABLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO enable(String id) {
        CfgMoldReturnAlertRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到模具返还策略数据"));
        // 只有禁用数据才能启用
        if (Objects.equals(DisabledEnum.ENABLE.getCode(), entity.getDisabled())) {
            throw new ServiceException(ApiError.ERROR_ENABLE_FAIL);
        }
        CfgMoldReturnAlertRuleServiceImpl bean = ApplicationContextUtils.getBean(CfgMoldReturnAlertRuleServiceImpl.class);
        bean.changeDisable(entity);
        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.DISABLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO changeDisable(CfgMoldReturnAlertRuleEntity entity) {
        if (Objects.isNull(entity)) {
            throw new ServiceException("未找到模具返还策略数据");
        }
        String id = entity.getId();
        Boolean disabled = entity.getDisabled() ? Boolean.FALSE : Boolean.TRUE;
        log.info("启用/禁用 开始修改模具返还策略状态数据，id：【{}】", id);
        lambdaUpdate().eq(CfgMoldReturnAlertRuleEntity::getId, id)
                .set(CfgMoldReturnAlertRuleEntity::getDisabled, disabled)
                .update();
        // 禁用 日志数据
        log.info("启用/禁用  开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("{}了一个模具返还策略【{}】",entity.getDisabled() ? "启用" : "禁用", entity.getMoldCode());
        operateLogService.addSysLogBySave(msg, "", id, "");
        return BatchResultDTO.success(entity.getId(), entity.getMoldCode(), OperationTypeEnum.DISABLED);
    }

    @Override
    public void exportList(CfgMoldReturnAlertRuleDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("模具返还策略导出", EXPORT_PLM_CFG_MOLD_RETURN.getCode(), param);
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入模具返还策略", IMPORT_PLM_CFG_MOLD_RETURN.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCfgMoldReturn(BaseDTO.ImportDTO dto) {
        //已审核且未作废的模具关联SKU
        List<MoldInfoEntity> moldInfoEntities = moldInfoService.lambdaQuery()
                .eq(MoldInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getCode())
                .eq(MoldInfoEntity::getInvalidStatus, Boolean.FALSE)
                .eq(MoldInfoEntity::getIsDeleted, Boolean.FALSE)
                .list();
        Map<String, MoldInfoEntity> moldInfoMap = moldInfoEntities.stream().collect(Collectors.toMap(MoldInfoEntity::getCode, Function.identity(), (o1, o2) -> o1));

        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();

        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        CfgMoldReturnExcelListener excelListenerUtil = new CfgMoldReturnExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),moldInfoMap);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), CfgMoldReturnImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<CfgMoldReturnImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            //排序
            List<CfgMoldReturnImportExcelDTO> sortedErrorList = errorList.stream()
                    .sorted(Comparator.comparing(CfgMoldReturnImportExcelDTO::getMoldCode))
                    .collect(Collectors.toList());
            String fileName = "模具返还策略错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", sortedErrorList, CfgMoldReturnImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);

    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<CfgMoldReturnImportExcelDTO> successList, List<CfgMoldReturnImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        CfgMoldReturnAlertRuleServiceImpl bean = ApplicationContextUtils.getBean(CfgMoldReturnAlertRuleServiceImpl.class);

        //判断detailList中returnQtyLimit字段是否存在值相同的数据
        Set<Integer> returnQtyLimitSet = new HashSet<>();
        //
        List<String> moldIdList = successList.stream().map(CfgMoldReturnImportExcelDTO::getMoldId).distinct().collect(Collectors.toList());
        List<CfgMoldReturnAlertRuleEntity> oldList = bean.lambdaQuery()
                .in(CfgMoldReturnAlertRuleEntity::getMoldId, moldIdList)
                .eq(CfgMoldReturnAlertRuleEntity::getInvalidStatus,Boolean.FALSE)
                .list();

        Map<String, CfgMoldReturnAlertRuleEntity> oldMap = oldList.stream().collect(Collectors.toMap(CfgMoldReturnAlertRuleEntity::getMoldId, Function.identity(), (o1, o2) -> o1));

//        List<CfgMoldReturnAlertDetailEntity> oldDetaiList = cfgMoldReturnAlertDetailService.lambdaQuery().in(CfgMoldReturnAlertDetailEntity::getMainId, moldIdList).list();
//        Map<String, List<CfgMoldReturnAlertDetailEntity>> oldDetaiMap = oldDetaiList.stream().collect(Collectors.groupingBy(CfgMoldReturnAlertDetailEntity::getMainId));

        List<String> errorMsgList = new ArrayList<>();
        errorMsgList.add("模具策略已存在");
        String errorMsg1 = FieldValidUtil.getMsgSort(errorMsgList);

        errorMsgList.clear();
        errorMsgList.add("返还数量上限不能存在相同的数据");
        String errorMsg2 = FieldValidUtil.getMsgSort(errorMsgList);

        //按模具分组
        Map<String, List<CfgMoldReturnImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(CfgMoldReturnImportExcelDTO::getMoldId));
        for (Map.Entry<String, List<CfgMoldReturnImportExcelDTO>> entry : collect.entrySet()) {
            String moldId = entry.getKey();
            List<CfgMoldReturnImportExcelDTO> value = entry.getValue();
            CfgMoldReturnImportExcelDTO importMainDTO = value.get(0);
            //备注
            String remark = value.stream().filter(e -> StringUtils.isNotBlank(e.getMainRemark())).map(CfgMoldReturnImportExcelDTO::getMainRemark).findFirst().orElse("");

            CfgMoldReturnAlertRuleEntity old = oldMap.getOrDefault(moldId, null);
            if(Objects.nonNull(old)){
                value.forEach(e -> {
                    e.setErrorMsg(errorMsg1);
                    errorList2.add(e);
                });
            }else {
                // 新增
                List<CfgMoldReturnAlertDetailDTO.AddDTO> detailList = new ArrayList<>();

                for (CfgMoldReturnImportExcelDTO detail : value) {
                    if (!returnQtyLimitSet.add(detail.getReturnQtyLimit())) {
                        detail.setErrorMsg(errorMsg2);
                        errorList2.add(detail);
                    }else {
                        CfgMoldReturnAlertDetailDTO.AddDTO detailDto = new CfgMoldReturnAlertDetailDTO.AddDTO();
                        BeanMapper.copy(detail, detailDto);
                        detailList.add(detailDto);
                    }
                }

                if(CollUtil.isNotEmpty(detailList)){
                    CfgMoldReturnAlertRuleDTO.AddDTO addDTO = new CfgMoldReturnAlertRuleDTO.AddDTO();
                    BeanMapper.copy(importMainDTO, addDTO);
                    addDTO.setRemark(remark);

                    addDTO.setDetailList(detailList);
                    bean.add(addDTO);
                }
                //清除set
                returnQtyLimitSet.clear();
            }
        }
    }
}
