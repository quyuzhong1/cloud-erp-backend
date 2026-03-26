package com.erp.server.tms.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 中转报关产品
 *
 * @author zdy
 * @since 2024-01-27
 */
@Slf4j
@RestController
@LogSystemModule("中转报关产品")
@RequestMapping("/transferDeclareProduct")
public class TransferDeclareProductController extends BaseController {
}
