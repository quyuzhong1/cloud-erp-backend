package com.sdk.third.tf.config;

import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.client.CompanyApiClient;
import com.sdk.third.tf.client.InvoiceApiClient;
import com.sdk.third.tf.client.TaxCategoryApiClient;
import com.sdk.third.tf.client.TfApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TF Fiscal SDK 自动配置类
 * 用于注册 SDK 中的 Bean
 *
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
@Configuration
public class TfFiscalAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TfApiClient tfApiClient() {
        log.debug("Creating TfApiClient bean");
        return new TfApiClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaxCategoryApiClient taxCategoryApiClient(TfApiClient tfApiClient) {
        log.debug("Creating TaxCategoryApiClient bean");
        return new TaxCategoryApiClient(tfApiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompanyApiClient companyApiClient(TfApiClient tfApiClient) {
        log.debug("Creating CompanyApiClient bean");
        return new CompanyApiClient(tfApiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public InvoiceApiClient invoiceApiClient(TfApiClient tfApiClient) {
        log.debug("Creating InvoiceApiClient bean");
        return new InvoiceApiClient(tfApiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public TfFiscalService tfFiscalService(
            TaxCategoryApiClient taxCategoryApiClient,
            CompanyApiClient companyApiClient,
            InvoiceApiClient invoiceApiClient) {
        log.debug("Creating TfFiscalService bean");
        return new TfFiscalService(taxCategoryApiClient, companyApiClient, invoiceApiClient);
    }
}
