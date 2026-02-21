package com.tesla.teslabackend.lesson.repository;

import com.tesla.teslabackend.lesson.entity.Leccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeccionRepository extends JpaRepository<Leccion, Integer>{
}
