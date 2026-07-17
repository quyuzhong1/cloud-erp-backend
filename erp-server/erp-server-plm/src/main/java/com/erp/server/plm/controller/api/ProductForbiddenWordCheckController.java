package com.erp.server.plm.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.server.plm.query.ProductForbiddenWordCheckQueryHandler;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.ProductForbiddenWordCheckDTO;
import com.erp.model.plm.entity.ProductForbiddenWordCheckEntity;
import com.erp.server.plm.service.ProductForbiddenWordCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品检测
 */
@Slf4j
@RestController
@LogSystemModule("产品检测")
@RequestMapping("/productForbiddenWordCheck")
public class ProductForbiddenWordCheckController extends BaseController {

    @Resource
    private ProductForbiddenWordCheckService productForbiddenWordCheckService;

    /**
     * 列表查询
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = ProductForbiddenWordCheckQueryHandler.class)
    public ApiResult<PagingVO<ProductForbiddenWordCheckDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ProductForbiddenWordCheckDTO.PagingParamDTO> dto) {
        return success(productForbiddenWordCheckService.paging(dto));
    }

    /**
     * 创建产品检测任务
     */
    @PostMapping("/detect")
    @LogAction(value = LogActionEnum.INSERT, desc = "创建产品检测任务")
    public ApiResult<ProductForbiddenWordCheckDTO.DetectDTO> detect() {
        return success(productForbiddenWordCheckService.detect());
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "产品检测删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ProductForbiddenWordCheckEntity> list = productForbiddenWordCheckService.lambdaQuery().in(ProductForbiddenWordCheckEntity::getId, ids).list();
        Map<String, ProductForbiddenWordCheckEntity> idEntityMap = list.stream().collect(Collectors.toMap(ProductForbiddenWordCheckEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = productForbiddenWordCheckService.delete(id);
            } catch (Exception e) {
                log.error("产品检测删除失败", e);
                ProductForbiddenWordCheckEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "产品检测记录不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getReportName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载报告
     */
    @GetMapping("/downloadReport")
    @LogAction(value = LogActionEnum.DOWNLOAD, desc = "产品检测下载报告")
    public ApiResult<String> downloadReport(@RequestParam("id") String id) {
        return success(productForbiddenWordCheckService.downloadReport(id));
    }
}
