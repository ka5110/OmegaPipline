//package com.project.omega.authentication;
//
//import com.project.omega.bean.dao.auth.Privilege;
//import com.project.omega.bean.dao.auth.Role;
//import com.project.omega.bean.dao.entity.User;
//import com.project.omega.repository.PrivilegeRepository;
//import com.project.omega.repository.RoleRepository;
//import com.project.omega.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//
//@Component
//public class SetupDataLoaderForRoleAndPrivilege implements ApplicationListener<ContextRefreshedEvent>
//    {
//        private boolean alreadySetup = false;
//
//        @Autowired
//        private UserRepository userRepository;
//
//        @Autowired
//        private RoleRepository roleRepository;
//
//        @Autowired
//        private PrivilegeRepository privilegeRepository;
//
//        @Autowired
//        private PasswordEncoder passwordEncoder;
//
//        // API
//
//        @Override
//        @Transactional
//        public void onApplicationEvent ( final ContextRefreshedEvent event){
//        if (alreadySetup) {
//            return;
//        }
//
//        // == create initial privileges
//        final Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
//        final Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
//        final Privilege passwordPrivilege = createPrivilegeIfNotFound("CHANGE_PASSWORD_PRIVILEGE");
//
//        // == create initial roles
//        final List<Privilege> adminPrivileges = new ArrayList<Privilege>(Arrays.asList(readPrivilege, writePrivilege, passwordPrivilege));
//        final List<Privilege> userPrivileges = new ArrayList<Privilege>(Arrays.asList(readPrivilege, passwordPrivilege));
//        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
//        createRoleIfNotFound("ROLE_USER", userPrivileges);
//
//        // == create initial user
//        createUserIfNotFound("test@test.com", "Test", "Test", "test", new ArrayList<Role>(Arrays.asList(adminRole)));
//
//        alreadySetup = true;
//    }
//        @Transactional
//        private final Privilege createPrivilegeIfNotFound ( final String name){
//        Privilege privilege = privilegeRepository.findByName(name);
//        if (privilege == null) {
//            privilege = new Privilege(name);
//            privilege = privilegeRepository.save(privilege);
//        }
//        return privilege;
//    }
//
//        @Transactional
//        private final Role createRoleIfNotFound ( final String name, final Collection<Privilege> privileges){
//        IRoles role = roleRepository.findByName(name);
//        if (role == null) {
//            role = new IRoles(name);
//        }
//        role.setPrivileges(privileges);
//        role = roleRepository.save(role);
//        return role;
//    }
//
//        @Transactional
//        private final User createUserIfNotFound ( final String email, final String firstName, final String lastName,
//        final String password, final Collection<Role> roles){
//        User user = userRepository.findByEmail(email);
//        if (user == null) {
//            user = new User();
//            user.setPassword(passwordEncoder.encode(password));
//        }
//        user.setRoles(roles);
//        user = userRepository.save(user);
//        return user;
//    }
//    }
