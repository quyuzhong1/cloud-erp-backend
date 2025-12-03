package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import com.erp.model.oms.dto.KolFeedbackDTO;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.entity.KolFeedbackCostEntity;
import com.erp.server.oms.mapper.KolFeedbackCostMapper;
import com.erp.server.oms.service.KolFeedbackCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolFeedbackCostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.erp.model.scm.enums.ModuleTypeEnum;
import cn.hutool.crypto.digest.DigestUtil;
import com.erp.server.oms.service.CfgKolOptionService;
import com.erp.model.oms.entity.CfgKolOptionEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.vo.LoginUser;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.model.oms.dto.excel.KolFeedbackCostExcelDTO;
import com.erp.server.oms.listener.KolFeedbackCostExcelListener;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.BaseDTO;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_FEEDBACK_COST;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_OMS_KOL_FEEDBACK_COST;
/**
 * <p>
 * KOL回片费用表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolFeedbackCostServiceImpl extends SuperServiceImpl<KolFeedbackCostMapper, KolFeedbackCostEntity> implements KolFeedbackCostService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgKolOptionService cfgKolOptionService;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private FileFeign fileFeign;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolFeedbackCostDTO.AddDTO addDTO) {
        KolFeedbackCostEntity kolFeedbackCostEntity = new KolFeedbackCostEntity();
        BeanMapperUtils.copy(addDTO, kolFeedbackCostEntity);

        // 数据处理
        handleData(kolFeedbackCostEntity);

        // 校验唯一性：费用名称ID + urlHash
        checkUnique(kolFeedbackCostEntity, null);

        log.info("开始新增KOL回片费用单");
        boolean save = super.save(kolFeedbackCostEntity);
        if(!save) {
            throw new ServiceException("KOL回片费用单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "KOL回片费用单" , kolFeedbackCostEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_FEEDBACK_COST.getCode(), kolFeedbackCostEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(kolFeedbackCostEntity.getId(), kolFeedbackCostEntity.getId());
    }

    /**
    * 批量新增
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchAdd(KolFeedbackCostDTO.BatchAddDTO dto) {
        List<KolFeedbackCostDTO.AddDTO> list = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());

        for (KolFeedbackCostDTO.AddDTO addDTO : list) {
            BatchResultDTO addResult;
            try {
                BaseResultDTO.AddDTO result = add(addDTO);
                addResult = BatchResultDTO.success(result.getId(), result.getCode(), "新增成功");
            } catch (Exception e) {
                log.error("KOL回片费用批量新增失败", e);
                String remark = addDTO.getRemark() != null ? addDTO.getRemark() : "";
                addResult = BatchResultDTO.fail("", remark, e.getMessage());
            }
            resultDTOS.add(addResult);
        }

        return resultDTOS;
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolFeedbackCostDTO.UpdateDTO addOrUpdateDTO) {
        KolFeedbackCostEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "KOL回片费用单"));
        KolFeedbackCostEntity kolFeedbackCostEntity =  BeanMapperUtils.map(KolFeedbackCostEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolFeedbackCostEntity);
        
        // 校验唯一性：费用名称ID + urlHash
        checkUnique(kolFeedbackCostEntity, old.getId());
        
        log.info("编辑 开始修改KOL回片费用单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolFeedbackCostEntity);
        if(!save) {
            throw new ServiceException("KOL回片费用单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录KOL回片费用单日志数据，id：【{}】", kolFeedbackCostEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolFeedbackCostEntity.getId(), "KOL回片费用单");
        operateLogService.addModuleOperateLogByObj(old, kolFeedbackCostEntity, ModuleTypeEnum.KOL_FEEDBACK_COST.getCode(), kolFeedbackCostEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<KolFeedbackCostDTO.ListDTO> paging(PagingDTO<KolFeedbackCostDTO.ParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<KolFeedbackCostDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<KolFeedbackCostDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolFeedbackCostEntity kolFeedbackCostEntity) {
        // urlHash 用 hutool hash 工具（如果 url 不为空）
        if (StrUtil.isNotBlank(kolFeedbackCostEntity.getUrl())) {
            String urlHash = DigestUtil.md5Hex(kolFeedbackCostEntity.getUrl());
            kolFeedbackCostEntity.setUrlHash(urlHash);
        }

        // costType 通过 costTypeId 查询 CfgKolOptionService
        if (StrUtil.isNotBlank(kolFeedbackCostEntity.getCostTypeId())) {
            try {
                CfgKolOptionEntity cfgKolOptionEntity = cfgKolOptionService.getById(kolFeedbackCostEntity.getCostTypeId());
                if (cfgKolOptionEntity != null && StrUtil.isNotBlank(cfgKolOptionEntity.getName())) {
                    kolFeedbackCostEntity.setCostType(cfgKolOptionEntity.getName());
                }
            } catch (Exception e) {
                log.warn("获取费用类型信息失败，costTypeId: {}", kolFeedbackCostEntity.getCostTypeId(), e);
            }
        }

        // 计算本位币金额：baseAmount = originalAmount * exchangeRate
        if (kolFeedbackCostEntity.getOriginalAmount() != null && kolFeedbackCostEntity.getExchangeRate() != null) {
            BigDecimal baseAmount = MathUtil.multiplyWithTwo(kolFeedbackCostEntity.getOriginalAmount(), kolFeedbackCostEntity.getExchangeRate());
            kolFeedbackCostEntity.setBaseAmount(baseAmount);
        }
    }

    /**
     * 校验唯一性：费用名称ID + urlHash
     * @param kolFeedbackCostEntity 当前实体
     * @param excludeId 排除的ID（修改时使用，排除当前记录）
     */
    private void checkUnique(KolFeedbackCostEntity kolFeedbackCostEntity, String excludeId) {
        if (StrUtil.isBlank(kolFeedbackCostEntity.getCostTypeId()) || StrUtil.isBlank(kolFeedbackCostEntity.getUrlHash())) {
            return;
        }

        // 查询是否存在相同的费用名称ID和urlHash的记录
        KolFeedbackCostEntity existEntity = lambdaQuery()
                .eq(KolFeedbackCostEntity::getCostTypeId, kolFeedbackCostEntity.getCostTypeId())
                .eq(KolFeedbackCostEntity::getUrlHash, kolFeedbackCostEntity.getUrlHash())
                .eq(KolFeedbackCostEntity::getIsDeleted, false)
                .ne(excludeId != null, KolFeedbackCostEntity::getId, excludeId)
                .one();

        if (existEntity != null) {
            throw new ServiceException("该回片链接和费用名称的组合已存在，不能重复添加");
        }
    }
    @Override
    public BatchResultDTO delete(String id) {
        KolFeedbackCostEntity entity = super.getById(id);
        if (entity == null) {
            throw new ServiceException("KOL回片费用不存在");
        }
        
        // 执行删除
        boolean remove = super.removeById(id);
        if (!remove) {
            throw new ServiceException("删除失败");
        }
        
        // 返回成功结果，使用备注作为 code
        String code = StrUtil.isNotBlank(entity.getRemark()) ? entity.getRemark() : entity.getId();
        return BatchResultDTO.success(entity.getId(), code);
    }

    @Override
    public Boolean export(PagingDTO<KolFeedbackCostDTO.ParamDTO> dto) {
        downloadTaskFeign.saveDownloadTask("KOL回片费用导出", EXPORT_OMS_KOL_FEEDBACK_COST.getCode(), dto);
        return true;
    }

    /**
     * 异步导入
     */
    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("KOL回片费用导入", IMPORT_OMS_KOL_FEEDBACK_COST.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 导入KOL回片费用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importKolFeedbackCost(BaseDTO.ImportDTO dto) {
        // 用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream()
                .filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId()))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(findUserDTO)) {
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        KolFeedbackCostExcelListener excelListenerUtil = new KolFeedbackCostExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), KolFeedbackCostExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<KolFeedbackCostExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "KOL回片费用错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, KolFeedbackCostExcelDTO.class);
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

    /**
     * 处理导入成功的数据
     */
    @Transactional(rollbackFor = Exception.class, propagation = org.springframework.transaction.annotation.Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<KolFeedbackCostExcelDTO> successList, List<String> errorNoList, List<KolFeedbackCostExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        // 批量查询费用类型配置
        List<CfgKolOptionEntity> cfgKolOptionList = cfgKolOptionService.list();
        Map<String, String> costTypeNameToIdMap = cfgKolOptionList.stream()
                .collect(Collectors.toMap(CfgKolOptionEntity::getName, CfgKolOptionEntity::getId, (k1, k2) -> k1));

        // 批量查询币别信息
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
        Map<String, String> currencyNameToIdMap = currencyList.stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getName, DictCurrencyEntity::getId, (k1, k2) -> k1));
        // 获取当前日期，用于查询汇率
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 遍历数据进行校验和保存
        for (KolFeedbackCostExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            
            try {
                // 数据校验和ID解析
                // 验证费用类型是否存在并解析ID
                String costTypeName = excelDTO.getCostTypeName();
                String costTypeId = costTypeNameToIdMap.get(costTypeName);
                if (StrUtil.isBlank(costTypeId)) {
                    errorMsgList.add("费用名称【" + costTypeName + "】不存在");
                } else {
                    excelDTO.setCostTypeId(costTypeId);
                }

                // 验证币别是否存在并解析ID
                String currencyName = excelDTO.getCurrencyName();
                String currency = currencyNameToIdMap.get(currencyName);
                if (StrUtil.isBlank(currency)) {
                    errorMsgList.add("付费币别【" + currencyName + "】不存在");
                } else {
                    excelDTO.setCurrency(currency);
                    try {
                        BigDecimal exchangeRate = dmpTaskFeign.getRate(currentDate, currency);
                        if (exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
                            excelDTO.setExchangeRate(exchangeRate);
                        } else {
                            errorMsgList.add("未找到币别【" + currencyName + "】在日期【" + currentDate + "】的汇率");
                        }
                    } catch (Exception e) {
                        log.warn("查询汇率失败，币别：{}，日期：{}", currency, currentDate, e);
                        errorMsgList.add("查询汇率失败：" + e.getMessage());
                    }
                }

                // 解析原币金额
                if (StringUtils.isNotBlank(excelDTO.getOriginalAmountStr())) {
                    try {
                        BigDecimal originalAmount = new BigDecimal(excelDTO.getOriginalAmountStr());
                        if (originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                            errorMsgList.add("金额（原币）必须大于0");
                        } else {
                            excelDTO.setOriginalAmount(originalAmount);
                        }
                    } catch (NumberFormatException e) {
                        errorMsgList.add("金额（原币）格式错误：" + excelDTO.getOriginalAmountStr());
                    }
                }

                // URL哈希值计算
                if (StrUtil.isNotBlank(excelDTO.getUrl())) {
                    String urlHash = cn.hutool.crypto.digest.DigestUtil.md5Hex(excelDTO.getUrl());
                    excelDTO.setUrlHash(urlHash);
                }

                // 如果有校验错误，添加到错误列表
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(com.common.core.utils.FieldValidUtil.getMsgSort(errorMsgList));
                    errorList2.add(excelDTO);
                    continue;
                }

                // 复制数据并保存
                KolFeedbackCostEntity entity = new KolFeedbackCostEntity();
                BeanMapperUtils.copy(excelDTO, entity);
                
                // 数据处理
                handleData(entity);
                
                // 保存数据
                boolean save = super.save(entity);
                if (!save) {
                    excelDTO.setErrorMsg("保存失败");
                    errorList2.add(excelDTO);
                }
            } catch (Exception e) {
                log.error("导入KOL回片费用数据失败", e);
                excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                errorList2.add(excelDTO);
            }
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 下载KOL回片费用导入模板
        String path = "classpath:excel/kolFeedbackCostTemplate.xlsx";
        String excelName = "KOL回片费用导入模板.xlsx";
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
            log.info("开始下载KOL回片费用导入模板");
        } catch (Exception e) {
            log.error("KOL回片费用导入模板下载失败", e);
            throw new ServiceException("下载模板失败：" + e.getMessage());
        }
    }

    /**
     * 填充列表数据（币别转换、金额拼接）
     */
    private void fillList(List<KolFeedbackCostDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        
        // 获取所有币别信息
        List<DictCurrencyEntity> currencyList = sysUserFeign.currencyList();
        if (CollUtil.isEmpty(currencyList)) {
            return;
        }
        
        // 构建币别 ID -> 币别实体的映射
        Map<String, DictCurrencyEntity> currencyMap = currencyList.stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getId, c -> c, (k1, k2) -> k1));
        
        // 获取 CNY 的 symbol
        String cnySymbol = currencyList.stream()
                .filter(c -> "CNY".equals(c.getSymbol()))
                .findFirst()
                .map(DictCurrencyEntity::getSymbol)
                .orElse("¥");
        
        // 遍历列表进行数据填充
        for (KolFeedbackCostDTO.ListDTO data : list) {
            // 设置币别名称
            if (StringUtils.isNotBlank(data.getCurrency())) {
                DictCurrencyEntity currency = currencyMap.get(data.getCurrency());
                if (currency != null) {
                    data.setCurrencyName(currency.getName());
                    
                    // 拼接原币金额显示：symbol + 金额
                    if (data.getOriginalAmount() != null) {
                        String symbol = StringUtils.isNotBlank(currency.getSymbol()) ? currency.getSymbol() : "";
                        data.setOriginalAmountDisplay(symbol + " " + data.getOriginalAmount());
                    }
                }
            }
            
            // 拼接本位币金额显示：CNY symbol + 金额
            if (data.getBaseAmount() != null) {
                data.setBaseAmountDisplay(cnySymbol + " " + data.getBaseAmount());
            }
        }
    }
}
