package com.mani.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mani.Entity.Users;
@Repository
public interface UserRepository extends JpaRepository<Users,Long>{

	public Users findByEmail(String email);
	Users findByEmailIgnoreCase(String email);

	 Users findByResetToken(String resetToken);
}
