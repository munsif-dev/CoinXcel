package com.munsif.CoinXcel.Repository;

import com.munsif.CoinXcel.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends  JpaRepository<User, Long> {

    User findByEmail(String email);
}
