package antifraud.logic;

import antifraud.entity.User;
import antifraud.entity.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {

    public UserDTO userToUserDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getUsername(), user.getRole());
    }

}
