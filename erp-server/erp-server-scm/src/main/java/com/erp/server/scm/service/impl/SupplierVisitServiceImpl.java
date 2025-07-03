package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.ExcelUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.model.scm.dto.excel.SupplierVisitImportExcelDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierVisitResultEnum;
import com.erp.model.scm.enums.SupplierVisitEnum;
import com.erp.model.wms.dto.excel.WarehouseExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.listener.SupplierVisitExcelListener;
import com.erp.server.scm.mapper.SupplierVisitMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_VISIT_REPORT;

/**
 * <p>
 * 现场考察 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Service
public class SupplierVisitServiceImpl extends SuperServiceImpl<SupplierVisitMapper, SupplierVisitEntity> implements SupplierVisitService {

    @Resource
    private SupplierService supplierService;


    @Resource
    private AttachmentService attachmentService;


    @Resource
    private SupplierVisitSkuService supplierVisitSkuService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    /**
     * 添加供应商现场考察
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-21 10:26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SupplierVisitDTO.AddDTO dto) {
        //供应商id
        String supplierId = dto.getSupplierId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }

        Class<SupplierVisitEntity> credentialClass = SupplierVisitEntity.class;
        TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
        //获取到表名
        String type = tableName.value();
        SupplierVisitEntity visit = new SupplierVisitEntity();
        visit.setContent(dto.getContent());
        visit.setSupplierId(supplierId);
        visit.setVisitTime(dto.getVisitTime());
        //拜访类型
        visit.setVisitType(dto.getVisitType());
        visit.setResult(dto.getResult());
        String id = IdWorker.getIdStr();
        visit.setId(id);
        List<String> peopleList = dto.getPeopleList();
        visit.setPeople(String.join(",", peopleList));
        Boolean result = this.save(visit);
        if (result) {
            List<String> urlList = dto.getAttachmentUrlList();
            List<String> nameList = dto.getAttachmentNameList();
            //附件
            attachmentService.batchSave(urlList, nameList, type, id);

            //物料
            List<String> skuIdList = dto.getSkuIdList();
            if (CollectionUtils.isNotEmpty(skuIdList)) {
                //获取到物料信息
                List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
                Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName, (o1, o2) -> o1));

                List<SupplierVisitSkuEntity> addVisitSkuList = new ArrayList<>(skuIdList.size());
                for (String skuId : skuIdList) {
                    //这是sku 的
                    SupplierVisitSkuEntity visitSku = new SupplierVisitSkuEntity();
                    visitSku.setSkuId(skuId);
                    visitSku.setSkuNo(skuMap.getOrDefault(skuId,""));
                    visitSku.setSupplierId(supplierId);
                    visitSku.setSupplierVisitId(id);
                    addVisitSkuList.add(visitSku);
                }
                supplierVisitSkuService.saveBatch(addVisitSkuList);
            }

            //添加日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一条现场考察"), ModuleTypeEnum.SUPPLIER.getCode(), dto.getSupplierId(), "现场考察");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SupplierVisitDTO.UpdateDTO dto) {
        if(StringUtils.isBlank(dto.getSupplierId())){
            throw new ServiceException("供应商不能为空");
        }
        SupplierEntity supplier = supplierService.getById(dto.getSupplierId());
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }

        SupplierVisitEntity oldEntity = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL,"现场考察"));
        SupplierVisitEntity entity = new SupplierVisitEntity();
        BeanMapper.copy(dto, entity);

        List<String> peopleList = dto.getPeopleList();
        entity.setPeople(String.join(",", peopleList));

        //更新
        boolean save = updateById(entity);
        if(!save){
            throw new ServiceException("现场考察更新失败");
        }

        //操作日志
        String msg = StrUtil.format("用户【{}】更新【{}】供应商现场考察", UserContext.getDefaultLoginUser().getUserName(), supplier.getName());
        moduleOperateLogService.addModuleOperateLogByObj(oldEntity, entity, ModuleTypeEnum.SUPPLIER.getCode(), dto.getSupplierId(), "", msg);

        //获取用户信息
        List<String> oldUserIdList = Arrays.asList(oldEntity.getPeople().split(","));
        //判断是否有变化
        boolean isEqual = oldUserIdList.size() == peopleList.size() &&
                oldUserIdList.containsAll(peopleList) &&
                peopleList.containsAll(oldUserIdList);
        //有变化再记录变化日志
        if(Boolean.FALSE.equals(isEqual)){
            //查询所有的拜访人
            List<String> userIdList = new ArrayList<>();
            userIdList.addAll(oldUserIdList);
            userIdList.addAll(peopleList);
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);

            if(CollUtil.isNotEmpty(oldUserIdList)){
                // 处理删除的数据
                List<String> removeIds = oldUserIdList.stream()
                        .filter(id -> !peopleList.contains(id))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(removeIds)){
                    List<FindUserDTO> removeUserList = userList.stream().filter(e -> removeIds.contains(e.getUserId())).collect(Collectors.toList());
                    //添加日志
                    List<Pair<String, String>> removePairList = removeUserList.stream().map(obj -> new Pair<>(dto.getSupplierId(), obj.getUserName())).collect(Collectors.toList());
                    moduleOperateLogService.batchAddModuleOperateLog("删除了一个拜访人【%s】", ModuleTypeEnum.SUPPLIER.getCode(), removePairList, "编辑操作");
                }
            }

            List<String> addIds = peopleList.stream()
                    .filter(id -> !oldUserIdList.contains(id))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(addIds)){
                List<FindUserDTO> addUserList = userList.stream().filter(e -> addIds.contains(e.getUserId())).collect(Collectors.toList());
                //添加日志
                List<Pair<String, String>> addPairList = addUserList.stream().map(obj -> new Pair<>(dto.getSupplierId(), obj.getUserName())).collect(Collectors.toList());
                moduleOperateLogService.batchAddModuleOperateLog("添加了一个拜访人【%s】", ModuleTypeEnum.SUPPLIER.getCode(), addPairList, "编辑操作");
            }
        }

        //处理物料
        List<String> skuIdList = dto.getSkuIdList();
        if (CollectionUtils.isEmpty(skuIdList)) {
            //sku信息
            List<String> oldSkuIdList = supplierVisitSkuService.lambdaQuery().eq(SupplierVisitSkuEntity::getSupplierVisitId, dto.getId()).list().stream().map(SupplierVisitSkuEntity::getSkuId).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(oldSkuIdList)){
                //删除所有物料
                supplierVisitSkuService.lambdaUpdate()
                        .eq(SupplierVisitSkuEntity::getSupplierVisitId,dto.getId())
                        .set(SupplierVisitSkuEntity::getIsDeleted,Boolean.TRUE)
                        .update();
                //获取到物料信息
                List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(oldSkuIdList);
                //添加日志
                List<Pair<String, String>> removePairList = skuList.stream().map(obj -> new Pair<>(dto.getSupplierId(), obj.getSkuNo())).collect(Collectors.toList());
                moduleOperateLogService.batchAddModuleOperateLog("删除了一个拜访物料【%s】", ModuleTypeEnum.SUPPLIER.getCode(), removePairList, "编辑操作");
            }
        }else {
            List<String> oldSkuIdList = supplierVisitSkuService.lambdaQuery().eq(SupplierVisitSkuEntity::getSupplierVisitId, dto.getId()).list().stream().map(SupplierVisitSkuEntity::getSkuId).collect(Collectors.toList());;
            if(CollectionUtils.isNotEmpty(oldSkuIdList)){
                // 处理删除的数据
                List<String> removeIds = oldSkuIdList.stream()
                        .filter(id -> !skuIdList.contains(id))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(removeIds)){
                    supplierVisitSkuService.lambdaUpdate()
                            .eq(SupplierVisitSkuEntity::getSupplierVisitId,dto.getId())
                            .in(SupplierVisitSkuEntity::getSkuId,removeIds)
                            .set(SupplierVisitSkuEntity::getIsDeleted,Boolean.TRUE)
                            .update();
                    //获取到物料信息
                    List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(removeIds);
                    //添加日志
                    List<Pair<String, String>> removePairList = skuList.stream().map(obj -> new Pair<>(dto.getSupplierId(), obj.getSkuNo())).collect(Collectors.toList());
                    moduleOperateLogService.batchAddModuleOperateLog("删除了一个拜访物料【%s】", ModuleTypeEnum.SUPPLIER.getCode(), removePairList, "编辑操作");
                }
            }

            //处理需要新增的数据
            List<SupplierVisitSkuEntity> addVisitSkuList = new ArrayList<>();
            List<String> addIds = skuIdList.stream()
                    .filter(id -> !oldSkuIdList.contains(id))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(addIds)){
                //获取到物料信息
                List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(addIds);
                Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuId, SkuVO::getSkuName, (o1, o2) -> o1));

                for (String skuId : addIds) {
                    //这是sku 的
                    SupplierVisitSkuEntity visitSku = new SupplierVisitSkuEntity();
                    visitSku.setSkuId(skuId);
                    visitSku.setSkuNo(skuMap.getOrDefault(skuId,""));
                    visitSku.setSupplierId(dto.getSupplierId());
                    visitSku.setSupplierVisitId(dto.getId());
                    addVisitSkuList.add(visitSku);
                }
                supplierVisitSkuService.saveBatch(addVisitSkuList);

                //添加日志
                List<Pair<String, String>> addPairList = skuList.stream().map(obj -> new Pair<>(dto.getSupplierId(), obj.getSkuNo())).collect(Collectors.toList());
                moduleOperateLogService.batchAddModuleOperateLog("添加了一个拜访物料【%s】", ModuleTypeEnum.SUPPLIER.getCode(), addPairList, "编辑操作");
            }

        }


        //附件集合
        List<String> attachmentUrlList = dto.getAttachmentUrlList();
        List<String> attachmentNameList = dto.getAttachmentNameList();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()){
            List<AttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessId(entity.getId());
            if(CollUtil.isNotEmpty(oldAttachmentList)){
                // 处理删除的数据
                List<AttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(remove)){
                    attachmentService.deleteByUrlList(remove.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                }
            }

            //处理需要新增的数据
            List<String> oldUrlList = oldAttachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> add = attachmentUrlList.stream()
                    .filter(url -> !oldUrlList.contains(url))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(add)){
                //处理附件
                Class<SupplierVisitEntity> credentialClass = SupplierVisitEntity.class;
                TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
                //获取到表名
                String type = tableName.value();
                List<AttachmentEntity> batchAttachmentList = new ArrayList<>(10);
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    if(!add.contains(attachmentUrlList.get(i))){
                        continue;
                    }
                    AttachmentEntity addAttachment = new AttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    addAttachment.setAttachName(attachmentNameList.get(i));
                    addAttachment.setBusinessId(entity.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }
                if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                    attachmentService.saveBatch(batchAttachmentList);
                }
            }
        }else {
            //删除所有 附件
            attachmentService.deleteByBusinessIds(Arrays.asList(entity.getId()));
        }
        return Boolean.TRUE;
    }


    /**
     * 获取到供应商现场考察
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierVisitDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-21 11:32
     */
    @Override
    public PagingVO<SupplierVisitDTO.PagingViewDTO> paging(PagingDTO<BaseIdDTO> dto) {
        //供应商id
        String supplierId = dto.getParams().getId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if(Objects.isNull(supplier)){
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, supplierId);
        List<SupplierVisitDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        List<String> ids = list.stream().map(SupplierVisitDTO.PagingViewDTO::getId).collect(Collectors.toList());
        List<String> peopleIdList = new ArrayList<>(5);

        List<String> peopleList = list.stream().map(SupplierVisitDTO.PagingViewDTO::getPeople).collect(Collectors.toList());
        for (String people : peopleList) {
            String peopleStr[] = people.split(",");
            for (String str : peopleStr) {
                peopleIdList.add(str);
            }
        }
        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(peopleIdList);

        //获取到附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(ids);
        //sku id
        List<SupplierVisitSkuEntity> visitSkuList = supplierVisitSkuService.getByVisitIds(ids);
        List<String> skuIds = visitSkuList.stream().map(SupplierVisitSkuEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        for (SupplierVisitDTO.PagingViewDTO item : list) {
            //拜访类型
            SupplierVisitEnum visitEnum = item.getVisitType();
            item.setVisitTypeName(visitEnum.getName());

            SupplierVisitResultEnum visitResultEnum = item.getResult();
            item.setResultName(visitResultEnum.getName());

            String people = item.getPeople();
            List<String> userIdList = Arrays.asList(people.split(","));
            String userName = userList.stream().filter(u -> userIdList.contains(u.getUserId())).
                    map(FindUserDTO::getUserName).collect(Collectors.joining(","));

            item.setPeopleName(userName);
            item.setSupplierName(supplier.getName());


            //附件地址
            List<String> attachmentUrlList = attachmentList.stream().filter(a -> a.getBusinessId().equals(item.getId())).map(AttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());

            //附件地址
            List<String> attachmentNameList = attachmentList.stream().filter(a -> a.getBusinessId().equals(item.getId())).map(AttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            item.setAttachmentUrlList(attachmentUrlList);
            item.setAttachmentNameList(attachmentNameList);
            List<String> skuIdList = visitSkuList.stream().filter(s -> s.getSupplierVisitId().
                    equals(item.getId())).map(SupplierVisitSkuEntity::getSkuId).collect(Collectors.toList());
            //获取到sku 信息
            List<SkuVO> skuInfoList = skuList.stream().filter(sku -> skuIdList.contains(sku.getSkuId())).
                    collect(Collectors.toList());
            String skuInfo = skuInfoList.stream().map(SkuVO::getSkuNo).collect(Collectors.joining(","));
            item.setSkuInfo(skuInfo);

        }

        return new PagingVO<>(pageData);
    }

    @Override
    public List<SupplierVisitDTO.TabListDTO> tabList(PermissionsDTO param) {
        SupplierCredentialDTO.PagingParamDTO searchParam = new SupplierCredentialDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SupplierVisitDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        Map<String, SupplierVisitDTO.TabListDTO> map = list.stream().collect(Collectors.toMap(SupplierVisitDTO.TabListDTO::getTabFlag, t -> t));
        List<SupplierVisitDTO.TabListDTO> result = new ArrayList<>();
        result.add(new SupplierVisitDTO.TabListDTO( SupplierVisitResultEnum.CONFORMITY.getCode(), SupplierVisitResultEnum.CONFORMITY.getName() , map.containsKey(SupplierVisitResultEnum.CONFORMITY.getCode()) ? map.get(SupplierVisitResultEnum.CONFORMITY.getCode()).getCount() : 0   ));
        result.add(new SupplierVisitDTO.TabListDTO( SupplierVisitResultEnum.PENDING.getCode(), SupplierVisitResultEnum.PENDING.getName() , map.containsKey(SupplierVisitResultEnum.PENDING.getCode()) ? map.get(SupplierVisitResultEnum.PENDING.getCode()).getCount() : 0   ));
        result.add(new SupplierVisitDTO.TabListDTO( SupplierVisitResultEnum.NONCONFORMITY.getCode(), SupplierVisitResultEnum.NONCONFORMITY.getName() , map.containsKey(SupplierVisitResultEnum.NONCONFORMITY.getCode()) ? map.get(SupplierVisitResultEnum.NONCONFORMITY.getCode()).getCount() : 0   ));
        return result;
    }

    @Override
    public PagingVO<SupplierVisitDTO.ListDTO> pagingList(PagingDTO<SupplierVisitDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SupplierVisitDTO.ListDTO> pageData = this.baseMapper.pagingList(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<SupplierVisitDTO.ListDTO> list) {
        List<String> ids = list.stream().map(SupplierVisitDTO.ListDTO::getId).collect(Collectors.toList());
        List<String> peopleIdList = new ArrayList<>(5);

        List<String> peopleList = list.stream().map(SupplierVisitDTO.ListDTO::getPeople).collect(Collectors.toList());
        for (String people : peopleList) {
            String peopleStr[] = people.split(",");
            for (String str : peopleStr) {
                peopleIdList.add(str);
            }
        }
        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(peopleIdList);

        //获取到附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(ids);
        //sku id
        List<SupplierVisitSkuEntity> visitSkuList = supplierVisitSkuService.getByVisitIds(ids);
        List<String> skuIds = visitSkuList.stream().map(SupplierVisitSkuEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        for (SupplierVisitDTO.ListDTO item : list) {
            //拜访类型
            String visitType = item.getVisitType();
            item.setVisitTypeName(SupplierVisitEnum.getName(visitType));
            String result = item.getResult();
            item.setResultName(SupplierVisitResultEnum.getName(result));
            String people = item.getPeople();
            List<String> userIdList = Arrays.asList(people.split(","));
            String userName = userList.stream().filter(u -> userIdList.contains(u.getUserId())).
                    map(FindUserDTO::getUserName).collect(Collectors.joining(","));

            item.setPeopleName(userName);
            //附件地址
            List<String> attachmentUrlList = attachmentList.stream().filter(a -> a.getBusinessId().equals(item.getId())).map(AttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());

            //附件地址
            List<String> attachmentNameList = attachmentList.stream().filter(a -> a.getBusinessId().equals(item.getId())).map(AttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            item.setAttachmentUrlList(attachmentUrlList);
            item.setAttachmentNameList(attachmentNameList);
            List<String> skuIdList = visitSkuList.stream().filter(s -> s.getSupplierVisitId().
                    equals(item.getId())).map(SupplierVisitSkuEntity::getSkuId).collect(Collectors.toList());
            //获取到sku 信息
            List<SkuVO> skuInfoList = skuList.stream().filter(sku -> skuIdList.contains(sku.getSkuId())).
                    collect(Collectors.toList());
            String skuInfo = skuInfoList.stream().map(SkuVO::getSkuNo).collect(Collectors.joining(","));
            item.setSkuInfo(skuInfo);
        }
    }

    @Override
    public SupplierVisitDTO.ViewDTO view(String id) {
        SupplierVisitEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL,"现场考察"));
        // 数据填充处理
        SupplierVisitDTO.ViewDTO view = fillOne(entity);
        return view;
    }

    private SupplierVisitDTO.ViewDTO fillOne(SupplierVisitEntity entity) {
        SupplierVisitDTO.ViewDTO view = new SupplierVisitDTO.ViewDTO();
        BeanMapper.copy(entity,view);
        //拜访类型
        String visitType = view.getVisitType();
        view.setVisitTypeName(SupplierVisitEnum.getName(visitType));
        //拜访结果
        String result = view.getResult();
        view.setResultName(SupplierVisitResultEnum.getName(result));

        String people = entity.getPeople();
        if(StringUtils.isNotBlank(people)){
            List<String> userIdList = Arrays.asList(people.split(","));
            //获取用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
            List<String> userNameList = userList.stream().map(FindUserDTO::getUserName).collect(Collectors.toList());
            view.setPeopleList(userIdList);
            view.setPeopleNameList(userNameList);
        }

        List<String> ids =Arrays.asList(entity.getId());
        //获取到附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(ids);
        //sku id
        List<SupplierVisitSkuEntity> visitSkuList = supplierVisitSkuService.getByVisitIds(ids);

        //附件地址
        List<String> attachmentUrlList = attachmentList.stream().filter(a -> a.getBusinessId().equals(view.getId())).map(AttachmentDTO.UpdateDTO::getAttachUrl).
                collect(Collectors.toList());

        //附件地址
        List<String> attachmentNameList = attachmentList.stream().filter(a -> a.getBusinessId().equals(view.getId())).map(AttachmentDTO.UpdateDTO::getAttachName).
                collect(Collectors.toList());
        view.setAttachmentUrlList(attachmentUrlList);
        view.setAttachmentNameList(attachmentNameList);

        if(CollUtil.isNotEmpty(visitSkuList)){
            List<String> skuIds = visitSkuList.stream().map(SupplierVisitSkuEntity::getSkuId).distinct().collect(Collectors.toList());
            //sku信息
            List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
            List<String> skuNoList = skuList.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
            view.setSkuIdList(skuIds);
            view.setSkuNoList(skuNoList);
        }
        return view;
    }

    @Override
    public void exportList(SupplierVisitDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("供应商现场考察导出", EXPORT_SCM_SUPPLIER_VISIT_REPORT.getCode(), param);
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //供应商 (已启用)
        List<SupplierEntity> supplierList = supplierService.lambdaQuery()
                .eq(SupplierEntity::getDisabled, Boolean.FALSE)
                .list();
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e,(o1,o2)->o1));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        SupplierVisitExcelListener excelListenerUtil = new SupplierVisitExcelListener(this,supplierList,map,userList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SupplierVisitImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入拜访错误！", e);
            return Boolean.FALSE;
        }
        List<SupplierVisitImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "拜访错误信息";
            ExcelUtil.export(fileName, "supplierVisitError", errorList, SupplierVisitImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/supplierVisit.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("warehouse downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchImportVisit(List<SupplierVisitDTO.ImportAddDTO> addList) {
        if(CollUtil.isNotEmpty(addList)){
            List<SupplierVisitEntity> supplierVisitEntities = BeanMapper.copyList(addList, SupplierVisitEntity.class);
            this.saveBatch(supplierVisitEntities);

            for (SupplierVisitDTO.ImportAddDTO importAddDTO : addList) {
                if(CollUtil.isNotEmpty(importAddDTO.getSupplierVisitSkuEntityList())){
                    supplierVisitSkuService.saveBatch(importAddDTO.getSupplierVisitSkuEntityList());
                }
            }
        }
    }
}
