package com.swisscom.cloud.sb.broker.services.bosh

import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class BoshTemplateFactory {
    BoshTemplate build(String template) {
        return new BoshTemplate(template)
    }
}
