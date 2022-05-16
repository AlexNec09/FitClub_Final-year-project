package com.project.fitclub.shared;

import com.project.fitclub.dao.RoleRepository;
import com.project.fitclub.model.Role;
import com.project.fitclub.model.RoleName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleLoader implements ApplicationRunner {

    private RoleRepository roleRepository;

    @Autowired
    public RoleLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void run(ApplicationArguments args) {
        if(roleRepository.findByName(RoleName.ROLE_ADMIN) == null){
            roleRepository.save(new Role(RoleName.ROLE_ADMIN));
            roleRepository.save(new Role(RoleName.ROLE_USER));
        }
    }
}