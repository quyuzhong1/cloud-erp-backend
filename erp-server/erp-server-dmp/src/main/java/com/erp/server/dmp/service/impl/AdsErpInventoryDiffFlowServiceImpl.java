package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO.*;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDetailDTO;
import com.erp.model.dmp.dto.excel.PlatformInitStockExcelDTO;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffFlowEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.listener.PlatformInitStockExcelListener;
import com.erp.server.dmp.mapper.doris.AdsErpInventoryDiffFlowMapper;
import com.erp.server.dmp.service.AdsErpInventoryDiffFlowService;
import com.erp.server.dmp.service.DmpRestCloudService;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.utils.RestCloudApiUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 第三方仓流水差异表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-14
 */
@DS("adsDoris")
@Slf4j
@Service
public class AdsErpInventoryDiffFlowServiceImpl extends SuperServiceImpl<AdsErpInventoryDiffFlowMapper, AdsErpInventoryDiffFlowEntity> implements AdsErpInventoryDiffFlowService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpRestCloudService dmpRestCloudService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsErpInventoryDiffFlowDTO.AddDTO addDTO) {
        AdsErpInventoryDiffFlowEntity adsErpInventoryDiffFlowEntity = new AdsErpInventoryDiffFlowEntity();
        BeanMapperUtils.copy(addDTO, adsErpInventoryDiffFlowEntity);

        // 数据处理
        handleData(adsErpInventoryDiffFlowEntity);

