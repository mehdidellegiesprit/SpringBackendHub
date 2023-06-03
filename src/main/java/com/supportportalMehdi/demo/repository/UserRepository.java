package com.supportportalMehdi.demo.repository;

import com.supportportalMehdi.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    User findUserByUsername(String username) ;
    User findUserByEmail(String email) ;
}
