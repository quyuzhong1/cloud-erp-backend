package com.erp.server.wms.controller.api;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoB2cReturnFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.wms.query.SoReturnInstockQueryHandler;
import com.erp.server.wms.service.SoReturnInstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售退货入库单
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("销售退货入库单")
@RequestMapping("/soReturnInstock")
public class SoReturnInstockController extends BaseController {

    @Resource
    private SoReturnInstockService soReturnInstockService;
    @Resource
    private SoB2cReturnFeign soB2cReturnFeign;
    @Resource
    private SoB2cFeign soB2cFeign;
    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnInstockDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "srid.warehouse_id",
            menuCode = "wms:soReturnInstock:paging",
            tableAlias = "sri"
    )
    @WebAdvanceQuery(handler = SoReturnInstockQueryHandler.class)
    public ApiResult<PagingVO<SoReturnInstockDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnInstockDTO.PagingParam> dto) {
        PagingVO<SoReturnInstockDTO.PagingView> pagingVO = soReturnInstockService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.soDeliveryNoticeCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,warehouse_keeper_id",
            warehouseTableField = "srid.warehouse_id",
            menuCode = "wms:soReturnInstock:paging",
            tableAlias = "sri"
    )
    public ApiResult<List<SoReturnInstockDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnInstockDTO.StatusCountDTO> soDeliveryNoticeCountDTOS = soReturnInstockService.listCount(dto);
        return success(soDeliveryNoticeCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增销售退货入库单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnInstockDTO.Add dto) {
        String id = soReturnInstockService.add(dto);
        return CharSequenceUtil.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售退货入库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:update",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnInstockDTO.Update dto) {
        Boolean flag = soReturnInstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnInstockDTO.ViewDTO>
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:view",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult<SoReturnInstockDTO.View> view(@RequestParam("id") String id) {
        SoReturnInstockDTO.View dto = soReturnInstockService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售退货入库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:submit",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnInstockService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售退货入库单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:add",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnInstockDTO.Add dto) {
        Boolean flag = soReturnInstockService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售退货入库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:update",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnInstockDTO.Update dto) {
        Boolean flag = soReturnInstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售退货入库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:approve",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnInstockEntity> entityList = soReturnInstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"退货入库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnInstockService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("退货入库单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售退货入库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:disApprove",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnInstockEntity> entityList = soReturnInstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"退货入库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnInstockService.disApprove(entity,Boolean.TRUE));
            }catch (Exception e){
                log.error("退货入库单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售退货入库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:cancelProcess",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnInstockService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售退货入库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:invalid",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnInstockService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:delete",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        try {
            List<BatchResultDTO> resultDTOS = soReturnInstockService.deleteByIds(idsDTO.getIds(), true);
            return success(resultDTOS);
        } catch (Exception e) {
            log.error("批量删除销售退货入库单失败", e);
            return failure(e.getMessage());
        }
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售退货入库单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody SoReturnInstockDTO.PagingParam dto) {
        Boolean flag = soReturnInstockService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 质检下推退货入库单-保存
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param list list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "质检下推退货入库单")
    @PostMapping(value = "/generateSoReturnInstockSave")
    public ApiResult generateSoReturnInstockSave(@RequestBody ValidList<SoReturnInstockDTO.GenerateSoReturnInstockView> list) {
        Boolean flag = soReturnInstockService.qcGenerateSoReturnInstockSave(list.getList());
        return flag == true ? success() : failure();
    }

    /**
     * 签收单下推退货入库单-保存
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param list list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "签收单下推退货入库单")
    @PostMapping(value = "/receiveGenerateSoReturnInstockSave")
    public ApiResult receiveGenerateSoReturnInstockSave(@RequestBody ValidList<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> list) {
        Boolean flag = soReturnInstockService.receiveGenerateSoReturnInstockSave(list.getList());
        return flag == true ? success() : failure();
    }

    /**
     * 下推加工单显示
     * @author Will
     * @date: 2023/8/28 14:12
     * @param dto
     * @return ApiResult<List<viewGenerateMachineInfoDTO>>
     */
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,warehouse_keeper_id",
            menuCode = "wms:soReturnInstock:viewGenerateMachineInfo",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    @PostMapping(value = "/viewGenerateMachineInfo")
    public ApiResult<List<SoReturnInstockDTO.ViewGenerateMachineInfoDTO>> viewGenerateMachineInfo(@RequestBody @Validated  BaseIdsDTO.IdsDTO dto) {
        List<SoReturnInstockDTO.ViewGenerateMachineInfoDTO> resultList =  soReturnInstockService.viewGenerateMachineInfo(dto.getIds());
        return success(resultList) ;
    }

    /**
     * 下推加工单保存
     * @author Will
     * @date: 2023/8/28 15:26
     * @param list
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推加工单保存")
    @PostMapping(value = "/generateMachineInfo")
    public ApiResult generateMachineInfo(@RequestBody @Validated  ValidList<SoReturnInstockDTO.GenerateMachineInfoDTO> list) {
        Boolean flag = soReturnInstockService.generateMachineInfo(list);
        return flag == true ? success() : failure();
    }

    /**
     * b2c退货入库远程搜索
     */
    @PostMapping("/b2cPagingSelect")
    public ApiResult<PagingVO<SoReturnInstockDTO.SearchDTO>> b2cPagingSelect(@RequestBody @Valid PagingDTO<SoReturnInstockDTO.SelectDTO> searchDTO) {
        PagingVO<SoReturnInstockDTO.SearchDTO> list = soReturnInstockService.pagingSelect(searchDTO);
        return success(list);
    }
    
    /**
     * 下推物流自发货费用
     * @author Will
     * @date: 2023/8/28 15:26
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推物流单")
    @PostMapping(value = "/generateLogisticsBill")
    public ApiResult<List<BatchResultDTO>> generateLogisticsBill(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoReturnInstockEntity> entityList = soReturnInstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoReturnInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"退货入库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soReturnInstockService.generateLogisticsBill(entity));
            }catch (Exception e){
                log.error("退货入库单下推物流单失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }




    /**
     * 下载模板
     * @author will
     * @date 2025/4/24 19:47
     * @param response
     * @return ApiResult
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        soReturnInstockService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入
     * @author will
     * @date 2025/4/24 19:48
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入销售退货入库单")
    @PostMapping("/import")
    public ApiResult exportWarehouse(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = soReturnInstockService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下推退货入库单保存
     * @param dtos
     * @return
     */
    @PostMapping("/returnInstockSave")
    public ApiResult<List<BatchResultDTO>> returnInstockSave(@RequestBody @Valid ValidList<SoB2cReturnDTO.ReturnInstockDTO> dtos) {
        if (dtos.isEmpty()) {
            return success();
        }
        List<String> ids = dtos.stream().map(SoB2cReturnDTO.ReturnInstockDTO::getId).distinct().collect(Collectors.toList());
        List<SoB2cReturnEntity> returnEntityList = soB2cReturnFeign.listByIds(ids);
        List<SoB2cReturnDetailEntity> returnDetailEntityList = soB2cReturnFeign.listDetailByMainIds(ids);
        List<String> soIds = returnEntityList.stream().map(SoB2cReturnEntity::getSoId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(soIds);
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids){
            List<SoB2cReturnDTO.ReturnInstockDTO> returnInstockDTOS = dtos.stream().filter(e -> e.getId().equals(id)).collect(Collectors.toList());
            SoB2cReturnEntity soB2cReturnEntity = returnEntityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(soB2cReturnEntity)){
                resultDTOS.add(BatchResultDTO.fail(id, returnInstockDTOS.get(0).getCode(), "退货订单不存在"));
                continue;
            }
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> e.getId().equals(soB2cReturnEntity.getSoId())).findFirst().orElse(null);
            List<SoB2cReturnDetailEntity> detailEntityList = returnDetailEntityList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            List<SoB2cDetailEntity> b2cDetailEntityList = soB2cDetailEntityList.stream().filter(e -> e.getMainId().equals(soB2cReturnEntity.getSoId())).collect(Collectors.toList());
            try {
                resultDTOS.add(soReturnInstockService.returnInstockSave(soB2cReturnEntity,detailEntityList,returnInstockDTOS,soB2cEntity,b2cDetailEntityList));
            }catch (Exception e) {
                resultDTOS.add(BatchResultDTO.fail(id, soB2cReturnEntity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
