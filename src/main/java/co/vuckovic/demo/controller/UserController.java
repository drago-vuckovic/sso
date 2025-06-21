package co.vuckovic.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/users")
    public String getUsers() {
        return "User list";
    }

    @GetMapping("/edit")
    public String editResource() {
        return "Edit access";
    }

    @GetMapping("/admin")
    public String adminResource() {
        return "Admin access";
    }
}