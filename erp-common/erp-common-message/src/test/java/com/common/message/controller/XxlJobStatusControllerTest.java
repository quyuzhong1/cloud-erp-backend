package com.common.message.controller;

import com.common.core.controller.vo.ApiResult;
import com.common.message.config.ReleaseControlledXxlJobSpringExecutor;
import com.common.message.controller.vo.XxlJobLifecycleStatusVO;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

public class XxlJobStatusControllerTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAcceptedWhileDrainIsRunning() {
        ObjectProvider<XxlJobSpringExecutor> provider = Mockito.mock(ObjectProvider.class);
        ReleaseControlledXxlJobSpringExecutor executor =
                Mockito.mock(ReleaseControlledXxlJobSpringExecutor.class);
        Mockito.when(provider.getIfAvailable()).thenReturn(executor);
        Mockito.when(executor.getDrainStateValue())
                .thenReturn(ReleaseControlledXxlJobSpringExecutor.DrainState.DRAINING);
        XxlJobStatusController controller = new XxlJobStatusController(provider);
        MockHttpServletRequest request = loopbackRequest();

        ResponseEntity<ApiResult<XxlJobLifecycleStatusVO>> response = controller.drain(request);

        Assert.assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertEquals("DRAINING", response.getBody().getData().getStatus());
        Mockito.verify(executor).beginDrain();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotExposeDrainExceptionMessage() {
        ObjectProvider<XxlJobSpringExecutor> provider = Mockito.mock(ObjectProvider.class);
        ReleaseControlledXxlJobSpringExecutor executor =
                Mockito.mock(ReleaseControlledXxlJobSpringExecutor.class);
        Mockito.when(provider.getIfAvailable()).thenReturn(executor);
        Mockito.doThrow(new IllegalStateException("embedServer private field detail"))
                .when(executor).beginDrain();
        XxlJobStatusController controller = new XxlJobStatusController(provider);
        MockHttpServletRequest request = loopbackRequest();

        ResponseEntity<ApiResult<XxlJobLifecycleStatusVO>> response = controller.drain(request);

        Assert.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assert.assertEquals("XXL-JOB terminal drain failed", response.getBody().getMsg());
        Assert.assertEquals("XXL-JOB terminal drain failed", response.getBody().getData().getMessage());
        Assert.assertFalse(response.getBody().getMsg().contains("embedServer private field detail"));
    }

    /**
     * Creates a loopback request accepted by lifecycle endpoints.
     *
     * @return loopback request
     */
    private MockHttpServletRequest loopbackRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
