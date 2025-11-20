package com.erp.server.dmp.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO.ExpotParamDTO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO.PagingParamDTO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO.ReCreateDTO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO.TotalDTO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO.UpdateErpDTO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO.UpdateRemarkDTO;
import com.erp.model.dmp.entity.doris.AdsErpDiffOutstockSyncEntity;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffFlowEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.doris.AdsErpDiffOutstockSyncMapper;
import com.erp.server.dmp.service.AdsErpDiffOutstockSyncService;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.utils.RestCloudApiUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ERP出库单差异表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-11-18
 */
@DS("adsDoris")
@Slf4j
@Service
public class AdsErpDiffOutstockSyncServiceImpl extends SuperServiceImpl<AdsErpDiffOutstockSyncMapper, AdsErpDiffOutstockSyncEntity> implements AdsErpDiffOutstockSyncService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsErpDiffOutstockSyncDTO.AddDTO addDTO) {
        AdsErpDiffOutstockSyncEntity adsErpDiffOutstockSyncEntity = new AdsErpDiffOutstockSyncEntity();
        BeanMapperUtils.copy(addDTO, adsErpDiffOutstockSyncEntity);

        // 数据处理
        handleData(adsErpDiffOutstockSyncEntity);

        log.info("开始新增ERP出库单差异单");
        boolean save = super.save(adsErpDiffOutstockSyncEntity);
        if(!save) {
            throw new ServiceException("ERP出库单差异单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "ERP出库单差异单" , adsErpDiffOutstockSyncEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, adsErpDiffOutstockSyncEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(adsErpDiffOutstockSyncEntity.getId(), adsErpDiffOutstockSyncEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsErpDiffOutstockSyncDTO.UpdateDTO addOrUpdateDTO) {
        AdsErpDiffOutstockSyncEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "ERP出库单差异单"));
        AdsErpDiffOutstockSyncEntity adsErpDiffOutstockSyncEntity =  BeanMapperUtils.map(AdsErpDiffOutstockSyncEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsErpDiffOutstockSyncEntity);
        log.info("编辑 开始修改ERP出库单差异单数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsErpDiffOutstockSyncEntity);
        if(!save) {
            throw new ServiceException("ERP出库单差异单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录ERP出库单差异单日志数据，id：【{}】", adsErpDiffOutstockSyncEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), adsErpDiffOutstockSyncEntity.getId(), "ERP出库单差异单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, adsErpDiffOutstockSyncEntity, null, adsErpDiffOutstockSyncEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO> paging(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AdsErpDiffOutstockSyncDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AdsErpDiffOutstockSyncDTO.TabListDTO> tabList(PermissionsDTO param) {
    	List<AdsErpDiffOutstockSyncDTO.TabListDTO> list = new ArrayList<>();
        AdsErpDiffOutstockSyncDTO.PagingParamDTO searchParam = new AdsErpDiffOutstockSyncDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<AdsErpDiffOutstockSyncDTO.TabListDTO> dblist = baseMapper.tabList(searchParam);
        AdsErpDiffOutstockSyncDTO.TabListDTO l = new AdsErpDiffOutstockSyncDTO.TabListDTO();
        l.setTabFlag("platform");
        l.setTabFlagName("单据1多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("platform")).map(AdsErpDiffOutstockSyncDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpDiffOutstockSyncDTO.TabListDTO();
        l.setTabFlag("erp");
        l.setTabFlagName("单据2多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("erp")).map(AdsErpDiffOutstockSyncDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpDiffOutstockSyncDTO.TabListDTO();
        l.setTabFlag("field");
        l.setTabFlagName("字段错误");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("field")).map(AdsErpDiffOutstockSyncDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(AdsErpDiffOutstockSyncDTO.ExportDTO param, HttpServletResponse response) {
        List<AdsErpDiffOutstockSyncDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/adsErpDiffOutstockSync.xlsx";
        String name = "ERP出库单差异单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(AdsErpDiffOutstockSyncEntity adsErpDiffOutstockSyncEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public AdsErpDiffOutstockSyncDTO.ViewDTO view(String id) {
    AdsErpDiffOutstockSyncEntity adsErpDiffOutstockSyncEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到ERP出库单差异单数据"));
    AdsErpDiffOutstockSyncDTO.ViewDTO data = BeanMapperUtils.map(AdsErpDiffOutstockSyncDTO.ViewDTO.class, adsErpDiffOutstockSyncEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(AdsErpDiffOutstockSyncDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AdsErpDiffOutstockSyncDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(AdsErpDiffOutstockSyncDTO.ListDTO data : list) {
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
		Integer count = lambdaQuery().eq(AdsErpDiffOutstockSyncEntity::getCheckMonth, checkMonth)
				.eq(AdsErpDiffOutstockSyncEntity::getExecStatus, "doing").count();
		if(count != null && count > 0) {
			throw new ServiceException(dto.getCheckMonth() + "核对任务正在执行中");
		}
		boolean reCreate = RestCloudApiUtil.reCreate(checkMonth, "ods_antu/ods_flow_antu_excel_outstock");
		if(reCreate) {
			lambdaUpdate().eq(AdsErpDiffOutstockSyncEntity::getCheckMonth, checkMonth)
			.set(AdsErpDiffOutstockSyncEntity::getExecStatus, "doing")
			.set(AdsErpDiffOutstockSyncEntity::getExecStatusName, "执行中")
			.setSql(" finish_time = null ")
			.update();
		}
		return true;
	}
	
	@Override
	public Boolean updateErp(UpdateErpDTO dto) {
		return null;
	}
	
	@Override
	public Boolean updateRemark(UpdateRemarkDTO dto) {
		return lambdaUpdate().eq(AdsErpDiffOutstockSyncEntity::getId, dto.getId()).set(AdsErpDiffOutstockSyncEntity::getRemark, dto.getRemark()).update();
	}
	
	@Override
	public Boolean exportExcel(ExpotParamDTO dto) {
		downloadTaskFeign.saveDownloadTask("出库同步差异", FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_OUTSTOCK_SYNC.getCode(), dto);
		return true;
	}

}
