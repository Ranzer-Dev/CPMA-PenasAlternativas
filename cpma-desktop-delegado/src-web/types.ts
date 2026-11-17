export interface Administrador {
  adminId: number;
  id_admin?: number;
  nome: string;
  cpf: string;
  nivelPermissao: number;
  nivel_permissao?: number;
  token: string;
}

export interface Usuario {
  idUsuario: number;
  codigo: string;
  nome: string;
  cpf: string;
  dataNascimento?: string;
  endereco?: string;
  bairro?: string;
  cidade?: string;
  cep?: string;
  uf?: string;
  nacionalidade?: string;
  foto?: string;
  observacao?: string;
  telefone?: string;
  criadoEm?: string;
}

export interface CadastroUsuarioPayload {
  nome: string;
  cpf: string;
  codigo: string;
  dataNascimento?: string;
  endereco?: string;
  bairro?: string;
  cidade?: string;
  cep?: string;
  uf?: string;
  nacionalidade?: string;
  foto?: string;
  observacao?: string;
  telefone?: string;
  adminId: number;
}

export interface TipoInstituicao {
  idTipo: number;
  tipo: string;
}

export interface DisponibilidadeDTO {
  diaSemana: string;
  horaInicio1?: string;
  horaFim1?: string;
  horaInicio2?: string;
  horaFim2?: string;
}

export interface Instituicao {
  idInstituicao: number;
  nome: string;
  endereco?: string;
  cidade?: string;
  uf?: string;
  bairro?: string;
  cep?: string;
  responsavel?: string;
  telefone?: string;
  tipoId: number;
  tipoNome: string;
  criadoEm?: string;
  disponibilidades?: DisponibilidadeDTO[];
}

export interface CadastroInstituicaoPayload {
  nome: string;
  endereco?: string;
  cidade?: string;
  uf?: string;
  bairro?: string;
  cep?: string;
  responsavel?: string;
  telefone?: string;
  tipoId: number;
  disponibilidades?: DisponibilidadeDTO[];
}

export interface Pena {
  idPena: number;
  usuarioId: number;
  usuarioNome: string;
  usuarioCodigo: string;
  instituicaoPrincipalId: number;
  instituicaoPrincipalNome: string;
  tipoPena: string;
  dataInicio: string;
  dataTermino?: string;
  descricao?: string;
  diasSemanaEHorariosDisponivel?: string;
  atividadesAcordadas?: string;
  horasSemanais: number;
  tempoPena: number;
  horasTotais: number;
  outrasInstituicoesIds?: number[];
  criadoEm?: string;
}

export interface CadastroPenaPayload {
  usuarioId: number;
  instituicaoId: number;
  tipoPena: string;
  dataInicio: string;
  dataTermino?: string | null;
  descricao?: string;
  diasSemanaEHorariosDisponivel?: string;
  atividadesAcordadas?: string;
  horasSemanais: number;
  horasTotais: number;
  outrasInstituicoesIds?: number[];
}

export interface EstimativaPenaResponse {
  tempoEstimadoMeses: number;
  dataTerminoEstimada: string;
}

export interface RegistroDeTrabalho {
  idRegistro: number;
  penaId: number;
  instituicaoId?: number;
  instituicaoNome?: string;
  dataTrabalho: string;
  horasCumpridas: number;
  atividades?: string;
  horarioInicio?: string;
  horarioAlmoco?: string;
  horarioVolta?: string;
  horarioSaida?: string;
  criadoEm?: string;
}

export interface CadastroRegistroTrabalhoPayload {
  penaId: number;
  instituicaoId?: number;
  dataTrabalho: string;
  horasCumpridas: number;
  atividades?: string;
  horarioInicio?: string;
  horarioAlmoco?: string;
  horarioVolta?: string;
  horarioSaida?: string;
}

export interface ResumoCumprimentoResponse {
  penaId: number;
  horasTotais: number;
  horasCumpridas: number;
  horasRestantes: number;
  percentualConcluido: number;
}

export interface CodigoAcessoResponse {
  codigo: string;
  usuarioId: number;
  usuarioNome: string;
  dataGeracao: string;
  dataExpiracao: string;
  status: string;
}

export interface LogAuditoriaDTO {
  idLog: number;
  dataHora: string;
  sucesso: boolean;
  metodo: string;
  usuarioNome?: string;
  usuarioCpf?: string;
  usuarioMatricula?: string;
  codigoTentado?: string;
  mensagem?: string;
  nivelRisco: string;
}

export interface EstatisticasAuditoriaDTO {
  totalLogs: number;
  totalSucessos: number;
  totalFalhas: number;
  falhasBiometricas: number;
  falhasCodigo: number;
}

export interface ReconhecimentoFacialResponse {
  sucesso: boolean;
  mensagem: string;
  usuarioId?: number;
  usuarioNome?: string;
  usuarioCodigo?: string;
  confidence?: number;
}

export interface BackendStatusResponse {
  status: string;
  service: string;
  database?: string;
}

export type ViewName =
  | 'dashboard'
  | 'usuarios'
  | 'instituicoes'
  | 'penas'
  | 'registros'
  | 'facial'
  | 'auditoria';
