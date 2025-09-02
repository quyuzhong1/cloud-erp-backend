package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.TabApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.dto.InventorySkuCostDetailDTO;
import com.erp.model.tms.dto.excel.InventorySkuCostDetailExcelDTO;
import com.erp.model.tms.entity.FirstMileSkuCostRefEntity;
import com.erp.model.tms.entity.InventorySkuCostDetailEntity;
import com.erp.model.tms.entity.InventorySkuCostEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.convert.InventorySkuCostConverter;
import com.erp.server.tms.listener.InventorySkuCostDetailExcelListener;
import com.erp.server.tms.mapper.InventorySkuCostMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_INVENTORY_SKU_COST;

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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private InventorySkuCostDetailService inventorySkuCostDetailService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private FirstMileSkuCostRefService firstMileSkuCostRefService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;

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
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "SKU成本", inventorySkuCostEntity.getCode());
        //  此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVENTORY_SKU_COST.getCode(), inventorySkuCostEntity.getId(), "新增操作");
        // 新增明细
        if (CollUtil.isNotEmpty(addDTO.getDetailList())) {
            List<InventorySkuCostDetailEntity> detailEntityList = InventorySkuCostConverter.INSTANCE.addToDetail(addDTO.getDetailList());
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
        if (CollUtil.isNotEmpty(updateDTO.getDetailList())) {
            List<InventorySkuCostDetailEntity> detailEntityList = InventorySkuCostConverter.INSTANCE.updateToDetail(updateDTO.getDetailList());
            inventorySkuCostDetailService.buildDetail(detailEntityList, inventorySkuCostEntity);
        }else {
            //明细为空则清空
            inventorySkuCostDetailService.removeByMainId(inventorySkuCostEntity.getId());
        }
        // 记录主单操作日志
        log.info("编辑 开始记录SKU成本日志数据，单号：【{}】", inventorySkuCostEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), inventorySkuCostEntity.getCode(), "SKU成本");
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
        List<String> skuIds = list.stream().map(InventorySkuCostDTO.PagingVO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        list.forEach(e -> {
            e.setStatusName(ApproveStatusEnum.getName(e.getStatus()));
            SkuVO skuVO = skuVOList.stream().filter(f -> Objects.equals(e.getSkuId(), f.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)){
                e.setProductName(skuVO.getSkuName());
            }
            e.setProductCostStr(e.getCurrencySymbol()+e.getProductCost());
            e.setFirstMileShippingCostStr(e.getCurrencySymbol()+e.getFirstMileShippingCost());
            e.setClearanceCustomsTaxStr(e.getCurrencySymbol()+e.getClearanceCustomsTax());
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
        List<InventorySkuCostDetailEntity> detailEntityList = inventorySkuCostDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        List<String> detailIds = detailEntityList.stream().map(InventorySkuCostDetailEntity::getId).distinct().collect(Collectors.toList());
        //校验记录是否已被使用 费用分摊是否已使用
        List<FirstMileSkuCostRefEntity> firstMileSkuCostRefEntityList = firstMileSkuCostRefService.listBySkuCostDetailIds(detailIds);
        if (!CollectionUtils.isEmpty(firstMileSkuCostRefEntityList)){
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "SKU成本已使用不能反审核");
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
        //审核中允许撤销
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98007.msg);
        }
        return approve(entity, ApproveTypeEnum.CANCEL.getStatus(), "", Boolean.FALSE);
    }

    @Override
    public BatchResultDTO submit(InventorySkuCostEntity entity) {
        //只有待提交状态才能发起提交
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98032.msg);
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void updateAndSubmit(InventorySkuCostDTO.UpdateDTO dto) {
        this.update(dto);
        this.submit(this.getById(dto.getId()));
    }

    @Override
    public BatchResultDTO delete(InventorySkuCostEntity entity) {
        List<InventorySkuCostDetailEntity> detailEntityList = inventorySkuCostDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        List<String> detailIds = detailEntityList.stream().map(InventorySkuCostDetailEntity::getId).distinct().collect(Collectors.toList());
        //校验记录是否已被使用 费用分摊是否已使用
        List<FirstMileSkuCostRefEntity> firstMileSkuCostRefEntityList = firstMileSkuCostRefService.listBySkuCostDetailIds(detailIds);
        if (!CollectionUtils.isEmpty(firstMileSkuCostRefEntityList)){
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "SKU成本已使用不能删除");
        }
        inventorySkuCostDetailService.removeByMainId(entity.getId());
        this.lambdaUpdate().eq(InventorySkuCostEntity::getId, entity.getId()).remove();
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除记录操作成功");
    }

    @Override
    public void exportExcel(InventorySkuCostDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("SKU成本导出", EXPORT_TMS_INVENTORY_SKU_COST.getCode(), dto);
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public InventorySkuCostDTO.ImportDTO importFile(MultipartFile excelFile, List<InventorySkuCostDetailDTO.AddDTO> detailList, HttpServletResponse response) {
        InventorySkuCostDetailExcelListener excelListenerUtil = new InventorySkuCostDetailExcelListener(detailList);
        try {
            EasyExcel.read(excelFile.getInputStream(), InventorySkuCostDetailExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
//        List<InventorySkuCostDetailExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
//        if (CollectionUtils.isEmpty(excelDateList)) {
//            throw new ServiceException(ApiError.ERROR_95123);
//        } else if (excelDateList.size() > 5000) {
//            throw new ServiceException(ApiError.ERROR_EXCEL_IMPORT_SIZE);
//        }
        List<InventorySkuCostDetailExcelDTO> errorList = excelListenerUtil.getErrorList();

        List<InventorySkuCostDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();

        InventorySkuCostDTO.ImportDTO importDTO = new InventorySkuCostDTO.ImportDTO();
        String url = "";
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "SKU成本错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, InventorySkuCostDetailExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
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
        if (Objects.nonNull(viewDTO.getAllocatedMonth())){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            viewDTO.setAllocatedMonthStr(viewDTO.getAllocatedMonth().format(formatter));
        }
        List<InventorySkuCostDetailEntity> detailEntityList = inventorySkuCostDetailService.listByMainIds(Collections.singletonList(id));
        if (!CollectionUtils.isEmpty(detailEntityList)) {
            List<InventorySkuCostDetailDTO.ViewDTO> viewDTOList = InventorySkuCostConverter.INSTANCE.detailToViewDTO(detailEntityList);
            viewDTO.setDetailList(viewDTOList);
        }
        return viewDTO;
    }

    @Override
    public List<InventorySkuCostDTO.PagingVO> listDetailByOrgIdAndSkuIds(String orgId, List<String> skuIds, String status, LocalDate month, String warehouseId) {
        if (CharSequenceUtil.isBlank(orgId) && CharSequenceUtil.isBlank(status) && CollectionUtils.isEmpty(skuIds) && Objects.isNull(month) && CharSequenceUtil.isBlank(warehouseId)){
            return Collections.emptyList();
        }
        return baseMapper.listDetailByOrgIdAndSkuIds(orgId,skuIds,status,month,warehouseId);
    }

    @Override
    public List<InventorySkuCostDTO.SkuCostDTO> listSkuCostBySkuIds(InventorySkuCostDTO.QueryB2BDTO queryB2BDTO) {
        if (CollUtil.isEmpty(queryB2BDTO.getSkuIds()) || CharSequenceUtil.isEmpty(queryB2BDTO.getWarehouseId()) || CharSequenceUtil.isEmpty(queryB2BDTO.getSalesOrgId()) || Objects.isNull(queryB2BDTO.getBillDate())){
            return Collections.emptyList();
        }
        LocalDate billDate = queryB2BDTO.getBillDate();
        List<String> skuIds = queryB2BDTO.getSkuIds();
        //根据sku进行获取子件 然后根据bom进行累加组合品
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(queryB2BDTO.getSkuIds());
        List<String> childSkuIds = bomChildrenList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        List<String> skuIds2 = Stream.concat(childSkuIds.stream(), queryB2BDTO.getSkuIds().stream()).distinct().collect(Collectors.toList());
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = baseMapper.listSkuCost(skuIds2, Collections.singletonList(queryB2BDTO.getWarehouseId()), Collections.singletonList(queryB2BDTO.getSalesOrgId()), queryB2BDTO.getMonth());
        if (CollUtil.isEmpty(skuCostDTOS)){
            return Collections.emptyList();
        }
        //根据sku重新组合
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOList = new ArrayList<>();
        String combination = BomTypeEnum.COMBINATION.getType();
        for (String skuId: skuIds){
            List<BomChildrenSkuDTO> childrenSkuDTOS = bomChildrenList.stream().filter(e -> combination.equals(e.getType()) && e.getParentSkuId().equals(skuId)).collect(Collectors.toList());
            if (CollUtil.isEmpty(childrenSkuDTOS)){
                InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(e -> e.getSkuId().equals(skuId)).findFirst().orElse(null);
                if (Objects.isNull(skuCostDTO)){
                    continue;
                }
                BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
                if (Objects.isNull(rate)){
                    continue;
                }
                skuCostDTO.setProductCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getProductCost(),4));
                skuCostDTO.setFirstMileShippingCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getFirstMileShippingCost(),4));
                skuCostDTO.setClearanceCustomsTax(MathUtil.multiplyWithTwo(rate, skuCostDTO.getClearanceCustomsTax(),4));
                skuCostDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                skuCostDTOList.add(skuCostDTO);
            }else {
                InventorySkuCostDTO.SkuCostDTO skuCostDTO = new InventorySkuCostDTO.SkuCostDTO();
                skuCostDTO.setSkuId(skuId);
                skuCostDTO.setWarehouseId(queryB2BDTO.getWarehouseId());
                addProductCost(childrenSkuDTOS,skuCostDTOS,skuCostDTO, billDate);
                if (Objects.nonNull(skuCostDTO.getCountAllChild()) && !skuCostDTO.getCountAllChild()){
                    continue;
                }
                skuCostDTOList.add(skuCostDTO);
            }
        }
        return skuCostDTOList;
    }

    @Override
    public List<InventorySkuCostDTO.SkuCostDTO> listSkuCostByDetail(InventorySkuCostDTO.QueryB2CDTO queryB2CDTO) {
        if (CharSequenceUtil.isBlank(queryB2CDTO.getSalesOrgId()) || CollUtil.isEmpty(queryB2CDTO.getDetailDTOS()) || Objects.isNull(queryB2CDTO.getBillDate())){
            return Collections.emptyList();
        }
        List<InventorySkuCostDTO.QueryB2CDetailDTO> detailDTOS = queryB2CDTO.getDetailDTOS();
        List<String> skuIds = detailDTOS.stream().map(InventorySkuCostDTO.QueryB2CDetailDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> warehouseIds = detailDTOS.stream().map(InventorySkuCostDTO.QueryB2CDetailDTO::getWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        LocalDate billDate = queryB2CDTO.getBillDate();
        if (CollUtil.isEmpty(skuIds) || CollUtil.isEmpty(warehouseIds)){
            return Collections.emptyList();
        }

        //根据sku进行获取子件 然后根据bom进行累加组合品
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<String> childSkuIds = bomChildrenList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        List<String> skuIds2 = Stream.concat(childSkuIds.stream(), skuIds.stream()).distinct().collect(Collectors.toList());
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = baseMapper.listSkuCost(skuIds2, warehouseIds, Collections.singletonList(queryB2CDTO.getSalesOrgId()),null);
        if (CollUtil.isEmpty(skuCostDTOS)){
            return Collections.emptyList();
        }
        //根据sku重新组合
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOList = new ArrayList<>();
        String combination = BomTypeEnum.COMBINATION.getType();
        for (InventorySkuCostDTO.QueryB2CDetailDTO queryB2CDetailDTO: detailDTOS){
            List<BomChildrenSkuDTO> childrenSkuDTOS = bomChildrenList.stream().filter(e -> CharSequenceUtil.isNotBlank(queryB2CDetailDTO.getSkuId()) &&
                    combination.equals(e.getType()) && e.getParentSkuId().equals(queryB2CDetailDTO.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childrenSkuDTOS)){
                InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(e -> CharSequenceUtil.isNotBlank(queryB2CDetailDTO.getSkuId()) &&
                        e.getSkuId().equals(queryB2CDetailDTO.getSkuId())).findFirst().orElse(null);
                if (Objects.isNull(skuCostDTO)){
                    continue;
                }
                BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
                if (Objects.isNull(rate)){
                    continue;
                }
                skuCostDTO.setProductCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getProductCost(),4));
                skuCostDTO.setFirstMileShippingCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getFirstMileShippingCost(),4));
                skuCostDTO.setClearanceCustomsTax(MathUtil.multiplyWithTwo(rate, skuCostDTO.getClearanceCustomsTax(),4));
                skuCostDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                skuCostDTOList.add(skuCostDTO);
            }else {
                InventorySkuCostDTO.SkuCostDTO skuCostDTO = new InventorySkuCostDTO.SkuCostDTO();
                skuCostDTO.setSkuId(queryB2CDetailDTO.getSkuId());
                skuCostDTO.setWarehouseId(queryB2CDetailDTO.getWarehouseId());
                addProductCost(childrenSkuDTOS,skuCostDTOS,skuCostDTO, billDate);
                if (Objects.nonNull(skuCostDTO.getCountAllChild()) && !skuCostDTO.getCountAllChild()){
                    continue;
                }
                skuCostDTOList.add(skuCostDTO);
            }
        }
        return skuCostDTOList;
    }

    @Override
    public List<InventorySkuCostDTO.SkuCostDTO> listSkuCostByDetailList(List<InventorySkuCostDTO.QueryDetailDTO> queryDetailDTOList) {
        List<String> salesOrgIds = queryDetailDTOList.stream().map(InventorySkuCostDTO.QueryDetailDTO::getSalesOrgId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> skuIds = queryDetailDTOList.stream().map(InventorySkuCostDTO.QueryDetailDTO::getSkuId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> warehouseIds = queryDetailDTOList.stream().map(InventorySkuCostDTO.QueryDetailDTO::getWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<String> shopIds = queryDetailDTOList.stream().map(InventorySkuCostDTO.QueryDetailDTO::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollUtil.isEmpty(salesOrgIds) && CollUtil.isNotEmpty(shopIds)){
            List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
            salesOrgIds = shopInfoEntityList.stream().map(ShopInfoEntity::getSalesOrgId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(salesOrgIds) || CollUtil.isEmpty(skuIds) || CollUtil.isEmpty(warehouseIds)){
            return Collections.emptyList();
        }

        //根据sku进行获取子件 然后根据bom进行累加组合品
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<String> childSkuIds = bomChildrenList.stream().map(BomChildrenSkuDTO::getSkuId).collect(Collectors.toList());
        List<String> skuIds2 = Stream.concat(childSkuIds.stream(), skuIds.stream()).distinct().collect(Collectors.toList());
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = baseMapper.listSkuCost(skuIds2, warehouseIds, salesOrgIds,null);
        if (CollUtil.isEmpty(skuCostDTOS)){
            return Collections.emptyList();
        }
        //根据sku重新组合
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOList = new ArrayList<>();
        String combination = BomTypeEnum.COMBINATION.getType();
        for (InventorySkuCostDTO.QueryDetailDTO queryB2CDetailDTO: queryDetailDTOList){
            LocalDate billDate = queryB2CDetailDTO.getBillDate();
            if (Objects.isNull(billDate)){
                continue;
            }
            List<BomChildrenSkuDTO> childrenSkuDTOS = bomChildrenList.stream().filter(e -> CharSequenceUtil.isNotBlank(queryB2CDetailDTO.getSkuId()) &&
                    combination.equals(e.getType()) && e.getParentSkuId().equals(queryB2CDetailDTO.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childrenSkuDTOS)){
                InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(e -> CharSequenceUtil.isNotBlank(queryB2CDetailDTO.getSkuId()) &&
                        e.getSkuId().equals(queryB2CDetailDTO.getSkuId())).findFirst().orElse(null);
                if (Objects.isNull(skuCostDTO)){
                    continue;
                }
                BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
                if (Objects.isNull(rate)){
                    continue;
                }
                skuCostDTO.setProductCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getProductCost(),4));
                skuCostDTO.setFirstMileShippingCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getFirstMileShippingCost(),4));
                skuCostDTO.setClearanceCustomsTax(MathUtil.multiplyWithTwo(rate, skuCostDTO.getClearanceCustomsTax(),4));
                skuCostDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                skuCostDTOList.add(skuCostDTO);
            }else {
                InventorySkuCostDTO.SkuCostDTO skuCostDTO = new InventorySkuCostDTO.SkuCostDTO();
                skuCostDTO.setSkuId(queryB2CDetailDTO.getSkuId());
                skuCostDTO.setWarehouseId(queryB2CDetailDTO.getWarehouseId());
                addProductCost(childrenSkuDTOS,skuCostDTOS,skuCostDTO,billDate);
                if (Objects.nonNull(skuCostDTO.getCountAllChild()) && !skuCostDTO.getCountAllChild()){
                    continue;
                }
                skuCostDTOList.add(skuCostDTO);
            }
        }
        return skuCostDTOList;
    }

    private void addProductCost(List<BomChildrenSkuDTO> childrenSkuDTOS, List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS, InventorySkuCostDTO.SkuCostDTO newSkuCostDTO, LocalDate billDate) {
        for (BomChildrenSkuDTO bomChildrenSkuDTO : childrenSkuDTOS){
            InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(f -> f.getSkuId().equals(bomChildrenSkuDTO.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuCostDTO)){
                BigDecimal rate = dmpTaskFeign.getRate(billDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
                if (Objects.isNull(rate)){
                    newSkuCostDTO.setCountAllChild(Boolean.FALSE);
                    continue;
                }
                skuCostDTO.setProductCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getProductCost(),4));
                skuCostDTO.setFirstMileShippingCost(MathUtil.multiplyWithTwo(rate, skuCostDTO.getFirstMileShippingCost(),4));
                skuCostDTO.setClearanceCustomsTax(MathUtil.multiplyWithTwo(rate, skuCostDTO.getClearanceCustomsTax(),4));
                skuCostDTO.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                //材料成本
                BigDecimal cost = MathUtil.multiplyWithTwo(skuCostDTO.getProductCost(),bomChildrenSkuDTO.getQuantity());
                BigDecimal productCost = Objects.nonNull(newSkuCostDTO.getProductCost()) ? newSkuCostDTO.getProductCost() : BigDecimal.ZERO;
                newSkuCostDTO.setProductCost(MathUtil.add(cost, productCost));
                //头程运费
                BigDecimal firstMileShipingCost = MathUtil.multiplyWithTwo(skuCostDTO.getFirstMileShippingCost(),bomChildrenSkuDTO.getQuantity());
                BigDecimal newFirstMileShipingCost = Objects.nonNull(newSkuCostDTO.getFirstMileShippingCost()) ? newSkuCostDTO.getFirstMileShippingCost() : BigDecimal.ZERO;
                newSkuCostDTO.setFirstMileShippingCost(MathUtil.add(firstMileShipingCost, newFirstMileShipingCost));
                //清关税费
                BigDecimal clearanceCustomsTax = MathUtil.multiplyWithTwo(skuCostDTO.getClearanceCustomsTax(),bomChildrenSkuDTO.getQuantity());
                BigDecimal newClearanceCustomsTax = Objects.nonNull(newSkuCostDTO.getClearanceCustomsTax()) ? newSkuCostDTO.getClearanceCustomsTax() : BigDecimal.ZERO;
                newSkuCostDTO.setClearanceCustomsTax(MathUtil.add(clearanceCustomsTax, newClearanceCustomsTax));
                
                newSkuCostDTO.setAllocatedMonth(skuCostDTO.getAllocatedMonth());
                newSkuCostDTO.setCurrency(skuCostDTO.getCurrency());
            }else {
                newSkuCostDTO.setCountAllChild(Boolean.FALSE);
            }
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(InventorySkuCostEntity inventorySkuCostEntity) {
        // 生成单号
        if (CharSequenceUtil.isBlank(inventorySkuCostEntity.getCode())) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_CHCB);
            inventorySkuCostEntity.setCode(code);
        }
        if (CharSequenceUtil.isBlank(inventorySkuCostEntity.getStatus())) {
            inventorySkuCostEntity.setStatus(ApproveStatusEnum.WAIT_SUBMIT.getCode());
        }
        if (CharSequenceUtil.isBlank(inventorySkuCostEntity.getCurrency())){
            inventorySkuCostEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
            inventorySkuCostEntity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
        }else {
            if (CurrencyEnum.CNY.getCurrencyCode().equals(inventorySkuCostEntity.getCurrency())){
                inventorySkuCostEntity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
            }else {
                List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
                DictCurrencyEntity dictCurrencyEntity = currencyList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(inventorySkuCostEntity.getCurrency())).findFirst().orElse(null);
                if (Objects.nonNull(dictCurrencyEntity)){
                    inventorySkuCostEntity.setCurrencySymbol(dictCurrencyEntity.getSymbol());
                }
            }
        }
        if (CurrencyEnum.CNY.getCurrencyCode().equals(inventorySkuCostEntity.getCurrency())){
            inventorySkuCostEntity.setExchangeRate(BigDecimal.ONE);
        }else {
            //获取dmp汇率
            String currentDate = inventorySkuCostEntity.getAllocatedMonth().withDayOfMonth(inventorySkuCostEntity.getAllocatedMonth().lengthOfMonth()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            BigDecimal rate = dmpTaskFeign.getRate(currentDate, inventorySkuCostEntity.getCurrency());
            if (Objects.nonNull(rate)){
                inventorySkuCostEntity.setExchangeRate(rate);
            }
        }
        if (CharSequenceUtil.isNotBlank(inventorySkuCostEntity.getCompanyId())){
            SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(inventorySkuCostEntity.getCompanyId());
            if (Objects.nonNull(company)){
                inventorySkuCostEntity.setCompanyName(company.getCompanyName());
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
