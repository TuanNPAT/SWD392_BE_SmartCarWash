package com.swd392.smartcarwash.config;

import com.swd392.smartcarwash.annotation.PublicEndpoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PublicEndpointHandlerMapping {

    private final List<String> publicEndpoints = new ArrayList<>();

    public PublicEndpointHandlerMapping(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping
    ) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();

            boolean isPublic =
                    handlerMethod.hasMethodAnnotation(PublicEndpoint.class)
                            || handlerMethod.getBeanType().isAnnotationPresent(PublicEndpoint.class);

            if (!isPublic) {
                continue;
            }

            RequestMappingInfo mappingInfo = entry.getKey();

            if (mappingInfo.getPathPatternsCondition() != null) {
                mappingInfo.getPathPatternsCondition()
                        .getPatterns()
                        .forEach(pattern -> publicEndpoints.add(pattern.getPatternString()));
            }

            if (mappingInfo.getPatternsCondition() != null) {
                publicEndpoints.addAll(mappingInfo.getPatternsCondition().getPatterns());
            }
        }
    }

    public List<String> getPublicEndpoints() {
        return new ArrayList<>(publicEndpoints);
    }
}