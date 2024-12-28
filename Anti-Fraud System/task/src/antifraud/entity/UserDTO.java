package antifraud.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private final int id;
    private String name;
    private String username;
    private Role role;

    public UserDTO(int id, String name, String username, Role role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
    }
}
