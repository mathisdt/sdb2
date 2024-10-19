package org.zephyrsoft.sdb2.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public class Endpoints {
    @GetMapping("/{name}")
    public String getGreeting(@PathVariable String name) {
        return "Hello " + name;
    }
}
