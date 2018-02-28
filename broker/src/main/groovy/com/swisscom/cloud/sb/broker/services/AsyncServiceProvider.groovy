package com.swisscom.cloud.sb.broker.services

import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointLookup
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointProvider
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceDeprovisioner
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cloud.sb.broker.provisioning.job.DeprovisioningJobConfig
import com.swisscom.cloud.sb.broker.provisioning.job.ProvisioningjobConfig
import com.swisscom.cloud.sb.broker.provisioning.job.ServiceDeprovisioningJob
import com.swisscom.cloud.sb.broker.provisioning.job.ServiceProvisioningJob
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.Utils
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
@Slf4j
abstract class AsyncServiceProvider<T extends AsyncServiceConfig> implements ServiceProvider, AsyncServiceProvisioner, AsyncServiceDeprovisioner, EndpointProvider {

    @Autowired
    protected AsyncProvisioningService asyncProvisioningService
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    @Autowired
    protected T serviceConfig
    @Autowired
    protected EndpointLookup endpointLookup

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        Utils.verifyAsychronousCapableClient(request)

        String lastOpId = asyncProvisioningService.scheduleProvision(
                new ProvisioningjobConfig(ServiceProvisioningJob.class, request,
                        serviceConfig.retryIntervalInSeconds,
                        serviceConfig.maxRetryDurationInMinutes))
        return new ProvisionResponse(isAsync: true, operation: lastOpId)
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        String lastOpId = asyncProvisioningService.scheduleDeprovision(new DeprovisioningJobConfig(ServiceDeprovisioningJob.class, request,
                serviceConfig.retryIntervalInSeconds, serviceConfig.maxRetryDurationInMinutes))
        return new DeprovisionResponse(isAsync: true, operation: lastOpId)
    }

    @Override
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance) {
        return endpointLookup.findEndpoints(serviceInstance, serviceConfig)
    }

}
