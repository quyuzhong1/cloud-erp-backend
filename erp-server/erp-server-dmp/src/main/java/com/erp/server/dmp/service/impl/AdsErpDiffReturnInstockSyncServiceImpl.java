package com.erp.server.dmp.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.common.business.dto.base.*;
import com.common.business.enums.OperationTypeEnum;
import com.common.core.utils.BeanMapper;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.model.dmp.entity.doris.AdsErpDiffOutstockSyncEntity;
import com.erp.model.dmp.entity.doris.AdsErpDiffReturnInstockSyncEntity;
import com.erp.server.dmp.service.DmpRestCloudService;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO.ExpotParamDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO.PagingParamDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO.ReCreateDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO.TotalDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO.UpdateErpDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO.UpdateRemarkDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.doris.AdsErpDiffReturnInstockSyncMapper;
import com.erp.server.dmp.service.AdsErpDiffReturnInstockSyncService;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.utils.RestCloudApiUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ERP退货入库单差异表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-19
 */
@DS("adsDoris")
@Slf4j
@Service
public class AdsErpDiffReturnInstockSyncServiceImpl extends SuperServiceImpl<AdsErpDiffReturnInstockSyncMapper, AdsErpDiffReturnInstockSyncEntity> implements AdsErpDiffReturnInstockSyncService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpRestCloudService dmpRestCloudService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsErpDiffReturnInstockSyncDTO.AddDTO addDTO) {
        AdsErpDiffReturnInstockSyncEntity adsErpDiffReturnInstockSyncEntity = new AdsErpDiffReturnInstockSyncEntity();
        BeanMapperUtils.copy(addDTO, adsErpDiffReturnInstockSyncEntity);

        // 数据处理
        handleData(adsErpDiffReturnInstockSyncEntity);

