package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.AwdOutstockDetailEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.AwdOutstockDetailService;
import com.erp.server.wms.service.FirstMileDeliveryService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.AwdOutstockEntity;
import com.erp.server.wms.mapper.AwdOutstockMapper;
import com.erp.server.wms.service.AwdOutstockService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.AwdOutstockDTO;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import javax.servlet.http.HttpServletResponse;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_AWD_OUT_STOCK;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
 */
@Slf4j
@Service
public class AwdOutstockServiceImpl extends SuperServiceImpl<AwdOutstockMapper, AwdOutstockEntity> implements AwdOutstockService {

    @Resource
    private AwdOutstockDetailService awdOutstockDetailService;

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AwdOutstockDTO.AddDTO addDTO) {
        AwdOutstockEntity awdOutstockEntity = new AwdOutstockEntity();
        BeanMapperUtils.copy(addDTO, awdOutstockEntity);

        // 数据处理
        handleData(awdOutstockEntity);

        log.info("开始新增");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_AWD);
        awdOutstockEntity.setCode(code);
        boolean save = super.save(awdOutstockEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        boolean saveDetail = awdOutstockDetailService.add(addDTO.getAwdDetailList(),awdOutstockEntity.getId());
        if(!saveDetail) {
            throw new ServiceException("明细保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , awdOutstockEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AWD_OUTSTOCK.getCode(), awdOutstockEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(awdOutstockEntity.getId(), code);
    }

    @DistributeLocker(keyName = "#updateDTOList[0].id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchUpdateBillDate(List<AwdOutstockDTO.UpdateDTO> updateDTOList) {
        for (AwdOutstockDTO.UpdateDTO updateDTO : updateDTOList) {
            //查询旧数据
            AwdOutstockEntity old = Optional.ofNullable(super.getById(updateDTO.getId()))
                    .orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "AWD"));

            //映射新数据
            AwdOutstockEntity newEntity = BeanMapperUtils.map(AwdOutstockEntity.class, updateDTO);
            handleUpdateData(newEntity);

            //更新日期
            boolean updated = this.lambdaUpdate()
                    .set(AwdOutstockEntity::getBillDate, updateDTO.getBillDate())
                    .eq(AwdOutstockEntity::getId, updateDTO.getId())
                    .update();
            if (!updated) {
                throw new ServiceException("保存失败");
            }

            //下推发货单
            if (old.getBillDate() != null) {
                boolean generated = generateFirstMileDelivery(
                        new AwdOutstockDTO.GenerateDeliveryDTO(updateDTO.getId(), updateDTO.getBillDate())
                );
                if (!generated) {
                    throw new ServiceException(ApiError.ERROR_GENERATE_FIRST_MILE_DELIVERY);
                }
            }

            //记录日志
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【出库单】单据",
                    UserContext.getDefaultLoginUser().getUserName(), newEntity.getCode());
            operateLogService.addModuleOperateLogByObj(old, newEntity, null, newEntity.getId(), msg);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<AwdOutstockDTO.BatchUpdateBillDateViewDTO> batchUpdateBillDateView(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        ArrayList<AwdOutstockDTO.BatchUpdateBillDateViewDTO> batchUpdateBillDateViewDTOS = new ArrayList<>();
        List<AwdOutstockEntity> awdOutstockEntities = this.listByIds(ids);
        for (AwdOutstockEntity awdOutstockEntity : awdOutstockEntities) {
            AwdOutstockDTO.BatchUpdateBillDateViewDTO billDateViewDTO = new AwdOutstockDTO.BatchUpdateBillDateViewDTO();
            BeanUtils.copyProperties(awdOutstockEntity,billDateViewDTO);
            batchUpdateBillDateViewDTOS.add(billDateViewDTO);
        }
        return batchUpdateBillDateViewDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateFirstMileDelivery(AwdOutstockDTO.GenerateDeliveryDTO dto) {
        BatchResultDTO batchResultDTO = firstMileDeliveryService.generateFirstMileDeliveryByAwdOutStock(dto);
        return batchResultDTO.getSuccess();
    }


    @Override
    public PagingVO<AwdOutstockDTO.ListDTO> paging(PagingDTO<AwdOutstockDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AwdOutstockDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(AwdOutstockDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("AWD出库货件导出", EXPORT_WMS_AWD_OUT_STOCK.getCode(), param);
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(AwdOutstockEntity awdOutstockEntity) {

    }

    private void handleUpdateData(AwdOutstockEntity awdOutstockEntity) {
        List<AwdOutstockDetailEntity> detailList = awdOutstockDetailService.lambdaQuery()
                .eq(AwdOutstockDetailEntity::getMainId, awdOutstockEntity.getId())
                .list();

        for (AwdOutstockDetailEntity awdOutstockDetailEntity : detailList) {
            if (StringUtils.isBlank(awdOutstockDetailEntity.getSkuId())) {
                throw new ServiceException(ApiError.ERROR_MSKU_NOT_MAPPING,awdOutstockDetailEntity.getMsku());
            }
        }

    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AwdOutstockDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
   }
}
