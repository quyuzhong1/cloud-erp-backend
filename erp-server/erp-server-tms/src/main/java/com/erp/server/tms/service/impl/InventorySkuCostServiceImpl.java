package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.TabApproveStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.CurrencyEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.dto.excel.PurchaseApplicationImportExcelDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.dto.excel.InitFirstMileAllocationDetailExcelDTO;
import com.erp.model.tms.dto.excel.InventorySkuCostDetailExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.listener.InventorySkuCostDetailExcelListener;
import com.erp.server.tms.mapper.InventorySkuCostMapper;
import com.erp.server.tms.service.InventorySkuCostDetailService;
import com.erp.server.tms.service.InventorySkuCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.InventorySkuCostDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * SKU成本 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
 */
@Slf4j
@Service
public class InventorySkuCostServiceImpl extends SuperServiceImpl<InventorySkuCostMapper, InventorySkuCostEntity> implements InventorySkuCostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private InventorySkuCostDetailService inventorySkuCostDetailService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InventorySkuCostDTO.AddDTO addDTO) {
        InventorySkuCostEntity inventorySkuCostEntity = new InventorySkuCostEntity();
        BeanMapperUtils.copy(addDTO, inventorySkuCostEntity);
        // 数据处理
        handleData(inventorySkuCostEntity);
        log.info("开始新增SKU成本");
        boolean save = super.save(inventorySkuCostEntity);
        if (!save) {
            throw new ServiceException("SKU成本保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "SKU成本", inventorySkuCostEntity.getCode());
        //  此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVENTORY_SKU_COST.getCode(), inventorySkuCostEntity.getId(), "新增操作");
        // 新增明细
        if (!CollectionUtils.isEmpty(addDTO.getDetailList())) {
            List<InventorySkuCostDetailEntity> detailEntityList = BeanMapperUtils.copyList(InventorySkuCostDetailEntity.class, addDTO.getDetailList());
            inventorySkuCostDetailService.buildDetail(detailEntityList, inventorySkuCostEntity);
        }
        return new BaseResultDTO.AddDTO(inventorySkuCostEntity.getId(), inventorySkuCostEntity.getCode());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InventorySkuCostDTO.UpdateDTO updateDTO) {
        InventorySkuCostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "SKU成本"));
        InventorySkuCostEntity inventorySkuCostEntity = BeanMapperUtils.map(InventorySkuCostEntity.class, updateDTO);

