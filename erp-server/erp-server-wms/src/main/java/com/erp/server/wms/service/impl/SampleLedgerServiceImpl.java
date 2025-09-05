package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.wms.dto.SampleScrapDetailDTO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.rpc.oms.feign.ExhibitionOrderFeign;
import com.erp.server.wms.mapper.SampleLedgerMapper;
import com.erp.server.wms.service.SampleLedgerService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleLedgerDTO;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.common.business.dto.base.PermissionsDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_LEDGER_REPORT;

/**
 * <p>
 * 样品台账统计 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleLedgerServiceImpl extends SuperServiceImpl<SampleLedgerMapper, SampleLedgerEntity> implements SampleLedgerService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private ExhibitionOrderFeign exhibitionOrderFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleLedgerDTO.AddDTO addDTO) {
        SampleLedgerEntity sampleLedgerEntity = new SampleLedgerEntity();
        BeanMapperUtils.copy(addDTO, sampleLedgerEntity);

        // 数据处理
        handleData(sampleLedgerEntity);

        log.info("开始新增样品台账统计");
        boolean save = super.save(sampleLedgerEntity);
        if(!save) {
            throw new ServiceException("样品台账统计保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品台账统计" , sampleLedgerEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleLedgerEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleLedgerEntity.getId(), sampleLedgerEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleLedgerDTO.UpdateDTO addOrUpdateDTO) {
        SampleLedgerEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品台账统计"));
        SampleLedgerEntity sampleLedgerEntity =  BeanMapperUtils.map(SampleLedgerEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleLedgerEntity);
        log.info("编辑 开始修改样品台账统计数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleLedgerEntity);
        if(!save) {
            throw new ServiceException("样品台账统计保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品台账统计日志数据，id：【{}】", sampleLedgerEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleLedgerEntity.getId(), "样品台账统计");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleLedgerEntity, null, sampleLedgerEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleLedgerEntity sampleLedgerEntity) {
    // TODO 验证数据 & 数据赋值
    }


    /**
     * 根据用户ID查询台账列表
     * @param dto 查询条件对象，包含用户ID、SKU编号等查询参数
     * @return 符合条件的台账实体列表，如果查询条件为空则返回空列表
     */
    @Override
    public List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerByUserId(SampleLedgerDTO.SearchDTO dto){
        if(Objects.isNull(dto)){
            return Collections.emptyList();
        }
        if(StringUtils.isBlank(dto.getUserId())){
            return Collections.emptyList();
        }
        if(CollUtil.isNotEmpty(dto.getSkuNos()) && dto.getSkuNos().size() == 1){
            dto.setSkuNo(dto.getSkuNos().get(0));
        }
        return this.baseMapper.listSkuAvailableQtyByUserId(dto);
    }


    @Override
    public PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO> listSku(PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO){
        Page<SampleLedgerDTO.SkuAvailableQtyDTO> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        SampleLedgerDTO.SearchDTO params = pagingDTO.getParams();
        if(CollUtil.isNotEmpty(params.getSkuNos()) && params.getSkuNos().size() == 1){
            params.setSkuNo(params.getSkuNos().get(0));
        }
        IPage<SampleLedgerDTO.SkuAvailableQtyDTO> pageData = this.baseMapper.listSku(query, params);

        List<SampleLedgerDTO.SkuAvailableQtyDTO> records = pageData.getRecords();
        //处理展会冻结库存数量
        handleExhibitionFreezeQty(params, records);
        pageData.setRecords(records);
        return new PagingVO<>(pageData);
    }

    /**
     * 处理展会冻结库存数量
     * <p>当查询类型为展会时，获取SKU的冻结数量并从可用库存中扣除</p>
     *
     * @param params   查询参数对象，包含查询类型和子ID等信息
     * @param records
     */
    private void handleExhibitionFreezeQty(SampleLedgerDTO.SearchDTO params, List<SampleLedgerDTO.SkuAvailableQtyDTO> records) {
        // 判断查询类型是否为展会类型
        if(Objects.equals(params.getType(), SampleLedgerTypeEnum.EXHIBITION.getCode())){
            // 提取所有记录的SKU并去重
            List<String> skuIds = records.stream().map(SampleLedgerDTO.SkuAvailableQtyDTO::getSkuId).distinct().collect(Collectors.toList());

            // 构造展会订单查询条件并获取冻结库存数量
            ExhibitionOrderDTO.SearchDTO dto = new  ExhibitionOrderDTO.SearchDTO();
            dto.setSkuIds(skuIds);
            dto.setChildId(params.getChildId());
            List<ExhibitionOrderDTO.FreezeQtyBySku> freezeQtyBySkus = exhibitionOrderFeign.listFreezeQtyBySku(dto);

            // 如果存在冻结库存数据，则更新可用库存数量
            if(CollUtil.isNotEmpty(freezeQtyBySkus)){
                // 将冻结库存数据转换为Map便于快速查找
                Map<String, ExhibitionOrderDTO.FreezeQtyBySku> map = freezeQtyBySkus.stream().collect(Collectors.toMap(ExhibitionOrderDTO.FreezeQtyBySku::getSkuId, Function.identity(),(o1,o2)->o1));

                // 遍历所有记录，扣除冻结库存数量
                for (SampleLedgerDTO.SkuAvailableQtyDTO record : records) {
                    ExhibitionOrderDTO.FreezeQtyBySku freezeQtyBySku = map.getOrDefault(record.getSkuId(), null);
                    if(Objects.nonNull(freezeQtyBySku)){
                        Integer freezeQty = freezeQtyBySku.getFreezeQty();
                        if(freezeQty > 0){
                            Integer availableQty = Objects.isNull(record.getAvailableQty()) ? 0 : record.getAvailableQty();
                            record.setAvailableQty(availableQty - freezeQty);
                        }
                        record.setMaxPrice(freezeQtyBySku.getMaxPrice());
                        record.setMinPrice(freezeQtyBySku.getMinPrice());
                        record.setAvgPrice(freezeQtyBySku.getAvgPrice());
                    }
                }
            }
        }
    }


    @Override
    public SampleLedgerDTO.SampleScrapView generateSampleScrapView(List<String> ids) {
        if(CollUtil.isEmpty(ids)){
            throw new ServiceException(ApiError.ERROR_92271);
        }

        List<SampleLedgerEntity> sampleLedgerEntities = lambdaQuery().in(SampleLedgerEntity::getId, ids).list();
        if(CollUtil.isEmpty(sampleLedgerEntities)){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_VIEW,"报废单");
        }
        //校验是否存在多个报废人
        long count = sampleLedgerEntities.stream().map(SampleLedgerEntity::getUserId).distinct().count();
        if(count > 1){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_USER_IDS,"报废人");
        }
        SampleLedgerDTO.SampleScrapView viewDTO = new SampleLedgerDTO.SampleScrapView();
        SampleLedgerDTO.SearchDTO params = new SampleLedgerDTO.SearchDTO();
        params.setIds(ids);
        params.setType(SampleLedgerTypeEnum.SCRAP.getCode());
        params.setUserId(sampleLedgerEntities.get(0).getUserId());
        List<SampleLedgerDTO.SkuAvailableQtyDTO> detailList = this.baseMapper.listSkuAvailableQtyByUserId(params);
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_VIEW,"报废单");
        }

        viewDTO.setScrapUserId(sampleLedgerEntities.get(0).getUserId());
        viewDTO.setScrapUserName(sampleLedgerEntities.get(0).getUserName());
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }



    @Override
    public SampleLedgerDTO.ExhibitionOrderView generateExhibitionOrderView(List<String> ids) {
        if(CollUtil.isEmpty(ids)){
            throw new ServiceException(ApiError.ERROR_92271);
        }

        List<SampleLedgerEntity> sampleLedgerEntities = lambdaQuery().in(SampleLedgerEntity::getId, ids).list();
        if(CollUtil.isEmpty(sampleLedgerEntities)){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_VIEW,"展会订单");
        }
        //校验是否存在多个领用人
        long count = sampleLedgerEntities.stream().map(SampleLedgerEntity::getUserId).distinct().count();
        if(count > 1){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_USER_IDS,"领用人");
        }

        SampleLedgerDTO.ExhibitionOrderView viewDTO = new SampleLedgerDTO.ExhibitionOrderView();
        SampleLedgerDTO.SearchDTO params = new SampleLedgerDTO.SearchDTO();
        params.setIds(ids);
        params.setType(SampleLedgerTypeEnum.EXHIBITION.getCode());
        params.setUserId(sampleLedgerEntities.get(0).getUserId());
        List<SampleLedgerDTO.SkuAvailableQtyDTO> detailList = this.baseMapper.listSkuAvailableQtyByUserId(params);
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_VIEW,"展会订单");
        }

        //处理展会冻结库存数量
        handleExhibitionFreezeQty(params, detailList);

        viewDTO.setRecipientUserId(sampleLedgerEntities.get(0).getUserId());
        viewDTO.setRecipientUserName(sampleLedgerEntities.get(0).getUserName());
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }

    /**
     * 分页列表查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param pagingParamDTO
     * @return PagingVO<SampleLedgerDTO.ListDTO>>
     */
    @Override
    public PagingVO<SampleLedgerDTO.ListDTO> paging(PagingDTO<SampleLedgerDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleLedgerDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 状态统计
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return List<SampleLedgerDTO.TabListDTO>>
     */
    @Override
    public List<SampleLedgerDTO.TabListDTO> tabList(PermissionsDTO dto) {
        // 使用一个SQL查询获取所有状态的统计数量
        List<SampleLedgerDTO.TabListDTO> list = baseMapper.getAllStatusCounts(dto.getPermissionSql(), false);
        return list;
    }

    /**
     * 状态统计（支持数量为0不显示）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto 权限参数
     * @param hideZeroQty 是否隐藏数量为0的记录
     * @return List<SampleLedgerDTO.TabListDTO>>
     */
    public List<SampleLedgerDTO.TabListDTO> tabList(PermissionsDTO dto, Boolean hideZeroQty) {
        // 使用一个SQL查询获取所有状态的统计数量
        List<SampleLedgerDTO.TabListDTO> list = baseMapper.getAllStatusCounts(dto.getPermissionSql(), hideZeroQty);
        return list;
    }

    /**
     * 填充列表数据
     * @param records 记录列表
     */
    private void fillList(List<SampleLedgerDTO.ListDTO> records) {
        // TODO: 根据业务需求填充额外的数据
        // 例如：填充关联的明细信息、计算字段等
    }

    /**
     * 异步导出
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param response
     * @return
     */
    @Override
    public Boolean exportList(SampleLedgerDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品台账统计导出", EXPORT_WMS_SAMPLE_LEDGER_REPORT.getCode(), dto);
        return true;
    }

    /**
     * 获取样品台账统计分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto 分页参数
     * @return 分页结果
     */
    @Override
    public PagingVO<SampleLedgerDTO.ListDTO> getSampleLedgerPageData(PagingDTO<SampleLedgerDTO.ExportDTO> dto) {
        Page<SampleLedgerDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SampleLedgerDTO.ListDTO> pageData = this.baseMapper.listExport(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

}
