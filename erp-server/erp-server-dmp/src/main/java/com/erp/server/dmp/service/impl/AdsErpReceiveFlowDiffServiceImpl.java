package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO.*;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDetailDTO;
import com.erp.model.dmp.entity.doris.AdsErpReceiveFlowDiffEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.doris.AdsErpReceiveFlowDiffMapper;
import com.erp.server.dmp.service.AdsErpReceiveFlowDiffService;
import com.erp.server.dmp.service.DmpRestCloudService;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.utils.RestCloudApiUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * ERP签收流水差异表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-03-09
 */
@DS("adsDoris")
@Slf4j
@Service
public class AdsErpReceiveFlowDiffServiceImpl extends SuperServiceImpl<AdsErpReceiveFlowDiffMapper, AdsErpReceiveFlowDiffEntity> implements AdsErpReceiveFlowDiffService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private DmpRestCloudService dmpRestCloudService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsErpReceiveFlowDiffDTO.AddDTO addDTO) {
        AdsErpReceiveFlowDiffEntity adsErpReceiveFlowDiffEntity = new AdsErpReceiveFlowDiffEntity();
        BeanMapperUtils.copy(addDTO, adsErpReceiveFlowDiffEntity);

        // 数据处理
        handleData(adsErpReceiveFlowDiffEntity);

        log.info("开始新增ERP签收流水差异单");
        boolean save = super.save(adsErpReceiveFlowDiffEntity);
        if(!save) {
            throw new ServiceException("ERP签收流水差异单保存失败");
        }