        log.info("开始新增第三方仓流水差异单");
        boolean save = super.save(adsErpInventoryDiffFlowEntity);
        if(!save) {
            throw new ServiceException("第三方仓流水差异单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "第三方仓流水差异单" , adsErpInventoryDiffFlowEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, adsErpInventoryDiffFlowEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(adsErpInventoryDiffFlowEntity.getId(), adsErpInventoryDiffFlowEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsErpInventoryDiffFlowDTO.UpdateDTO addOrUpdateDTO) {
        AdsErpInventoryDiffFlowEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "第三方仓流水差异单"));
        AdsErpInventoryDiffFlowEntity adsErpInventoryDiffFlowEntity =  BeanMapperUtils.map(AdsErpInventoryDiffFlowEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsErpInventoryDiffFlowEntity);
        log.info("编辑 开始修改第三方仓流水差异单数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsErpInventoryDiffFlowEntity);
        if(!save) {
            throw new ServiceException("第三方仓流水差异单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方仓流水差异单日志数据，id：【{}】", adsErpInventoryDiffFlowEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), adsErpInventoryDiffFlowEntity.getId(), "第三方仓流水差异单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, adsErpInventoryDiffFlowEntity, null, adsErpInventoryDiffFlowEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO> paging(PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AdsErpInventoryDiffFlowDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AdsErpInventoryDiffFlowDTO.TabListDTO> tabList(PermissionsDTO param) {
        AdsErpInventoryDiffFlowDTO.PagingParamDTO searchParam = new AdsErpInventoryDiffFlowDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AdsErpInventoryDiffFlowDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        // TODO 替换当前表Tab状态字段
        List<String> statusList = null;
        // 不存在的状态赋值为0
        Set<String> existStatusSet = list.stream().map(AdsErpInventoryDiffFlowDTO.TabListDTO::getTabFlag).collect(Collectors.toSet());
        for (String status : statusList) {
            if (!existStatusSet.contains(status)) {
                list.add(new AdsErpInventoryDiffFlowDTO.TabListDTO(status, 0));
            }
        }
        list.add(new AdsErpInventoryDiffFlowDTO.TabListDTO("all", list.stream().mapToInt(AdsErpInventoryDiffFlowDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(AdsErpInventoryDiffFlowDTO.ExportDTO param, HttpServletResponse response) {
        List<AdsErpInventoryDiffFlowDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/adsErpInventoryDiffFlow.xlsx";
        String name = "第三方仓流水差异单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(AdsErpInventoryDiffFlowEntity adsErpInventoryDiffFlowEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public AdsErpInventoryDiffFlowDTO.ViewDTO view(String id) {
    AdsErpInventoryDiffFlowEntity adsErpInventoryDiffFlowEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到第三方仓流水差异单数据"));
    AdsErpInventoryDiffFlowDTO.ViewDTO data = BeanMapperUtils.map(AdsErpInventoryDiffFlowDTO.ViewDTO.class, adsErpInventoryDiffFlowEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(AdsErpInventoryDiffFlowDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AdsErpInventoryDiffFlowDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(AdsErpInventoryDiffFlowDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }

	@Override
	public TotalDTO total(PagingDTO<PagingParamDTO> dto) {
		return baseMapper.total(dto.getParams());
	}
	
	@Override
	public Boolean reCreate(ReCreateDTO dto) {
		String checkMonth = dto.getCheckMonth();
		checkMonth = checkMonth.replace("-", "年") + "月";
		String sourceSystem = dto.getSourceSystem();
		Integer count = lambdaQuery().eq(AdsErpInventoryDiffFlowEntity::getCheckMonth, checkMonth)
				.eq(AdsErpInventoryDiffFlowEntity::getSourceSystem, sourceSystem)
				.eq(AdsErpInventoryDiffFlowEntity::getExecStatus, "doing").count();
		if(count != null && count > 0) {
			throw new ServiceException(dto.getCheckMonth() + "核对任务正在执行中");
		}
		boolean reCreate = RestCloudApiUtil.syncReCreateByWarehouse(checkMonth, sourceSystem, "ods_erp/ods_flow_excel_inventory_flow_recreate");
		if(reCreate) {
			lambdaUpdate().eq(AdsErpInventoryDiffFlowEntity::getCheckMonth, checkMonth)
			.eq(AdsErpInventoryDiffFlowEntity::getSourceSystem, sourceSystem)
			.set(AdsErpInventoryDiffFlowEntity::getExecStatus, "doing")
			.set(AdsErpInventoryDiffFlowEntity::getExecStatusName, "执行中")
			.setSql(" finish_time = null ")
			.update();
		}
		return true;
	}
	
	@Override
	public Boolean updateRemark(UpdateRemarkDTO dto) {
		return lambdaUpdate().eq(AdsErpInventoryDiffFlowEntity::getId, dto.getId()).set(AdsErpInventoryDiffFlowEntity::getRemark, dto.getRemark()).update();
	}
	
	@Override
	public Boolean exportExcel(ExpotParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("平台流水差异", FileTaskEventEnum.EXPORT_ADS_ERP_INVENTORY_DIFF_FLOW.getCode(), dto);
		return true;
	}
	
	@Override
	public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) throws Exception {
		PlatformInitStockExcelListener billListener = new PlatformInitStockExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), PlatformInitStockExcelDTO.class, billListener).sheet(0).doRead();
            List<PlatformInitStockExcelDTO> errorList = billListener.getErrorList();
            if (!errorList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                String excelPath = "excel/platformInitStockError.xlsx";
                String name = "期初导入错误信息.xlsx";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    throw new ServiceException(ApiError.FILE_EXPORT_ERROR_DATA_FAILED);
                }
                return Boolean.FALSE;
            }
        }catch (SocketTimeoutException e) {
            log.error("导入超时错误！>>>{}", e);
            throw new ServiceException(ApiError.FILE_IMPORT_TIMEOUT);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        } catch (ExcelCommonException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        return Boolean.TRUE;
	}

    @Override
    public PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO> sourcePlatformPaging(PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto) {
        PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO> pagingVO = dmpRestCloudService.inventorySourcePlatformPaging(dto);
        return pagingVO;
    }

    @Override
    public Boolean exportSourcePlatform(AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("朔源查询-每日库存", FileTaskEventEnum.EXPORT_ADS_ERP_INVENTORY_DETAIL_PLATFORM.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> sourceSelfPaging(PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto) {
        PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> pagingVO = dmpRestCloudService.inventorySourceSelfPaging(dto);
        return pagingVO;
    }

    @Override
    public Boolean exportSourceSelf(AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("朔源查询-库存流水", FileTaskEventEnum.EXPORT_ADS_ERP_INVENTORY_DETAIL_SELF.getCode(), dto);
        return Boolean.TRUE;
    }
}
