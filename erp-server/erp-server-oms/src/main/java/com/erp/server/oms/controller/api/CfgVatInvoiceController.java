package com.erp.server.oms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CfgVatInvoiceDTO;
import com.erp.model.oms.entity.CfgVatInvoiceEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.oms.service.CfgVatInvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * VAT发票设置
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@RestController
@LogSystemModule("VAT发票设置")
@RequestMapping("/cfgVatInvoice")
public class CfgVatInvoiceController extends BaseController {

    @Resource
    private CfgVatInvoiceService cfgVatInvoiceService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "VAT发票设置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgVatInvoiceDTO.AddDTO dto) {
        return success(cfgVatInvoiceService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-03-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "VAT发票设置修改")
    public ApiResult<?> update(@RequestBody @Validated CfgVatInvoiceDTO.UpdateDTO dto) {
        cfgVatInvoiceService.update(dto);
        return success();
    }

    /**
     * 批量修改
     * @author zdy
     * @date:  2025-03-07
     * @param dtoList
     * @return ApiResult
     */
    @PostMapping("/batchUpdate")
    @LogAction(value = LogActionEnum.UPDATE, desc = "VAT发票设置批量修改")
    public ApiResult<List<BatchResultDTO>> batchUpdate(@RequestBody @Validated List<CfgVatInvoiceDTO.UpdateDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (CfgVatInvoiceDTO.UpdateDTO dto : dtoList){
            try {
                Boolean update = cfgVatInvoiceService.update(dto);
                if (update){
                    resultDTOS.add(BatchResultDTO.success(dto.getId(),dto.getCountryId()));
                }else {
                    resultDTOS.add(BatchResultDTO.fail(dto.getId(),dto.getCountryId(),"修改失败"));
                }
            }catch (Exception e){
                resultDTOS.add(BatchResultDTO.fail(dto.getId(),dto.getCountryId(),e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * VAT配置分页
     *
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgVatInvoiceDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgVatInvoiceDTO.PagingParamDTO> dto) {
        PagingVO<CfgVatInvoiceDTO.PagingViewDTO> pagingVO = cfgVatInvoiceService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 批量启用/禁用
     * @param updateDTO
     * @return
     */
    @PostMapping("/updateState")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量启用/禁用 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult<List<BatchResultDTO>> updateState(@RequestBody @Validated CfgVatInvoiceDTO.InvoiceBatchUpdateDTO updateDTO) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<String> ids = updateDTO.getIds().stream().distinct().collect(Collectors.toList());
        List<CfgVatInvoiceEntity> entityList = cfgVatInvoiceService.listByIds(ids);
        List<String> shopIds = entityList.stream().map(CfgVatInvoiceEntity::getShopId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = FeignQuery.getByIds(ShopInfoEntity.class, shopIds);
        for (String id : ids){
            CfgVatInvoiceEntity entity = entityList.stream().filter(e -> id.equals(e.getId())).findFirst().orElse(null);
            if (Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"发票配置不存在"));
                continue;
            }
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(e -> e.getId().equals(entity.getShopId())).findFirst().orElse(null);
            if (Objects.isNull(shopInfoEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"店铺信息不存在"));
                continue;
            }
            if (updateDTO.getDisabled().equals(entity.getDisabled())){
                resultDTOS.add(BatchResultDTO.fail(id,shopInfoEntity.getName(),"状态未发生变化"));
                continue;
            }
            try {
                cfgVatInvoiceService.updateState(entity, updateDTO.getDisabled());
                resultDTOS.add(BatchResultDTO.success(id,shopInfoEntity.getName()));
            }catch (Exception e){
                resultDTOS.add(BatchResultDTO.fail(id,shopInfoEntity.getName(),e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 删除
     * @return
     */
    @PostMapping("/delete")
    public ApiResult<Boolean> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO idsDTO) {
        return success(cfgVatInvoiceService.delete(idsDTO.getIds()));
    }
}
