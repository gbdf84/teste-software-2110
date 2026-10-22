package com.gilberto.testesoftware2110.reporitory;

import com.gilberto.testesoftware2110.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuariosRepository extends JpaRepository<Usuarios, UUID> {
    boolean existsByCpf(String cpf);
    boolean existsByCpfAndIdNot(String cpf, UUID id);
    Optional<Usuarios> findByCpf(String cpf);

}
