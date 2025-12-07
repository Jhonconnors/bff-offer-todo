package com.example.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "security")
public class IpSecurityProperties {

    // Spring buscará automáticamente "security.allowed-ips" en tu yaml
    // y guardará la lista de IPs aquí adentro.
    private List<String> allowedIps;

    public List<String> getAllowedIps() {
        return allowedIps;
    }

    public void setAllowedIps(List<String> allowedIps) {
        this.allowedIps = allowedIps;
    }
}