        log.info("开始新增ERP退货入库单差异单");
        boolean save = super.save(adsErpDiffReturnInstockSyncEntity);
        if(!save) {
            throw new ServiceException("ERP退货入库单差异单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP退货入库单差异单" , adsErpDiffReturnInstockSyncEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, adsErpDiffReturnInstockSyncEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(adsErpDiffReturnInstockSyncEntity.getId(), adsErpDiffReturnInstockSyncEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsErpDiffReturnInstockSyncDTO.UpdateDTO addOrUpdateDTO) {
        AdsErpDiffReturnInstockSyncEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "ERP退货入库单差异单"));
        AdsErpDiffReturnInstockSyncEntity adsErpDiffReturnInstockSyncEntity =  BeanMapperUtils.map(AdsErpDiffReturnInstockSyncEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsErpDiffReturnInstockSyncEntity);
        log.info("编辑 开始修改ERP退货入库单差异单数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsErpDiffReturnInstockSyncEntity);
        if(!save) {
            throw new ServiceException("ERP退货入库单差异单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ERP退货入库单差异单日志数据，id：【{}】", adsErpDiffReturnInstockSyncEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), adsErpDiffReturnInstockSyncEntity.getId(), "ERP退货入库单差异单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, adsErpDiffReturnInstockSyncEntity, null, adsErpDiffReturnInstockSyncEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO> paging(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AdsErpDiffReturnInstockSyncDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AdsErpDiffReturnInstockSyncDTO.TabListDTO> tabList(PermissionsDTO param) {
        List<AdsErpDiffReturnInstockSyncDTO.TabListDTO> list = new ArrayList<>();
        AdsErpDiffReturnInstockSyncDTO.PagingParamDTO searchParam = new AdsErpDiffReturnInstockSyncDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AdsErpDiffReturnInstockSyncDTO.TabListDTO> dblist = baseMapper.tabList(searchParam);
        AdsErpDiffReturnInstockSyncDTO.TabListDTO l = new AdsErpDiffReturnInstockSyncDTO.TabListDTO();
        l.setTabFlag("all");
        l.setTabFlagName("所有");
        l.setCount(dblist.stream().map(AdsErpDiffReturnInstockSyncDTO.TabListDTO::getCount).reduce(Integer::sum).orElse(0));
        list.add(l);
        
        l = new AdsErpDiffReturnInstockSyncDTO.TabListDTO();
        l.setTabFlag("platform");
        l.setTabFlagName("单据1多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("platform")).map(AdsErpDiffReturnInstockSyncDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpDiffReturnInstockSyncDTO.TabListDTO();
        l.setTabFlag("erp");
        l.setTabFlagName("单据2多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("erp")).map(AdsErpDiffReturnInstockSyncDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpDiffReturnInstockSyncDTO.TabListDTO();
        l.setTabFlag("field");
        l.setTabFlagName("字段错误");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("field")).map(AdsErpDiffReturnInstockSyncDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(AdsErpDiffReturnInstockSyncDTO.ExportDTO param, HttpServletResponse response) {
        List<AdsErpDiffReturnInstockSyncDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/adsErpDiffReturnInstockSync.xlsx";
        String name = "ERP退货入库单差异单导出";
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
    private void handleData(AdsErpDiffReturnInstockSyncEntity adsErpDiffReturnInstockSyncEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public AdsErpDiffReturnInstockSyncDTO.ViewDTO view(String id) {
    AdsErpDiffReturnInstockSyncEntity adsErpDiffReturnInstockSyncEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到ERP退货入库单差异单数据"));
    AdsErpDiffReturnInstockSyncDTO.ViewDTO data = BeanMapperUtils.map(AdsErpDiffReturnInstockSyncDTO.ViewDTO.class, adsErpDiffReturnInstockSyncEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(AdsErpDiffReturnInstockSyncDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AdsErpDiffReturnInstockSyncDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(AdsErpDiffReturnInstockSyncDTO.ListDTO data : list) {
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
		if(!cn.hutool.core.date.DateUtil.format(cn.hutool.core.date.DateUtil.offsetMonth(new Date(), -1), "yyyy年MM月").equals(checkMonth)) {
			throw new ServiceException("只允许重新生成上月核对任务");
		}
		Integer count = lambdaQuery().eq(AdsErpDiffReturnInstockSyncEntity::getCheckMonth, checkMonth)
				.eq(AdsErpDiffReturnInstockSyncEntity::getExecStatus, "doing").count();
		if(count != null && count > 0) {
			throw new ServiceException(dto.getCheckMonth() + "核对任务正在执行中");
		}
		boolean reCreate = RestCloudApiUtil.syncReCreate(checkMonth, "ods_erp/ods_flow_return_instock_diff_recreate");
		if(reCreate) {
			lambdaUpdate().eq(AdsErpDiffReturnInstockSyncEntity::getCheckMonth, checkMonth)
			.set(AdsErpDiffReturnInstockSyncEntity::getExecStatus, "doing")
			.set(AdsErpDiffReturnInstockSyncEntity::getExecStatusName, "执行中")
			.setSql(" finish_time = null ")
			.update();
		}
		return true;
	}
	
	@Override
	public Boolean updateErp(UpdateErpDTO dto) {
		String querySql = dto.getSqlMap().get("default");
		String permissionSql = dto.getPermissionSql();
		List<AdsErpDiffReturnInstockSyncEntity> list = lambdaQuery().eq(AdsErpDiffReturnInstockSyncEntity::getIsDeleted, false)
				.eq(AdsErpDiffReturnInstockSyncEntity::getCheckMonth, cn.hutool.core.date.DateUtil.format(cn.hutool.core.date.DateUtil.offsetMonth(new Date(), -1), "yyyy年MM月"))
		.select(AdsErpDiffReturnInstockSyncEntity::getSourceSystem , AdsErpDiffReturnInstockSyncEntity::getAccountCode , AdsErpDiffReturnInstockSyncEntity::getCheckMonth)
		.last(" and " + querySql + " " + (permissionSql == null ? "" : permissionSql) + " group by source_system,account_code,check_month ")
		.list();
		if(CollUtil.isNotEmpty(list)) {
			Integer count = lambdaQuery().in(AdsErpDiffReturnInstockSyncEntity::getCheckMonth, list.stream().map(AdsErpDiffReturnInstockSyncEntity::getCheckMonth).collect(Collectors.toSet()))
					.eq(AdsErpDiffReturnInstockSyncEntity::getExecStatus, "doing").count();
			if(count != null && count > 0) {
				throw new ServiceException(list.stream().map(AdsErpDiffReturnInstockSyncEntity::getCheckMonth).distinct().collect(Collectors.joining("、")) + "中有核对任务正在执行中");
			}
			boolean reCreate = RestCloudApiUtil.syncReCreate("", "ods_erp/ods_flow_return_instock_diff_update");
			if(reCreate) {
				lambdaUpdate().eq(AdsErpDiffReturnInstockSyncEntity::getIsDeleted, false).last(" and " + querySql + " " + (permissionSql == null ? "" : permissionSql))
				.set(AdsErpDiffReturnInstockSyncEntity::getExecStatus, "doing")
				.set(AdsErpDiffReturnInstockSyncEntity::getExecStatusName, "执行中")
				.setSql(" finish_time = null ")
				.update();
			}
		}
		return true;
	}
	
	@Override
	public Boolean updateRemark(UpdateRemarkDTO dto) {
		return lambdaUpdate().eq(AdsErpDiffReturnInstockSyncEntity::getId, dto.getId()).set(AdsErpDiffReturnInstockSyncEntity::getRemark, dto.getRemark()).update();
	}
	
	@Override
	public Boolean exportExcel(ExpotParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("退货同步差异", FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_RETURN_INSTOCK_SYNC.getCode(), dto);
		return true;
	}

    @Override
    public PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO> sourcePlatformPaging(PagingDTO<PagingParamDTO> dto) {
        return  dmpRestCloudService.diffReturnInstockSourcePlatformPaging(dto);
    }

    @Override
    public Boolean exportSourcePlatform(PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("朔源查询-平台出库单", FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_RETURN_INSTOCK_SYNC_DETAIL_PLATFORM.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportSourceSelf(PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("朔源查询-ERP出库单", FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_RETURN_INSTOCK_SYNC_DETAIL_SELF.getCode(), dto);
        return Boolean.TRUE;
    }


    @Override
    public List<AdsErpDiffReturnInstockSyncDTO.PlateformReturnInstockNotExistRelationDTO> listPlateformReturnInstockNotExistRelation(BaseIdsDTO.IdsDTO dto) {
        List<AdsErpDiffReturnInstockSyncEntity> list = lambdaQuery().in(AdsErpDiffReturnInstockSyncEntity::getId, dto.getIds())
                .ne(AdsErpDiffReturnInstockSyncEntity::getSourceSystemName,"")
                .ne(AdsErpDiffReturnInstockSyncEntity::getCheckMonth,"")
                .eq(AdsErpDiffReturnInstockSyncEntity::getExecStatus,"finish")
                .eq(AdsErpDiffReturnInstockSyncEntity::getIsDeleted, false)
                .list();
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        List<AdsErpDiffReturnInstockSyncEntity> notPlatform = list.stream().filter(e -> StringUtil.isBlank(e.getDiffTag()) || !e.getDiffTag().equals("platform")).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(notPlatform)){
            throw new ServiceException(ApiError.DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_PLATFORM);
        }
        //判断list集合中的sourceSystemName值是否全部一致
        if (list.stream().map(AdsErpDiffReturnInstockSyncEntity::getSourceSystemName).distinct().count() > 1) {
            throw new ServiceException(ApiError.DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PLATFORM);
        }
        //判断list集合中的checkMonth值是否全部一致
        if (list.stream().map(AdsErpDiffReturnInstockSyncEntity::getCheckMonth).distinct().count() > 1) {
            throw new ServiceException(ApiError.DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_SAME_PERIOD);
        }
        return BeanMapper.copyList(list, AdsErpDiffReturnInstockSyncDTO.PlateformReturnInstockNotExistRelationDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO batchUpdateReturnInstockRelation(AdsErpDiffReturnInstockSyncDTO.PlateformReturnInstockNotExistRelationDTO dto) {
        AdsErpDiffReturnInstockSyncEntity entity = lambdaQuery().eq(AdsErpDiffReturnInstockSyncEntity::getId, dto.getId())
                .eq(AdsErpDiffReturnInstockSyncEntity::getExecStatus,"finish")
                .eq(AdsErpDiffReturnInstockSyncEntity::getDiffTag,"platform")
                .eq(AdsErpDiffReturnInstockSyncEntity::getIsDeleted, false)
                .one();
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_FOUND);
        }
        AdsErpDiffReturnInstockSyncEntity detailEntity = lambdaQuery().eq(AdsErpDiffReturnInstockSyncEntity::getId, dto.getDetailId())
                .eq(AdsErpDiffReturnInstockSyncEntity::getExecStatus,"finish")
                .eq(AdsErpDiffReturnInstockSyncEntity::getDiffTag,"erp")
                .eq(AdsErpDiffReturnInstockSyncEntity::getIsDeleted, false)
                .one();
        if (Objects.isNull(detailEntity)) {
            throw new ServiceException(ApiError.DMP_ADS_ERP_DIFF_OUTSTOCK_NOT_FOUND);
        }
        Boolean isDateSame = false;
        Boolean isQtySame = false;
        Boolean isStatusSame = false;

        Date billDate = entity.getPlatformBillDate();
        Date detailBillDate = detailEntity.getBillDate();
        if (Objects.nonNull(billDate) && Objects.nonNull(detailBillDate) && billDate.getYear() == detailBillDate.getYear() && billDate.getMonth() == detailBillDate.getMonth()) {
            isDateSame = true;
        }
        //平台-出库数量 、ERP-实发数量相等
        Integer platformQty = entity.getPlatformQty();
        Integer qty = detailEntity.getQty();
        Integer diffQty = 0;
        String diffTag ="same";
        String diffTagName ="完全匹配";
        String suggestType ="";
        if(Objects.nonNull(platformQty) && Objects.nonNull(qty)){
            if(Objects.equals(platformQty,qty)){
                isQtySame = true;
            }else if(platformQty > qty ){
                diffQty = platformQty - qty;
                suggestType ="请核实单据";
            }else if(platformQty < qty ){
                diffQty = platformQty - qty;
                suggestType ="请核实单据";
            }
        }
        //平台-单据状态/ERP-ERP单据状态
        String platformBillStatusName = entity.getPlatformBillStatusName();
        String billStatusName = detailEntity.getBillStatusName();
        if(StringUtils.isNotBlank(platformBillStatusName) && StringUtils.isNotBlank(billStatusName) && Objects.equals(platformBillStatusName,"已发货") && Objects.equals(billStatusName,"已审核")){
            isStatusSame =true;
        }

        if(isDateSame && isQtySame && isStatusSame){

        }else {
//            StringBuffer sb = new StringBuffer();
//            if(!isDateSame){
//                sb.append("单据日期;");
//            }
//            if(!isQtySame){
//                sb.append("单据数量;");
//            }
//            if(!isStatusSame){
//                sb.append("单据状态;");
//            }
            if(diffQty > 0){
                diffTag ="platform";
                diffTagName ="平台单据多";
            }else {
                diffTag ="erp";
                diffTagName ="ERP单据多";
            }
        }
        //获取diffQty的绝对值
        entity.setDiffQty(Math.abs(diffQty)+"");
        entity.setDiffTag(diffTag);
        entity.setDiffTagName(diffTagName);
        entity.setSuggestType(suggestType);
        //erp其余字段补充道平台
        entity.setBillName(detailEntity.getBillName());
        entity.setBillStatusName(detailEntity.getBillStatusName());
        entity.setShopName(detailEntity.getShopName());
        entity.setSalesPlatformName(detailEntity.getSalesPlatformName());
        entity.setOrderCode(detailEntity.getOrderCode());
        entity.setSoCode(detailEntity.getSoCode());
        entity.setReturnInstockCode(detailEntity.getReturnInstockCode());
        entity.setSkuNo(detailEntity.getSkuNo());
        entity.setQty(detailEntity.getQty());
        entity.setSkuQty(detailEntity.getSkuNo()+"*"+detailEntity.getQty());
        entity.setErpWarehouseName(detailEntity.getErpWarehouseName());
        entity.setBillDate(detailEntity.getBillDate());
        //更新平台记录
        updateById(entity);
        //删除对应的erp记录
        detailEntity.setIsDeleted(true);
        updateById(detailEntity);
        return BatchResultDTO.success(entity.getId(), entity.getPlatformReturnInstockCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public List<AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockResultDTO> listErpReturnInstockByParams(AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockParamsDTO dto) {
        // 参数校验
        if (dto == null || StringUtils.isBlank(dto.getCheckMonth()) || StringUtils.isBlank(dto.getSourceSystemName())) {
            throw new IllegalArgumentException("参数不能为空");
        }
        List<AdsErpDiffReturnInstockSyncEntity> list = lambdaQuery()
                .eq(AdsErpDiffReturnInstockSyncEntity::getCheckMonth, dto.getCheckMonth())
                .eq(AdsErpDiffReturnInstockSyncEntity::getSourceSystemName, dto.getSourceSystemName())
                .eq(AdsErpDiffReturnInstockSyncEntity::getDiffTag, "erp")
                .eq(AdsErpDiffReturnInstockSyncEntity::getExecStatus,"finish")
                .eq(AdsErpDiffReturnInstockSyncEntity::getIsDeleted, false)
                .list();

        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockResultDTO> result = new ArrayList<>();
        Map<String, List<AdsErpDiffReturnInstockSyncEntity>> group = list.stream().filter(e -> StringUtils.isNotBlank(e.getReturnInstockCode())).collect(Collectors.groupingBy(AdsErpDiffReturnInstockSyncEntity::getReturnInstockCode));
        for (Map.Entry<String, List<AdsErpDiffReturnInstockSyncEntity>> entry : group.entrySet()) {
            AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockResultDTO entity = new AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockResultDTO();
            List<AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockResultDetailDTO> skuList = new ArrayList<>();
            for (AdsErpDiffReturnInstockSyncEntity syncEntity : entry.getValue()) {
                AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockResultDetailDTO detail = new AdsErpDiffReturnInstockSyncDTO.ErpReturnInstockResultDetailDTO();
                detail.setDetailId(syncEntity.getId());
                detail.setSkuNo(syncEntity.getSkuNo());
                skuList.add(detail);
            }
            entity.setReturnInstockCode(entry.getKey());
            entity.setSkuList(skuList);
            result.add(entity);
        }
        return result;
    }


}
