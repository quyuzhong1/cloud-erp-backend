package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.erp.model.dmp.dto.AdsErpInventoryDiffDTO;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.doris.AdsErpInventoryDiffKingdeeEntity;
import com.erp.server.dmp.mapper.doris.AdsErpInventoryDiffKingdeeMapper;
import com.erp.server.dmp.service.AdsErpInventoryDiffKingdeeService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.AdsErpInventoryDiffKingdeeDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 金蝶库存差异 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@Service
public class AdsErpInventoryDiffKingdeeServiceImpl extends SuperServiceImpl<AdsErpInventoryDiffKingdeeMapper, AdsErpInventoryDiffKingdeeEntity> implements AdsErpInventoryDiffKingdeeService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AdsErpInventoryDiffKingdeeDTO.AddDTO addDTO) {
        AdsErpInventoryDiffKingdeeEntity adsErpInventoryDiffKingdeeEntity = new AdsErpInventoryDiffKingdeeEntity();
        BeanMapperUtils.copy(addDTO, adsErpInventoryDiffKingdeeEntity);

        // 数据处理
        handleData(adsErpInventoryDiffKingdeeEntity);

        log.info("开始新增金蝶库存差异");
        boolean save = super.save(adsErpInventoryDiffKingdeeEntity);
        if(!save) {
            throw new ServiceException("金蝶库存差异保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "金蝶库存差异" , adsErpInventoryDiffKingdeeEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, adsErpInventoryDiffKingdeeEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(adsErpInventoryDiffKingdeeEntity.getId(), adsErpInventoryDiffKingdeeEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AdsErpInventoryDiffKingdeeDTO.UpdateDTO addOrUpdateDTO) {
        AdsErpInventoryDiffKingdeeEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "金蝶库存差异"));
        AdsErpInventoryDiffKingdeeEntity adsErpInventoryDiffKingdeeEntity =  BeanMapperUtils.map(AdsErpInventoryDiffKingdeeEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(adsErpInventoryDiffKingdeeEntity);
        log.info("编辑 开始修改金蝶库存差异数据，id：【{}】", old.getId());
        boolean save = super.updateById(adsErpInventoryDiffKingdeeEntity);
        if(!save) {
            throw new ServiceException("金蝶库存差异保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录金蝶库存差异日志数据，id：【{}】", adsErpInventoryDiffKingdeeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), adsErpInventoryDiffKingdeeEntity.getId(), "金蝶库存差异");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, adsErpInventoryDiffKingdeeEntity, null, adsErpInventoryDiffKingdeeEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO> paging(PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AdsErpInventoryDiffKingdeeDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public AdsErpInventoryDiffKingdeeDTO.StatisticsDTO statistics(PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> pagingParamDTO) {
        // 计算合计数量
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        List<AdsErpInventoryDiffKingdeeDTO.StatisticsDTO> list = baseMapper.statistics(pagingParamDTO.getParams());
        if (CollectionUtils.isNotEmpty(list)){
            if (null != list.get(0)){
                return list.get(0);
            }
        }
        return AdsErpInventoryDiffKingdeeDTO.StatisticsDTO.init();
    }

    @Override
    public Boolean exportList(AdsErpInventoryDiffKingdeeDTO.ExportDTO param, HttpServletResponse response) {
       downloadTaskFeign.saveDownloadTask("金蝶库存差异Excel导出", FileTaskEventEnum.EXPORT_ADS_ERP_INVENTORY_DIFF_KINGDEE.getCode(), param);
       return true;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AdsErpInventoryDiffKingdeeEntity adsErpInventoryDiffKingdeeEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public AdsErpInventoryDiffKingdeeDTO.ViewDTO view(String id) {
    AdsErpInventoryDiffKingdeeEntity adsErpInventoryDiffKingdeeEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到金蝶库存差异数据"));
    AdsErpInventoryDiffKingdeeDTO.ViewDTO data = BeanMapperUtils.map(AdsErpInventoryDiffKingdeeDTO.ViewDTO.class, adsErpInventoryDiffKingdeeEntity);
    // 数据填充处理
    fillOne(data);
    // TODO 查询明细数据（如果有的话）
    return data;
    }

    private void fillOne(AdsErpInventoryDiffKingdeeDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
          return;
        }
    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<AdsErpInventoryDiffKingdeeDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(AdsErpInventoryDiffKingdeeDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }

    @Override
    public BatchResultDTO updateRemark(String id, String remark) {
        AdsErpInventoryDiffKingdeeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("金蝶库存差异数据"));

        // 删除主单数据
        log.info("删除 金蝶库存主单数据，id：【{}】", id);
        this.lambdaUpdate()
                .set(AdsErpInventoryDiffKingdeeEntity::getRemark, remark)
                .eq(AdsErpInventoryDiffKingdeeEntity::getId, id)
                .update();
        // 删除日志数据
        log.info("删除 开始金蝶库存差异日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】更新备注操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "金蝶库存差异");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ADS_ERP_INVENTORY_DIFF_KINGDEE.getCode(), entity.getId(), "更新备注金蝶库存差异");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }
}
