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
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO.ExportParamDTO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO.PagingParamDTO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO.TotalDTO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO.UpdateRemarkDTO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDetailDTO;
import com.erp.model.dmp.entity.doris.AdsErpReceiveFlowDiffEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.mapper.doris.AdsErpReceiveFlowDiffMapper;
import com.erp.server.dmp.service.AdsErpReceiveFlowDiffService;
import com.erp.server.dmp.service.DmpRestCloudService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        l.setTabFlagName("平台单据多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("platform")).map(AdsErpReceiveFlowDiffDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpReceiveFlowDiffDTO.TabListDTO();
        l.setTabFlag("erp");
        l.setTabFlagName("ERP单据多");
        l.setCount(dblist.stream().filter(d -> d.getTabFlag().equals("erp")).map(AdsErpReceiveFlowDiffDTO.TabListDTO::getCount).findFirst().orElse(0));
        list.add(l);
        
        l = new AdsErpReceiveFlowDiffDTO.TabListDTO();
        l.setTabFlag("field");
        l.setTabFlagName("字段差异");
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
