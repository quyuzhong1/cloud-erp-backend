package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierVisitEntity;
import com.erp.model.scm.entity.SupplierVisitSkuEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierVisitEnum;
import com.erp.model.scm.enums.SupplierVisitResultEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.SupplierVisitMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 供应商拜访表 服务实现类
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
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    /**
     * 添加供应商拜访记录
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-21 10:26
     */
    @Override
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
        String visitType = dto.getVisitType();
        SupplierVisitEnum visitTypeEnum = SupplierVisitEnum.getByStatus(visitType);
        visit.setVisitType(visitTypeEnum);
        String visitResult = dto.getResult();
        SupplierVisitResultEnum visitResultEnum = SupplierVisitResultEnum.getByStatus(visitResult);
        visit.setResult(visitResultEnum);
        String id = IdWorker.getIdStr();
        visit.setId(id);
        List<String> peopleList = dto.getPeopleList();
        visit.setPeople(String.join(",", peopleList));
        Boolean result = this.save(visit);
        if (result) {
            List<String> urlList = dto.getAttachmentUrlList();
            List<String> nameList = dto.getAttachmentNameList();
            //附件
            attachmentService.batchSave(urlList,nameList,type,id);
            List<String> skuIdList = dto.getSkuIdList();
            if (CollectionUtils.isNotEmpty(skuIdList)) {
                List<SupplierVisitSkuEntity> addVisitSkuList = new ArrayList<>(skuIdList.size());
                for (String skuId : skuIdList) {
                    //这是sku 的
                    SupplierVisitSkuEntity visitSku = new SupplierVisitSkuEntity();
                    visitSku.setSkuId(skuId);
                    visitSku.setSupplierId(supplierId);
                    visitSku.setSupplierVisitId(id);
                    addVisitSkuList.add(visitSku);
                }
                supplierVisitSkuService.saveBatch(addVisitSkuList);
            }
            //添加日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一条拜访记录"), ModuleTypeEnum.SUPPLIER.getCode(),id,"新增拜访");
        }

        return result;
    }


    /**
     * 获取到供应商拜访信息
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
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        for (SupplierVisitDTO.PagingViewDTO item : list) {
            //拜访类型
            SupplierVisitEnum visitEnum = item.getVisitType();
            item.setTypeName(visitEnum.getName());
            SupplierVisitResultEnum visitResultEnum = item.getResult();
            item.setResultName(visitResultEnum.getName());
            String people = item.getPeople();
            List<String> userIdList = Arrays.asList(people.split(","));
            String userName = userList.stream().filter(u -> userIdList.contains(u.getUserId())).
                    map(FindUserDTO::getUserName).collect(Collectors.joining(","));

            item.setPeopleName(userName);


            //附件地址
            List<String> attachmentUrlList= attachmentList.stream().filter(a -> a.getBusinessId().equals(item.getId())).map(AttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());

            //附件地址
            List<String> attachmentNameList= attachmentList.stream().filter(a -> a.getBusinessId().equals(item.getId())).map(AttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            item.setAttachmentUrlList(attachmentUrlList);
            item.setAttachmentNameList(attachmentNameList);
            List<String> skuIdList=visitSkuList.stream().filter(s->s.getSupplierVisitId().
                    equals(item.getId())).map(SupplierVisitSkuEntity::getSkuId).collect(Collectors.toList());
            //获取到sku 信息
            List<SkuVO>  skuInfoList= skuList.stream().filter(sku->skuIdList.contains(sku.getSkuId())).
                    collect(Collectors.toList());
            String skuInfo=skuInfoList.stream().map(SkuVO::getSkuNo).collect(Collectors.joining(","));
            item.setSkuInfo(skuInfo);

        }

        return new PagingVO<>(pageData);
    }
}
