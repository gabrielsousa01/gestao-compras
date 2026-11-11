package br.com.gestaocompras.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Produto comprado, com a unidade de medida em que o estoque é controlado (kg, L, un). */
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String unidadeMedida;

    protected Item() {
    }

    public Item(String nome, String unidadeMedida) {
        this.nome = nome;
        this.unidadeMedida = unidadeMedida;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }
}