        return new BaseResultDTO.AddDTO(adsErpReceiveFlowDiffEntity.getId(), adsErpReceiveFlowDiffEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsErpReceiveFlowDiffDTO.UpdateDTO addOrUpdateDTO) {
        AdsErpReceiveFlowDiffEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "ERP签收流水差异单"));
        AdsErpReceiveFlowDiffEntity adsErpReceiveFlowDiffEntity =  BeanMapperUtils.map(AdsErpReceiveFlowDiffEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsErpReceiveFlowDiffEntity);
        log.info("编辑 开始修改ERP签收流水差异单数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsErpReceiveFlowDiffEntity);
        if(!save) {
            throw new ServiceException("ERP签收流水差异单保存失败");
        }
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AdsErpReceiveFlowDiffDTO.ListDTO> paging(PagingDTO<PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AdsErpReceiveFlowDiffDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AdsErpReceiveFlowDiffDTO.TabListDTO> tabList(PermissionsDTO param) {
    	List<AdsErpReceiveFlowDiffDTO.TabListDTO> list = new ArrayList<>();
        PagingParamDTO searchParam = new PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AdsErpReceiveFlowDiffDTO.TabListDTO> dblist = baseMapper.tabList(searchParam);
        AdsErpReceiveFlowDiffDTO.TabListDTO l = new AdsErpReceiveFlowDiffDTO.TabListDTO();
        l.setTabFlag("all");
        l.setTabFlagName("所有");
        l.setCount(dblist.stream().map(AdsErpReceiveFlowDiffDTO.TabListDTO::getCount).reduce(Integer::sum).orElse(0));
        list.add(l);
        
        l = new AdsErpReceiveFlowDiffDTO.TabListDTO();
        l.setTabFlag("platform");
        l.setTabFlagName("单据1多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("platform")).map(AdsErpReceiveFlowDiffDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpReceiveFlowDiffDTO.TabListDTO();
        l.setTabFlag("erp");
        l.setTabFlagName("单据2多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("erp")).map(AdsErpReceiveFlowDiffDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpReceiveFlowDiffDTO.TabListDTO();
        l.setTabFlag("field");
        l.setTabFlagName("字段错误");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("field")).map(AdsErpReceiveFlowDiffDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        // 计算合计数量
        return list;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AdsErpReceiveFlowDiffEntity AdsErpReceiveFlowDiffEntity) {
    }

    @Override
    public AdsErpReceiveFlowDiffDTO.ViewDTO view(String id) {
    AdsErpReceiveFlowDiffEntity AdsErpReceiveFlowDiffEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到ERP签收流水差异单数据"));
    AdsErpReceiveFlowDiffDTO.ViewDTO data = BeanMapperUtils.map(AdsErpReceiveFlowDiffDTO.ViewDTO.class, AdsErpReceiveFlowDiffEntity);
    // 数据填充处理
    fillOne(data);
    return data;
    }

    private void fillOne(AdsErpReceiveFlowDiffDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AdsErpReceiveFlowDiffDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(AdsErpReceiveFlowDiffDTO.ListDTO data : list) {
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
		Integer count = lambdaQuery().eq(AdsErpReceiveFlowDiffEntity::getCheckMonth, checkMonth)
				.eq(AdsErpReceiveFlowDiffEntity::getExecStatus, "doing").count();
		if(count != null && count > 0) {
			throw new ServiceException(dto.getCheckMonth() + "核对任务正在执行中");
		}
		boolean reCreate = RestCloudApiUtil.syncReCreate(checkMonth, "ods_erp/ods_flow_outstock_diff_recreate");
		if(reCreate) {
			lambdaUpdate().eq(AdsErpReceiveFlowDiffEntity::getCheckMonth, checkMonth)
			.set(AdsErpReceiveFlowDiffEntity::getExecStatus, "doing")
			.set(AdsErpReceiveFlowDiffEntity::getExecStatusName, "执行中")
			.setSql(" finish_time = null ")
			.update();
		}
		return true;
	}
	
	@Override
	public Boolean updateErp(UpdateErpDTO dto) {
		String querySql = dto.getSqlMap().get("default");
		String permissionSql = dto.getPermissionSql();
		List<AdsErpReceiveFlowDiffEntity> list = lambdaQuery().eq(AdsErpReceiveFlowDiffEntity::getIsDeleted, false)
				.eq(AdsErpReceiveFlowDiffEntity::getCheckMonth, cn.hutool.core.date.DateUtil.format(cn.hutool.core.date.DateUtil.offsetMonth(new Date(), -1), "yyyy年MM月"))
		.select(AdsErpReceiveFlowDiffEntity::getSourceSystem , AdsErpReceiveFlowDiffEntity::getAccountCode , AdsErpReceiveFlowDiffEntity::getCheckMonth)
		.last(" and " + querySql + " " + (permissionSql == null ? "" : permissionSql) + " group by source_system,account_code,check_month ")
		.list();
		if(CollUtil.isNotEmpty(list)) {
			Integer count = lambdaQuery().in(AdsErpReceiveFlowDiffEntity::getCheckMonth, list.stream().map(AdsErpReceiveFlowDiffEntity::getCheckMonth).collect(Collectors.toSet()))
					.eq(AdsErpReceiveFlowDiffEntity::getExecStatus, "doing").count();
			if(count != null && count > 0) {
				throw new ServiceException(list.stream().map(AdsErpReceiveFlowDiffEntity::getCheckMonth).distinct().collect(Collectors.joining("、")) + "中有核对任务正在执行中");
			}
			boolean reCreate = RestCloudApiUtil.syncReCreate("", "ods_erp/ods_receive_flow_diff_recreate");
			if(reCreate) {
				lambdaUpdate().eq(AdsErpReceiveFlowDiffEntity::getIsDeleted, false).last(" and " + querySql + " " + (permissionSql == null ? "" : permissionSql))
				.set(AdsErpReceiveFlowDiffEntity::getExecStatus, "doing")
				.set(AdsErpReceiveFlowDiffEntity::getExecStatusName, "执行中")
				.setSql(" finish_time = null ")
				.update();
			}
		}
		return true;
	}
	
	@Override
	public Boolean updateRemark(UpdateRemarkDTO dto) {
		return lambdaUpdate().eq(AdsErpReceiveFlowDiffEntity::getId, dto.getId()).set(AdsErpReceiveFlowDiffEntity::getRemark, dto.getRemark()).update();
	}
	
	@Override
	public Boolean exportExcel(ExportParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("签收流水差异", FileTaskEventEnum.EXPORT_ADS_ERP_RECEIVE_FLOW_DIFF.getCode(), dto);
		return Boolean.TRUE;
	}

    @Override
    public PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO> transferInfoPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto) {
        PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO> pagingVO = dmpRestCloudService.transferInfoPaging(dto);
        return pagingVO;
    }

    @Override
    public Boolean exportTransferInfo(AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("朔源查询-直接调拨单", FileTaskEventEnum.EXPORT_ADS_ERP_RECEIVE_TRANSFER_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> sourcePlatformFlowPaging(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto) {
        PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> pagingVO = dmpRestCloudService.sourcePlatformFlowPaging(dto);
        return pagingVO;
    }

    @Override
    public Boolean exportPlatformFlow(AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("朔源查询-库存流水", FileTaskEventEnum.EXPORT_ADS_ERP_RECEIVE_INVENTORY_FLOW.getCode(), dto);
        return Boolean.TRUE;
    }

}
