import { Routes } from '@angular/router';
import { Dashboard } from './components/dashboard/dashboard';
import { ProvasSalvas } from './pages/provas-salvas/provas-salvas';
import { DetalheProva } from './pages/detalhe-prova/detalhe-prova';
import { Alimentacao } from './pages/alimentacao/alimentacao';
import { GeradorManual } from './pages/gerador-prova/gerador-manual/gerador-manual';
import { GeradorAutomatico } from './pages/gerador-prova/gerador-automatico/gerador-automatico';
import { GeradorProva } from './pages/gerador-prova/gerador-prova';
import { GerenciarBanco } from './pages/banco-dashboard/gerenciar-banco/gerenciar-banco';
import { BancoDashboard } from './pages/banco-dashboard/banco-dashboard';
import { BancoQuestoes } from './pages/banco-dashboard/banco-questoes/banco-questoes';
import { SelecionarQuestao } from './pages/banco-dashboard/selecionar-questao/selecionar-questao';
import { Gerenciamento } from './pages/gerenciamento/gerenciamento';
import { ProvaBuilder } from './pages/prova-builder/prova-builder';
import { CadastroDocumento } from './pages/gerenciamento/cadastro-documento/cadastro-documento';
import { DetalhePrompt } from './pages/gerenciamento/detalhe-prompt/detalhe-prompt';
import { Revisao } from './pages/alimentacao/revisao/revisao';
import { HomeDisciplinas } from './pages/home-disciplinas/home-disciplinas';
import { DisciplinaGuard } from './guards/disciplina/disciplina-guard';
import { GeracaoRapida } from './pages/gerador-prova/geracao-rapida/geracao-rapida';
import { MenuGeracao } from './pages/gerador-prova/menu-geracao/menu-geracao';
import { Login } from './pages/login/login';
import { AuthGuard } from './guards/auth/auth-guard';
import { QuestoesDocumento } from './pages/gerenciamento/questoes-documento/questoes-documento';
import { EditarTaxonomia } from './pages/banco-dashboard/gerenciar-banco/editar-taxonomia/editar-taxonomia';
import { Processamentos } from './pages/processamentos/processamentos';

export const routes: Routes = [
  {
    path: 'login',
    component: Login,
    title: 'Login - Elaborar'
  },
  {
    path: '', 
    component: HomeDisciplinas,
    title: 'Minhas Disciplinas - Elaborar',
    canActivate: [AuthGuard],
    data: { breadcrumb: 'Início' }
  },
  {
    path: 'painel/:idDisciplina', 
    component: Dashboard,
    title: 'Painel da Disciplina',
    canActivate: [AuthGuard],
    data: { breadcrumb: 'Painel da Disciplina' }
  },
  {
    path: 'gerar-prova', 
    component: GeradorProva,
    title: 'Gerador de Prova',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Gerador de Prova' }
  },
  {
    path: 'gerar-prova/automatico', 
    component: GeradorAutomatico,
    title: 'Gerar Nova Prova',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Automático' }
  },
  {
    path: 'gerar-prova/rapida', 
    component: GeracaoRapida,
    title: 'Gerar Prova - Rápida',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Rápida' }
  },
  {
    path: 'gerar-prova/manual',
    component: GeradorManual,
    title: 'Gerador de Prova',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Manual' }
  },
  {
    path: 'gerar-prova/prova-builder',
    component: ProvaBuilder,
    title: 'Gerador de Prova',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Builder' }
  },
  {
    path: 'gerar-prova/escolher-modelo-prova', 
    component: MenuGeracao,
    title: 'Escolher Modelo de Geração',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Escolher Modelo' }
  },
  {
    path: 'provas-salvas',
    component: ProvasSalvas,
    title: 'Provas Salvas',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Provas Salvas' }
  },
  {
    path: 'provas-salvas/:id', 
    component: DetalheProva,
    title: 'Detalhe da Prova',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Detalhe da Prova' }
  },
  {
    path: 'banco-questoes',
    component: BancoDashboard,
    title: 'Dashboard Banco de Questões',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Banco de Questões' }
  },
  {
    path: 'banco-questoes/gerenciar',
    component: GerenciarBanco,
    title: 'Gerenciar Banco de Questões',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Gerenciar' }
  },
  {
    path: 'banco-questoes/taxonomia',
    component: EditarTaxonomia,
    title: 'Taxonomia da Disciplina',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Taxonomia' }
  },
  {
    path: 'banco-questoes/novo',
    component: BancoQuestoes,
    title: 'Banco de Questões',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Nova Questão' }
  },
  {
    path: 'banco-questoes/selecionar-questao',
    component: SelecionarQuestao,
    title: 'Selecionar Questão do Banco',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Selecionar Questão' }
  },
  {
    path: 'alimentacao',
    component: Alimentacao,
    title: 'Alimentacao - RAG',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Alimentação RAG' }
  },
  {
    path: 'cadastro-documento',
    component: CadastroDocumento,
    title: 'Cadastro de Documento',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { Nbreadcrumb: 'Cadastrar Documento' }
  },
  {
    path: 'detalhe-prompt',
    component: DetalhePrompt,
    title: 'Detalhes de Prompt',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Detalhes de Prompt' }
  },
  {
    path: 'revisao/:id', 
    component: Revisao,
    title: 'Revisão de Questões',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Revisão' }
  },
  {
    path: 'gerenciamento',
    component: Gerenciamento,
    title: 'Gerenciamento - RAG',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Gerenciamento RAG' }
   },
   {
    path: 'processamentos',
    component: Processamentos, 
    title: 'Status dos Processamentos',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Processamentos' }
  },
   { 
    path: 'gerenciamento/prova/:id/questoes', 
    component: QuestoesDocumento,
    title: 'Auditoria de Questões - RAG',
    canActivate: [AuthGuard, DisciplinaGuard],
    data: { breadcrumb: 'Auditoria de Questões' }
   }
];