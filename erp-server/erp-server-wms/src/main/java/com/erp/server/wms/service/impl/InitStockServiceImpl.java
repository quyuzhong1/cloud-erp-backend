package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.ExportInitStockExcelDTO;
import com.erp.model.wms.dto.excel.ImportInitStockExcelDTO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.InitStockDetailEntity;
import com.erp.model.wms.entity.InitStockEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.listener.InitStockDetailExcelListener;
import com.erp.server.wms.mapper.InitStockMapper;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 期初库存表 服务实现类
 * </p>
 *
 * @author ZHANGCHUNLIN
 * @since 2023-05-10
 */
@Slf4j
@Service
public class InitStockServiceImpl extends SuperServiceImpl<InitStockMapper, InitStockEntity> implements InitStockService {

    @Autowired
    private InitStockDetailService initStockDetailService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Override
    public PagingVO<InitStockDTO.ListDTO> paging(PagingDTO<InitStockDTO.SearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        // 分页查询数据
        IPage<InitStockDTO.ListDTO> pageData = this.baseMapper.page(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        filling(pageData.getRecords());
        // 明细数据主单字段只有第一条明细数据显示，其他主单数据字段置位空
        Set<String> mainIds = Sets.newHashSet();
        for(InitStockDTO.ListDTO data: pageData.getRecords()) {
            if(mainIds.contains(data.getId())) {
                data.setCode(null);
                data.setApproveStatus(null);
                data.setApproveStatusName(null);
                data.setInvalidStatus(null);
                data.setInvalidStatusName(null);
                data.setOrgName(null);
                data.setWarehouseName(null);
                continue;
            }
            mainIds.add(data.getId());
        }
        return new PagingVO(pageData);
    }

    @Override
    public InitStockDTO.ViewDTO view(String id) {
        // 查询期初库存信息
        InitStockEntity entity = this.getById(id);
        ValidatorUtil.isTrue(Objects.nonNull(entity),()->new ServiceException("未找到期初库存信息"));
        // 查询期初库存明细信息
        List<InitStockDetailEntity> entityMembers = initStockDetailService.findList(id);
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(entityMembers),()->new ServiceException("未找到期初库存明细信息"));

        // 其他字段赋值
        InitStockDTO.ViewDTO viewDTO = BeanMapperUtils.map(InitStockDTO.ViewDTO.class, entity);
        // 仓库
        WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(viewDTO.getWarehouseId());
        if(Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
            viewDTO.setWarehouseName(warehouseDetail.getName());
        }
        // 仓库组织
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(viewDTO.getOrgId());
        viewDTO.setOrgName(sysAccountingCompanyEntity.getCompanyName());

        // 状态
        viewDTO.setApproveStatus(entity.getApproveStatus());
        ApproveStatusEnum approveStatusEnum = ApproveStatusEnum.getByStatus(entity.getApproveStatus());
        if(Objects.nonNull(approveStatusEnum)) {
            viewDTO.setApproveStatusName(approveStatusEnum.getName());
        }

        List<InitStockDetailDTO.ViewDTO> members = BeanMapperUtils.copyList(InitStockDetailDTO.ViewDTO.class, entityMembers);
        // 获取SKU产品名称
        List<String> skuIds = members.stream().map(InitStockDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIds);
        Map<String, ProductDetailEntity> productMap = productDetailEntityList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity()));
        members.stream().forEach(member->member.setProductName(productMap.getOrDefault(member.getSkuId(),new ProductDetailEntity()).getName()));
        viewDTO.setDetails(members);

        return viewDTO;
    }

    @Override
    public void exportExcel(InitStockDTO.ExportSearchParamDTO param, HttpServletResponse response) {
        List<InitStockDTO.ListDTO> list = this.baseMapper.exportList(param);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        filling(list);
        List<ExportInitStockExcelDTO> resultList = BeanMapperUtils.copyList(ExportInitStockExcelDTO.class, list);
        String fileName = "期初库存数据";
        try {
            ExcelUtil.exportAdapt(fileName, "期初库存数据", resultList, ExportInitStockExcelDTO.class, response, null);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public InitStockDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response) {
        // 查询所有审核通过的产品信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        InitStockDetailExcelListener listener = new InitStockDetailExcelListener(skuList);
        try {
            EasyExcel.read(excelFile.getInputStream(), ImportInitStockExcelDTO.class, listener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("excel导入错误", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<InitStockDetailDTO.AddDTO> successList = listener.getSuccessList(); // 导入成功数据
        List<ImportInitStockExcelDTO> errorList = listener.getErrorList(); // 导入失败数据
        InitStockDetailDTO.ImportDTO result = new InitStockDetailDTO.ImportDTO();
        result.setSuccessList(successList);
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "期初库存导入错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, ImportInitStockExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(InitStockDTO.AddDTO dto) {
        InitStockEntity initStockEntity = BeanMapperUtils.map(InitStockEntity.class, dto);
        checkAddRepeateSku(dto, dto.getDetails());
        // 保存期初库存主单
        fillingAddOrUpdate(initStockEntity, dto.getWarehouseId());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QCKC, BusinessNoTypeEnum.CODE_INIT_STOCK.getCode()));
        initStockEntity.setCode(code);
        initStockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        initStockEntity.setDictTradeType(InventoryBusinessTypeEnum.INVENTORY_INIT.getCode());
        initStockEntity.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());

        log.info("创建 开始新增期初库存数据，单号：【{}】", code);
        boolean save = super.save(initStockEntity);
        ValidatorUtil.isTrue(save, ()->new ServiceException("期初库存保存失败"));

        // 保存期初库存明细
        log.info("创建 开始新增期初库存明细数据，单号：【{}】", code);
        initStockDetailService.add(dto.getDetails(), initStockEntity.getId());

        // 记录主单操作日志
        log.info("创建 开始新增期初库存日志数据，单号：【{}】", code);
        operateLogService.addModuleOperateLog(String.format("新增了一个期初库存【%s】", code), ModuleTypeEnum.INIT_STOCK.getCode(), initStockEntity.getId(), "新增操作");
        return initStockEntity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(InitStockDTO.UpdateDTO dto) {
        // 判断数据是否存在
        InitStockEntity originInitStock = super.getById(dto.getId());
        Optional.ofNullable(originInitStock).orElseThrow(()->new ServiceException("期初库存数据不存在"));
        checkUpdateRepeateSku(dto, dto.getDetails(), dto.getId());
        // 判断状态是否允许操作（只有待提交且未作废的的才允许修改）
        ValidatorUtil.isTrue(Objects.equals(originInitStock.getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()) && Objects.equals(originInitStock.getInvalidStatus(),Boolean.FALSE),
                ()->new ServiceException("当前单据状态不允许修改"));

        InitStockEntity nowInitStock =  BeanMapperUtils.map(InitStockEntity.class, originInitStock);
        fillingAddOrUpdate(nowInitStock, dto.getWarehouseId());
        // 修改期初库存主单数据
        nowInitStock.setBillDate(dto.getBillDate());
        log.info("编辑 开始修改期初库存数据，单号：【{}】", originInitStock.getCode());
        boolean save = super.updateById(nowInitStock);
        ValidatorUtil.isTrue(save, ()->new ServiceException("期初库存保存失败"));

        // 修改期初库存明细数据（包含增删改）
        log.info("编辑 开始修改期初库存明细数据，单号：【{}】", originInitStock.getCode());
        initStockDetailService.update(dto.getDetails(), nowInitStock.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录期初库存日志数据，单号：【{}】", originInitStock.getCode());
        operateLogService.addModuleOperateLogByObj(originInitStock, nowInitStock, ModuleTypeEnum.INIT_STOCK.getCode(), nowInitStock.getId(), "", "");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submit(List<String> ids) {
        ValidatorUtil.isTrue(ids.size() == new HashSet<>(ids).size(),()->new ServiceException("提交的数据存在重复期初库存id"));
        // 判断id是否正确
        List<InitStockEntity> list = super.listByIds(ids);
        Map<String, InitStockEntity> initStockEntityMap = list.stream().collect(Collectors.toMap(InitStockEntity::getId, Function.identity()));
        // 能查询到的数据id集合
        List<String> findIds = list.stream().map(InitStockEntity::getId).distinct().collect(Collectors.toList());
        IntStream.range(0,ids.size()).forEach(idx->{
            String id = ids.get(idx);
            ValidatorUtil.isTrue(findIds.contains(id),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据不存在", idx + 1)));
            InitStockEntity initStockEntity = initStockEntityMap.get(id);
            //待提交或审核不通过并且未作废允许提交
            if((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(initStockEntity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(initStockEntity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(initStockEntity.getInvalidStatus())) {
                throw new ServiceException(StrUtil.format("只有待提交或审核不通过并且未作废数据支持提交，第{}行期初库存数据状态不允许操作", idx + 1));
            }
        });
        // 更新单据审核状态
        log.info("提交 开始修改期初库存状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        lambdaUpdate().in(InitStockEntity::getId, ids).set(InitStockEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus()).update();

        // TODO 启动流程

        // 记录操作日志
        log.info("提交 开始记录期初库存日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个期初库存【%s】", ModuleTypeEnum.INIT_STOCK.getCode(), pairList, "提交操作");
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndSubmit(InitStockDTO.AddDTO dto) {
        String id = this.add(dto); // 新增
        this.submit(Lists.newArrayList(id));// 提交
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(InitStockDTO.UpdateDTO dto) {
        this.update(dto);// 修改
        this.submit(Lists.newArrayList(dto.getId()));// 提交
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();// 提交审核的单据id
        ValidatorUtil.isTrue(ids.size() == new HashSet<>(ids).size(),()->new ServiceException("提交的数据存在重复期初库存id"));
        List<InitStockEntity> list = super.listByIds(ids);
        ValidatorUtil.isTrue(CollUtil.isNotEmpty(list),()->new ServiceException("未找到期初库存数据"));
        Map<String, InitStockEntity> initStockEntityMap = list.stream().collect(Collectors.toMap(InitStockEntity::getId, Function.identity()));
        //只有审核中的数据允许审核
        IntStream.range(0,ids.size()).forEach(idx->{
            String id = ids.get(idx);
            ValidatorUtil.isTrue(initStockEntityMap.containsKey(id),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据不存在",idx + 1)));
            ValidatorUtil.isTrue(Objects.equals(initStockEntityMap.get(id).getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus()),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据状态不为审核中，不允许操作",idx + 1)));
        });
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(baseApproveParamDTO.getType());
        ApproveStatusEnum approveStatus = null;
        if(Objects.equals(ApproveTypeEnum.PASS, approveType)) { // 审核通过
            approveStatus = ApproveStatusEnum.APPROVE;
           this.send2Inventory(initStockEntityMap);
            // TODO 审核通过流程
        } else if (Objects.equals(ApproveTypeEnum.REJECT, approveType)) { // 审核不通过
            approveStatus = ApproveStatusEnum.REJECT;
           // TODO 中止当前审批流程
        }

        log.info("审核 开始修改期初库存状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        updateForApprove(ids, approveStatus.getStatus()); // 修改单据状态
        //操作日志
        log.info("审核 开始修改期初库存日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(data -> new Pair<>(data.getId(), data.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个期初库存", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.INIT_STOCK.getCode(), pairList, "审核操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> ids) {
        ValidatorUtil.isTrue(ids.size() == new HashSet<>(ids).size(),()->new ServiceException("提交的数据存在重复期初库存id"));
        List<InitStockEntity> list = super.listByIds(ids);
        Map<String, InitStockEntity> initStockEntityMap = list.stream().collect(Collectors.toMap(InitStockEntity::getId, Function.identity()));
        //只有待提交的数据允许删除
        IntStream.range(0,ids.size()).forEach(idx->{
            String id = ids.get(idx);
            ValidatorUtil.isTrue(initStockEntityMap.containsKey(id),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据不存在",idx + 1)));
            ValidatorUtil.isTrue(Objects.equals(initStockEntityMap.get(id).getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据状态不为待提交，不允许操作",idx + 1)));
        });
        // 删除期初库存日志数据
        log.info("删除 开始删除期初库存日志数据，id集合：【{}】", JSONObject.toJSONString(ids));
        operateLogService.removeByBusinessIds(ids);

        // 删除期初库存明细数据
        log.info("删除 开始删除期初库存明细数据，id集合：【{}】", JSONObject.toJSONString(ids));
        initStockDetailService.removeByMainIds(ids);

        // 删除期初库存主单数据
        log.info("删除 开始删除期初库存主单数据，id集合：【{}】", JSONObject.toJSONString(ids));
        super.removeByIds(ids);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disApprove(List<String> ids) {
        ValidatorUtil.isTrue(ids.size() == new HashSet<>(ids).size(),()->new ServiceException("提交的数据存在重复期初库存id"));
        List<InitStockEntity> list = super.listByIds(ids);
        Map<String, InitStockEntity> initStockEntityMap = list.stream().collect(Collectors.toMap(InitStockEntity::getId, Function.identity()));
        //只有待提交的数据允许删除
        IntStream.range(0,ids.size()).forEach(idx->{
            String id = ids.get(idx);
            ValidatorUtil.isTrue(initStockEntityMap.containsKey(id),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据不存在",idx + 1)));
            ValidatorUtil.isTrue(Objects.equals(initStockEntityMap.get(id).getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据状态不为已审核，不允许操作",idx + 1)));
        });
        log.info("反审核 开始修改期初库存状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        ApproveStatusEnum approveStatus = ApproveStatusEnum.WAIT_SUBMIT;
        updateForDisApprove(ids, approveStatus.getStatus()); // 修改单据状态为待提交

        log.info("反审核 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        // 操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个期初库存【%s】", ModuleTypeEnum.INIT_STOCK.getCode(), pairList, "反审核操作");
        // 库存交易反审核
        inventoryTransCoreService.batchUnApprove(new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.INIT_STOCK, ids));
        // TODO 流程
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void invalid(List<String> ids, String remark) {
        ValidatorUtil.isTrue(ids.size() == new HashSet<>(ids).size(),()->new ServiceException("提交的数据存在重复期初库存id"));
        List<InitStockEntity> list = super.listByIds(ids);
        Map<String, InitStockEntity> initStockEntityMap = list.stream().collect(Collectors.toMap(InitStockEntity::getId, Function.identity()));
        //只有待提交的数据允许删除
        IntStream.range(0,ids.size()).forEach(idx->{
            String id = ids.get(idx);
            ValidatorUtil.isTrue(initStockEntityMap.containsKey(id),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据不存在",idx + 1)));
            ValidatorUtil.isTrue(Objects.equals(initStockEntityMap.get(id).getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus()) || Objects.equals(initStockEntityMap.get(id).getApproveStatus(), ApproveStatusEnum.REJECT.getStatus()),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据状态不为待提交或审核不通过，不允许操作",idx + 1)));
        });
        log.info("作废 开始修改期初库存状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        lambdaUpdate().in(InitStockEntity::getId, ids)
                .set(InitStockEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(InitStockEntity::getInvalidRemark, remark)
                .update();

        log.info("作废 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个期初库存【%s】，作废原因：".concat(remark), ModuleTypeEnum.INIT_STOCK.getCode(), pairList, "作废操作");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancel(List<String> ids) {
        ValidatorUtil.isTrue(ids.size() == new HashSet<>(ids).size(),()->new ServiceException("提交的数据存在重复期初库存id"));
        List<InitStockEntity> list = super.listByIds(ids);
        Map<String, InitStockEntity> initStockEntityMap = list.stream().collect(Collectors.toMap(InitStockEntity::getId, Function.identity()));
        //只有待提交的数据允许删除
        IntStream.range(0,ids.size()).forEach(idx->{
            String id = ids.get(idx);
            ValidatorUtil.isTrue(initStockEntityMap.containsKey(id),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据不存在",idx + 1)));
            ValidatorUtil.isTrue(Objects.equals(initStockEntityMap.get(id).getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus()),()->new ServiceException(StrUtil.format("您选择的第{}行期初库存数据状态不为审核中，不允许操作",idx + 1)));
        });
        log.info("撤销  开始撤销流程，id集合：【{}】",JSONObject.toJSONString(ids));
        workflowFeign.cancelProcess(ids);

        log.info("撤销 开始修改期初库存状态数据，id集合：【{}】", JSONObject.toJSONString(ids));
        updateForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id集合：【{}】", JSONObject.toJSONString(ids));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("期初库存【%s】取消流程", ModuleTypeEnum.INIT_STOCK.getCode(), pairList, "取消流程操作");
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/initStock.xlsx";
        String excelName = "initStock_template.xlsx";
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
            log.error("initStock downloadTemplate异常", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Integer getInitQty(InitStockDTO.ConditionDTO condition) {
        // 根据仓库、sku、日期范围查询期初数量
        return this.baseMapper.getTotalQty(condition);
    }

    public void send2Inventory(Map<String, InitStockEntity> initStockEntityMap) {
        Map<String, List<InitStockDetailEntity>> initStockDetailMap = initStockDetailService.findListByIds(new ArrayList<>(initStockEntityMap.keySet()));
        // 调用库存组件
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.INVENTORY_INIT.getCode());
        List<InOutStockDTO> stockMembers = Lists.newArrayList();
        initStockEntityMap.forEach((id, initStock)->{
            // 获取期初库存明细（此处优化，防止循环遍历慢）
            List<InitStockDetailEntity> members = initStockDetailMap.get(id);
            members.stream().forEach(member->{
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setWarehouseId(initStock.getWarehouseId());
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.INIT_STOCK);
                inOutStockDTO.setWarehouseLocation(member.getWarehouseLocation());
                inOutStockDTO.setSourceId(initStock.getId());
                inOutStockDTO.setSourceCode(initStock.getCode());
                inOutStockDTO.setBillDate(initStock.getBillDate());
                inOutStockDTO.setSourceDetailId(member.getId());
                inOutStockDTO.setSkuId(member.getSkuId());
                inOutStockDTO.setSkuNo(member.getSkuNo());
                inOutStockDTO.setQty(member.getQty());
                stockMembers.add(inOutStockDTO);
            });
        });
        inventoryInOutStockDTO.setMembers(stockMembers);
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * 审核更新审核状态、审核人、审核时间
     * @param ids
     * @param approveStatus
     */
    public void updateForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().in(InitStockEntity::getId, ids)
                .set(InitStockEntity::getApproveUserId, userInfo.getUid())
                .set(InitStockEntity::getApproveUserName, userInfo.getUserName())
                .set(InitStockEntity::getApproveStatus, approveStatus)
                .set(InitStockEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核更新审核状态、审核人、审核时间
     * @param ids
     * @param approveStatus
     */
    public void updateForDisApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();
        this.lambdaUpdate().in(InitStockEntity::getId, ids)
                .set(InitStockEntity::getApproveUserId, "")
                .set(InitStockEntity::getApproveUserName, "")
                .set(InitStockEntity::getApproveStatus, approveStatus)
                .set(InitStockEntity::getApproveTime, null)
                .update();
    }



    /**
     * 新增检查
     * @param
     * @param details
     */
    public void checkAddRepeateSku(InitStockDTO.AddDTO mainDTO, List<InitStockDetailDTO.AddDTO> details) {
        WarehouseDTO.UpdateDTO warehouseDTO = warehouseService.detailWithCache(mainDTO.getWarehouseId());
        Set<String> skuWareLocationSet = Sets.newHashSet();
        for(InitStockDetailDTO.AddDTO addDTO : details) {
            String skuWareLocation = addDTO.getSkuId() + "-" + StrUtils.null2EmptyWithTrim(addDTO.getWarehouseLocation());
            if(skuWareLocationSet.contains(skuWareLocation)) {
                throw new ServiceException(StrUtil.format("sku编码【{}】仓位【{}】不允许重复", addDTO.getSkuNo(),  StrUtils.null2EmptyWithTrim(addDTO.getWarehouseLocation())));
            } else {
                skuWareLocationSet.add(skuWareLocation);
            }
            // 同一个仓库同一个仓位相同SKU仅可添加一次（不包括已作废单据）
            String skuId = addDTO.getSkuId();
            String warehouseLocationCode = StrUtils.null2EmptyWithTrim(addDTO.getWarehouseLocation());
            if(StrUtils.isNotEmpty(warehouseLocationCode)) {
                WarehouseLocationEntity warehouseLocation = warehouseLocationService.findByWarehouseIdAndCode(mainDTO.getWarehouseId(), warehouseLocationCode);
                ValidatorUtil.isTrue(Objects.nonNull(warehouseLocation),()->new ServiceException("仓位信息不存在"));
            }
            Integer checkCnt = initStockDetailService.countCondition(mainDTO.getWarehouseId(), warehouseLocationCode, skuId, null);
            if(checkCnt > 0) {
                throw new ServiceException(StrUtil.format("sku编码【{}】在仓库【{}】仓位【{}】中已经存在", addDTO.getSkuNo(), warehouseDTO.getName(), warehouseLocationCode));
            }
        }
        // 不允许出现重复的sku
        Map<String,List<InitStockDetailDTO.AddDTO>> skuMap = details.stream().collect(Collectors.groupingBy(InitStockDetailDTO.AddDTO::getSkuId));
        skuMap.forEach((skuId,skuIdList)->{
            if(skuIdList.size() > 1) {
                throw new ServiceException(StrUtil.format("sku编码【{}】不能重复", skuIdList.get(0).getSkuNo()));
            }

        });

    }

    /**
     * 修改检查
     * @param details
     */
    public void checkUpdateRepeateSku(InitStockDTO.UpdateDTO mainDTO, List<InitStockDetailDTO.UpdateDTO> details, String mainId) {
        WarehouseDTO.UpdateDTO warehouseDTO = warehouseService.detailWithCache(mainDTO.getWarehouseId());
        Set<String> skuWareLocationSet = Sets.newHashSet();
        for(InitStockDetailDTO.UpdateDTO updateDTO : details) {
            String skuWareLocation = updateDTO.getSkuId() + "-" + StrUtils.null2EmptyWithTrim(updateDTO.getWarehouseLocation());
            if(skuWareLocationSet.contains(skuWareLocation)) {
                throw new ServiceException(StrUtil.format("sku编码【{}】仓位【{}】不允许重复", updateDTO.getSkuNo(),  StrUtils.null2EmptyWithTrim(updateDTO.getWarehouseLocation())));
            } else {
                skuWareLocationSet.add(skuWareLocation);
            }
            // 判断是否在明细表中已经存在的sku（增加仓位判断）
            String skuId = updateDTO.getSkuId();
            List<InitStockDetailEntity> detailEntities = initStockDetailService.findDetail(mainId, skuId);
            if(CollUtil.isNotEmpty(detailEntities)) {
                for(InitStockDetailEntity initStockDetailEntity : detailEntities) {
                    if(!Objects.equals(initStockDetailEntity.getId(), updateDTO.getId()) && !Objects.equals(initStockDetailEntity.getWarehouseLocation(), StrUtils.null2EmptyWithTrim(updateDTO.getWarehouseLocation()))) {
                        throw new ServiceException(StrUtil.format("sku编码【{}】在仓库中已存在", updateDTO.getSkuNo()));
                    }
                }
            }
            // 同一个仓库同一个仓位相同SKU仅可添加一次（不包括已作废单据）,修改需排除本身
            String warehouseLocationCode = StrUtils.null2EmptyWithTrim(updateDTO.getWarehouseLocation());
            if(StrUtils.isNotEmpty(warehouseLocationCode)) {
                WarehouseLocationEntity warehouseLocation = warehouseLocationService.findByWarehouseIdAndCode(mainDTO.getWarehouseId(), warehouseLocationCode);
                ValidatorUtil.isTrue(Objects.nonNull(warehouseLocation),()->new ServiceException("库位信息不存在"));
                warehouseLocationCode = warehouseLocation.getCode();
            }
            Integer checkCnt = initStockDetailService.countCondition(mainDTO.getWarehouseId(), warehouseLocationCode, skuId, mainId);
            if(checkCnt > 0) {
                throw new ServiceException(StrUtil.format("sku编码【{}】在仓库【{}】库位【{}】中已经存在", updateDTO.getSkuNo(), warehouseDTO.getName(), warehouseLocationCode));
            }
        }
    }

    /**
     * 新增和修改填充值
     * @param initStockEntity
     */
    public void fillingAddOrUpdate(InitStockEntity initStockEntity, String warehouseId) {
        initStockEntity.setDictTradeType(InventoryBusinessTypeEnum.INVENTORY_INIT.getCode());
        if(StrUtils.isNotEmpty(warehouseId)) {
            WarehouseDTO.UpdateDTO warehouseDetail = warehouseService.detailWithCache(warehouseId);
            ValidatorUtil.isTrue(Objects.nonNull(warehouseDetail) && StrUtils.isNotEmpty(warehouseDetail.getId()),
                    ()->new ServiceException(ApiError.ERROR_99002));
            initStockEntity.setWarehouseId(warehouseId);
            initStockEntity.setOrgId(warehouseDetail.getOrgId());
        }
    }

    /**
     * 分页列表和导出excel填充
     * @param list
     */
    public void filling(List<InitStockDTO.ListDTO> list) {
        Map<String, WarehouseDTO.UpdateDTO> warehouseMap = Maps.newHashMap();
        Map<String, SysAccountingCompanyEntity> accountingCompanyMap = Maps.newHashMap();
        // 获取SKU产品名称
        List<String> skuIds = list.stream().map(InitStockDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOs =  plmTaskFeign.getSkuInfoByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOs.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity()));

        list.stream().forEach(data->{
            // 单据状态
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            // 作废状态
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // 仓库名称赋值
            WarehouseDTO.UpdateDTO warehouseDetail = warehouseMap.computeIfAbsent(data.getWarehouseId(),(v)->warehouseService.detailWithCache(v));
            if(Objects.nonNull(warehouseDetail) && StrUtil.isNotEmpty(warehouseDetail.getId())) {
                data.setWarehouseName(warehouseDetail.getName());
            }
            // 仓库组织
            SysAccountingCompanyEntity sysAccountingCompanyEntity = accountingCompanyMap.computeIfAbsent(data.getOrgId(),(v)->sysUserFeign.getCompanyById(v));
            if(Objects.nonNull(sysAccountingCompanyEntity)) {
                data.setOrgName(sysAccountingCompanyEntity.getCompanyName());
            }
            if(skuMap.containsKey(data.getSkuId())) {
                SkuVO skuVO = skuMap.get(data.getSkuId());
                // 产品名称
                data.setProductName(skuVO.getSkuName());
                // spu型号
                data.setSpuNo(skuVO.getSpuNo());
                // 品牌
                data.setBrandName(skuVO.getBrandName());
                // 销售状态
                data.setSaleState(skuVO.getSaleState());
                data.setSaleStateName(SaleStateEnum.getNameByCode(skuVO.getSaleState()));
            }
        });
    }

}
