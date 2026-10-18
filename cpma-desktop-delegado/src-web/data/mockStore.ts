import {
  Administrador,
  Usuario,
  Instituicao,
  TipoInstituicao,
  DisponibilidadeInstituicao,
  Pena,
  RegistroDeTrabalho,
  DadosFaciais,
} from '../types';

const STORAGE_KEY = 'cpma_database_v1';

export interface DatabaseState {
  administradores: Administrador[];
  usuarios: Usuario[];
  instituicoes: Instituicao[];
  tiposInstituicao: TipoInstituicao[];
  disponibilidades: DisponibilidadeInstituicao[];
  penas: Pena[];
  registrosTrabalho: RegistroDeTrabalho[];
  dadosFaciais: DadosFaciais[];
  usuarioLogado: Administrador | null;
}

const INITIAL_DATA: DatabaseState = {
  usuarioLogado: null,
  administradores: [
    {
      id_admin: 1,
      nome: 'Administrador CPMA',
      cpf: '12345678900',
      senha: '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', // hash of "123456"
      nivel_permissao: 1,
      pergunta_secreta: 'Qual a sua cidade natal?',
      resposta_secreta: 'Votorantim',
      criado_em: '2025-01-10T10:00:00Z',
    },
  ],
  tiposInstituicao: [
    { id_tipo: 1, tipo: 'Escola Municipal', criado_em: '2025-01-10T10:00:00Z' },
    { id_tipo: 2, tipo: 'Unidade Básica de Saúde (UBS)', criado_em: '2025-01-10T10:00:00Z' },
    { id_tipo: 3, tipo: 'Centro Comunitário / ONG', criado_em: '2025-01-10T10:00:00Z' },
    { id_tipo: 4, tipo: 'Secretaria de Meio Ambiente / Parques', criado_em: '2025-01-10T10:00:00Z' },
    { id_tipo: 5, tipo: 'Biblioteca Pública / Cultura', criado_em: '2025-01-10T10:00:00Z' },
  ],
  instituicoes: [
    {
      id_instituicao: 1,
      nome: 'E.M. Professora Dely de Oliveira',
      endereco: 'Rua Pedro Fontes, 150',
      bairro: 'Parque Bela Vista',
      cidade: 'Votorantim',
      uf: 'SP',
      cep: '18110-330',
      responsavel: 'Maria de Lourdes Ferreira',
      telefone: '(15) 3243-1122',
      tipo: 1,
      criado_em: '2025-01-15T09:00:00Z',
    },
    {
      id_instituicao: 2,
      nome: 'UBS Barra Funda - Votorantim',
      endereco: 'Av. Ireno da Silva Venâncio, 85',
      bairro: 'Barra Funda',
      cidade: 'Votorantim',
      uf: 'SP',
      cep: '18114-000',
      responsavel: 'Dr. André Santos Costa',
      telefone: '(15) 3243-4455',
      tipo: 2,
      criado_em: '2025-01-15T09:30:00Z',
    },
    {
      id_instituicao: 3,
      nome: 'Associação Votorantinense de Apoio Comunitário (AVAC)',
      endereco: 'Rua Albertina Nascimento, 410',
      bairro: 'Centro',
      cidade: 'Votorantim',
      uf: 'SP',
      cep: '18110-025',
      responsavel: 'Clarice Moreira da Silva',
      telefone: '(15) 3353-8899',
      tipo: 3,
      criado_em: '2025-01-16T14:00:00Z',
    },
    {
      id_instituicao: 4,
      nome: 'Parque Ecológico do Matão',
      endereco: 'Rua Ângelo Delapasi, 117',
      bairro: 'Jardim Parque Morumbi',
      cidade: 'Votorantim',
      uf: 'SP',
      cep: '18110-230',
      responsavel: 'Carlos Alberto Rezende',
      telefone: '(15) 3247-2199',
      tipo: 4,
      criado_em: '2025-02-01T10:00:00Z',
    },
  ],
  disponibilidades: [
    {
      id_disponibilidade: 1,
      fk_instituicao_id_instituicao: 1,
      dia_semana: 'Segunda',
      hora_inicio_1: '08:00',
      hora_fim_1: '12:00',
      hora_inicio_2: '13:00',
      hora_fim_2: '17:00',
      criado_em: '2025-01-15T09:00:00Z',
    },
    {
      id_disponibilidade: 2,
      fk_instituicao_id_instituicao: 1,
      dia_semana: 'Quarta',
      hora_inicio_1: '08:00',
      hora_fim_1: '12:00',
      hora_inicio_2: '13:00',
      hora_fim_2: '17:00',
      criado_em: '2025-01-15T09:00:00Z',
    },
    {
      id_disponibilidade: 3,
      fk_instituicao_id_instituicao: 1,
      dia_semana: 'Sexta',
      hora_inicio_1: '08:00',
      hora_fim_1: '12:00',
      hora_inicio_2: '13:00',
      hora_fim_2: '17:00',
      criado_em: '2025-01-15T09:00:00Z',
    },
    {
      id_disponibilidade: 4,
      fk_instituicao_id_instituicao: 2,
      dia_semana: 'Terça',
      hora_inicio_1: '07:30',
      hora_fim_1: '11:30',
      hora_inicio_2: '12:30',
      hora_fim_2: '16:30',
      criado_em: '2025-01-15T09:30:00Z',
    },
    {
      id_disponibilidade: 5,
      fk_instituicao_id_instituicao: 2,
      dia_semana: 'Quinta',
      hora_inicio_1: '07:30',
      hora_fim_1: '11:30',
      hora_inicio_2: '12:30',
      hora_fim_2: '16:30',
      criado_em: '2025-01-15T09:30:00Z',
    },
    {
      id_disponibilidade: 6,
      fk_instituicao_id_instituicao: 3,
      dia_semana: 'Sábado',
      hora_inicio_1: '08:00',
      hora_fim_1: '14:00',
      hora_inicio_2: '',
      hora_fim_2: '',
      criado_em: '2025-01-16T14:00:00Z',
    },
  ],
  usuarios: [
    {
      id_usuario: 1,
      codigo: 'PENA-001',
      nome: 'João Pedro de Almeida',
      cpf: '11144477735',
      data_nascimento: '1988-06-14',
      endereco: 'Rua Monte Alegre, 320',
      bairro: 'Vossoroca',
      cidade: 'Votorantim',
      uf: 'SP',
      cep: '18116-100',
      nacionalidade: 'Brasileira',
      foto: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=250&auto=format&fit=crop&q=80',
      observacao: 'Prestação de serviços comunitários na área de manutenção predial e jardinagem.',
      telefone: '(15) 99712-3456',
      fk_administrador_id_admin: 1,
      criado_em: '2025-01-20T11:00:00Z',
    },
    {
      id_usuario: 2,
      codigo: 'PENA-002',
      nome: 'Carlos Eduardo Ramos',
      cpf: '22255588846',
      data_nascimento: '1993-11-25',
      endereco: 'Av. São João, 1240',
      bairro: 'Jardim Icatu',
      cidade: 'Votorantim',
      uf: 'SP',
      cep: '18110-210',
      nacionalidade: 'Brasileira',
      foto: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=250&auto=format&fit=crop&q=80',
      observacao: 'Designado para apoio logístico e conservação de áreas públicas.',
      telefone: '(15) 98144-8899',
      fk_administrador_id_admin: 1,
      criado_em: '2025-01-25T14:30:00Z',
    },
    {
      id_usuario: 3,
      codigo: 'PENA-001',
      nome: 'Ana Carolina Mendes',
      cpf: '33366699957',
      data_nascimento: '1995-03-08',
      endereco: 'Rua Paula Ney, 55',
      bairro: 'Centro',
      cidade: 'Votorantim',
      uf: 'SP',
      cep: '18110-010',
      nacionalidade: 'Brasileira',
      foto: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=250&auto=format&fit=crop&q=80',
      observacao: 'Apoio em triagem documental e organização em instituição assistencial.',
      telefone: '(15) 99123-7788',
      fk_administrador_id_admin: 1,
      criado_em: '2025-02-05T09:15:00Z',
    },
  ],
  penas: [
    {
      id_pena: 1,
      tipo_pena: 'Prestação de Serviços à Comunidade (PSC)',
      data_inicio: '2025-02-01',
      data_termino: '2025-06-30',
      descricao: 'Processo n° 0004521-82.2024.8.26.0663 - Vara Criminal de Votorantim',
      dias_semana_e_horarios_disponivel: 'Segunda e Quarta, das 08:00 às 12:00',
      atividades_acordadas: 'Auxílio na pintura, pequenos reparos, capina e jardinagem no pátio escolar.',
      horas_semanais: 8,
      tempo_pena: 18,
      horas_totais: 144,
      fk_usuario_id_usuario: 1,
      fk_instituicao_id_instituicao: 1,
      criado_em: '2025-02-01T08:00:00Z',
    },
    {
      id_pena: 2,
      tipo_pena: 'Prestação de Serviços à Comunidade (PSC)',
      data_inicio: '2025-02-10',
      data_termino: '2025-05-15',
      descricao: 'Processo n° 0001290-44.2024.8.26.0663 - Cumprimento de transação penal',
      dias_semana_e_horarios_disponivel: 'Terça e Quinta, das 08:00 às 12:00',
      atividades_acordadas: 'Limpeza e desinfecção de áreas externas e organização de arquivos na UBS.',
      horas_semanais: 8,
      tempo_pena: 14,
      horas_totais: 112,
      fk_usuario_id_usuario: 2,
      fk_instituicao_id_instituicao: 2,
      criado_em: '2025-02-10T08:00:00Z',
    },
    {
      id_pena: 3,
      tipo_pena: 'Prestação de Serviços à Comunidade (PSC)',
      data_inicio: '2025-02-15',
      data_termino: '2025-07-20',
      descricao: 'Processo n° 0008871-12.2024.8.26.0663 - Central de Penas Alternativas',
      dias_semana_e_horarios_disponivel: 'Sábado, das 08:00 às 14:00',
      atividades_acordadas: 'Organização de doações de alimentos e roupas, atendimento recepção.',
      horas_semanais: 6,
      tempo_pena: 20,
      horas_totais: 120,
      fk_usuario_id_usuario: 3,
      fk_instituicao_id_instituicao: 3,
      criado_em: '2025-02-15T09:00:00Z',
    },
  ],
  registrosTrabalho: [
    {
      id_registro: 1,
      fk_pena_id_pena: 1,
      data_trabalho: '2025-02-03',
      horario_inicio: '08:00',
      horario_almoco: '',
      horario_volta: '',
      horario_saida: '12:00',
      horas_cumpridas: 4,
      atividades: 'Pintura dos bancos e limpeza do pátio principal da escola.',
      criado_em: '2025-02-03T12:05:00Z',
    },
    {
      id_registro: 2,
      fk_pena_id_pena: 1,
      data_trabalho: '2025-02-05',
      horario_inicio: '08:00',
      horario_almoco: '',
      horario_volta: '',
      horario_saida: '12:00',
      horas_cumpridas: 4,
      atividades: 'Reparo em maçanetas e poda de cerca viva.',
      criado_em: '2025-02-05T12:02:00Z',
    },
    {
      id_registro: 3,
      fk_pena_id_pena: 1,
      data_trabalho: '2025-02-10',
      horario_inicio: '08:00',
      horario_almoco: '',
      horario_volta: '',
      horario_saida: '12:00',
      horas_cumpridas: 4,
      atividades: 'Organização da sala de ferramentas e limpeza dos bebedouros.',
      criado_em: '2025-02-10T12:00:00Z',
    },
    {
      id_registro: 4,
      fk_pena_id_pena: 1,
      data_trabalho: '2025-02-12',
      horario_inicio: '08:00',
      horario_almoco: '',
      horario_volta: '',
      horario_saida: '12:00',
      horas_cumpridas: 4,
      atividades: 'Limpeza de calhas e preparação de canteiros.',
      criado_em: '2025-02-12T12:10:00Z',
    },
    {
      id_registro: 5,
      fk_pena_id_pena: 2,
      data_trabalho: '2025-02-11',
      horario_inicio: '08:00',
      horario_almoco: '',
      horario_volta: '',
      horario_saida: '12:00',
      horas_cumpridas: 4,
      atividades: 'Desinfecção de calçadas e varrição da entrada da UBS.',
      criado_em: '2025-02-11T12:00:00Z',
    },
    {
      id_registro: 6,
      fk_pena_id_pena: 2,
      data_trabalho: '2025-02-13',
      horario_inicio: '08:00',
      horario_almoco: '',
      horario_volta: '',
      horario_saida: '12:00',
      horas_cumpridas: 4,
      atividades: 'Higienização de lixeiras e apoio na guarda de materiais.',
      criado_em: '2025-02-13T12:00:00Z',
    },
    {
      id_registro: 7,
      fk_pena_id_pena: 3,
      data_trabalho: '2025-02-22',
      horario_inicio: '08:00',
      horario_almoco: '',
      horario_volta: '',
      horario_saida: '14:00',
      horas_cumpridas: 6,
      atividades: 'Separação de cestas básicas e conferência de validades.',
      criado_em: '2025-02-22T14:10:00Z',
    },
  ],
  dadosFaciais: [
    {
      id_dados_faciais: 1,
      fk_usuario_id_usuario: 1,
      imagem_rosto: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=250&auto=format&fit=crop&q=80',
      descritores_faciais: '[100,150,200,120,180,90,160,140,110,130]',
      data_cadastro: '2025-01-20',
      data_atualizacao: '2025-01-20',
      ativo: true,
    },
    {
      id_dados_faciais: 2,
      fk_usuario_id_usuario: 2,
      imagem_rosto: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=250&auto=format&fit=crop&q=80',
      descritores_faciais: '[95,145,195,115,175,85,155,135,105,125]',
      data_cadastro: '2025-01-25',
      data_atualizacao: '2025-01-25',
      ativo: true,
    },
    {
      id_dados_faciais: 3,
      fk_usuario_id_usuario: 3,
      imagem_rosto: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=250&auto=format&fit=crop&q=80',
      descritores_faciais: '[110,160,210,130,190,95,170,150,115,135]',
      data_cadastro: '2025-02-05',
      data_atualizacao: '2025-02-05',
      ativo: true,
    },
  ],
};

