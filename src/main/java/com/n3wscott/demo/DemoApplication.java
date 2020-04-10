package com.n3wscott.demo;

import io.cloudevents.CloudEvent;
import io.cloudevents.format.Wire;
import io.cloudevents.v1.AttributesImpl;
import io.cloudevents.v1.http.Marshallers;
import io.cloudevents.v1.http.Unmarshallers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.cloudevents.v1.CloudEventBuilder;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
@RestController
public class DemoApplication {

    private CloudEvent<AttributesImpl, DemoData> lastCE;

    @GetMapping("/")
    public ResponseEntity<String> index() {
        var ce = this.lastCE;

        if (ce == null) {
            DemoData data = new DemoData();
            data.setData("hello, demo");
            
            // Build a CloudEvent instance
            ce = CloudEventBuilder.<DemoData>builder()
                    .withType("com.github.pull.create")
                    .withSource(URI.create("https://github.com/cloudevents/spec/pull"))
                    .withId("A234-1234-1234")
                    .withDataschema(URI.create("http://my.br"))
                    .withTime(ZonedDateTime.now())
                    .withDataContentType("text/plain")
                    .withData(data)
                    .build();
        } else {
            System.out.println(ce.getData().toString());
        }

        CloudEvent<AttributesImpl, DemoData> finalCe = ce;
        Wire<String, String, String> wire =
                Marshallers.<DemoData>
                        binary() //structured()
                        .withEvent(() -> finalCe)
                        .marshal();

        HttpHeaders responseHeaders = new HttpHeaders();
        wire.getHeaders().forEach(responseHeaders::set);

        if (wire.getPayload().isPresent()) {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(wire.getPayload().get());
        } else {
            return ResponseEntity.ok()
                    .headers(responseHeaders).build();
        }
    }

    @PostMapping("/")
    public String greetingSubmit(@RequestHeader HttpHeaders headers, @RequestBody String body) {
        Map<String, Object> httpHeaders = new HashMap<>();
        headers.forEach((k, v) -> httpHeaders.put(k, v.get(0)));

        System.out.printf("got raw data: %s \n\n data:\n%s", headers.toString(), body);

        this.lastCE = Unmarshallers.binary(DemoData.class)
                .withHeaders(() -> httpHeaders)
                .withPayload(() -> body)
                .unmarshal();

        System.out.println("Got event:");
        System.out.println(this.lastCE.getAttributes().toString());
        System.out.println(this.lastCE.getExtensions().toString());
        System.out.println(this.lastCE.getData().toString());

        return "";
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
