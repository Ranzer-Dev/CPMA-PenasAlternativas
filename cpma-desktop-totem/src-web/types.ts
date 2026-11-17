export interface TotemConfig {
  serverIp: string;
  serverPort: number;
  terminalId: string;
  terminalKey: string;
}

export interface TotemStatusResponse {
  status: string;
  facialService: 'ONLINE' | 'OFFLINE';
  database?: string;
  service: string;
}

export interface ValidarCodigoEtapa1Response {
  codigoValido: boolean;
  usuarioId?: number;
  nomeApenado?: string;
  possuiBiometriaFacial: boolean;
  etapaSeguinte: 'BIOMETRIA_FACIAL' | 'BLOQUEADO';
  mensagem: string;
}

export interface ValidarAcessoResponse {
  autorizado: boolean;
  usuarioId?: number;
  nomeApenado?: string;
  similaridadeObtida?: number;
  motivo: string;
  tokenSessao?: string;
}

export interface UsuarioResponse {
  id: number;
  nome: string;
  cpf: string;
  telefone?: string;
  processoNumero?: string;
  dataNascimento?: string;
  foto?: string;
  observacao?: string;
  habilidades?: string[];
  disponibilidades?: Array<{
    diaSemana: string;
    horaInicio: string;
    horaFim: string;
  }>;
}

export interface PenaResponse {
  id: number;
  usuarioId: number;
  nomeUsuario?: string;
  instituicaoId: number;
  nomeInstituicao?: string;
  horasCumprir: number;
  horasCumpridas: number;
  dataInicio: string;
  dataEncerramento?: string;
  descricao: string;
  atividadesAcordadas?: string;
}

export interface RegistroTrabalhoResponse {
  id: number;
  penaId: number;
  dataTrabalho: string;
  horaInicio: string;
  horaFim: string;
  horasCumpridasDia?: number;
}

export interface ResumoCumprimentoResponse {
  penaId: number;
  usuarioId: number;
  nomeApenado: string;
  instituicaoId: number;
  nomeInstituicao: string;
  horasTotaisACumprir: number;
  horasCumpridas: number;
  horasRestantes: number;
  percentualConcluido: number;
  quantidadeRegistrosPresenca: number;
  statusCumprimento: 'CONCLUIDA' | 'EM_ANDAMENTO' | 'NAO_INICIADA';
}

export interface ProntuarioApenado {
  usuario: UsuarioResponse;
  penas: PenaResponse[];
  penaPrincipal: PenaResponse | null;
  resumo: ResumoCumprimentoResponse | null;
  registros: RegistroTrabalhoResponse[];
}

declare global {
  interface Window {
    electronAPI?: {
      isElectron: boolean;
      platform: string;
      getVersion: () => Promise<string>;
      getConfig: () => Promise<TotemConfig>;
      saveConfig: (config: Partial<TotemConfig>) => Promise<TotemConfig>;
      exitKiosk: () => void;
      enterKiosk: () => void;
      quitApp: () => void;
      print: () => void;
    };
  }
}
