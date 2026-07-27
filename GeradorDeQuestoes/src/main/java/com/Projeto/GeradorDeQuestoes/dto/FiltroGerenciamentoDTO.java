package com.Projeto.GeradorDeQuestoes.dto;

import com.Projeto.GeradorDeQuestoes.enums.FiltroGerenciamento;

public class FiltroGerenciamentoDTO {
     
    private FiltroGerenciamento filtro;
    

    public FiltroGerenciamentoDTO(FiltroGerenciamento filtro) {
        this.filtro = filtro;
    }


    public FiltroGerenciamento getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroGerenciamento filtro) {
        this.filtro = filtro;
    }

    
}
