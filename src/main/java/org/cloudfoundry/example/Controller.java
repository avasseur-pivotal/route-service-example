/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import java.net.URI;

//2016-09-01T16:56:03.17+0200 [APP/0]      OUT INFO  Incoming Request: GET http://route-service-example-unsmothered-felicitousness.cfapps.semea.piv/,{host=[route-service-example-unsmothered-felicitousness.cfapps.semea.piv], user-agent=[Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/601.2.7 (KHTML, like Gecko) Version/9.0.1 Safari/601.2.7], accept=[*/*], accept-encoding=[gzip, deflate], accept-language=[en-us], cache-control=[max-age=0], referer=[http://scale.cfapps.semea.piv/], x-cf-applicationid=[0246c616-2c93-44ef-9679-f1dd988267f4], x-cf-forwarded-url=[https://scale.cfapps.semea.piv/js/bootstrap.min.js], x-cf-instanceid=[bcce44ae-ea15-482b-7992-82285386c18c], x-cf-proxy-metadata=[eyJub25jZSI6Ik93NlF4dVVwY0V4ODJYNnAifQ==], x-cf-proxy-signature=[zJRCZh-S0Tzhmw1z3VizUetIm26m6kxerA6jRcMd5NuFShNYSEtmdGAEQOehlBs5UlXjtTa_9N8njQ-LrmrCD11LfsLmGWI6iGWJLoYwDjir01Yr5R26cKNcsVCTc-hqmkpFmZWCLHd6D-dd8UCZWyVjWMq_ZqjcX_teSZbzfEIzVKkI8J1hzA==], connection=[close], x-forwarded-proto=[http], x-request-start=[1472741763126], x-vcap-request-id=[e1134263-ad80-42de-6759-7fcc09b296e9]}
//2016-09-01T16:56:03.17+0200 [APP/0]      OUT INFO  Outgoing Request: GET https://scale.cfapps.semea.piv/css/bootstrap.min.css,{host=[route-service-example-unsmothered-felicitousness.cfapps.semea.piv], user-agent=[Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/601.2.7 (KHTML, like Gecko) Version/9.0.1 Safari/601.2.7], accept=[text/css,*/*;q=0.1], accept-encoding=[gzip, deflate], accept-language=[en-us], cache-control=[max-age=0], if-modified-since=[Sat, 04 Jun 2016 13:11:58 GMT], referer=[http://scale.cfapps.semea.piv/], x-cf-applicationid=[0246c616-2c93-44ef-9679-f1dd988267f4], x-cf-instanceid=[bcce44ae-ea15-482b-7992-82285386c18c], x-cf-proxy-metadata=[eyJub25jZSI6IjFMQzV2QzZoMXhWVndZL0gifQ==], x-cf-proxy-signature=[lf-jsnTZKftBc5OcfWB4oiLovjKOLiKZy878rZ0P1t5c0gkn5Ek4LtH2L2aG8ayokffjn6sPgqkb_YtrSAcG0C-fPOJ2s3N2sVqNwSkIgOLRPL3nrVATPZGBPj83l-sgrIUu4zriPOVgeB_QBx3W8JI_jnc6hGF_AFfclqFV6Auk8NPsUWwXGSs=], connection=[close], x-forwarded-proto=[http], x-request-start=[1472741763125], x-vcap-request-id=[ba7fc163-a7a8-429d-560f-a3e9e38f87f4]}
@RestController
final class Controller {

    static final String FORWARDED_URL = "X-CF-Forwarded-Url";

    static final String PROXY_METADATA = "X-CF-Proxy-Metadata";

    static final String PROXY_SIGNATURE = "X-CF-Proxy-Signature";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RestOperations restOperations;

    @Autowired
    Controller(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @RequestMapping(headers = {FORWARDED_URL, PROXY_METADATA, PROXY_SIGNATURE})
    ResponseEntity<?> service(RequestEntity<byte[]> incoming) {
        this.logger.info("Incoming Request: {}", incoming);

        RequestEntity<?> outgoing = getOutgoingRequest(incoming);
        this.logger.info("Outgoing Request: {}", outgoing);

        return this.restOperations.exchange(outgoing, byte[].class);
    }

    private static RequestEntity<?> getOutgoingRequest(RequestEntity<?> incoming) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(incoming.getHeaders());

        // that code below takes the route service injected X-CF-Forwarded-Url to build the new request        
        URI uri = headers.remove(FORWARDED_URL).stream()
            .findFirst()
            .map(URI::create)
            .orElseThrow(() -> new IllegalStateException(String.format("No %s header present", FORWARDED_URL)));

        return new RequestEntity<>(incoming.getBody(), headers, incoming.getMethod(), uri);
    }

}