class MockDatabase {
  private state: DatabaseState;

  constructor() {
    this.state = this.loadState();
  }

  private loadState(): DatabaseState {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch (e) {
      console.warn('Could not read from localStorage, using initial data:', e);
    }
    return { ...INITIAL_DATA };
  }

  private saveState(): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.state));
    } catch (e) {
      console.warn('Could not save to localStorage:', e);
    }
  }

  public resetToDefault(): void {
    this.state = { ...INITIAL_DATA };
    this.saveState();
  }

  // Auth & Session
  public getUsuarioLogado(): Administrador | null {
    return this.state.usuarioLogado;
  }

  public setUsuarioLogado(admin: Administrador | null): void {
    this.state.usuarioLogado = admin;
    this.saveState();
  }

  public buscarAdminPorCPF(cpf: string): Administrador | undefined {
    const limpo = cpf.replace(/\D/g, '');
    return this.state.administradores.find(a => a.cpf.replace(/\D/g, '') === limpo);
  }

  public alterarSenhaAdmin(cpf: string, novaSenhaHash: string): boolean {
    const admin = this.buscarAdminPorCPF(cpf);
    if (!admin) return false;
    admin.senha = novaSenhaHash;
    this.saveState();
    return true;
  }

  // Usuários (Apenados)
  public listarUsuarios(): Usuario[] {
    return [...this.state.usuarios];
  }

  public buscarUsuarioPorId(id: number): Usuario | undefined {
    return this.state.usuarios.find(u => u.id_usuario === id);
  }

  public buscarUsuarioPorTermo(termo: string): Usuario | undefined {
    const limpo = termo.trim().toLowerCase();
    const termoCpf = termo.replace(/\D/g, '');
    return this.state.usuarios.find(u => {
      const uCpf = u.cpf.replace(/\D/g, '');
      if (termoCpf && uCpf.includes(termoCpf)) return true;
      if (u.nome.toLowerCase().includes(limpo)) return true;
      if (u.codigo && u.codigo.toLowerCase().includes(limpo)) return true;
      return false;
    });
  }

  public salvarUsuario(dados: Omit<Usuario, 'id_usuario' | 'criado_em'> & { id_usuario?: number }): Usuario {
    if (dados.id_usuario) {
      const idx = this.state.usuarios.findIndex(u => u.id_usuario === dados.id_usuario);
      if (idx >= 0) {
        this.state.usuarios[idx] = {
          ...this.state.usuarios[idx],
          ...dados,
          id_usuario: dados.id_usuario,
        };
        this.saveState();
        return this.state.usuarios[idx];
      }
    }
    const novoId = (Math.max(0, ...this.state.usuarios.map(u => u.id_usuario))) + 1;
    const novoUsuario: Usuario = {
      ...dados,
      id_usuario: novoId,
      criado_em: new Date().toISOString(),
    };
    this.state.usuarios.push(novoUsuario);
    this.saveState();
    return novoUsuario;
  }

  public excluirUsuario(id: number): void {
    this.state.usuarios = this.state.usuarios.filter(u => u.id_usuario !== id);
    // Cascade cleanup for penas and records
    const penasIds = this.state.penas.filter(p => p.fk_usuario_id_usuario === id).map(p => p.id_pena);
    this.state.penas = this.state.penas.filter(p => p.fk_usuario_id_usuario !== id);
    this.state.registrosTrabalho = this.state.registrosTrabalho.filter(r => !penasIds.includes(r.fk_pena_id_pena));
    this.state.dadosFaciais = this.state.dadosFaciais.filter(d => d.fk_usuario_id_usuario !== id);
    this.saveState();
  }

  // Instituições
  public listarInstituicoes(): Instituicao[] {
    return this.state.instituicoes.map(inst => ({
      ...inst,
      disponibilidades: this.state.disponibilidades.filter(d => d.fk_instituicao_id_instituicao === inst.id_instituicao),
    }));
  }

  public buscarInstituicaoPorId(id: number): Instituicao | undefined {
    const inst = this.state.instituicoes.find(i => i.id_instituicao === id);
    if (!inst) return undefined;
    return {
      ...inst,
      disponibilidades: this.state.disponibilidades.filter(d => d.fk_instituicao_id_instituicao === inst.id_instituicao),
    };
  }

  public salvarInstituicao(dados: Omit<Instituicao, 'id_instituicao' | 'criado_em' | 'disponibilidades'> & { id_instituicao?: number }): Instituicao {
    if (dados.id_instituicao) {
      const idx = this.state.instituicoes.findIndex(i => i.id_instituicao === dados.id_instituicao);
      if (idx >= 0) {
        this.state.instituicoes[idx] = {
          ...this.state.instituicoes[idx],
          ...dados,
          id_instituicao: dados.id_instituicao,
        };
        this.saveState();
        return this.state.instituicoes[idx];
      }
    }
    const novoId = (Math.max(0, ...this.state.instituicoes.map(i => i.id_instituicao))) + 1;
    const novaInstituicao: Instituicao = {
      ...dados,
      id_instituicao: novoId,
      criado_em: new Date().toISOString(),
    };
    this.state.instituicoes.push(novaInstituicao);
    this.saveState();
    return novaInstituicao;
  }

  public excluirInstituicao(id: number): void {
    this.state.instituicoes = this.state.instituicoes.filter(i => i.id_instituicao !== id);
    this.state.disponibilidades = this.state.disponibilidades.filter(d => d.fk_instituicao_id_instituicao !== id);
    this.saveState();
  }

  // Tipos de Instituição
  public listarTiposInstituicao(): TipoInstituicao[] {
    return [...this.state.tiposInstituicao];
  }

  public salvarTipoInstituicao(tipoNome: string): TipoInstituicao {
    const novoId = (Math.max(0, ...this.state.tiposInstituicao.map(t => t.id_tipo))) + 1;
    const novo: TipoInstituicao = {
      id_tipo: novoId,
      tipo: tipoNome,
      criado_em: new Date().toISOString(),
    };
    this.state.tiposInstituicao.push(novo);
    this.saveState();
    return novo;
  }

  // Disponibilidades
  public salvarDisponibilidade(disp: Omit<DisponibilidadeInstituicao, 'id_disponibilidade' | 'criado_em'> & { id_disponibilidade?: number }): DisponibilidadeInstituicao {
    if (disp.id_disponibilidade) {
      const idx = this.state.disponibilidades.findIndex(d => d.id_disponibilidade === disp.id_disponibilidade);
      if (idx >= 0) {
        this.state.disponibilidades[idx] = {
          ...this.state.disponibilidades[idx],
          ...disp,
          id_disponibilidade: disp.id_disponibilidade,
        };
        this.saveState();
        return this.state.disponibilidades[idx];
      }
    }
    const novoId = (Math.max(0, ...this.state.disponibilidades.map(d => d.id_disponibilidade))) + 1;
    const novaDisp: DisponibilidadeInstituicao = {
      ...disp,
      id_disponibilidade: novoId,
      criado_em: new Date().toISOString(),
    };
    this.state.disponibilidades.push(novaDisp);
    this.saveState();
    return novaDisp;
  }

  public removerDisponibilidade(id: number): void {
    this.state.disponibilidades = this.state.disponibilidades.filter(d => d.id_disponibilidade !== id);
    this.saveState();
  }

  // Penas
  public listarPenas(): Pena[] {
    return [...this.state.penas];
  }

  public buscarPenasPorUsuario(idUsuario: number): Pena[] {
    return this.state.penas.filter(p => p.fk_usuario_id_usuario === idUsuario);
  }

  public buscarPenaAtivaPorUsuario(idUsuario: number): Pena | undefined {
    const penas = this.buscarPenasPorUsuario(idUsuario);
    return penas[penas.length - 1]; // Latest penalty
  }

  public contarPenasPorUsuario(idUsuario: number): number {
    return this.buscarPenasPorUsuario(idUsuario).length;
  }

  public salvarPena(dados: Omit<Pena, 'id_pena' | 'criado_em'> & { id_pena?: number }): Pena {
    if (dados.id_pena) {
      const idx = this.state.penas.findIndex(p => p.id_pena === dados.id_pena);
      if (idx >= 0) {
        this.state.penas[idx] = {
          ...this.state.penas[idx],
          ...dados,
          id_pena: dados.id_pena,
        };
        this.saveState();
        return this.state.penas[idx];
      }
    }
    const novoId = (Math.max(0, ...this.state.penas.map(p => p.id_pena))) + 1;
    const novaPena: Pena = {
      ...dados,
      id_pena: novoId,
      criado_em: new Date().toISOString(),
    };
    this.state.penas.push(novaPena);
    this.saveState();
    return novaPena;
  }

  public excluirPena(id: number): void {
    this.state.penas = this.state.penas.filter(p => p.id_pena !== id);
    this.state.registrosTrabalho = this.state.registrosTrabalho.filter(r => r.fk_pena_id_pena !== id);
    this.saveState();
  }

  // Registros de Trabalho
  public listarRegistros(): RegistroDeTrabalho[] {
    return [...this.state.registrosTrabalho];
  }

  public buscarRegistrosPorPena(idPena: number): RegistroDeTrabalho[] {
    return this.state.registrosTrabalho.filter(r => r.fk_pena_id_pena === idPena);
  }

  public salvarRegistroTrabalho(dados: Omit<RegistroDeTrabalho, 'id_registro' | 'criado_em'> & { id_registro?: number }): RegistroDeTrabalho {
    if (dados.id_registro) {
      const idx = this.state.registrosTrabalho.findIndex(r => r.id_registro === dados.id_registro);
      if (idx >= 0) {
        this.state.registrosTrabalho[idx] = {
          ...this.state.registrosTrabalho[idx],
          ...dados,
          id_registro: dados.id_registro,
        };
        this.saveState();
        return this.state.registrosTrabalho[idx];
      }
    }
    const novoId = (Math.max(0, ...this.state.registrosTrabalho.map(r => r.id_registro))) + 1;
    const novoRegistro: RegistroDeTrabalho = {
      ...dados,
      id_registro: novoId,
      criado_em: new Date().toISOString(),
    };
    this.state.registrosTrabalho.push(novoRegistro);
    this.saveState();
    return novoRegistro;
  }

  public excluirRegistroTrabalho(id: number): void {
    this.state.registrosTrabalho = this.state.registrosTrabalho.filter(r => r.id_registro !== id);
    this.saveState();
  }

  // Dados Faciais (Biometria)
  public listarDadosFaciais(): DadosFaciais[] {
    return [...this.state.dadosFaciais];
  }

  public buscarDadosFaciaisPorUsuario(idUsuario: number): DadosFaciais | undefined {
    return this.state.dadosFaciais.find(d => d.fk_usuario_id_usuario === idUsuario && d.ativo);
  }

  public salvarDadosFaciais(dados: Omit<DadosFaciais, 'id_dados_faciais'>): DadosFaciais {
    const idx = this.state.dadosFaciais.findIndex(d => d.fk_usuario_id_usuario === dados.fk_usuario_id_usuario);
    if (idx >= 0) {
      this.state.dadosFaciais[idx] = {
        ...this.state.dadosFaciais[idx],
        ...dados,
        data_atualizacao: new Date().toISOString().split('T')[0],
      };
      this.saveState();
      return this.state.dadosFaciais[idx];
    }
    const novoId = (Math.max(0, ...this.state.dadosFaciais.map(d => d.id_dados_faciais))) + 1;
    const novo: DadosFaciais = {
      ...dados,
      id_dados_faciais: novoId,
    };
    this.state.dadosFaciais.push(novo);
    this.saveState();
    return novo;
  }
}

export const db = new MockDatabase();
