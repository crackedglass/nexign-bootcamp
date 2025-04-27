package ru.crackedglass.cdr_generator.impl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.crackedglass.cdr_generator.impl.entity.Call;

@Repository
public interface CallRepository extends JpaRepository<Long, Call> {
} 
