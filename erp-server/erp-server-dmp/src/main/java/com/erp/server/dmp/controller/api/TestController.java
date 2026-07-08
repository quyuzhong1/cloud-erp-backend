//
//package com.erp.server.dmp.controller.api;
//
//import com.common.business.wrapper.FeignQuery;
//import com.common.core.controller.BaseController;
//import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
//import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
//import com.erp.model.oms.entity.SoB2cEntity;
//import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
//import com.erp.server.dmp.controller.feign.DmpFeignController;
//import com.erp.server.dmp.inout.job.DmpInputCreateJob;
//import com.erp.server.dmp.push.consumer.KingdeeSoReturnConsumer;
//import com.erp.server.dmp.service.BiSettlementExchangeRateService;
//import com.sdk.oms.shopify.api.rest.ShopifyRestClientService;
//import com.sdk.wangdian.server.WangDianClientService;
//import org.apache.skywalking.apm.toolkit.trace.TraceContext;
//import org.slf4j.MDC;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import java.util.concurrent.ExecutorService;
//
//
//@RestController
//@RequestMapping(value = "/test")
//public class TestController extends BaseController {
//
//    @Resource
//    private DmpInputCreateJob dmpInputCreateJob;
//
//    @Resource
//    private BiSettlementExchangeRateService biSettlementExchangeRateService;
//
//    @Resource
//    private KingdeeSoReturnConsumer kingdeeSoReturnConsumer;
//
//    @Resource
//    private WangDianClientService wangDianClientService;
//
//    @Resource
//    private DmpInoutTaskFeign dmpInoutTaskFeign;
//    @Resource
//    private DmpFeignController dmpFeignController;
//
//    @Autowired
//    @Qualifier("dmpInputExecutorPool")
//    protected ExecutorService dmpOutputExecutorPool;
//
//    @Resource(name = "pullErpOpenApi")
//    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
//
//    @Resource
//    private ShopifyRestClientService shopifyRestClientService;
//    @GetMapping("/test")
//    public void select(@RequestBody String param) throws Exception {
//        String traceId = TraceContext.traceId();
//        String segmentId = TraceContext.segmentId();
//        String g = MDC.get("traceId");
//        System.out.println(123);
////        SoB2cEntity a = FeignQuery.getById(SoB2cEntity.class,"1737772190898671618");
//        threadPoolTaskExecutor.execute(() -> {
//            String b = TraceContext.traceId();
//            String c = TraceContext.segmentId();
//            System.out.println(123);
//            threadPoolTaskExecutor.execute(() -> {
//                String s = TraceContext.traceId();
//                String t = TraceContext.segmentId();
//                System.out.println(123);
//            });
//        });
//    }
//}
