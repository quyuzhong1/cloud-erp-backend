package com.erp.server.plm.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.vo.LoginUser;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.dto.excel.SkuStdRetailPriceExcelDTO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.SkuStdRetailPriceExcelListener;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BaseResultDTO.AddDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO.ExportDTO;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO.SettingDTO;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO.UpdateDTO;
import com.erp.model.plm.entity.PlmCfgSettingEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.SkuStdRetailPriceEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.enums.SaleStateEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.mapper.SkuStdRetailPriceMapper;
import com.erp.server.plm.rocketmq.sync.wangdian.SyncWangDianProductDetailService;
import com.erp.server.plm.service.CfgSettingService;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.SkuStdRetailPriceService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * sku标准零售价表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
 */
@Slf4j
@Service
public class SkuStdRetailPriceServiceImpl extends SuperServiceImpl<SkuStdRetailPriceMapper, SkuStdRetailPriceEntity> implements SkuStdRetailPriceService {
    @Resource
    private OperateLogService operateLogService;
    
    @Resource
    private ProductDetailService productDetailService;
    
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    
    @Resource
    private CfgSettingService cfgSettingService;
    
    @Resource
    private SyncWangDianProductDetailService syncWangDianProductDetailService;

    @Resource
    private FileFeign fileFeign;
    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SkuStdRetailPriceDTO.AddDTO addDTO) {
        SkuStdRetailPriceEntity skuStdRetailPriceEntity = new SkuStdRetailPriceEntity();
        BeanMapperUtils.copy(addDTO, skuStdRetailPriceEntity);

        // 数据处理
        handleData(skuStdRetailPriceEntity);

        log.info("开始新增sku标准零售价单");
        boolean save = super.save(skuStdRetailPriceEntity);
        if(!save) {
            throw new ServiceException("sku标准零售价单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "sku标准零售价单" , skuStdRetailPriceEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SKU_STD_RETAIL_PRICE.getName(), skuStdRetailPriceEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(skuStdRetailPriceEntity.getId(), skuStdRetailPriceEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SkuStdRetailPriceDTO.UpdateDTO addOrUpdateDTO) {
        SkuStdRetailPriceEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "sku标准零售价单"));
        SkuStdRetailPriceEntity skuStdRetailPriceEntity =  BeanMapperUtils.map(SkuStdRetailPriceEntity.class, addOrUpdateDTO);
        Boolean isDeleted = addOrUpdateDTO.getIsDeleted();

        // 数据处理
        handleData(skuStdRetailPriceEntity);
        log.info("编辑 开始修改sku标准零售价单数据，id：【{}】", old.getId());
        boolean save = false;
        if(isDeleted != null && isDeleted) {
			skuStdRetailPriceEntity.setIsDeleted(true);
			save = removeById(skuStdRetailPriceEntity.getId());
        }else {
        	skuStdRetailPriceEntity.setIsDeleted(false);
        	save = super.updateById(skuStdRetailPriceEntity);
        }
        if(!save) {
            throw new ServiceException("sku标准零售价单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录sku标准零售价单日志数据，id：【{}】", skuStdRetailPriceEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), skuStdRetailPriceEntity.getId(), "sku标准零售价单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, skuStdRetailPriceEntity, ModuleTypeEnum.SKU_STD_RETAIL_PRICE.getName(), skuStdRetailPriceEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SkuStdRetailPriceDTO.ListDTO> paging(PagingDTO<SkuStdRetailPriceDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        query.setOptimizeCountSql(false);
        IPage<SkuStdRetailPriceDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SkuStdRetailPriceDTO.TabListDTO> tabList(PermissionsDTO param) {
    	int allCount = productDetailService.count();
    	Integer inCount = productDetailService.lambdaQuery().eq(ProductDetailEntity::getIsDeleted, false)
    			.last(" and id in (select sku_id from sku_std_retail_price where is_deleted = false) ").count();
    	
    	SkuStdRetailPriceDTO.TabListDTO allDto = new SkuStdRetailPriceDTO.TabListDTO();
    	allDto.setTabFlag("all");
    	allDto.setTabFlagName("全部");
    	allDto.setCount(allCount);
    	
    	SkuStdRetailPriceDTO.TabListDTO inDto = new SkuStdRetailPriceDTO.TabListDTO();
    	inDto.setTabFlag("in");
    	inDto.setTabFlagName("已维护");
    	inDto.setCount(inCount);
    	
    	SkuStdRetailPriceDTO.TabListDTO notInDto = new SkuStdRetailPriceDTO.TabListDTO();
    	notInDto.setTabFlag("notIn");
    	notInDto.setTabFlagName("未维护");
    	notInDto.setCount(allCount - inCount);
        return Arrays.asList(allDto , inDto , notInDto);
    }

    @Override
    public void exportList(SkuStdRetailPriceDTO.ExportDTO param, HttpServletResponse response) {
        List<SkuStdRetailPriceDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/skuStdRetailPrice.xlsx";
        String name = "sku标准零售价单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException("文件导出失败");
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(SkuStdRetailPriceEntity skuStdRetailPriceEntity) {
    	ProductDetailEntity byId = productDetailService.getById(skuStdRetailPriceEntity.getSkuId());
    	if(byId != null) {
    		skuStdRetailPriceEntity.setSkuNo(byId.getSkuNo());
    	}
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public SkuStdRetailPriceDTO.ViewDTO view(String id) {
    SkuStdRetailPriceEntity skuStdRetailPriceEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到sku标准零售价单数据"));
    SkuStdRetailPriceDTO.ViewDTO data = BeanMapperUtils.map(SkuStdRetailPriceDTO.ViewDTO.class, skuStdRetailPriceEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(SkuStdRetailPriceDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<SkuStdRetailPriceDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(SkuStdRetailPriceDTO.ListDTO data : list) {
        	BigDecimal vatRate = data.getVatRate();
        	if(vatRate != null) {
        		data.setVatRateStr(vatRate + "%");
        	}
        	Integer status = data.getStatus();
        	if(status != null) {
        		data.setStatusName(ProductDetailStatusEnum.getName(status));
        	}
        	Integer saleState = data.getSaleState();
        	if(saleState != null) {
        		data.setSaleStateName(SaleStateEnum.getNameByCode(saleState));
        	}
        }
   }

   	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<AddDTO> batchAdd(List<SkuStdRetailPriceDTO.AddDTO> dtoList , boolean isValidateCNY, boolean checkAdd) {
   		List<AddDTO> result = new ArrayList<>();
   		
   		Set<String> CNYSkuSet = dtoList.stream().filter(d -> d.getCurrency().equals("CNY")).map(SkuStdRetailPriceDTO.AddDTO::getSkuId).collect(Collectors.toSet());
        Map<String, BigDecimal> beforeCNYRetailPriceMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(CNYSkuSet)) {
            beforeCNYRetailPriceMap = lambdaQuery().in(SkuStdRetailPriceEntity::getSkuId, CNYSkuSet)
                    .eq(SkuStdRetailPriceEntity::getCurrency, "CNY")
                    .list().stream().collect(Collectors.toMap(SkuStdRetailPriceEntity::getSkuId, SkuStdRetailPriceEntity::getStdRetailPriceVat));
        }
   		Set<String> skuIdSet = dtoList.stream().map(SkuStdRetailPriceDTO.AddDTO::getSkuId).collect(Collectors.toSet());
   		Set<String> currencySet = dtoList.stream().map(SkuStdRetailPriceDTO.AddDTO::getCurrency).collect(Collectors.toSet());
   		Map<String , String> dbSkuIdCurrencyMap = lambdaQuery().in(SkuStdRetailPriceEntity::getSkuId, skuIdSet)
   				.in(SkuStdRetailPriceEntity::getCurrency, currencySet).list()
   				.stream().collect(Collectors.toMap(l -> l.getSkuId() + "_" + l.getCurrency(), SkuStdRetailPriceEntity::getId));
   		for(SkuStdRetailPriceDTO.AddDTO dto : dtoList) {
   			String dbId = dbSkuIdCurrencyMap.get(dto.getSkuId() + "_" + dto.getCurrency());
			if(StringUtils.isNotBlank(dbId)) {
                if (checkAdd){
                    // 同SKU同币种不可重复创建
                    ServiceException.runError(ApiError.PRODUCT_RETAIL_SKU_DUPLICATE);
                }
   				UpdateDTO updateDto = BeanUtil.copyProperties(dto, SkuStdRetailPriceDTO.UpdateDTO.class);
   				updateDto.setId(dbId);
				this.update(updateDto);
				result.add(new AddDTO(dbId , dbId));
   			}else {
   				AddDTO add = this.add(dto);
   				result.add(add);
   			}
   		}
   		if(isValidateCNY) {
   			validateCNY(skuIdSet);
   		}
   		
   		if(CollUtil.isNotEmpty(CNYSkuSet)) {
   			Map<String, BigDecimal> afterCNYRetailPriceMap = lambdaQuery().in(SkuStdRetailPriceEntity::getSkuId, CNYSkuSet)
   	   				.eq(SkuStdRetailPriceEntity::getCurrency, "CNY")
   	   				.list().stream().collect(Collectors.toMap(SkuStdRetailPriceEntity::getSkuId, SkuStdRetailPriceEntity::getStdRetailPriceVat));
   			Set<String> pushSkuSet = new HashSet<>();
   			for(String cnySku : CNYSkuSet) {
   				BigDecimal beforeRetailPrice = beforeCNYRetailPriceMap.get(cnySku);
   				BigDecimal afterRetailPrice = afterCNYRetailPriceMap.get(cnySku);
   				if((beforeRetailPrice == null && afterRetailPrice != null) 
   						|| (beforeRetailPrice != null && afterRetailPrice == null)
   						|| (beforeRetailPrice != null && afterRetailPrice != null && beforeRetailPrice.compareTo(afterRetailPrice) != 0)) {
   					pushSkuSet.add(cnySku);
   				}
   			}
   			if(CollUtil.isNotEmpty(pushSkuSet)) {
   				syncWangDianProductDetailService.syncDataToWangDian(productDetailService.listByIds(pushSkuSet));
   			}
   		}
   		
		return result;
	}

   	private void validateCNY(Set<String> skuIdSet) {
   		Set<String> dbCNYSkuIdSet = lambdaQuery().in(SkuStdRetailPriceEntity::getSkuId, skuIdSet)
   				.eq(SkuStdRetailPriceEntity::getCurrency, "CNY").list()
   				.stream().map(SkuStdRetailPriceEntity::getSkuId).collect(Collectors.toSet());
   		Set<String> notCNYSet = new HashSet<>();
   		for(String skuId : skuIdSet) {
   			if(!dbCNYSkuIdSet.contains(skuId)) {
   				notCNYSet.add(skuId);
   			}
   		}
   		if(CollUtil.isNotEmpty(notCNYSet)) {
   			throw new ServiceException("如下SKU没有CNY币种零售价，请维护：" + (productDetailService.listByIds(notCNYSet).stream().map(ProductDetailEntity::getSkuNo).collect(Collectors.joining("、"))));
   		}
   	}
   	
   	@Override
	public Boolean importExcel(BaseDTO.ImportDTO dto, HttpServletResponse response) throws Exception {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入sku标准零售价", FileTaskEventEnum.IMPORT_PLM_SKU_STD_RETAIL_PRICE.getCode(), dto);
        return Boolean.TRUE;
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSkuStdRetailPrice(BaseDTO.ImportDTO dto) {
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

   		SkuStdRetailPriceExcelListener excelListenerUtil = new SkuStdRetailPriceExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SkuStdRetailPriceExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<SkuStdRetailPriceExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "sku标准零售价错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SkuStdRetailPriceExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
//        try {
//            EasyExcel.read(FastDFSClientUtil.getInputStream(excelFile.getFileUrl()), SkuStdRetailPriceExcelDTO.class, billListener).sheet(0).doRead();
//            List<SkuStdRetailPriceExcelDTO> errorList = billListener.getErrorList();
//            if (!errorList.isEmpty()) {
//                StringBuilder sb = new StringBuilder();
//                String excelPath = "excel/skuStdRetailPriceError.xlsx";
//                String name = "sku标准零售价错误信息.xlsx";
//                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
//                sb.append(date);
//                sb.append(name);
//                try {
//                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
//                } catch (IOException e) {
//                    throw new ServiceException(ApiError.FILE_EXPORT_ERROR_DATA_FAILED);
//                }
//            }
//        }catch (Exception e) {
//        	log.error("导入失败" , e);
//            throw new ServiceException("导入失败");
//        }
    }

	@Override
	public Boolean exportExcel(ExportDTO dto) {
		downloadTaskFeign.saveDownloadTask("sku标准零售价", FileTaskEventEnum.EXPORT_PLM_SKU_STD_RETAIL_PRICE.getCode(), dto);
		return true;
	}

	@Override
	public Boolean setting(SettingDTO dto) {
		String skuStdSetting = dto.getSkuStdSetting();
		String key = "sku_std_setting";
		List<PlmCfgSettingEntity> list = cfgSettingService.lambdaQuery().eq(PlmCfgSettingEntity::getKey, key).list();
		if(CollUtil.isNotEmpty(list)) {
			PlmCfgSettingEntity plmCfgSettingEntity = list.get(0);
			plmCfgSettingEntity.setRemark(skuStdSetting);
			cfgSettingService.updateById(plmCfgSettingEntity);
		}else {
			PlmCfgSettingEntity plmCfgSettingEntity = new PlmCfgSettingEntity();
			plmCfgSettingEntity.setKey(key);
			plmCfgSettingEntity.setRemark(skuStdSetting);
			cfgSettingService.save(plmCfgSettingEntity);
		}
		return true;
	}
}
