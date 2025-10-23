package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import org.springframework.beans.BeanUtils;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ExhibitionOrderFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.SampleLedgerMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SampleLedgerService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private SysDictFeign sysDictFeign;

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

    /**
     * 根据类型查询所有台账信息
     *
     * @param searchAllDTO 查询条件对象，包含查询类型等参数
     * @return 返回SKU可用数量DTO列表，如果查询条件为空或类型为空则返回空列表
     */
    @Override
    public List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerAll(SampleLedgerDTO.SearchAllDTO searchAllDTO){
        if(Objects.isNull(searchAllDTO) || StringUtils.isBlank(searchAllDTO.getType())){
            return Collections.emptyList();
        }
        SampleLedgerDTO.SearchDTO dto = new SampleLedgerDTO.SearchDTO();
        dto.setType(searchAllDTO.getType());
        return this.baseMapper.listSkuAvailableQtyByUserId(dto);
    }

    /**
     * 批量查询台账当前数量
     * 
     * @param sampleLedgerIds 样品台账ID列表
     * @return 台账ID到当前数量的映射
     */
    @Override
    public Map<String, Integer> getLedgerQtyMap(List<String> sampleLedgerIds) {
        if (CollUtil.isEmpty(sampleLedgerIds)) {
            return Collections.emptyMap();
        }

        // 查询台账实体
        List<SampleLedgerEntity> ledgerList = this.lambdaQuery()
                .in(SampleLedgerEntity::getId, sampleLedgerIds)
                .list();

        // 构建ID到数量的映射
        return ledgerList.stream()
                .collect(Collectors.toMap(
                        SampleLedgerEntity::getId,
                        SampleLedgerEntity::getQty,
                        (existing, replacement) -> existing
                ));
    }


    @Override
    public PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO> listSku(PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO){
        if (pagingDTO == null || pagingDTO.getParams() == null) {
            throw new IllegalArgumentException("pagingDTO and its params must not be null");
        }
        Page<SampleLedgerDTO.SkuAvailableQtyDTO> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        SampleLedgerDTO.SearchDTO params = pagingDTO.getParams();
        if(CollUtil.isNotEmpty(params.getSkuNos()) && params.getSkuNos().size() == 1){
            params.setSkuNo(params.getSkuNos().get(0));
        }

        IPage<SampleLedgerDTO.SkuAvailableQtyDTO> pageData = this.baseMapper.listSku(query, params);

        List<SampleLedgerDTO.SkuAvailableQtyDTO> records = pageData.getRecords();
        //处理展会冻结库存数量
        handleExhibitionFreezeQty(params, records);
        //小于0则赋值为0
        records.forEach(e -> {
            if(e.getAvailableQty() < 0){
                e.setAvailableQty(0);
            }
        });

        Map<String, SampleLedgerDTO.SkuAvailableQtyDTO> map = records.stream().collect(Collectors.toMap(SampleLedgerDTO.SkuAvailableQtyDTO::getSkuNo, Function.identity(), (o1, o2) -> o1));

        List<SampleLedgerDTO.SkuAvailableQtyDTO> result = new ArrayList<>();

        if(CollUtil.isNotEmpty(params.getSkuNos()) && params.getSkuNos().size() > 1){
            for (String skuNo :  params.getSkuNos()) {
                SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = map.getOrDefault(skuNo, null);
                if(Objects.isNull(skuAvailableQtyDTO)){
                    skuAvailableQtyDTO = new SampleLedgerDTO.SkuAvailableQtyDTO();
                }
                result.add(skuAvailableQtyDTO);
            }
        }else {
            result.addAll(records);
        }

        pageData.setRecords(result);
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
        // 提取所有记录的SKU并去重
        List<String> sampleLedgerIds = records.stream().map(SampleLedgerDTO.SkuAvailableQtyDTO::getSampleLedgerId).distinct().collect(Collectors.toList());
        List<String> skuIds = records.stream().map(SampleLedgerDTO.SkuAvailableQtyDTO::getSkuId).distinct().collect(Collectors.toList());

        // 构造展会订单查询条件并获取冻结库存数量
        ExhibitionOrderDTO.SearchDTO dto = new  ExhibitionOrderDTO.SearchDTO();
        dto.setSkuIds(skuIds);
        dto.setSampleLedgerIds(sampleLedgerIds);
        // 判断查询类型是否为展会类型
        if(Objects.equals(params.getType(), SampleLedgerTypeEnum.EXHIBITION.getCode())){
            dto.setChildId(params.getChildId());
        }
        List<ExhibitionOrderDTO.FreezeQtyBySku> freezeQtyBySkus = exhibitionOrderFeign.listFreezeQtyBySku(dto);
        // 如果存在冻结库存数据，则更新可用库存数量
        if(CollUtil.isNotEmpty(freezeQtyBySkus)){
            // 将冻结库存数据转换为Map便于快速查找
            Map<String, ExhibitionOrderDTO.FreezeQtyBySku> map = freezeQtyBySkus.stream().collect(Collectors.toMap(ExhibitionOrderDTO.FreezeQtyBySku::getSampleLedgerId, Function.identity(),(o1,o2)->o1));

            // 遍历所有记录，扣除冻结库存数量
            for (SampleLedgerDTO.SkuAvailableQtyDTO record : records) {
                ExhibitionOrderDTO.FreezeQtyBySku freezeQtyBySku = map.getOrDefault(record.getSampleLedgerId(), null);
                if(Objects.nonNull(freezeQtyBySku)){
                    Integer freezeQty = freezeQtyBySku.getFreezeQty();
                    if(Objects.nonNull(freezeQty) && freezeQty > 0){
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

    @Override
    public SampleLedgerDTO.SampleBackView generateSampleBackInfo(List<String> ids) {
        if(CollUtil.isEmpty(ids)){
            throw new ServiceException(ApiError.ERROR_92271);
        }

        List<SampleLedgerEntity> sampleLedgerEntities = lambdaQuery().in(SampleLedgerEntity::getId, ids).list();
        if(CollUtil.isEmpty(sampleLedgerEntities)){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_VIEW,"退回单");
        }
        //校验是否存在多个退回人
        long count = sampleLedgerEntities.stream().map(SampleLedgerEntity::getUserId).distinct().count();
        if(count > 1){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_USER_IDS,"退回人");
        }

        SampleLedgerDTO.SampleBackView viewDTO = new SampleLedgerDTO.SampleBackView();
        SampleLedgerDTO.SearchDTO params = new SampleLedgerDTO.SearchDTO();
        params.setIds(ids);
        params.setType(SampleLedgerTypeEnum.BACK.getCode());
        params.setUserId(sampleLedgerEntities.get(0).getUserId());
        List<SampleLedgerDTO.SkuAvailableQtyDTO> detailList = this.baseMapper.listSkuAvailableQtyByUserId(params);
        if(CollUtil.isEmpty(detailList)){
            throw new ServiceException(ApiError.ERROR_GENERATE_SAMPLE_VIEW,"退回单");
        }

        viewDTO.setBackUserId(sampleLedgerEntities.get(0).getUserId());
        viewDTO.setBackUserName(sampleLedgerEntities.get(0).getUserName());
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
        if (CollUtil.isEmpty(records)) {
            return;
        }

        // 提取所有用户ID（包括userId和useUserId）
        List<String> userIds = records.stream()
            .flatMap(record -> Stream.of(record.getUserId(), record.getUseUserId()))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        // 提取useUserId列表
        List<String> useUserIdList = records.stream()
            .map(SampleLedgerDTO.ListDTO::getUseUserId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        try {
            // 调用用户服务获取用户信息（包含禁用状态）
            com.common.business.dto.base.BaseSearchDTO searchDTO = new com.common.business.dto.base.BaseSearchDTO();
            com.common.core.controller.vo.ApiResult<List<com.common.business.dto.FindUserDTO>> userResult = sysUserFeign.userList(searchDTO);
            
            // 查询示例用户信息（只查询需要的useUserId）
            List<BaseIdDTO> byIds = sysDictFeign.getByIds(useUserIdList);

            // 构建用户ID到用户信息的映射
            Map<String, com.common.business.dto.FindUserDTO> userMap = new HashMap<>();
            if (userResult != null && userResult.isSuccess() && CollUtil.isNotEmpty(userResult.getData())) {
                userMap = userResult.getData().stream()
                    .collect(Collectors.toMap(
                        com.common.business.dto.FindUserDTO::getUserId, 
                        Function.identity(),
                        (existing, replacement) -> existing
                    ));
            }
            
            // 构建示例用户ID到用户信息的映射
            Map<String, BaseIdDTO> sampleUserMap = new HashMap<>();
            sampleUserMap = byIds.stream()
                    .collect(Collectors.toMap(
                            BaseIdDTO::getId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));

            // 填充用户禁用状态和兜底userName、useUserName
            for (SampleLedgerDTO.ListDTO record : records) {
                // 兜底userName
                if (StrUtil.isBlank(record.getUserName()) && StrUtil.isNotBlank(record.getUserId())) {
                    com.common.business.dto.FindUserDTO user = userMap.get(record.getUserId());
                    if (user != null) {
                        record.setUserName(user.getUserName());
                    }
                }
                
                // 兜底useUserName
                if (StrUtil.isBlank(record.getUseUserName()) && StrUtil.isNotBlank(record.getUseUserId())) {
                    com.common.business.dto.FindUserDTO user = userMap.get(record.getUseUserId());
                    if (user != null) {
                        record.setUseUserName(user.getUserName());
                    } else {
                        // 如果通过sysUserFeign查不到，尝试通过SampleUseUserFeign查询
                        BaseIdDTO sampleUser = sampleUserMap.get(record.getUseUserId());
                        if (sampleUser != null) {
                            record.setUseUserName(sampleUser.getName());
                        }
                    }
                }
                
                // 填充用户禁用状态
                if (StringUtils.isNotBlank(record.getUserId())) {
                    com.common.business.dto.FindUserDTO user = userMap.get(record.getUserId());
                    if (user != null) {
                        record.setDisabled(user.getDisabled());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败，错误：{}", e.getMessage());
        }
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

    // ========== APP端专用方法实现 ==========

    @Override
    public List<SampleLedgerDTO.TabListDTO> tabListApp(PermissionsDTO dto) {
        SampleLedgerDTO.PagingParamDTO searchParam = new SampleLedgerDTO.PagingParamDTO();
        searchParam.setPermissionSql(dto.getPermissionSql());

        // 获取所有台账记录
        List<SampleLedgerEntity> allLedgers = baseMapper.selectList(
            new LambdaQueryWrapper<SampleLedgerEntity>()
                .eq(SampleLedgerEntity::getIsDeleted, false)
        );

        // 获取用户信息（包含禁用状态）
        com.common.business.dto.base.BaseSearchDTO searchDTO = new com.common.business.dto.base.BaseSearchDTO();
        com.common.core.controller.vo.ApiResult<List<com.common.business.dto.FindUserDTO>> userResult = sysUserFeign.userList(searchDTO);
        
        List<FindUserDTO> userList = new ArrayList<>();
        if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
            userList = userResult.getData();
        }
        
        Map<String, FindUserDTO> userMap = userList.stream()
            .collect(Collectors.toMap(FindUserDTO::getUserId, Function.identity()));

        // 统计启用和禁用的台账数量
        int enabledCount = 0;
        int disabledCount = 0;
        
        for (SampleLedgerEntity ledger : allLedgers) {
            FindUserDTO user = userMap.get(ledger.getUserId());
            if (user != null) {
                if (!user.getDisabled()) {
                    enabledCount++;
                } else {
                    disabledCount++;
                }
            } else {
                // 用户不存在，按禁用处理
                disabledCount++;
            }
        }

        // 构建移动端标签列表
        List<SampleLedgerDTO.TabListDTO> appList = new ArrayList<>();
        
        // 全部标签
        SampleLedgerDTO.TabListDTO allItem = new SampleLedgerDTO.TabListDTO();
        allItem.setTabFlag("all");
        allItem.setTabFlagName("全部");
        allItem.setCount(allLedgers.size());
        appList.add(allItem);
        
        // 启用标签
        SampleLedgerDTO.TabListDTO enabledItem = new SampleLedgerDTO.TabListDTO();
        enabledItem.setTabFlag("enabled");
        enabledItem.setTabFlagName("启用");
        enabledItem.setCount(enabledCount);
        appList.add(enabledItem);
        
        // 禁用标签
        SampleLedgerDTO.TabListDTO disabledItem = new SampleLedgerDTO.TabListDTO();
        disabledItem.setTabFlag("disabled");
        disabledItem.setTabFlagName("禁用");
        disabledItem.setCount(disabledCount);
        appList.add(disabledItem);

        return appList;
    }

    @Override
    public PagingVO<SampleLedgerDTO.ListDTO> pagingApp(PagingDTO<SampleLedgerDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleLedgerDTO.ListDTO> pageData = this.baseMapper.pagingApp(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public SampleLedgerDTO.ViewDTO view(String id) {
        SampleLedgerEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品台账数据");
        }
        
        SampleLedgerDTO.ViewDTO viewDTO = new SampleLedgerDTO.ViewDTO();
        BeanUtils.copyProperties(entity, viewDTO);
        
        return viewDTO;
    }


}
