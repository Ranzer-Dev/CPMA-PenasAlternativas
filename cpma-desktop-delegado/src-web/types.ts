export interface Administrador {
  id_admin: number;
  nome: string;
  cpf: string;
  senha: string; // sha-256 hash
  nivel_permissao: number;
  pergunta_secreta?: string;
  resposta_secreta?: string;
  criado_em: string;
}

export interface Usuario {
  id_usuario: number;
  codigo: string;
  nome: string;
  cpf: string;
  data_nascimento: string;
  endereco: string;
  bairro: string;
  cidade: string;
  uf: string;
  cep: string;
  nacionalidade: string;
  foto?: string;
  observacao?: string;
  telefone: string;
  fk_administrador_id_admin: number;
  criado_em: string;
}

export interface TipoInstituicao {
  id_tipo: number;
  tipo: string;
  criado_em: string;
}

export interface DisponibilidadeInstituicao {
  id_disponibilidade: number;
  dia_semana: string; // "Segunda", "Terça", "Quarta", etc.
  hora_inicio_1: string; // "08:00"
  hora_fim_1: string; // "12:00"
  hora_inicio_2?: string; // "13:00"
  hora_fim_2?: string; // "17:00"
  fk_instituicao_id_instituicao: number;
  criado_em: string;
}

export interface Instituicao {
  id_instituicao: number;
  nome: string;
  endereco: string;
  cidade: string;
  uf: string;
  bairro: string;
  cep: string;
  responsavel: string;
  telefone: string;
  tipo: number; // Foreign key to TipoInstituicao
  criado_em: string;
  disponibilidades?: DisponibilidadeInstituicao[];
}

export interface Pena {
  id_pena: number;
  tipo_pena: string;
  data_inicio: string;
  data_termino?: string;
  descricao?: string;
  dias_semana_e_horarios_disponivel?: string;
  atividades_acordadas?: string;
  horas_semanais: number;
  tempo_pena: number; // in weeks or days
  horas_totais: number;
  fk_usuario_id_usuario: number;
  fk_instituicao_id_instituicao: number;
  criado_em: string;
}

export interface RegistroDeTrabalho {
  id_registro: number;
  data_trabalho: string;
  horas_cumpridas: number;
  atividades?: string;
  horario_inicio?: string;
  horario_almoco?: string;
  horario_volta?: string;
  horario_saida?: string;
  fk_pena_id_pena: number;
  criado_em: string;
}

export interface DadosFaciais {
  id_dados_faciais: number;
  fk_usuario_id_usuario: number;
  imagem_rosto?: string;
  descritores_faciais: string; // JSON array string
  data_cadastro: string;
  data_atualizacao: string;
  ativo: boolean;
}

export interface RegistroDTO {
  data: string;
  instituicao: string;
  horasCumpridas: number;
  horasFalta: number;
  atividades?: string;
  horario?: string;
}

export interface ElectronAPI {
  isElectron: boolean;
  platform: string;
  getVersion: () => Promise<string>;
  minimize: () => void;
  maximize: () => void;
  close: () => void;
  toggleFullscreen: () => void;
  print: () => void;
}

declare global {
  interface Window {
    electronAPI?: ElectronAPI;
  }
}

