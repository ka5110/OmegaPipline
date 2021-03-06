package com.project.omega.service;


import com.project.omega.bean.dao.auth.AdminRoles;
import com.project.omega.bean.dao.auth.Role;
import com.project.omega.bean.dao.entity.User;
import com.project.omega.bean.dto.UserDTO;
import com.project.omega.exceptions.DuplicateUserException;
import com.project.omega.helper.Constant;
import com.project.omega.helper.RoleBasedConstant;
import com.project.omega.repository.UserRepository;
import com.project.omega.service.interfaces.AdminRoleService;
import com.project.omega.service.interfaces.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    /*
     * ALEX - You can Call a @Service within a @Service - The Intent is to create a Cyclic dependency.
     * */
    @Autowired
    private AdminRoleService adminRoleService;

    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                new ArrayList<>());
    }

    /*
     * This method is a Service Layer Method which is @Autowired within JWTAuthenticationController. The createUser(UserDTO user), accepts
     * the parameters passed in through the UserDTO object, the UserDTO object has 3 parameter - email, password and Collection<Roles>. We
     * extract the values and if there are no roles passed in the UserDTO Object a default role - DEFAULT_USER_ROLE is added and then the User
     * is saved in the USER TABLE.
     *
     * NOTE : The DEFAULT_USER_ROLE will already be available in the AdminRoles table, if not then create it. This way, the service method will always
     * return a ROLE Object.
     *
     * NOTE: The roles and assigned privileges corresponding to AdminRoles would be created by the respective Admins of the business.
     * */

    public User createUser(UserDTO user) throws DuplicateUserException {
        LOGGER.debug("Creating user account with information: {}", user);
        String email = user.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(Constant.ERROR_USER_EXISTS + email);
        }
        Collection<Role> assignedRoles = user.getRoles();
        Role role = new Role();
        if (assignedRoles.isEmpty()) {
            AdminRoles adminRoles = adminRoleService.findByName(RoleBasedConstant.DEFAULT_USER_ROLE);
            role.setName(adminRoles.getName());
            role.setPrivileges(adminRoles.getPrivileges());
            assignedRoles.add(role);
        }
        User registeredUser = bindUser(user, assignedRoles);
        addUserRole(role, registeredUser);
        return registeredUser;
    }

    public User bindUser(UserDTO users, Collection<Role> assignedRoles) {
        User u = new User.UserBuilder()
                .setEmail(users.getEmail())
                .setPassword(bcryptEncoder.encode(users.getPassword()))
                .setRoles(assignedRoles)
                .build();
        return userRepository.save(u);
    }

    public void addUserRole(Role role, User u) {
        Collection<User> userCollection = new ArrayList<>();
        userCollection.add(u);
        role.setUsers(userCollection);
        roleService.save(role);
    }
}