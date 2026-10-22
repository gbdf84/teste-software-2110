package com.gilberto.testesoftware2110.entity;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "TB_USUARIOS")
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dataNascimento;

    @Column(nullable = false)
    private String senha;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Date getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(Date dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
