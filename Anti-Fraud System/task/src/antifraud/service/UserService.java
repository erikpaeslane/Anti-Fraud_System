package antifraud.service;

import antifraud.entity.Role;
import antifraud.entity.User;
import antifraud.entity.UserStatus;
import antifraud.exception.user.InvalidNameException;
import antifraud.exception.user.InvalidPasswordException;
import antifraud.exception.user.InvalidUsernameException;
import antifraud.logic.UserDTOMapper;
import antifraud.model.*;
import antifraud.model.requests.RoleCredentials;
import antifraud.model.requests.UserCredentials;
import antifraud.model.requests.UserStatusCredentials;
import antifraud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDTOMapper userDTOMapper;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDTOMapper = userDTOMapper;
    }


    public ResponseEntity<?> registerUser(UserCredentials userCredentials) {
        boolean isUserWithGivenUsernameExists = userRepository.existsByUsername(userCredentials.username());
        if (isUserWithGivenUsernameExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User user = createUser(userCredentials);
        return new ResponseEntity<>(userDTOMapper.userToUserDTO(user), HttpStatus.CREATED);
    }


    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAllByOrderByIdAsc();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(users.stream().map(userDTOMapper::userToUserDTO));
    }


    public ResponseEntity<?> deleteUser(String username) {
        Optional<User> user = userRepository.findUserByUsername(username);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "username", username,
                    "status", "Deleted successfully!"
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    public ResponseEntity<?> updateRoleOfUser(RoleCredentials roleCredentials) {
        if (!isRoleValidForChanging(roleCredentials.role()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Optional<User> optionalUser = userRepository.findUserByUsername(roleCredentials.username());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = optionalUser.get();
        if (user.getRole() == Role.valueOf(roleCredentials.role()))
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        user.setRole(Role.valueOf(roleCredentials.role()));
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(userDTOMapper.userToUserDTO(user));
    }


    public ResponseEntity<?> updateStatusOfUser(UserStatusCredentials userStatusCredentials) {
        if (userStatusCredentials.operation() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Optional<User> optionalUser = userRepository.findUserByUsername(userStatusCredentials.username());
        if (optionalUser.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        User user = optionalUser.get();
        if (!isStatusChangeIsPossible(user, userStatusCredentials.operation()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        if (user.getUserStatus() == UserStatus.BLOCKED) {
            user.setUserStatus(UserStatus.UNBLOCKED);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("status",("User " + user.getUsername() + " unlocked!")));
        } else {
            user.setUserStatus(UserStatus.BLOCKED);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("status",("User " + user.getUsername() + " locked!")));
        }
    }


    private boolean isRoleValidForChanging(String roleString) {
        Role role;
        try {
            role = Role.valueOf(roleString);
            if (role != Role.MERCHANT &&
                    Role.valueOf(roleString) != Role.SUPPORT)
                return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }


    private boolean isStatusChangeIsPossible(User user, String statusChangeOperation) {

        if (user.getRole() == Role.ADMINISTRATOR)
            return false;
        else if (Objects.equals(statusChangeOperation, "LOCK"))
            return user.getUserStatus() != UserStatus.BLOCKED;
        else if (Objects.equals(statusChangeOperation, "UNLOCK"))
            return user.getUserStatus() != UserStatus.UNBLOCKED;
        return false;
    }


    private User createUser(UserCredentials userCredentials) {
        if (userCredentials.name() == null || userCredentials.name().isEmpty())
            throw new InvalidNameException("Name cannot be empty");
        if (userCredentials.username() == null || userCredentials.username().isEmpty())
            throw new InvalidUsernameException("Username cannot be empty");
        if (userCredentials.password() == null || userCredentials.password().isEmpty())
            throw new InvalidPasswordException("Password cannot be empty");

        User user = new User();
        user.setName(userCredentials.name());
        user.setUsername(userCredentials.username());
        user.setPassword(passwordEncoder.encode(userCredentials.password()));
        if (userRepository.count() == 0){
            user.setRole(Role.ADMINISTRATOR);
            user.setUserStatus(UserStatus.UNBLOCKED);
        } else {
            user.setRole(Role.MERCHANT);
            user.setUserStatus(UserStatus.BLOCKED);
        }
        userRepository.save(user);
        return user;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository
                .findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserAdapter(user);
    }
}
