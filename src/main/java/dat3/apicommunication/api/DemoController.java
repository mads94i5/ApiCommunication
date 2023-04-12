package dat3.apicommunication.api;

import dat3.apicommunication.entity.NameInfo;
import dat3.apicommunication.service.NameInfoService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final int SLEEP_TIME = 1000*3;

    private NameInfoService nameInfoService;

    public DemoController(NameInfoService nameInfoService) {
        this.nameInfoService = nameInfoService;
    }
    @GetMapping(value = "/random-string-slow")
    public String slowEndpoint() throws InterruptedException {
        Thread.sleep(SLEEP_TIME);
        return RandomStringUtils.randomAlphanumeric(10);
    }

    @GetMapping(value = "/name-info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NameInfo  nameInfo(@RequestParam String name) {
        return nameInfoService.getNameInfo(name);
    }
}
