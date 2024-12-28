package antifraud.controller;

import antifraud.model.requests.UserStatusCredentials;
import antifraud.model.requests.RoleCredentials;
import antifraud.model.requests.UserCredentials;
import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/user")
    public ResponseEntity<?> user(@RequestBody UserCredentials userCredentials) {
        return userService.registerUser(userCredentials);
    }

    @GetMapping("/api/auth/list")
    public ResponseEntity<?> listUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/api/auth/user/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        return userService.deleteUser(username);
    }

    @PutMapping("/api/auth/role")
    public ResponseEntity<?> updateRole(@RequestBody RoleCredentials roleCredentials) {
        return userService.updateRoleOfUser(roleCredentials);
    }

    @PutMapping("/api/auth/access")
    public ResponseEntity<?> updateAccess(@RequestBody UserStatusCredentials userStatusCredentials) {
        return userService.updateStatusOfUser(userStatusCredentials);
    }

}