        // 数据处理
        handleData(inventorySkuCostEntity);
        log.info("编辑 开始修改SKU成本数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(inventorySkuCostEntity);
        if (!save) {
            throw new ServiceException("SKU成本保存失败");
        }
        // 修改明细
        if (!CollectionUtils.isEmpty(updateDTO.getDetailList())) {
            List<InventorySkuCostDetailEntity> detailEntityList = BeanMapperUtils.copyList(InventorySkuCostDetailEntity.class, updateDTO.getDetailList());
            inventorySkuCostDetailService.buildDetail(detailEntityList, inventorySkuCostEntity);
        }else {
            //明细为空则清空
            inventorySkuCostDetailService.removeByMainId(inventorySkuCostEntity.getId());
        }
        // 记录主单操作日志
        log.info("编辑 开始记录SKU成本日志数据，单号：【{}】", inventorySkuCostEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), inventorySkuCostEntity.getCode(), "SKU成本");
        //此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, inventorySkuCostEntity, ModuleTypeEnum.INVENTORY_SKU_COST.getCode(), inventorySkuCostEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<InventorySkuCostDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<InventorySkuCostDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<InventorySkuCostDTO.TabListDTO> tabListDTOList = new ArrayList<>(4);
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.WAIT_SUBMIT.getCode()).tabFlagName(TabApproveStatusEnum.WAIT_SUBMIT.getName()).count(getTabCount(TabApproveStatusEnum.WAIT_SUBMIT.getCode(), list)).build());
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.APPROVE_ING.getCode()).tabFlagName(TabApproveStatusEnum.APPROVE_ING.getName()).count(getTabCount(TabApproveStatusEnum.APPROVE_ING.getCode(), list)).build());
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.REJECT.getCode()).tabFlagName(TabApproveStatusEnum.REJECT.getName()).count(getTabCount(TabApproveStatusEnum.REJECT.getCode(), list)).build());
        tabListDTOList.add(InventorySkuCostDTO.TabListDTO.builder().tabFlag(TabApproveStatusEnum.APPROVE.getCode()).tabFlagName(TabApproveStatusEnum.APPROVE.getName()).count(getTabCount(TabApproveStatusEnum.APPROVE.getCode(), list)).build());
        return tabListDTOList;
    }

    @Override
    public PagingVO<InventorySkuCostDTO.PagingVO> paging(PagingDTO<InventorySkuCostDTO.PagingParamDTO> dto) {
        InventorySkuCostDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<InventorySkuCostDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<InventorySkuCostDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<InventorySkuCostDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    private void fillPagingDb(List<InventorySkuCostDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(e -> {
            e.setStatusName(ApproveStatusEnum.getName(e.getStatus()));
        });
    }

    @Override
    public BatchResultDTO approve(InventorySkuCostEntity entity, String type, String comment, Boolean isNeedProcess) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98006.msg);
        }
        log.info("SKU成本记录【{}】，code=【{}】", ApproveTypeEnum.getName(type), entity.getCode());
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE.getStatus());
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.REJECT.getStatus());
        } else if (ApproveTypeEnum.CANCEL.getStatus().equals(type)) {
            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个SKU成本记录【%s】", ApproveTypeEnum.getName(type), entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "审核操作成功");
    }

    @Override
    public BatchResultDTO disApprove(InventorySkuCostEntity entity) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98014.msg);
        }
        log.info("SKU成本记录反审核，code=【{}】", entity.getCode());
        //更新单据为待提交
        updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("反审核了一个SKU成本记录【%s】", entity.getCode()), ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "反审核操作成功");
    }

    @Override
    public BatchResultDTO cancel(InventorySkuCostEntity entity) {
        return approve(entity, ApproveTypeEnum.CANCEL.getStatus(), "", Boolean.FALSE);
    }

    @Override
    public BatchResultDTO submit(InventorySkuCostEntity entity) {
        //只有待提交状态才能发起提交
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_1029.msg);
        }
        log.info("SKU成本记录提交审核，code=【{}】", entity.getCode());
        //更新单据为审核中
        updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        operateLogService.addModuleOperateLog(String.format("提交审核了一个SKU成本记录【%s】", entity.getCode()), ModuleTypeEnum.INIT_FIRST_MILE_ALLOCATION.getCode(), entity.getId(), "提交审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "提交审核操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void updateAndSubmit(InventorySkuCostDTO.UpdateDTO dto) {
        this.update(dto);
        this.submit(this.getById(dto.getId()));
    }

    @Override
    public BatchResultDTO delete(InventorySkuCostEntity entity) {
        //TODO 校验记录是否已被使用 费用分摊是否已使用
        inventorySkuCostDetailService.removeByMainId(entity.getId());
        this.lambdaUpdate().eq(InventorySkuCostEntity::getId, entity.getId()).remove();
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除记录操作成功");
    }

    @Override
    public void exportExcel(InventorySkuCostDTO.PagingParamDTO dto, HttpServletResponse response) {
        dto.setPermissionSql(dto.getPermissionSql());
        List<InventorySkuCostDTO.PagingVO> list = baseMapper.exportList(dto);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 填充字段值
        fillPagingDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/inventorySkuCostExport.xlsx";
        String name = "SKU成本导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/inventorySkuCostDetailTemplate.xlsx";
        String excelName = "SKU成本导入模板.xlsx";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public InventorySkuCostDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        InventorySkuCostDetailExcelListener excelListenerUtil = new InventorySkuCostDetailExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), InventorySkuCostDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<InventorySkuCostDetailExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        } else if (excelDateList.size() > 5000) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<InventorySkuCostDetailExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<InventorySkuCostDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();

        InventorySkuCostDTO.ImportDTO importDTO = new InventorySkuCostDTO.ImportDTO();
        String url = "";
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "SKU成本错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PurchaseApplicationImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    private void handleImportSuccessList(List<InitFirstMileAllocationDetailExcelDTO> successList, List<InitFirstMileAllocationDetailExcelDTO> errorList) {
    }

    @Override
    public InventorySkuCostDTO.ViewDTO view(String id) {
        InventorySkuCostEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("SKU成本记录不存在");
        }
        InventorySkuCostDTO.ViewDTO viewDTO = new InventorySkuCostDTO.ViewDTO();
        BeanMapperUtils.copy(entity, viewDTO);
        viewDTO.setStatusName(ApproveStatusEnum.getName(viewDTO.getStatus()));
        List<InventorySkuCostDetailEntity> detailEntityList = inventorySkuCostDetailService.listByMainIds(Collections.singletonList(id));
        if (!CollectionUtils.isEmpty(detailEntityList)) {
            List<InventorySkuCostDetailDTO.ViewDTO> viewDTOList = BeanMapperUtils.copyList(InventorySkuCostDetailDTO.ViewDTO.class, detailEntityList);
            viewDTO.setDetailList(viewDTOList);
        }
        return viewDTO;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(InventorySkuCostEntity inventorySkuCostEntity) {
        // 生成单号
        if (StrUtil.isBlank(inventorySkuCostEntity.getCode())) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CHCB);
            inventorySkuCostEntity.setCode(code);
        }
        if (StrUtil.isBlank(inventorySkuCostEntity.getStatus())) {
            inventorySkuCostEntity.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
        }
        if (StrUtil.isBlank(inventorySkuCostEntity.getCurrency())){
            inventorySkuCostEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            inventorySkuCostEntity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
        }
        if (Objects.isNull(inventorySkuCostEntity.getExchangeRate())){
            //获取dmp汇率
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BigDecimal rate = dmpTaskFeign.getRate(currentDate, inventorySkuCostEntity.getCurrency());
            if (Objects.nonNull(rate)){
                inventorySkuCostEntity.setExchangeRate(rate);
            }
        }
    }

    private void updateApproveStatusForApprove(List<String> ids, String status) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isBlank(status)) {
            return;
        }
        this.lambdaUpdate().in(InventorySkuCostEntity::getId, ids)
                .set(InventorySkuCostEntity::getStatus, status)
                .update();
    }

    /**
     * 根据状态获取分页统计数量
     *
     * @param status
     * @param list
     * @return
     */
    private Integer getTabCount(String status, List<InventorySkuCostDTO.TabListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return MathUtil.ZERO;
        }
        InventorySkuCostDTO.TabListDTO tabListDTO = list.stream().filter(e -> Objects.nonNull(e) && status.equals(e.getTabFlag())).findFirst().orElse(null);
        if (Objects.nonNull(tabListDTO)) {
            return tabListDTO.getCount();
        } else {
            return MathUtil.ZERO;
        }
    }
}
