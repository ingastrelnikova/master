package example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;
import java.util.List;

@Service
public class PrometheusClient {

    private final RestTemplate restTemplate;
    private final String prometheusBaseUrl;

    @Autowired
    public PrometheusClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.prometheusBaseUrl = "http://prometheus:9090";
    }

    public double queryMetric(String query) throws InterruptedException {
        String url = UriComponentsBuilder.fromHttpUrl(prometheusBaseUrl)
                .path("/api/v1/query")
                .queryParam("query", query)
                .toUriString();

        int attempt = 0;
        int maxRetries = 5;
        while (attempt<maxRetries) {
            try {
                Map<String,Object> response = restTemplate.getForObject(url, Map.class);
                if (response !=null &&"success".equals(response.get("status"))) {
                    Map<String,Object> data = (Map<String, Object>)response.get("data");
                    if (data != null &&data.get("result") != null) {
                        List<Object> results = (List<Object>) data.get("result");
                        if (!results.isEmpty()) {
                            Map<String, Object> result = (Map<String, Object>) results.get(0);
                            if (result != null &&result.get("value") != null) {
                                List<Object> value = (List<Object>) result.get("value");
                                return Double.parseDouble((String) value.get(1));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error:"+e.getMessage());
            }

            Thread.sleep(5000);
            attempt++;
        }

        throw new RuntimeException("No results");
    }
}
