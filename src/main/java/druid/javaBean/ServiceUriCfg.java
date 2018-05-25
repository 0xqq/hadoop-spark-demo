package druid.javaBean;

import java.io.Serializable;

public class ServiceUriCfg implements Serializable {
    private String service_code;
    private String uri;
    private int timeout_time;

    public ServiceUriCfg(String service_code, String uri, int timeout_time) {
        this.service_code = service_code;
        this.uri = uri;
        this.timeout_time = timeout_time;
    }

    public String getService_code() {
        return service_code;
    }

    public void setService_code(String service_code) {
        this.service_code = service_code;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getTimeout_time() {
        return timeout_time;
    }

    public void setTimeout_time(int timeout_time) {
        this.timeout_time = timeout_time;
    }

    @Override
    public String toString() {
        return "ServiceUriCfg{" +
                "service_code='" + service_code + '\'' +
                ", uri='" + uri + '\'' +
                ", timeout_time=" + timeout_time +
                '}';
    }
}
