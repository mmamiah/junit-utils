package lu.mms.common.controller;

import lu.mms.common.service.MyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MyController {

    private final MyService service;

    public MyController(final MyService service) {
        this.service = service;
    }

    @GetMapping("/")
    public @ResponseBody ResponseEntity<?> appRoot(@RequestParam(value = "password") String password) {
        final String message = String.format(
                "%s : %s",
                service.getDescription(),
                "If you can read this, then it's all good, the app is running"
        );

        if (StringUtils.defaultString(password).isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid password provided",
                    "details", String.format("password = [%s]", StringUtils.isBlank(password) ? "x" : password)
            ));
        }

        return ResponseEntity.ok(Map.of(
                "appName", "JUnit Utils Sample Rest App (Spring)",
                "message", message,
                "password", password
        ));
    }
}
